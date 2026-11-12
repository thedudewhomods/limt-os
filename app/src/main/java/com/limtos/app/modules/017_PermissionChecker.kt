package com.limtos.app.modules

/**
 * Permission state inspection.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object PermissionChecker {
    const val MODULE_ID: String = "LIMT-017"
    const val MODULE_NAME: String = "PermissionChecker"
    const val DESCRIPTION: String = "Permission state inspection"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
