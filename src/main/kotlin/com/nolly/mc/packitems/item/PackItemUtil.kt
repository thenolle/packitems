package com.nolly.mc.packitems.item

import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.NamespacedKey

object PackItemUtil {
	fun getId(stack: ItemStack?, key: NamespacedKey): String? {
		if (stack == null || !stack.hasItemMeta()) return null
		val meta = stack.itemMeta ?: return null
		return meta.persistentDataContainer.get(key, PersistentDataType.STRING)
	}
}
