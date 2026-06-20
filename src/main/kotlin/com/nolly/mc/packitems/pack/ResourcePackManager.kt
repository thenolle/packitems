package com.nolly.mc.packitems.pack

import com.nolly.mc.packitems.PackItems
import com.nolly.mc.packitems.item.PackItemRegistry
import org.bukkit.entity.Player

class ResourcePackManager(private val plugin: PackItems, private val registry: PackItemRegistry) {
	private val generator = ResourcePackGenerator(plugin)
	val config = PackServerConfig()
	private val packRoot = PackItems.getGeneratedFolder().resolve("resourcepack")
	private val server = ResourcePackHttpServer(plugin, config, packRoot)

	fun rebuild() {
		PackMcMetaGenerator.generate(packRoot)
		generator.generateAll(registry.all())
		server.rebuildZip()
		plugin.logger.info("Hash after rebuild: ${server.zipHash.size} bytes")
	}

	fun startServer() {
		server.start()
		plugin.logger.info("Hash after start: ${server.zipHash.size} bytes, enabled: ${config.enabled}, serverEnabled: ${config.serverEnabled}")
	}

	fun stopServer() = server.stop()

	fun sendPack(player: Player) {
		plugin.logger.info("sendPack called: enabled=${config.enabled}, hashSize=${server.zipHash.size}, force=${config.force}, url=${config.publicUrl}")
		if (!config.enabled) {
			plugin.logger.warning("Resource pack not enabled")
			return
		}
		if (server.zipHash.isEmpty()) {
			plugin.logger.warning("Zip hash is empty!")
			return
		}
		player.setResourcePack(config.publicUrl, server.zipHash, config.force)
	}
}
