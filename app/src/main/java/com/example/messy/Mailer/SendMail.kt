package com.example.messy.Mailer

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

const val GMAIL_SMTP_HOST = "smtp.gmail.com"
const val GMAIL_SMTP_TLS_PORT = "587"

class SendMail(private val username: String, private val password: String) {
    suspend fun sendMail (dest: String, subject: String, body: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val props = Properties().apply {
                    put("mail.smtp.host", GMAIL_SMTP_HOST)
                    put("mail.smtp.port", GMAIL_SMTP_TLS_PORT)
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                }
                val session = Session.getInstance(props, object: Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(username, password)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(username))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(dest))
                    this.subject = subject
                    setText(body)
                }

                Transport.send(message)
                true
            } catch (e: Exception) {
                Log.e("SendMail", "Failed to send email: ${e.message}")
                false
            }
        }
    }
}