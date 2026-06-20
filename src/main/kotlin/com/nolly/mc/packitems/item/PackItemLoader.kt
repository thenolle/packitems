package com.nolly.mc.packitems.item

import com.nolly.mc.packitems.PackItems
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class PackItemLoader(private val plugin: PackItems) {
	fun loadAll(): Int {
		val folder = PackItems.getItemsFolder()
		if (!folder.exists()) {
			folder.mkdirs()
			return 0
		}
		plugin.registry.unregisterAll()
		var loaded = 0
		folder.listFiles()?.filter { it.isDirectory }?.forEach { itemFolder ->
			val item = loadItem(itemFolder)
			if (item != null) {
				plugin.registry.register(item)
				loaded++
			}
		}
		plugin.logger.info("Loaded $loaded PackItems")
		return loaded
	}

	private fun loadItem(folder: File): PackItem? {
		val id = folder.name.lowercase()
		val configFile = folder.resolve("item.yml")
		val textureFile = folder.resolve("texture.png")
		val modelFile = folder.resolve("model.json")
		if (!configFile.exists()) {
			plugin.logger.warning("Skipping $id: missing item.yml")
			return null
		}
		if (!textureFile.exists()) {
			plugin.logger.warning("Skipping $id: missing texture.png")
			return null
		}
		val config = YamlConfiguration.loadConfiguration(configFile)
		val materialName = config.getString("material") ?: "STONE"
		val renderTypeRaw = config.getString("render") ?: "FLAT"
		val material = try {
			Material.valueOf(materialName.uppercase())
		} catch (_: Exception) {
			plugin.logger.warning("Skipping $id: invalid material $materialName")
			return null
		}
		val renderType = try {
			RenderType.valueOf(renderTypeRaw.uppercase())
		} catch (_: Exception) {
			plugin.logger.warning("Invalid render type for $id, defaulting to FLAT")
			RenderType.FLAT
		}
		return PackItem(
			id = id,
			material = material,
			renderType = renderType,
			folder = folder,
			config = config,
			textureFile = textureFile,
			modelFile = if (modelFile.exists()) modelFile else null
		)
	}
}
