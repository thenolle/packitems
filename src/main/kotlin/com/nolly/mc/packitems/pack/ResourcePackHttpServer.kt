package com.nolly.mc.packitems.pack

import com.nolly.mc.packitems.PackItems
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ResourcePackHttpServer(
	private val plugin: PackItems,
	private val config: PackServerConfig,
	private val packFolder: File
) {
	private var server: HttpServer? = null
	private val zipFile: File = PackItems.getGeneratedFolder().resolve("packitems.zip")
	private val tempZipFile: File = PackItems.getGeneratedFolder().resolve("packitems.zip.tmp")
	private val lock = ReentrantLock()

	var zipHash: ByteArray = ByteArray(0)
		private set

	fun start() {
		if (!config.enabled || !config.serverEnabled) return
		rebuildZip()
		val socket = InetSocketAddress(config.host, config.port)
		server = HttpServer.create(socket, 0)
		server!!.createContext(config.path) { exchange ->
			lock.lock()
			try {
				if (!zipFile.exists()) {
					exchange.sendResponseHeaders(500, 0)
					exchange.close()
					return@createContext
				}
				exchange.responseHeaders.add("Content-Type", "application/zip")
				exchange.sendResponseHeaders(200, zipFile.length())
				zipFile.inputStream().use { input -> exchange.responseBody.use { output -> input.copyTo(output) } }
			} finally {
				lock.unlock()
				exchange.close()
			}
		}
		server!!.executor = Executors.newFixedThreadPool(2)
		server!!.start()
		plugin.logger.info("ResourcePack HTTP server started on port ${config.port}")
	}

	fun stop() {
		server?.stop(0)
		server = null
	}

	fun rebuildZip() {
		lock.lock()
		try {
			if (tempZipFile.exists()) tempZipFile.delete()
			ZipOutputStream(tempZipFile.outputStream()).use { zip ->
				packFolder.walkTopDown().filter { it.isFile }.forEach { file ->
					val relative = file.relativeTo(packFolder).path.replace("\\", "/")
					zip.putNextEntry(ZipEntry(relative))
					file.inputStream().use { it.copyTo(zip) }
					zip.closeEntry()
				}
			}
			if (zipFile.exists()) zipFile.delete()
			tempZipFile.renameTo(zipFile)
			zipHash = calculateHash()
		} finally {
			lock.unlock()
		}
	}

	private fun calculateHash(): ByteArray {
		if (!zipFile.exists()) return ByteArray(0)
		val digest = MessageDigest.getInstance("SHA-1")
		zipFile.inputStream().use { input ->
			val buffer = ByteArray(8192)
			var read: Int
			while (input.read(buffer).also { read = it } != -1) {
				digest.update(buffer, 0, read)
			}
		}
		return digest.digest()
	}
}
