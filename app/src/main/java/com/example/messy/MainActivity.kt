package com.example.messy

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.messy.Preferences.Settings
import com.example.messy.Preferences.SettingsActivity

const val READ_RECEIVE_SMS_PERMISSION = 0

class MainActivity: AppCompatActivity() {
    private val bcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val origin = intent?.getStringExtra("ORIGIN")
            val body = intent?.getStringExtra("BODY")

            if (body != null) {
                updateReceivedSmsInfo(origin, body)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Also register receiving broadcasts for SMS Received events
//        LocalBroadcastManager.getInstance(this)
//            .registerReceiver(bcastReceiver, IntentFilter(NEW_DATA_BROADCAST_INTENT))

        val missingPermissions = ArrayList<String>()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.READ_SMS)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.RECEIVE_SMS)
        }
        if (missingPermissions.size > 0) {
            ActivityCompat.requestPermissions(this,
                /* permissions = */ missingPermissions.toArray(arrayOfNulls<String>(0)),
                /* requestCode = */ READ_RECEIVE_SMS_PERMISSION
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val menuItemStopForwarding = menu?.findItem(R.id.action_stop_forwarding)
        val menuItemStartForwarding = menu?.findItem(R.id.action_start_forwarding)

        val isForwarding = Settings(this).forwardingEnabled

        // Stop forwarding should be visible if `isForwarding`
        // otherwise we want Start Forwarding to be visible.
        menuItemStopForwarding?.isVisible = isForwarding
        menuItemStartForwarding?.isVisible = !isForwarding
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val settings = Settings(this)
        return when(item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_start_forwarding -> {
                // Start forwarding
                settings.forwardingEnabled = true
                Toast.makeText(this, "Forwarding is enabled", Toast.LENGTH_LONG).show()
                updatePreferencesView()
                this.invalidateOptionsMenu();
                return true
            }
            R.id.action_stop_forwarding -> {
                // Stop forwarding
                settings.forwardingEnabled = false
                Toast.makeText(this, "Forwarding stopped", Toast.LENGTH_LONG).show()
                this.invalidateOptionsMenu();
                updatePreferencesView()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_RECEIVE_SMS_PERMISSION -> {
                // pass
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()
        updatePreferencesView()
    }

    override fun onStop() {
        super.onStop()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(bcastReceiver)
    }
    private fun updateReceivedSmsInfo(origin: String?, body: String) {
        // No-op for now
        // findViewById<TextView>(R.id.sms_origin).text = origin
        // findViewById<TextView>(R.id.message_contents).text = body
    }

    private fun updatePreferencesView() {
        val settings = Settings(this)
        val bar = supportActionBar
        Log.d("FORWARDER/Main", "Destination Address is ${settings.destEmailAddress}")

        var forwardingText: String
        if (!settings.forwardingEnabled) {
            forwardingText = "Forwarding stopped"
            bar?.setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(this, R.color.tangerine)))
        } else {
            val shortCodeOnlyText = if (settings.onlyForwardShortCodes) "short-code" else "all"
            forwardingText = "Forwarding ${shortCodeOnlyText} messages to ${settings.destEmailAddress}"
            bar?.setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(this, R.color.olivine)))
        }

        findViewById<TextView>(R.id.forwarding_address).text = forwardingText
    }
}