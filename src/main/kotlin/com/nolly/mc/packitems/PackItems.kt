package com.nolly.mc.packitems

import com.nolly.mc.packitems.action.ActionDispatcher
import com.nolly.mc.packitems.action.ActionRegistry
import com.nolly.mc.packitems.action.CooldownManager
import com.nolly.mc.packitems.action.TriggerRouter
import com.nolly.mc.packitems.command.PackItemsCommand
import com.nolly.mc.packitems.item.PackItemBuilder
import com.nolly.mc.packitems.item.PackItemLoader
import com.nolly.mc.packitems.item.PackItemRegistry
import com.nolly.mc.packitems.pack.ResourcePackListener
import com.nolly.mc.packitems.pack.ResourcePackManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class PackItems : JavaPlugin() {
	companion object {
		lateinit var instance: PackItems private set
		fun getItemsFolder() = instance.dataFolder.resolve("items")
		fun getGeneratedFolder() = instance.dataFolder.resolve("generated")
	}

	lateinit var registry: PackItemRegistry private set
	lateinit var itemBuilder: PackItemBuilder private set
	lateinit var actionDispatcher: ActionDispatcher private set
	lateinit var loader: PackItemLoader
	lateinit var packManager: ResourcePackManager

	override fun onEnable() {
		instance = this
		saveDefaultConfig()
		createDirectories()
		registry = PackItemRegistry()
		itemBuilder = PackItemBuilder(this)
		actionDispatcher = ActionDispatcher(this)
		ActionRegistry.registerAll()
		loader = PackItemLoader(this)
		loader.loadAll()
		packManager = ResourcePackManager(this, registry)
		packManager.rebuild()
		packManager.startServer()
		val command = PackItemsCommand(this)
		getCommand("packitems")?.setExecutor(command)
		getCommand("packitems")?.tabCompleter = command
		server.pluginManager.registerEvents(TriggerRouter(this), this)
		server.pluginManager.registerEvents(ResourcePackListener(packManager), this)
		server.pluginManager.registerEvents(object : Listener { @EventHandler fun onQuit(event: PlayerQuitEvent) { CooldownManager.clear(event.player) } }, this)
		logger.info("PackItems enabled.")
	}

	override fun onDisable() {
		packManager.stopServer()
		logger.info("PackItems disabled.")
	}

	private fun createDirectories() {
		dataFolder.mkdirs()
		getItemsFolder().mkdirs()
		getGeneratedFolder().mkdirs()
	}
}
