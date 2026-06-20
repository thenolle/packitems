package com.nolly.mc.packitems.item

import com.nolly.mc.packitems.PackItems
import com.nolly.mc.packitems.util.Text
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class PackItemBuilder(private val plugin: PackItems) {
	private val idKey = NamespacedKey(plugin, "id")

	fun build(item: PackItem): ItemStack {
		val stack = ItemStack(item.material)
		applyStackSize(stack, item)
		val meta = stack.itemMeta ?: return stack
		applyIdentity(meta, item)
		applyDisplay(meta, item)
		applyEnchantments(meta, item)
		applyItemFlags(meta, item)
		applyUnbreakable(meta, item)
		applyCustomModelData(meta, item)
		applyRenderHint(meta, item)
		applyItemModel(meta, item)
		applyAttributes(meta, item)
		stack.itemMeta = meta
		return stack
	}

	private fun applyIdentity(meta: ItemMeta, item: PackItem) {
		meta.persistentDataContainer.set(idKey, PersistentDataType.STRING, item.id)
	}

	private fun applyDisplay(meta: ItemMeta, item: PackItem) {
		val name = item.config.getString("name") ?: item.id
		if (name.isNotBlank()) meta.setDisplayName(Text.parse(name, null))
		val lore = item.config.getStringList("lore")
		if (lore.isNotEmpty()) meta.lore = lore.map { Text.parse(it, null) }
	}

	private fun applyStackSize(stack: ItemStack, item: PackItem) {
		val size = item.config.getInt("item.max-stack-size", -1)
		if (size <= 0) return
		val meta = stack.itemMeta ?: return
		meta.setMaxStackSize(size.coerceIn(1, 64))
		stack.itemMeta = meta
	}

	private fun resolveAttribute(name: String): Attribute? =
		runCatching { Attribute.valueOf(name.uppercase()) }.getOrNull()

	@Suppress("UnstableApiUsage")
	private fun applyAttributes(meta: ItemMeta, item: PackItem) {
		val section = item.config.getConfigurationSection("item.attributes") ?: return
		for (key in section.getKeys(false)) {
			val attribute = resolveAttribute(key)
			if (attribute == null) {
				plugin.logger.warning("[PackItems] Unknown attribute '$key' on item '${item.id}'")
				continue
			}
			val value = section.getDouble(key)
			meta.removeAttributeModifier(attribute)
			val modKey = NamespacedKey(plugin, "attr_${item.id}_$key")
			val modifier = AttributeModifier(
				modKey,
				value,
				AttributeModifier.Operation.ADD_NUMBER,
				EquipmentSlotGroup.ANY
			)
			meta.addAttributeModifier(attribute, modifier)
		}
	}

	private fun applyEnchantments(meta: ItemMeta, item: PackItem) {
		val section = item.config.getConfigurationSection("item.enchantments") ?: return
		for (key in section.getKeys(false)) {
			val level = section.getInt(key, 1)
			val enchant = resolveEnchantment(key)
			if (enchant == null) {
				plugin.logger.warning("[PackItems] Unknown enchantment '$key' on item '${item.id}'")
				continue
			}
			meta.addEnchant(enchant, level, true)
		}
	}

	@Suppress("DEPRECATION")
	private fun resolveEnchantment(name: String): Enchantment? {
		val key = runCatching { NamespacedKey.minecraft(name.lowercase()) }.getOrNull()
		if (key != null) {
			val found = Enchantment.getByKey(key)
			if (found != null) return found
		}
		return Enchantment.getByName(name.uppercase())
	}

	private fun applyItemFlags(meta: ItemMeta, item: PackItem) {
		val flags = item.config.getStringList("item.flags")
		for (flag in flags) {
			val resolved = runCatching { ItemFlag.valueOf(flag.uppercase()) }.getOrNull()
			if (resolved == null) {
				plugin.logger.warning("[PackItems] Unknown item flag '$flag' on item '${item.id}'")
				continue
			}
			meta.addItemFlags(resolved)
		}
	}

	private fun applyUnbreakable(meta: ItemMeta, item: PackItem) {
		if (item.config.contains("item.unbreakable")) {
			meta.isUnbreakable = item.config.getBoolean("item.unbreakable", false)
		}
	}

	@Suppress("DEPRECATION")
	private fun applyCustomModelData(meta: ItemMeta, item: PackItem) {
		val cmd = item.config.getInt("item.custom-model-data", -1)
		if (cmd >= 0) meta.setCustomModelData(cmd)
	}

	private fun applyRenderHint(meta: ItemMeta, item: PackItem) {
		meta.persistentDataContainer.set(
			NamespacedKey(plugin, "render_type"),
			PersistentDataType.STRING,
			item.renderType.name
		)
	}

	@Suppress("UnstableApiUsage")
	private fun applyItemModel(meta: ItemMeta, item: PackItem) {
		meta.itemModel = NamespacedKey("packitems", item.id)
	}
}
