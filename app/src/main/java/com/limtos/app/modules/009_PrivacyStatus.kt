package com.limtos.app.modules

/**
 * Privacy-safe data collection policy.
 *
 * This source module is an isolated building block for Limt OS.
 * It deliberately contains no privileged device operations.
 */
object PrivacyStatus {
    const val MODULE_ID: String = "LIMT-009"
    const val MODULE_NAME: String = "PrivacyStatus"
    const val DESCRIPTION: String = "Privacy-safe data collection policy"
    const val IMPLEMENTATION_STATUS: String = "foundation"
}
