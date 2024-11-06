package com.example.messy

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
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
        Log.d("FORWARDER/Main", "Destination Address is ${settings.destEmailAddress}")

        val shortCodeOnlyText = if (settings.onlyForwardShortCodes) "short-code" else "all"
        val forwardingText = "Forwarding ${shortCodeOnlyText} messages to ${settings.destEmailAddress}"

        findViewById<TextView>(R.id.forwarding_address).text = forwardingText
    }
}