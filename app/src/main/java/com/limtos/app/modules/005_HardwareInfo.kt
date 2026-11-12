package com.limtos.app.modules

/**
 * Hardware identifiers exposed by Android.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object HardwareInfo {
    const val MODULE_ID: String = "LIMT-005"
    const val MODULE_NAME: String = "HardwareInfo"
    const val DESCRIPTION: String = "Hardware identifiers exposed by Android"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
