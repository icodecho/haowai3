package evilcode.notification.hwpush.util

import android.content.Context
import evilcode.notification.hwpush.database.HaoWaiDatabase
import evilcode.notification.hwpush.model.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object LogManager {
    private var database: HaoWaiDatabase? = null

    fun init(context: Context) {
        database = HaoWaiDatabase.getInstance(context)
    }

    fun i(tag: String, message: String) {
        log("INFO", tag, message)
        android.util.Log.i("HaoWai_$tag", message)
    }

    fun e(tag: String, message: String) {
        log("ERROR", tag, message)
        android.util.Log.e("HaoWai_$tag", message)
    }

    fun w(tag: String, message: String) {
        log("WARN", tag, message)
        android.util.Log.w("HaoWai_$tag", message)
    }

    fun d(tag: String, message: String) {
        log("DEBUG", tag, message)
        android.util.Log.d("HaoWai_$tag", message)
    }

    private fun log(level: String, tag: String, message: String) {
        val db = database ?: return
        val appLog = AppLog(level = level, tag = tag, message = message)
        GlobalScope.launch(Dispatchers.IO) {
            db.appLogDao().insertLog(appLog)
        }
    }
}
