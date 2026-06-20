package com.nolly.mc.packitems.util

import java.io.File

object Files {
	fun ensureDirectory(file: File) {
		if (!file.exists()) {
			file.mkdirs()
		}
	}
}
