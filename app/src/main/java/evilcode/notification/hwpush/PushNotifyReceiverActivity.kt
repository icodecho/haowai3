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
        LogManager.i("PushNotifyReceiverActivity", "Intent action: ${intent.action}")
        LogManager.i("PushNotifyReceiverActivity", "Intent data: ${intent.data}")
        
        val extras = intent.extras
        if (extras != null) {
            for (key in extras.keySet()) {
                val value = extras.get(key)
                LogManager.i("PushNotifyReceiverActivity", "Extra key: $key, value: $value")
            }
        } else {
            LogManager.w("PushNotifyReceiverActivity", "Intent extras is null")
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) {
            LogManager.w("PushNotifyReceiverActivity", "Intent is null")
            return
        }

        val extras = intent.extras
        var title: String? = null
        var body: String? = null
        var dataStr: String? = null
        var msgId: String? = null

        if (extras != null) {
            title = extras.getString("title")
                ?: extras.getString("push_title")
                ?: extras.getString("msg_title")
                ?: extras.getString("HW_NOTIFICATION_TITLE")
            
            body = extras.getString("body")
                ?: extras.getString("push_body")
                ?: extras.getString("msg_body")
                ?: extras.getString("HW_NOTIFICATION_BODY")
            
            dataStr = extras.getString("data")
                ?: extras.getString("msg_data")
            
            msgId = extras.getString("msgId")
                ?: extras.getString("msg_id")
                ?: extras.getString("HW_MSG_ID")

            if (dataStr.isNullOrEmpty() && title == null && body == null) {
                for (key in extras.keySet()) {
                    val value = extras.getString(key)
                    if (!value.isNullOrEmpty() && (value.startsWith("{") || value.startsWith("["))) {
                        try {
                            val json = JSONObject(value)
                            title = json.optString("title", json.optString("msg", null))
                            body = json.optString("body", json.optString("content", json.optString("message", null)))
                            if (title.isNullOrEmpty() && body.isNullOrEmpty()) {
                                dataStr = value
                                title = "通知消息"
                            }
                        } catch (e: Exception) {
                            // not json
                        }
                        break
                    }
                }
            }
        }

        LogManager.i("PushNotifyReceiverActivity", "Parsed - Title: $title, Body: $body, Data: $dataStr")

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
                LogManager.i("PushNotifyReceiverActivity", "Message saved via click_action")
            }
        } else {
            LogManager.w("PushNotifyReceiverActivity", "No message content found in intent")
        }

        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(mainIntent)
    }
}
