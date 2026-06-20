package com.nolly.mc.packitems.pack

import com.nolly.mc.packitems.PackItems
import com.nolly.mc.packitems.item.PackItem
import com.nolly.mc.packitems.item.RenderType
import java.nio.file.Files

class ResourcePackGenerator(private val plugin: PackItems) {
	private val outputRoot = PackItems.getGeneratedFolder().resolve("resourcepack")
	private val assetsRoot = outputRoot.resolve("assets/packitems")

	fun generateAll(items: Collection<PackItem>) {
		clearOutput()
		assetsRoot.mkdirs()
		generateTextures(items)
		generateModels(items)
		generateItemDefinitions(items)
		PackMcMetaGenerator.generate(outputRoot)
		copyPackIcon()
		plugin.logger.info("Resource pack generated with ${items.size} items")
	}

	private fun clearOutput() {
		if (outputRoot.exists()) outputRoot.deleteRecursively()
	}

	private fun generateTextures(items: Collection<PackItem>) {
		val texturesDirectory = assetsRoot.resolve("textures/item")
		texturesDirectory.mkdirs()
		for (item in items) {
			val target = texturesDirectory.resolve("${item.id}.png")
			Files.copy(item.textureFile.toPath(), target.toPath())
		}
	}

	private fun generateModels(items: Collection<PackItem>) {
		val modelsDirectory = assetsRoot.resolve("models/item")
		modelsDirectory.mkdirs()
		for (item in items) {
			val target = modelsDirectory.resolve("${item.id}.json")
			if (item.modelFile != null) {
				Files.copy(item.modelFile.toPath(), target.toPath())
				continue
			}
			target.writeText(createDefaultModel(item.id, item.renderType))
		}
	}

	private fun generateItemDefinitions(items: Collection<PackItem>) {
		val itemsDirectory = assetsRoot.resolve("items")
		itemsDirectory.mkdirs()
		for (item in items) {
			itemsDirectory.resolve("${item.id}.json").writeText(createItemDefinition(item.id))
		}
	}

	private fun copyPackIcon() {
		val stream = plugin.getResource("pack.png") ?: return
		val target = outputRoot.resolve("pack.png")
		stream.use { input -> target.outputStream().use { input.copyTo(it) } }
	}

	private fun createDefaultModel(id: String, renderType: RenderType): String {
		val parent = when (renderType) {
			RenderType.HANDHELD -> "minecraft:item/handheld"
			RenderType.FLAT -> "minecraft:item/generated"
		}
		return """
        {
          "parent": "$parent",
          "textures": {
            "layer0": "packitems:item/$id"
          }
        }
        """.trimIndent()
	}

	private fun createItemDefinition(id: String): String {
		return """
        {
          "model": {
            "type": "minecraft:model",
            "model": "packitems:item/$id"
          }
        }
        """.trimIndent()
	}
}
