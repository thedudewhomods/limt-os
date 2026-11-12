package com.limtos.app.modules

/**
 * Non-sensitive device identity summary.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object DeviceIdentity {
    const val MODULE_ID: String = "LIMT-003"
    const val MODULE_NAME: String = "DeviceIdentity"
    const val DESCRIPTION: String = "Non-sensitive device identity summary"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
