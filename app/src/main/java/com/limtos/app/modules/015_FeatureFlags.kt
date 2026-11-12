package com.limtos.app.modules

/**
 * Runtime feature availability.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object FeatureFlags {
    const val MODULE_ID: String = "LIMT-015"
    const val MODULE_NAME: String = "FeatureFlags"
    const val DESCRIPTION: String = "Runtime feature availability"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
