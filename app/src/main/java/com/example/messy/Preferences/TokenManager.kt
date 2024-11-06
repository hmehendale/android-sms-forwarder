package com.example.messy.Preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class TokenManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val TOKEN_KEY = "gmail_app_token"
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val ePrefs = EncryptedSharedPreferences.create(
        PREFS_NAME,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun storeToken(token: String) {
        ePrefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        return ePrefs.getString(TOKEN_KEY, null)
    }

    fun clear() {
        ePrefs.edit().remove(TOKEN_KEY).apply()
    }
}