package evilcode.notification.hwpush.service

import android.content.Intent
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import evilcode.notification.hwpush.database.HaoWaiDatabase
import evilcode.notification.hwpush.model.PushMessage
import evilcode.notification.hwpush.util.LogManager
import evilcode.notification.hwpush.util.NotificationHelper
import evilcode.notification.hwpush.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

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
        LogManager.i("HwPushService", "onMessageReceived called") //只有透传消息会触发
        if (message == null) {
            LogManager.e("HwPushService", "Received message is null")
            return
        }

        val notification = message.notification
        val data = message.data
        
        var title: String? = null
        var body: String? = null
        var dataStr: String? = null
        var messageType = "notification" // 默认通知消息
        //遍历notification
        notification?.let {
            LogManager.i("HwPushService", "遍历notification:")
            LogManager.i("HwPushService", "  title: ${it.title}")
            LogManager.i("HwPushService", "  body: ${it.body}")
            LogManager.i("HwPushService", "  icon: ${it.icon}")
            LogManager.i("HwPushService", "  color: ${it.color}")
            LogManager.i("HwPushService", "  sound: ${it.sound}")
            LogManager.i("HwPushService", "  tag: ${it.tag}")
            LogManager.i("HwPushService", "  channelId: ${it.channelId}")
            LogManager.i("HwPushService", "  imageUrl: ${it.imageUrl}")
            LogManager.i("HwPushService", "  link: ${it.link}")
            LogManager.i("HwPushService", "  notifyId: ${it.notifyId}")
            LogManager.i("HwPushService", "  when: ${it.`when`}")
            LogManager.i("HwPushService", "  lightSettings: ${it.lightSettings?.contentToString()}")
            LogManager.i("HwPushService", "  vibrateConfig: ${it.vibrateConfig?.contentToString()}")
            LogManager.i("HwPushService", "  visibility: ${it.visibility}")
            LogManager.i("HwPushService", "  importance: ${it.importance}")
            LogManager.i("HwPushService", "  ticker: ${it.ticker}")
            LogManager.i("HwPushService", "  vibrateTimings: ${it.vibrateTimings?.contentToString()}")
            LogManager.i("HwPushService", "  localOnly: ${it.isLocalOnly}")
            LogManager.i("HwPushService", "  defaultSound: ${it.isDefaultSound}")
            LogManager.i("HwPushService", "  defaultVibrate: ${it.isDefaultVibrate}")
            LogManager.i("HwPushService", "  defaultLight: ${it.isDefaultLight}")
        }
        //遍历data
        data?.let {
            LogManager.i("HwPushService", "遍历data:")
            LogManager.i("HwPushService", "  data: $it")
        }
        
        if (notification.title != null) {
            title = notification.title
            body = notification.body
            dataStr = if (!data.isNullOrEmpty()) data else null
            if (!data.isNullOrEmpty()) {
                messageType = "notification_with_data"
                LogManager.i("HwPushService", "通知消息(含透传数据) - Title: $title, Body: $body, Data: $dataStr") //还是透传消息，如果自定义键值键名有title且data不为空时，则匹配
            } else {
                messageType = "notification"
                LogManager.i("HwPushService", "通知消息 - Title: $title, Body: $body") //还是透传消息
            }
        } else if (!data.isNullOrEmpty()) {
            dataStr = data
            messageType = "data"
            try {
                val json = JSONObject(data)
                title = json.optString("title", null)
                body = json.optString("body", null)
                if (title.isNullOrEmpty() && body.isNullOrEmpty()) {
                    title = getString(evilcode.notification.hwpush.R.string.app_name)
                    body = data
                    LogManager.i("HwPushService", "透传消息(无title/body) - 使用应用名称, Body: $body")
                } else {
                    LogManager.i("HwPushService", "透传消息(有title/body) - Title: $title, Body: $body")
                }
            } catch (e: Exception) {
                title = getString(evilcode.notification.hwpush.R.string.app_name)
                body = data
                LogManager.i("HwPushService", "透传消息(非JSON) - 使用应用名称, Body: $body")
            }
        } else {
            LogManager.w("HwPushService", "Message has no notification and no data")
        }

        val pushMessage = PushMessage(
            messageId = message.messageId,
            title = title,
            body = body,
            data = dataStr,
            token = message.token,
            collapseKey = message.collapseKey,
            sentTime = message.sentTime,
            receivedTime = System.currentTimeMillis()
        )

        CoroutineScope(Dispatchers.IO).launch {
            val database = HaoWaiDatabase.getInstance(this@HwPushService)
            database.pushMessageDao().insertMessage(pushMessage)
            LogManager.i("HwPushService", "Message saved to database")
        }

        if (messageType == "data" && !title.isNullOrEmpty()) {
            LogManager.i("HwPushService", "Pass-through message received, showing notification")
            val notifyId = NotificationHelper.generateNotifyId()
            NotificationHelper.showNotification(
                this,
                notifyId,
                title,
                body ?: "",
                message.messageId
            )
        }

        val broadcastIntent = Intent(ACTION_MESSAGE_RECEIVED)
        broadcastIntent.putExtra(EXTRA_MESSAGE_TITLE, title ?: "")
        broadcastIntent.putExtra(EXTRA_MESSAGE_BODY, body ?: "")
        broadcastIntent.putExtra(EXTRA_MESSAGE_DATA, dataStr ?: "")
        sendBroadcast(broadcastIntent)
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
        const val EXTRA_MESSAGE_TITLE = "extra_message_title"
        const val EXTRA_MESSAGE_BODY = "extra_message_body"
        const val EXTRA_MESSAGE_DATA = "extra_message_data"
    }
}
