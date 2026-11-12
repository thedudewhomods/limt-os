package com.limtos.app.modules

/**
 * Backend reachability checks.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object BackendHealth {
    const val MODULE_ID: String = "LIMT-020"
    const val MODULE_NAME: String = "BackendHealth"
    const val DESCRIPTION: String = "Backend reachability checks"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
