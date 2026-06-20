package com.nolly.mc.packitems

import com.nolly.mc.packitems.action.Action
import com.nolly.mc.packitems.action.ActionContext
import com.nolly.mc.packitems.action.ActionParser
import com.nolly.mc.packitems.action.Trigger
import com.nolly.mc.packitems.item.PackItem
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import java.util.concurrent.ConcurrentHashMap

object PackItemsAPI {
	private val triggerHooks = ConcurrentHashMap<Trigger, MutableSet<(ActionContext) -> Unit>>()
	private val registeredListeners = ConcurrentHashMap.newKeySet<Listener>()

	lateinit var plugin: PackItems
		private set

	fun init(plugin: PackItems) {
		this.plugin = plugin
	}

	fun registerItem(item: PackItem): PackItem {
		plugin.registry.register(item)
		return item
	}

	fun unregisterItem(id: String): PackItem? {
		val existing = plugin.registry.get(id)
		plugin.registry.unregister(id)
		return existing
	}

	fun getItem(id: String): PackItem? = plugin.registry.get(id)

	fun listItems(): Collection<PackItem> = plugin.registry.all()

	fun registerAction(type: String, factory: (Map<*, *>) -> Action?) {
		ActionParser.register(type, factory)
	}

	fun unregisterAction(type: String) {
		ActionParser.unregister(type)
	}

	fun hookTrigger(trigger: Trigger, hook: (ActionContext) -> Unit) {
		triggerHooks.computeIfAbsent(trigger) { ConcurrentHashMap.newKeySet() }.add(hook)
	}

	fun unhookTrigger(trigger: Trigger, hook: (ActionContext) -> Unit) {
		triggerHooks[trigger]?.remove(hook)
	}

	fun dispatchTriggerHooks(context: ActionContext) {
		triggerHooks[context.trigger]?.forEach { hook ->
			runCatching { hook(context) }
				.onFailure {
					plugin.logger.warning(
						"Trigger hook failed for ${context.trigger.name}: ${it.message}"
					)
				}
		}
	}

	fun registerListener(listener: Listener) {
		Bukkit.getPluginManager().registerEvents(listener, plugin)
		registeredListeners.add(listener)
	}

	fun unregisterListener(listener: Listener) {
		HandlerList.unregisterAll(listener)
		registeredListeners.remove(listener)
	}

	fun unregisterAllListeners() {
		registeredListeners.forEach { HandlerList.unregisterAll(it) }
		registeredListeners.clear()
	}

	fun reset() {
		unregisterAllListeners()
		triggerHooks.clear()
	}
}
