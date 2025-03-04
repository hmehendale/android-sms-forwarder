package com.example.messy

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.telephony.SubscriptionManagerCompat
import com.example.messy.Mailer.SendMail
import com.example.messy.Preferences.Settings
import com.example.messy.Preferences.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SMSReceiver: BroadcastReceiver(), CoroutineScope by MainScope() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isEmpty()) {
                // Should never happen
                Log.i("SMS_RECEIVER", "Received zero-length SMS broadcast ?!")
                return
            }
            Log.d("SMS_RECEIVER", "Received ${messages.size} messages")
            val subscriptionId = intent?.getIntExtra("subscription", -1)
            var recvSIM: String? = null
            if (subscriptionId != null && subscriptionId != -1) {
                val slotIndex = SubscriptionManagerCompat.getSlotIndex(subscriptionId)
                val subsMgr = context?.getSystemService(
                    Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val simDetails = subsMgr.getActiveSubscriptionInfoForSimSlotIndex(slotIndex)
                recvSIM = "${simDetails.carrierName}"
                simDetails.carrierName
            }

            val origin = messages[0].originatingAddress.orEmpty()

            var body = ""

            var i = 0
            do {
                body += messages[i].messageBody
            } while (++i < messages.size)

            Log.d("SMS_RECEIVER",
                "Final message origin=$origin (via ${recvSIM.orEmpty()}, body=$body")

            if (context != null && shouldForwardMessage(context, origin)) {
                    Log.d("SMS_RECEIVER/Send", "Should forward: true, sending" )
                    generateAndSendEmail(context, origin, recvSIM, body)
            }
        }
    }

    fun generateAndSendEmail(context: Context, origin: String, recvSIM: String?, body: String) {
        val username = Settings(context).sourceEmailAddress
        val dest = Settings(context).destEmailAddress
        val appPassword = TokenManager(context).getToken()

        if (username == null || username == ""
            || appPassword == null || appPassword == ""
            || dest == null || dest == "") {
            return
        }

        val mailSender = SendMail(username, appPassword)
        var subject = "Forwarded SMS from $origin"
        if (recvSIM != null) {
            subject += " (via ${recvSIM})"
        }

        launch {
            // Send - fire and forget. If there's an error in sending the email,
            // worry about it later (and in the Mailer package, not here)
            mailSender.sendMail(dest, subject, body)
        }
    }

    // Decides whether or not to forward the message, based on whether the
    // origin matches the setting (only short-codes for now)
    private fun shouldForwardMessage(context: Context, origin: String): Boolean {
        val forwardingEnabled: Boolean = Settings(context).forwardingEnabled
        val shortCodesOnly: Boolean = Settings(context).onlyForwardShortCodes

        if (!forwardingEnabled) {
            Log.d("FORWARDER", "SMS forwarding stopped")
            return false
        }

        if (!shortCodesOnly) {
            // Forward all messages
            Log.d("FORWARDER", "SMS forwarding enabled for all messages")
            return true
        }

        // Can't use the builtin Telephony API because this should also catc
        // all numeric short codes (like 121 or 50505)
        val phoneREx = Regex("^\\+?[0-9]{7,}$")
        val matches = phoneREx.matches(origin)
        // Send if the regex for at least a 7-digit number does not match
        Log.d("FORWARDER", "SMS forwarding for this message = $matches")
        return !matches
    }
}
