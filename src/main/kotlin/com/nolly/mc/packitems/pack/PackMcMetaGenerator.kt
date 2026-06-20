package com.nolly.mc.packitems.pack

import java.io.File

object PackMcMetaGenerator {
	fun generate(root: File) {
		val file = root.resolve("pack.mcmeta")
		file.parentFile.mkdirs()
		file.writeText(
			"""
			{
			  "pack": {
				"pack_format": 64,
				"description": "PackItems Generated Resource Pack",
				"min_format": 64,
				"max_format": 64
			  }
			}
			""".trimIndent()
		)
	}
}
