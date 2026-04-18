package evilcode.notification.hwpush

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import evilcode.notification.hwpush.database.HaoWaiDatabase
import evilcode.notification.hwpush.model.PushMessage
import evilcode.notification.hwpush.util.LogManager
import evilcode.notification.hwpush.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class PushNotifyReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logAllIntentExtras(intent)
        handleNotificationIntent(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        logAllIntentExtras(intent)
        handleNotificationIntent(intent)
        finish()
    }

    private fun logAllIntentExtras(intent: Intent?) {
        if (intent == null) {
            LogManager.w("PushNotifyReceiverActivity", "Intent is null")
            return
        }
        LogManager.i("PushNotifyReceiverActivity", "=== Intent Info ===")
        LogManager.i("PushNotifyReceiverActivity", "Action: ${intent.action}")
        LogManager.i("PushNotifyReceiverActivity", "Type: ${intent.type}")
        LogManager.i("PushNotifyReceiverActivity", "Data URI: ${intent.data}")
        LogManager.i("PushNotifyReceiverActivity", "Scheme: ${intent.scheme}")

        val extras = intent.extras
        if (extras != null) {
            LogManager.i("PushNotifyReceiverActivity", "Extras count: ${extras.keySet().size}")
            for (key in extras.keySet()) {
                val value = extras.get(key)
                LogManager.i("PushNotifyReceiverActivity", "Extra [$key] = $value")
                if($key == "analysisExt") {

                }
            }
        } else {
            LogManager.w("PushNotifyReceiverActivity", "Extras bundle is null")
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) {
            LogManager.w("PushNotifyReceiverActivity", "Intent is null, abort")
            return
        }

        var title: String? = null
        var body: String? = null
        var dataStr: String? = null
        var msgId: String? = null

        val extras = intent.extras
        if (extras != null) {
            dataStr = extras.getString("data")

            msgId = extras.getString("_push_msgid")
                ?: extras.getString("msgId")
                ?: extras.getString("msg_id")
                ?: extras.getString("_hw_msg_id")
                ?: extras.getString("HW_MSG_ID")

            if (!dataStr.isNullOrEmpty()) {
                try {
                    val json = JSONObject(dataStr)
                    title = json.optString("title", null)
                        ?: json.optString("msg", null)
                    body = json.optString("body", null)
                        ?: json.optString("content", null)
                        ?: json.optString("message", null)
                    LogManager.i("PushNotifyReceiverActivity", "Parsed data JSON - title: $title, body: $body")
                } catch (e: Exception) {
                    LogManager.e("PushNotifyReceiverActivity", "Failed to parse data JSON: ${e.message}")
                }
            }

            if (title.isNullOrEmpty() && body.isNullOrEmpty()) {
                title = extras.getString("title")
                    ?: extras.getString("push_title")
                    ?: extras.getString("msg_title")
                    ?: extras.getString("HW_NOTIFICATION_TITLE")
                    ?: extras.getString("hw_noti_title")

                body = extras.getString("body")
                    ?: extras.getString("push_body")
                    ?: extras.getString("msg_body")
                    ?: extras.getString("HW_NOTIFICATION_BODY")
                    ?: extras.getString("hw_noti_body")

                msgId = extras.getString("_push_msgid")
                    ?: extras.getString("msgId")
                    ?: extras.getString("msg_id")
                    ?: extras.getString("_hw_msg_id")
                    ?: extras.getString("HW_MSG_ID")

                if (!title.isNullOrEmpty() || !body.isNullOrEmpty()) {
                    LogManager.i("PushNotifyReceiverActivity", "Got title/body directly from extras")
                }
            }

            if (title.isNullOrEmpty() && body.isNullOrEmpty() && dataStr.isNullOrEmpty()) {
                for (key in extras.keySet()) {
                    val value = extras.getString(key)
                    if (!value.isNullOrEmpty() && (value.startsWith("{") || value.startsWith("["))) {
                        try {
                            val json = JSONObject(value)
                            title = json.optString("title", json.optString("msg", null))
                            body = json.optString("body", json.optString("content", json.optString("message", null)))
                            if (!title.isNullOrEmpty() || !body.isNullOrEmpty()) {
                                dataStr = value
                                LogManager.i("PushNotifyReceiverActivity", "Found JSON in extra [$key]")
                            }
                        } catch (e: Exception) {
                            // ignore
                        }
                        break
                    }
                }
            }
        }

        LogManager.i("PushNotifyReceiverActivity", "Final result - Title: $title, Body: $body, Data: $dataStr, msgId: $msgId")

        if (!title.isNullOrEmpty() || !body.isNullOrEmpty()) {
            val pushMessage = PushMessage(
                messageId = msgId ?: System.currentTimeMillis().toString(),
                title = title ?: "通知消息",
                body = body ?: "",
                data = dataStr,
                token = TokenManager.getToken(),
                collapseKey = null,
                sentTime = System.currentTimeMillis(),
                receivedTime = System.currentTimeMillis()
            )

            CoroutineScope(Dispatchers.IO).launch {
                val database = HaoWaiDatabase.getInstance(this@PushNotifyReceiverActivity)
                database.pushMessageDao().insertMessage(pushMessage)
                LogManager.i("PushNotifyReceiverActivity", "Message saved successfully: ${pushMessage.title}")
            }
        } else {
            LogManager.w("PushNotifyReceiverActivity", "No message content found, cannot save")
        }

        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(mainIntent)
    }
}
