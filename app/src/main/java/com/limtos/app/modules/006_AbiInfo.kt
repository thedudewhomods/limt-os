package com.limtos.app.modules

/**
 * Supported CPU application binary interfaces.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object AbiInfo {
    const val MODULE_ID: String = "LIMT-006"
    const val MODULE_NAME: String = "AbiInfo"
    const val DESCRIPTION: String = "Supported CPU application binary interfaces"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
