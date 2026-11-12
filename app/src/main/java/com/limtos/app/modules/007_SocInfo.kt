package com.limtos.app.modules

/**
 * System-on-chip details when Android exposes them.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object SocInfo {
    const val MODULE_ID: String = "LIMT-007"
    const val MODULE_NAME: String = "SocInfo"
    const val DESCRIPTION: String = "System-on-chip details when Android exposes them"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
