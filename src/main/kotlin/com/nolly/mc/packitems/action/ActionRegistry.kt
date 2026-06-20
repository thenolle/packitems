package com.nolly.mc.packitems.action

import com.nolly.mc.packitems.util.Text
import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Cancellable
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class FunctionalAction(private val logic: (ActionContext) -> Unit) : Action {
	override fun execute(context: ActionContext) = logic(context)
}

object ActionRegistry {
	fun registerAll() {
		// ── Chat & Audio ──────────────────────────────────────────────────────────
		ActionParser.register("message") { map ->
			val text = map["text"] as? String ?: return@register null
			FunctionalAction { Text.send(it.player, text) }
		}
		ActionParser.register("broadcast") { map ->
			val text = map["text"] as? String ?: return@register null
			FunctionalAction { Bukkit.broadcastMessage(Text.parse(text, null)) }
		}
		ActionParser.register("sound") { map ->
			val sound = map["sound"] as? String ?: return@register null
			val vol = (map["volume"] as? Number)?.toFloat() ?: 1f
			val pitch = (map["pitch"] as? Number)?.toFloat() ?: 1f
			FunctionalAction {
				try {
					it.player.playSound(it.player.location, Sound.valueOf(sound.uppercase()), vol, pitch)
				} catch (_: Exception) {
				}
			}
		}
		ActionParser.register("stop_sound") { map ->
			val sound = map["sound"] as? String
			val categoryRaw = map["category"] as? String
			FunctionalAction { ctx ->
				try {
					val category = categoryRaw?.let { runCatching { SoundCategory.valueOf(it.uppercase()) }.getOrNull() }
					if (sound != null) {
						val resolved = runCatching { Sound.valueOf(sound.uppercase()) }.getOrNull()
						if (resolved != null && category != null) ctx.player.stopSound(resolved, category)
						else if (resolved != null) ctx.player.stopSound(resolved)
					} else if (category != null) {
						ctx.player.stopAllSounds()
					}
				} catch (_: Exception) {
				}
			}
		}

		// ── Flow Control ──────────────────────────────────────────────────────────
		ActionParser.register("abort") {
			FunctionalAction { throw ActionDispatcher.ActionAbortException("explicit abort") }
		}

		// ── Core Mechanics ────────────────────────────────────────────────────────
		ActionParser.register("cancel_event") {
			FunctionalAction { (it.event as? Cancellable)?.isCancelled = true }
		}
		ActionParser.register("consume_item") { map ->
			val amount = (map["amount"] as? Number)?.toInt() ?: 1
			FunctionalAction { ctx ->
				val item = ctx.handItem ?: return@FunctionalAction
				item.amount = (item.amount - amount).coerceAtLeast(0)
			}
		}
		ActionParser.register("damage_item") { map ->
			val amount = (map["amount"] as? Number)?.toInt() ?: 1
			FunctionalAction { ctx ->
				val item = ctx.handItem ?: return@FunctionalAction
				val meta = item.itemMeta as? org.bukkit.inventory.meta.Damageable ?: return@FunctionalAction
				meta.damage += amount
				item.itemMeta = meta
			}
		}

		// ── Player State ──────────────────────────────────────────────────────────
		ActionParser.register("heal") { map ->
			val amount = (map["amount"] as? Number)?.toDouble() ?: 1.0
			FunctionalAction { ctx ->
				val max = ctx.player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
				ctx.player.health = (ctx.player.health + amount).coerceAtMost(max)
			}
		}
		ActionParser.register("damage") { map ->
			val amount = (map["amount"] as? Number)?.toDouble() ?: 1.0
			FunctionalAction { ctx -> ctx.player.damage(amount) }
		}
		ActionParser.register("set_fire") { map ->
			val ticks = (map["ticks"] as? Number)?.toInt() ?: 60
			FunctionalAction { ctx -> ctx.player.fireTicks = ticks }
		}
		ActionParser.register("extinguish") {
			FunctionalAction { ctx -> ctx.player.fireTicks = 0 }
		}
		ActionParser.register("add_potion_effect") { map ->
			val effect = map["effect"] as? String ?: return@register null
			val duration = (map["duration"] as? Number)?.toInt() ?: 200
			val amp = (map["amplifier"] as? Number)?.toInt() ?: 0
			FunctionalAction { ctx ->
				PotionEffectType.getByName(effect.uppercase())?.let { type ->
					ctx.player.addPotionEffect(PotionEffect(type, duration, amp))
				}
			}
		}
		ActionParser.register("remove_potion_effect") { map ->
			val effect = map["effect"] as? String ?: return@register null
			FunctionalAction { ctx ->
				PotionEffectType.getByName(effect.uppercase())?.let { type -> ctx.player.removePotionEffect(type) }
			}
		}
		ActionParser.register("clear_potion_effects") {
			FunctionalAction { ctx -> ctx.player.activePotionEffects.forEach { ctx.player.removePotionEffect(it.type) } }
		}
		ActionParser.register("feed") { map ->
			val amount = (map["amount"] as? Number)?.toInt() ?: 1
			FunctionalAction { ctx -> ctx.player.foodLevel = (ctx.player.foodLevel + amount).coerceAtMost(20) }
		}

		// ── World & Execution ─────────────────────────────────────────────────────
		ActionParser.register("command_player") { map ->
			val cmd = map["command"] as? String ?: return@register null
			FunctionalAction { ctx -> Bukkit.dispatchCommand(ctx.player, cmd.replace("%player%", ctx.player.name)) }
		}
		ActionParser.register("command_console") { map ->
			val cmd = map["command"] as? String ?: return@register null
			FunctionalAction { ctx ->
				Bukkit.dispatchCommand(
					Bukkit.getConsoleSender(),
					cmd.replace("%player%", ctx.player.name)
				)
			}
		}
		ActionParser.register("play_particle") { map ->
			val particleStr = map["particle"] as? String ?: return@register null
			val count = (map["count"] as? Number)?.toInt() ?: 1
			FunctionalAction { ctx ->
				try {
					ctx.player.world.spawnParticle(
						Particle.valueOf(particleStr.uppercase()),
						ctx.player.location,
						count
					)
				} catch (_: Exception) {
				}
			}
		}
		ActionParser.register("strike_lightning") { map ->
			val range = (map["range"] as? Number)?.toDouble() ?: 24.0
			FunctionalAction { ctx ->
				val player = ctx.player
				val world = player.world
				val origin = player.eyeLocation
				val direction = origin.direction
				val result = world.rayTrace(
					origin,
					direction,
					range,
					FluidCollisionMode.NEVER,
					true,
					0.3
				) { entity -> entity != player && entity is LivingEntity }
				// Prefer the hit entity's location, fall back to hit block
				val strikeLocation = result?.hitEntity?.location
					?: result?.hitBlock?.location
					?: return@FunctionalAction
				world.strikeLightning(strikeLocation)
			}
		}
		ActionParser.register("explosion") { map ->
			val power = (map["power"] as? Number)?.toFloat() ?: 4f
			FunctionalAction { ctx ->
				val loc = ctx.block?.location ?: ctx.entity?.location ?: ctx.player.location
				ctx.player.world.createExplosion(loc, power)
			}
		}
		ActionParser.register("push") { map ->
			val x = (map["x"] as? Number)?.toDouble() ?: 0.0
			val y = (map["y"] as? Number)?.toDouble() ?: 0.0
			val z = (map["z"] as? Number)?.toDouble() ?: 0.0
			FunctionalAction { ctx -> ctx.player.velocity = ctx.player.velocity.add(Vector(x, y, z)) }
		}

		// ── Block Interactions ────────────────────────────────────────────────────
		ActionParser.register("set_block") { map ->
			val matStr = map["material"] as? String ?: return@register null
			FunctionalAction { ctx ->
				try {
					ctx.block?.type = org.bukkit.Material.valueOf(matStr.uppercase())
				} catch (_: Exception) {
				}
			}
		}
		ActionParser.register("break_block") {
			FunctionalAction { ctx -> ctx.block?.breakNaturally(ctx.handItem) }
		}

		// ── Target (Entity) Interactions ──────────────────────────────────────────
		ActionParser.register("damage_target") { map ->
			val amount = (map["amount"] as? Number)?.toDouble() ?: 1.0
			FunctionalAction { ctx -> (ctx.entity as? LivingEntity)?.damage(amount, ctx.player) }
		}
		ActionParser.register("heal_target") { map ->
			val amount = (map["amount"] as? Number)?.toDouble() ?: 1.0
			FunctionalAction { ctx ->
				val target = ctx.entity as? LivingEntity ?: return@FunctionalAction
				val max = target.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
				target.health = (target.health + amount).coerceAtMost(max)
			}
		}
		ActionParser.register("ignite_target") { map ->
			val ticks = (map["ticks"] as? Number)?.toInt() ?: 60
			val range = (map["range"] as? Number)?.toDouble() ?: 8.0
			FunctionalAction { ctx ->
				val player = ctx.player
				val result = player.world.rayTrace(
					player.eyeLocation,
					player.eyeLocation.direction,
					range,
					FluidCollisionMode.NEVER,
					true,
					0.3
				) { entity -> entity != player && entity is LivingEntity }
				val target = result?.hitEntity as? LivingEntity ?: return@FunctionalAction
				target.fireTicks = ticks
			}
		}
		ActionParser.register("add_potion_effect_target") { map ->
			val effect = map["effect"] as? String ?: return@register null
			val duration = (map["duration"] as? Number)?.toInt() ?: 200
			val amp = (map["amplifier"] as? Number)?.toInt() ?: 0
			FunctionalAction { ctx ->
				PotionEffectType.getByName(effect.uppercase())?.let { type ->
					(ctx.entity as? LivingEntity)?.addPotionEffect(PotionEffect(type, duration, amp))
				}
			}
		}

		// ── Cooldowns ─────────────────────────────────────────────────────────────
		ActionParser.register("set_cooldown") { map ->
			val ticks = (map["ticks"] as? Number)?.toInt() ?: 20
			FunctionalAction { ctx ->
				CooldownManager.set(ctx.player, ctx.item.id, ticks, ctx.handItem)
			}
		}
		ActionParser.register("check_cooldown") {
			FunctionalAction { ctx ->
				if (CooldownManager.has(ctx.player, ctx.item.id)) {
					throw ActionDispatcher.ActionAbortException("cooldown")
				}
			}
		}
	}

	fun unregister(type: String): Boolean {
		return ActionParser.unregister(type)
	}

	fun clearCustom() {
		ActionParser.clear()
		registerAll()
	}
}
