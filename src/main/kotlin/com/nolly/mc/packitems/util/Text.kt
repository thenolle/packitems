package com.nolly.mc.packitems.util

import com.nolly.mc.textapi.api.TextAPI
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Text {
	fun parse(text: String, player: Player? = null): String = TextAPI.parse(text, player)
	fun components(text: String, player: Player? = null): Array<BaseComponent> = TextAPI.components(text, player)
	fun send(sender: CommandSender, text: String) {
		when (sender) {
			is Player -> sender.spigot().sendMessage(*components(text, sender))
			else -> sender.sendMessage(parse(text))
		}
	}
}
