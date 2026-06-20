package com.nolly.mc.packitems.command

import com.nolly.mc.packitems.PackItems
import com.nolly.mc.packitems.util.Text
import org.bukkit.command.*

class PackItemsCommand(private val plugin: PackItems) : CommandExecutor, TabCompleter {
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (args.isEmpty()) {
			sendHelp(sender)
			return true
		}
		when (args[0].lowercase()) {
			"reload" -> {
				if (!sender.hasPermission("packitems.reload")) {
					Text.send(sender, "<red>No permission.</red>")
					return true
				}
				handleReload(sender)
			}
			"rebuild" -> {
				if (!sender.hasPermission("packitems.rebuild")) {
					Text.send(sender, "<red>No permission.</red>")
					return true
				}
				handleRebuild(sender)
			}
			"list" -> {
				if (!sender.hasPermission("packitems.list")) {
					Text.send(sender, "<red>No permission.</red>")
					return true
				}
				handleList(sender)
			}
			"give" -> {
				if (!sender.hasPermission("packitems.give")) {
					Text.send(sender, "<red>No permission.</red>")
					return true
				}
				handleGive(sender, args)
			}
			else -> sendHelp(sender)
		}
		return true
	}

	private fun handleReload(sender: CommandSender) {
		plugin.reloadConfig()
		plugin.registry.unregisterAll()
		plugin.loader.loadAll()
		Text.send(sender, "<green>Items reloaded.</green>")
	}

	private fun handleRebuild(sender: CommandSender) {
		plugin.packManager.rebuild()
		Text.send(sender, "<gradient:#00ffcc:#0066ff>Resource pack rebuilt.</gradient>")
	}

	private fun handleList(sender: CommandSender) {
		val items = plugin.registry.all()
		Text.send(sender, "<yellow>Loaded items (${items.size}):</yellow>")
		items.forEach { Text.send(sender, "<gray>- ${it.id}</gray>") }
	}

	private fun handleGive(sender: CommandSender, args: Array<out String>) {
		if (args.size < 3) {
			Text.send(sender, "<red>Usage: /packitems give <player> <item></red>")
			return
		}
		val player = plugin.server.getPlayer(args[1])
		if (player == null) {
			Text.send(sender, "<red>Player not found.</red>")
			return
		}
		val item = plugin.registry.get(args[2])
		if (item == null) {
			Text.send(sender, "<red>Item not found.</red>")
			return
		}
		val stack = plugin.itemBuilder.build(item)
		player.inventory.addItem(stack)
		Text.send(sender, "<green>Gave ${item.id} to ${player.name}.</green>")
	}

	private fun sendHelp(sender: CommandSender) {
		Text.send(sender, "<gold>PackItems Commands:</gold>")
		Text.send(sender, "<gray>/packitems reload</gray>")
		Text.send(sender, "<gray>/packitems rebuild</gray>")
		Text.send(sender, "<gray>/packitems list</gray>")
		Text.send(sender, "<gray>/packitems give <player> <item></gray>")
	}

	override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
		if (args.size == 1) return listOf("reload", "rebuild", "list", "give").filter { it.startsWith(args[0], true) }
		if (args.size == 2 && args[0].equals("give", true)) return plugin.server.onlinePlayers.map { it.name }
		if (args.size == 3 && args[0].equals("give", true)) return plugin.registry.all().map { it.id }.filter { it.startsWith(args[2], true) }
		return emptyList()
	}
}
