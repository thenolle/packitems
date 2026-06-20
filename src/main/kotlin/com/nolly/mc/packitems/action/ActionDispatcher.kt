package com.nolly.mc.packitems.action

import com.nolly.mc.packitems.PackItems

class ActionDispatcher(private val plugin: PackItems) {

	fun dispatch(context: ActionContext) {
		val config = context.item.config
		val path = "actions.${context.trigger.name}"
		if (!config.isList(path)) return
		val rawList = config.getMapList(path)
		val actions = ActionParser.parse(rawList)
		for (action in actions) {
			try {
				action.execute(context)
			} catch (_: ActionAbortException) {
				return
			} catch (exception: Exception) {
				plugin.logger.warning(
					"Action failed on item '${context.item.id}' trigger '${context.trigger.name}': ${exception.message}"
				)
			}
		}
	}

	class ActionAbortException(reason: String = "") : RuntimeException(reason)
}
