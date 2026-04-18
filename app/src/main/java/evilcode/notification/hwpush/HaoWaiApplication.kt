package evilcode.notification.hwpush

import android.app.Application
import evilcode.notification.hwpush.util.LogManager
import evilcode.notification.hwpush.util.NotificationHelper
import evilcode.notification.hwpush.util.TokenManager

class HaoWaiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LogManager.init(this)
        TokenManager.init(this)
        NotificationHelper.createNotificationChannel(this)
        LogManager.i("HaoWaiApplication", "Application started")
    }
}
