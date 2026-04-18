package evilcode.notification.hwpush

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import evilcode.notification.hwpush.database.HaoWaiDatabase
import evilcode.notification.hwpush.model.PushMessage
import evilcode.notification.hwpush.util.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PushNotifyReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val title = intent.getStringExtra("title")
        val body = intent.getStringExtra("body")
        val msgId = intent.getStringExtra("msg_id")
        val data = intent.getStringExtra("data")

        LogManager.i("PushNotifyReceiver", "Received notification click - title: $title, body: $body, msgId: $msgId")

        if (!title.isNullOrEmpty() || !body.isNullOrEmpty()) {
            saveNotificationMessage(msgId, title, body, data)
        }

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(mainIntent)
        finish()
    }

    private fun saveNotificationMessage(msgId: String?, title: String?, body: String?, data: String?) {
        val database = HaoWaiDatabase.getInstance(this)
        val pushMessage = PushMessage(
            messageId = msgId,
            title = title ?: "(无标题)",
            body = body ?: "(无内容)",
            data = data,
            token = null,
            collapseKey = null,
            sentTime = System.currentTimeMillis(),
            receivedTime = System.currentTimeMillis()
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                database.pushMessageDao().insertMessage(pushMessage)
            }
            LogManager.i("PushNotifyReceiver", "Notification message saved")
            Toast.makeText(this@PushNotifyReceiverActivity, "消息已保存", Toast.LENGTH_SHORT).show()
        }
    }
}
