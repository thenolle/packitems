package com.nolly.mc.packitems.action

import com.nolly.mc.packitems.PackItems
import com.nolly.mc.packitems.PackItemsAPI
import com.nolly.mc.packitems.item.PackItemUtil
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.BoundingBox
import java.util.*
import kotlin.math.floor
import org.bukkit.event.block.Action as ActionMC

class TriggerRouter(private val plugin: PackItems) : Listener {
	private val idKey = NamespacedKey(plugin, "id")
	private val lastDispatch = mutableMapOf<String, Long>()
	private val wasOnGround = mutableSetOf<UUID>()

	private fun debounceKey(player: Player, trigger: Trigger) = "${player.uniqueId}:${trigger.name}"

	@EventHandler
	fun onInteract(event: PlayerInteractEvent) {
		val player = event.player
		val trigger = when (event.action) {
			ActionMC.RIGHT_CLICK_AIR -> if (player.isSneaking) Trigger.SHIFT_RIGHT_CLICK_AIR else Trigger.RIGHT_CLICK_AIR
			ActionMC.RIGHT_CLICK_BLOCK -> if (player.isSneaking) Trigger.SHIFT_RIGHT_CLICK_BLOCK else Trigger.RIGHT_CLICK_BLOCK
			ActionMC.LEFT_CLICK_AIR -> if (player.isSneaking) Trigger.SHIFT_LEFT_CLICK_AIR else Trigger.LEFT_CLICK_AIR
			ActionMC.LEFT_CLICK_BLOCK -> if (player.isSneaking) Trigger.SHIFT_LEFT_CLICK_BLOCK else Trigger.LEFT_CLICK_BLOCK
			else -> return
		}
		dispatch(event, player, trigger, block = event.clickedBlock)
	}

	@EventHandler(ignoreCancelled = true)
	fun onHitEntity(event: EntityDamageByEntityEvent) {
		val player = event.damager as? Player ?: return
		val isCritical = player.fallDistance > 0.0f
				&& !player.onGround()
				&& !player.hasPotionEffect(PotionEffectType.BLINDNESS)
				&& player.vehicle == null
		val trigger = if (isCritical) Trigger.CRIT_ENTITY else Trigger.HIT_ENTITY
		dispatch(event, player, trigger, entity = event.entity)
	}

	@Suppress("UnstableApiUsage")
	@EventHandler(ignoreCancelled = true)
	fun onProjectileHit(event: ProjectileHitEvent) {
		val player = event.entity.shooter as? Player ?: return
		val projectileItem = when (val projectile = event.entity) {
			is org.bukkit.entity.AbstractArrow -> projectile.weapon
			is org.bukkit.entity.ThrowableProjectile -> projectile.item
			else -> null
		}
		dispatch(
			event, player, Trigger.PROJECTILE_HIT,
			entity = event.hitEntity,
			block = event.hitBlock,
			overrideItem = projectileItem
		)
	}

	@EventHandler(ignoreCancelled = true)
	fun onBowShoot(event: EntityShootBowEvent) {
		val player = event.entity as? Player ?: return
		val trigger = when (event.bow?.type) {
			Material.BOW -> Trigger.BOW_SHOOT
			Material.CROSSBOW -> Trigger.CROSSBOW_SHOOT
			Material.TRIDENT -> Trigger.TRIDENT_THROW
			else -> return
		}
		val bowItem = event.bow ?: return
		dispatch(event, player, trigger, overrideItem = bowItem)
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onMove(event: PlayerMoveEvent) {
		val from = event.from
		val to = event.to ?: return
		if (from.x == to.x && from.y == to.y && from.z == to.z) return
		val player = event.player
		val uuid = player.uniqueId
		val onGround = player.onGround()
		if (!onGround && wasOnGround.contains(uuid) && to.y > from.y) dispatch(event, player, Trigger.JUMP)
		if (onGround) wasOnGround.add(uuid) else wasOnGround.remove(uuid)
	}

	private fun Player.onGround(): Boolean {
		val box = boundingBox
		val feetBox = BoundingBox(box.minX, box.minY - 0.05, box.minZ, box.maxX, box.minY, box.maxZ)
		val minX = floor(feetBox.minX).toInt()
		val minY = floor(feetBox.minY).toInt()
		val minZ = floor(feetBox.minZ).toInt()
		val maxX = floor(feetBox.maxX).toInt()
		val maxY = floor(feetBox.maxY).toInt()
		val maxZ = floor(feetBox.maxZ).toInt()
		val currentWorld = world
		for (x in minX..maxX) {
			for (y in minY..maxY) {
				for (z in minZ..maxZ) {
					val block = currentWorld.getBlockAt(x, y, z)
					if (block.isPassable) continue
					if (block.boundingBox.overlaps(feetBox)) return true
				}
			}
		}
		return false
	}

	@EventHandler
	fun onSneak(event: PlayerToggleSneakEvent) {
		dispatch(event, event.player, if (event.isSneaking) Trigger.SNEAK_START else Trigger.SNEAK_STOP)
	}

	@EventHandler
	fun onSwap(event: PlayerSwapHandItemsEvent) {
		dispatch(event, event.player, Trigger.SWAP_HAND)
	}

	@EventHandler
	fun onDrop(event: PlayerDropItemEvent) {
		dispatch(event, event.player, Trigger.DROP_ITEM, overrideItem = event.itemDrop.itemStack)
	}

	@EventHandler(ignoreCancelled = true)
	fun onBlockBreak(event: BlockBreakEvent) {
		dispatch(event, event.player, Trigger.BLOCK_BREAK, block = event.block)
	}

	@EventHandler(ignoreCancelled = true)
	fun onBlockPlace(event: BlockPlaceEvent) {
		dispatch(event, event.player, Trigger.BLOCK_PLACE, block = event.block)
	}

	@EventHandler(ignoreCancelled = true)
	fun onFallDamage(event: EntityDamageEvent) {
		if (event.entity !is Player) return
		if (event.cause != EntityDamageEvent.DamageCause.FALL) return
		dispatch(event, event.entity as Player, Trigger.FALL_DAMAGE)
	}

	@EventHandler
	fun onDeath(event: PlayerDeathEvent) {
		dispatch(event, event.entity, Trigger.DEATH)
	}

	private fun dispatch(
		event: Event,
		player: Player,
		trigger: Trigger,
		block: Block? = null,
		entity: Entity? = null,
		overrideItem: ItemStack? = null
	) {
		val now = System.nanoTime()
		val key = debounceKey(player, trigger)
		val last = lastDispatch[key]
		if (last != null && now - last < 50_000_000L) return
		lastDispatch[key] = now
		val main = player.inventory.itemInMainHand
		val off = player.inventory.itemInOffHand
		val handItem = when {
			overrideItem != null -> overrideItem
			main.type.isAir.not() -> main
			off.type.isAir.not() -> off
			else -> return
		}
		val id = PackItemUtil.getId(handItem, idKey) ?: if (overrideItem != null) {
			val fallback = if (main.type.isAir.not()) main else if (off.type.isAir.not()) off else null
			fallback?.let { PackItemUtil.getId(it, idKey) }
		} else return
		val packItem = plugin.registry.get(id ?: return) ?: return
		if (!player.hasPermission("packitems.use.$id") && !player.hasPermission("packitems.use.*")) return
		val context = ActionContext(
			event = event,
			player = player,
			item = packItem,
			handItem = handItem,
			block = block,
			entity = entity,
			trigger = trigger
		)
		PackItemsAPI.dispatchTriggerHooks(context)
		plugin.actionDispatcher.dispatch(context)
	}
}
