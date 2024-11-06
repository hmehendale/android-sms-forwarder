package com.example.messy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.telephony.SmsMessage
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.messy.Mailer.SendMail
import com.example.messy.Preferences.Settings
import com.example.messy.Preferences.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

const val NEW_DATA_BROADCAST_INTENT = "com.example.messy.NEW_MESSAGE_RECEIVED"

class SMSReceiver: BroadcastReceiver(), CoroutineScope by MainScope() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            for (message: SmsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                val origin = message.originatingAddress.orEmpty()
                val body = message.messageBody


                if (context != null) {
                    if (!shouldForwardMessage(context, origin)) {
                        return
                    }
                    generateAndSendEmail(context, origin, body)

//                    val sendIntent = Intent(NEW_DATA_BROADCAST_INTENT)
//                    sendIntent.putExtra("ORIGIN", origin)
//                    sendIntent.putExtra("BODY", body)
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent)
//                    Log.d("SMS_RECEIVER", "Forwarded ($origin, $body) to interested listeners")
                } else {
                    Log.d("SMS_RECEIVER", "No context to pass data along; received SMS from $origin")
                }
            }
        }
    }

    fun generateAndSendEmail(context: Context, origin: String, body: String) {
        val username = Settings(context).sourceEmailAddress
        val dest = Settings(context).destEmailAddress
        val appPassword = TokenManager(context).getToken()

        if (username == null || username == ""
            || appPassword == null || appPassword == ""
            || dest == null || dest == "") {
            return
        }

        val mailSender = SendMail(username, appPassword);
        val subject = "Forwarded SMS from $origin"

        launch {
            // Send - fire and forget. If there's an error in sending the email,
            // worry about it later (and in the Mailer package, not here)
            mailSender.sendMail(dest, subject, body)
        }
    }

    // Decides whether or not to forward the message, based on whether the
    // origin matches the setting (only short-codes for now)
    private fun shouldForwardMessage(context: Context, origin: String): Boolean {
        val shortCodesOnly: Boolean = Settings(context).onlyForwardShortCodes

        if (!shortCodesOnly) {
            // Forward all messages
            return true
        }

        // Can't use the builtin Telephony API because this should also catc
        // all numeric short codes (like 121 or 50505)
        val phoneREx = Regex("^\\+?[0-9]{7,}$")
        val matches = phoneREx.matches(origin)
        // Send if the regex for at least a 7-digit number does not match
        return !matches
    }
}
