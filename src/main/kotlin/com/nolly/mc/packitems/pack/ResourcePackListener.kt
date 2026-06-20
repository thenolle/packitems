package com.nolly.mc.packitems.pack

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ResourcePackListener(private val manager: ResourcePackManager) : Listener {
	@EventHandler
	fun onJoin(event: PlayerJoinEvent) {
		if (manager.config.sendOnJoin) {
			manager.sendPack(event.player)
		}
	}
}
