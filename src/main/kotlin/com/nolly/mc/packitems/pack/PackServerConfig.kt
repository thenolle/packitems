package com.nolly.mc.packitems.pack

import com.nolly.mc.packitems.PackItems

class PackServerConfig {
	private val config = PackItems.instance.config

	val enabled: Boolean get() = config.getBoolean("resource-pack.enabled")
	val sendOnJoin: Boolean get() = config.getBoolean("resource-pack.send-on-join")
	val force: Boolean get() = config.getBoolean("resource-pack.force")

	val serverEnabled: Boolean get() = config.getBoolean("resource-pack.server.enabled")
	val host: String get() = config.getString("resource-pack.server.host") ?: "0.0.0.0"
	val port: Int get() = config.getInt("resource-pack.server.port")
	val path: String get() = config.getString("resource-pack.server.path") ?: "/packitems.zip"

	val publicUrl: String get() {
		val configured = config.getString("resource-pack.server.public-url")
		return if (!configured.isNullOrEmpty()) configured
		else "http://${if (host == "0.0.0.0") "127.0.0.1" else host}:$port$path"
	}
}
