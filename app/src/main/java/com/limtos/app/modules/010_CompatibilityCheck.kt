package com.limtos.app.modules

/**
 * Device and OS compatibility evaluation.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object CompatibilityCheck {
    const val MODULE_ID: String = "LIMT-010"
    const val MODULE_NAME: String = "CompatibilityCheck"
    const val DESCRIPTION: String = "Device and OS compatibility evaluation"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
