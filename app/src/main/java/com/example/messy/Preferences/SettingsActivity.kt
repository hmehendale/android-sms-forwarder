package com.example.messy.Preferences

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.messy.R
import com.google.android.material.textfield.TextInputEditText

const val TOKEN_EXISTS_STRING = "******"

fun TextInputEditText.debouncedTextChangeListener(
    debounceTime: Long = 300L, onDebouncedTextChange: (String) -> Unit) {
    var lastTextEdit: Long = 0L
    val handler = Handler(Looper.getMainLooper())
    val runnable = Runnable {
        onDebouncedTextChange(text.toString())
    }

    addTextChangedListener(object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No need to override
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // We're only interested in afterTextChanged
        }

        override fun afterTextChanged(s: Editable?) {
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, debounceTime)
        }
    })
}


class SettingsActivity: AppCompatActivity() {
    private lateinit var settings: Settings
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        settings = Settings(this)
        tokenManager = TokenManager(this)

        val shortCodesOnly = settings.onlyForwardShortCodes
        val sourceEmail = settings.sourceEmailAddress
        val destEmail = settings.destEmailAddress
        val sourceFilterCsv = settings.sourceFilters
        val emailToken = if (tokenManager.getToken() != null) TOKEN_EXISTS_STRING else ""
        Log.d("FORWARDER/Settings", "Stored token is " + tokenManager.getToken())

        val shortCodesOnlySwitch = findViewById<SwitchCompat>(R.id.short_codes_only_switch)
        shortCodesOnlySwitch.isChecked = shortCodesOnly
        shortCodesOnlySwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.onlyForwardShortCodes = isChecked
        }

        val sourceEmailEditView = findViewById<TextInputEditText>(R.id.source_email_address)
        sourceEmailEditView.setText(sourceEmail)
        sourceEmailEditView.debouncedTextChangeListener { text ->
            Log.d("FORWARDER/Settings", "Setting (partial?) email $text")
            settings.sourceEmailAddress = text

        }

        val destEmailEditView = findViewById<TextInputEditText>(R.id.dest_email_address)
        destEmailEditView.setText(destEmail)
        destEmailEditView.debouncedTextChangeListener { text ->
            if (text == "") {
                Log.d("FORWARDER/Settings", "No destination address, using source")
                settings.destEmailAddress = settings.sourceEmailAddress
            } else {
                Log.d("FORWARDER/Settings", "Setting (partial?) email $text")
                settings.destEmailAddress = text
            }
        }

        val emailTokenEditView = findViewById<TextInputEditText>(R.id.email_app_token)
        emailTokenEditView.setText(emailToken)
        emailTokenEditView.debouncedTextChangeListener() { text ->
            Log.d("FORWARDER/Settings", "Maybe setting (partial?) token $text")
            if (text != TOKEN_EXISTS_STRING) {
                tokenManager.storeToken(text)
            }
        }

        val srcFilterView = findViewById<TextInputEditText>(R.id.source_filters)
        srcFilterView.setText(sourceFilterCsv)
        srcFilterView.debouncedTextChangeListener() { text ->
            settings.sourceFilters = text
        }
    }

    // Handle the "back" button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // finish and close this activity view
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}