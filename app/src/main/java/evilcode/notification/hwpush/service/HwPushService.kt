package evilcode.notification.hwpush.service

import android.content.Intent
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import evilcode.notification.hwpush.HaoWaiApplication
import evilcode.notification.hwpush.database.HaoWaiDatabase
import evilcode.notification.hwpush.model.PushMessage
import evilcode.notification.hwpush.util.LogManager
import evilcode.notification.hwpush.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HwPushService : HmsMessageService() {

    override fun onNewToken(token: String?) {
        LogManager.i("HwPushService", "onNewToken called")
        if (token.isNullOrEmpty()) {
            LogManager.w("HwPushService", "Token is null or empty")
            return
        }
        TokenManager.saveToken(token)
        LogManager.i("HwPushService", "Token saved: ${token.take(10)}...")
        
        val intent = Intent(ACTION_TOKEN_UPDATE)
        intent.putExtra(EXTRA_TOKEN, token)
        sendBroadcast(intent)
    }

    override fun onMessageReceived(message: RemoteMessage?) {
        LogManager.i("HwPushService", "onMessageReceived called")
        if (message == null) {
            LogManager.e("HwPushService", "Received message is null")
            return
        }

        val notification = message.notification
        val title = notification?.title
        val body = notification?.body
        val data = message.data
        
        val messageType = if (notification != null) {
            "通知消息"
        } else {
            "透传消息"
        }
        
        LogManager.i("HwPushService", "Message type: $messageType, Title: $title, Body: $body, Data: $data")

        val pushMessage = PushMessage(
            messageId = message.messageId,
            messageType = messageType,
            title = title,
            body = body,
            data = data,
            token = message.token,
            collapseKey = message.collapseKey,
            sentTime = message.sentTime,
            receivedTime = System.currentTimeMillis()
        )

        val database = HaoWaiDatabase.getInstance(this)
        CoroutineScope(Dispatchers.IO).launch {
            database.pushMessageDao().insertMessage(pushMessage)
            LogManager.i("HwPushService", "Message saved to database, type: $messageType")
        }

        val intent = Intent(ACTION_MESSAGE_RECEIVED)
        intent.putExtra(EXTRA_MESSAGE_TYPE, messageType)
        intent.putExtra(EXTRA_MESSAGE_TITLE, title ?: "")
        intent.putExtra(EXTRA_MESSAGE_BODY, body ?: "")
        intent.putExtra(EXTRA_MESSAGE_DATA, data ?: "")
        sendBroadcast(intent)
    }

    override fun onMessageSent(msgId: String?) {
        LogManager.i("HwPushService", "onMessageSent called, Message id: $msgId")
    }

    override fun onSendError(msgId: String?, exception: Exception?) {
        LogManager.e("HwPushService", "onSendError called, message id: $msgId, error: ${exception?.message}")
    }

    companion object {
        const val ACTION_TOKEN_UPDATE = "evilcode.notification.hwpush.ACTION_TOKEN_UPDATE"
        const val ACTION_MESSAGE_RECEIVED = "evilcode.notification.hwpush.ACTION_MESSAGE_RECEIVED"
        const val EXTRA_TOKEN = "extra_token"
        const val EXTRA_MESSAGE_TYPE = "extra_message_type"
        const val EXTRA_MESSAGE_TITLE = "extra_message_title"
        const val EXTRA_MESSAGE_BODY = "extra_message_body"
        const val EXTRA_MESSAGE_DATA = "extra_message_data"
    }
}
