package com.nolly.mc.packitems.action

object ActionParser {
	private val registry = mutableMapOf<String, (Map<*, *>) -> Action?>()

	fun register(type: String, factory: (Map<*, *>) -> Action?) {
		registry[type.lowercase()] = factory
	}

	fun parse(list: List<Map<*, *>>): List<Action> {
		val result = mutableListOf<Action>()
		for (map in list) {
			val type = (map["type"] as? String)?.lowercase() ?: continue
			val factory = registry[type] ?: continue
			val action = factory(map)
			if (action != null) result.add(action)
		}
		return result
	}
}
