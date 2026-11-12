package com.limtos.app.modules

/**
 * Network connectivity summary.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object NetworkStatus {
    const val MODULE_ID: String = "LIMT-018"
    const val MODULE_NAME: String = "NetworkStatus"
    const val DESCRIPTION: String = "Network connectivity summary"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
