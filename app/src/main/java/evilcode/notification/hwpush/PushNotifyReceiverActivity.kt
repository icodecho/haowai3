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

class PushNotifyReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogManager.i("PushNotifyReceiverActivity", "Activity started, received intent")
        
        handleNotificationIntent(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        LogManager.i("PushNotifyReceiverActivity", "onNewIntent called")
        handleNotificationIntent(intent)
        finish()
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) {
            LogManager.w("PushNotifyReceiverActivity", "Intent is null")
            return
        }

        val title = intent.getStringExtra("title")
            ?: intent.getStringExtra("push_title")
            ?: intent.getStringExtra("msg_title")
        
        val body = intent.getStringExtra("body")
            ?: intent.getStringExtra("push_body")
            ?: intent.getStringExtra("msg_body")
        
        val data = intent.getStringExtra("data")
        val msgId = intent.getStringExtra("msgId")

        LogManager.i("PushNotifyReceiverActivity", "Title: $title, Body: $body, Data: $data")

        if (!title.isNullOrEmpty() || !body.isNullOrEmpty()) {
            val pushMessage = PushMessage(
                messageId = msgId ?: System.currentTimeMillis().toString(),
                title = title,
                body = body,
                data = data,
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
