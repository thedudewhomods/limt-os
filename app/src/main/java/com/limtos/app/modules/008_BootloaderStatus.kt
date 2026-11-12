package com.limtos.app.modules

/**
 * Conservative bootloader status reporting.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object BootloaderStatus {
    const val MODULE_ID: String = "LIMT-008"
    const val MODULE_NAME: String = "BootloaderStatus"
    const val DESCRIPTION: String = "Conservative bootloader status reporting"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
