package evilcode.notification.hwpush.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import evilcode.notification.hwpush.database.HaoWaiDatabase
import evilcode.notification.hwpush.model.PushMessage
import evilcode.notification.hwpush.util.LogManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HaoWaiNotificationListener : NotificationListenerService() {

    override fun onCreate() {
        super.onCreate()
        LogManager.i("HaoWaiNotificationListener", "Service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogManager.i("HaoWaiNotificationListener", "Service destroyed")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        LogManager.i("HaoWaiNotificationListener", "Listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        LogManager.w("HaoWaiNotificationListener", "Listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        if (sbn.packageName == packageName) return

        val extras = sbn.notification.extras
        val title = extras?.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
        val text = extras?.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()

        if (title.isNullOrEmpty() && text.isNullOrEmpty()) return

        LogManager.i("NotificationListener", "Captured notification - Package: ${sbn.packageName}, Title: $title, Text: $text")

        val pushMessage = PushMessage(
            messageId = sbn.key,
            title = title,
            body = text,
            data = null,
            token = null,
            collapseKey = sbn.tag,
            sentTime = sbn.notification.`when`,
            receivedTime = System.currentTimeMillis()
        )

        CoroutineScope(Dispatchers.IO).launch {
            val database = HaoWaiDatabase.getInstance(this@HaoWaiNotificationListener)
            database.pushMessageDao().insertMessage(pushMessage)
            LogManager.i("NotificationListener", "Notification saved to database")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return
        LogManager.i("HaoWaiNotificationListener", "Notification removed - Package: ${sbn.packageName}")
    }
}
