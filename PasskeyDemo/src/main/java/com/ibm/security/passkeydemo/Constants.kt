/*
 * Copyright contributors to the IBM Security Verify Sample App for Passkey on Android
 */

package com.ibm.security.passkeydemo

import org.json.JSONObject
import java.util.Base64

object Constants {

    const val TAG = "Passkey Demo"
    const val SHARED_PREFERENCES = "com.ibm.security.passkey.SHARED_PREFERENCES"
    const val SHARED_PREF_TOKEN = "TOKEN"
    const val SHARED_PREF_DISPLAY_NAME = "DISPLAY_NAME"
    const val SERVER = "https://<your-rp-server>"

    fun getDisplayName(jwt: String): String {
        val parts = jwt.split(".")
        val payload = String(
            Base64.getUrlDecoder().decode(parts[1].toByteArray(charset("UTF-8"))),
            charset("UTF-8")
        )
        return JSONObject(payload).getString("displayName")
    }

}