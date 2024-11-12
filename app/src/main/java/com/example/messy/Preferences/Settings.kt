package com.example.messy.Preferences

import android.content.Context
import androidx.preference.PreferenceManager

class Settings(context: Context) {
    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val ENABLE_FORWARDING = "forwarding_enabled"
        private const val SOURCE_EMAIL_ADDRESS = "source_email_addr"
        private const val DEST_EMAIL_ADDRESS = "dest_email_addr"
        private const val ONLY_FORWARD_SHORTCODE_SMS = "shortcode_sms_only"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var sourceEmailAddress: String?
        get() = prefs.getString(SOURCE_EMAIL_ADDRESS, null)
        set(value: String?) = prefs.edit().putString(SOURCE_EMAIL_ADDRESS, value).apply()

    var onlyForwardShortCodes: Boolean
        get() = prefs.getBoolean(ONLY_FORWARD_SHORTCODE_SMS, true)
        set(value: Boolean) = prefs.edit().putBoolean(ONLY_FORWARD_SHORTCODE_SMS, value).apply()

    var destEmailAddress: String?
        get() = prefs.getString(DEST_EMAIL_ADDRESS, null)
        set(value: String?) = prefs.edit().putString(DEST_EMAIL_ADDRESS, value).apply()

    var forwardingEnabled: Boolean
        get() = prefs.getBoolean(ENABLE_FORWARDING, true)
        set(value: Boolean) = prefs.edit().putBoolean(ENABLE_FORWARDING, value).apply()

}