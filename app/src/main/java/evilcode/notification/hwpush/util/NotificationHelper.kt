package evilcode.notification.hwpush.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import evilcode.notification.hwpush.MainActivity
import evilcode.notification.hwpush.R

object NotificationHelper {
    const val CHANNEL_ID = "haowai_push_channel"
    private const val CHANNEL_NAME = "号外号外推送通知"
    private const val CHANNEL_DESC = "接收号外号外推送消息通知"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.parseColor("#BB86FC")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50, 100)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            LogManager.i("NotificationHelper", "Notification channel created: $CHANNEL_ID")
        }
    }

    fun showNotification(
        context: Context,
        notifyId: Int,
        title: String,
        body: String,
        messageId: String?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("message_id", messageId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notifyId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notifyId, notification)
        LogManager.i("NotificationHelper", "Notification shown: id=$notifyId, title=$title")
    }

    fun generateNotifyId(): Int {
        return (System.currentTimeMillis() % 100000).toInt()
    }
}
