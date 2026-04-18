package evilcode.notification.hwpush

import android.app.Application
import evilcode.notification.hwpush.util.LogManager

class HaoWaiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LogManager.init(this)
        LogManager.i("HaoWaiApplication", "Application started")
    }
}
