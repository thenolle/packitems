package com.nolly.mc.packitems.item

class PackItemRegistry {
	private val items = mutableMapOf<String, PackItem>()

	fun register(item: PackItem) {
		items[item.id.lowercase()] = item
	}

	fun unregister(id: String): PackItem? {
		return items.remove(id.lowercase())
	}

	fun unregisterAll() {
		items.clear()
	}

	fun get(id: String): PackItem? = items[id.lowercase()]
	fun all(): Collection<PackItem> = items.values
	fun size(): Int = items.size
}
