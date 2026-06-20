package com.nolly.mc.packitems.item

import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import java.io.File

data class PackItem(
	val id: String,
	val material: Material,
	val renderType: RenderType,
	val folder: File,
	val config: FileConfiguration,
	val textureFile: File,
	val modelFile: File?
) {
	fun name(): String = config.getString("name") ?: id
	fun lore(): List<String> = config.getStringList("lore")
}
