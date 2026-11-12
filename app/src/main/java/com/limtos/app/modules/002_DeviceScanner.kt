package com.limtos.app.modules

/**
 * Read-only Android device scan.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object DeviceScanner {
    const val MODULE_ID: String = "LIMT-002"
    const val MODULE_NAME: String = "DeviceScanner"
    const val DESCRIPTION: String = "Read-only Android device scan"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
