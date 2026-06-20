package com.nolly.mc.packitems.action

import com.nolly.mc.packitems.PackItems
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object CooldownManager {
	private val cooldowns: MutableMap<UUID, MutableMap<String, Long>> = ConcurrentHashMap()

	private val plugin: PackItems
		get() = PackItems.instance

	fun has(player: Player, itemId: String): Boolean {
		val map = cooldowns[player.uniqueId] ?: return false
		val end = map[itemId] ?: return false
		return System.currentTimeMillis() < end
	}

	fun remainingTicks(player: Player, itemId: String): Int {
		val map = cooldowns[player.uniqueId] ?: return 0
		val end = map[itemId] ?: return 0
		val ms = (end - System.currentTimeMillis()).coerceAtLeast(0)
		return (ms / 50L).toInt()
	}

	fun set(player: Player, itemId: String, ticks: Int, stack: ItemStack? = null) {
		val ms = ticks * 50L
		val map = cooldowns.computeIfAbsent(player.uniqueId) { ConcurrentHashMap() }
		map[itemId] = System.currentTimeMillis() + ms
		applyVanillaCooldown(player, ticks, stack)
	}

	fun clear(player: Player) {
		cooldowns.remove(player.uniqueId)
	}

	private fun applyVanillaCooldown(player: Player, ticks: Int, stack: ItemStack?) {
		val item = stack ?: return
		player.setCooldown(item.type, ticks)
	}
}
