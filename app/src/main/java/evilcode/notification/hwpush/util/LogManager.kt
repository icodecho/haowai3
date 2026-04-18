package evilcode.notification.hwpush.util

import android.content.Context
import android.util.Log
import evilcode.notification.hwpush.database.HaoWaiDatabase
import evilcode.notification.hwpush.model.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object LogManager {
    private var database: HaoWaiDatabase? = null
    private var isInitialized = false

    fun init(context: Context) {
        try {
            database = HaoWaiDatabase.getInstance(context)
            isInitialized = true
            Log.i("LogManager", "LogManager initialized successfully")
        } catch (e: Exception) {
            isInitialized = false
            Log.e("LogManager", "Failed to initialize LogManager: ${e.message}")
        }
    }

    fun i(tag: String, message: String) {
        Log.i("HaoWai_$tag", message)
        log("INFO", tag, message)
    }

    fun e(tag: String, message: String) {
        Log.e("HaoWai_$tag", message)
        log("ERROR", tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w("HaoWai_$tag", message)
        log("WARN", tag, message)
    }

    fun d(tag: String, message: String) {
        Log.d("HaoWai_$tag", message)
        log("DEBUG", tag, message)
    }

    private fun log(level: String, tag: String, message: String) {
        if (!isInitialized || database == null) {
            return
        }
        try {
            val appLog = AppLog(level = level, tag = tag, message = message)
            GlobalScope.launch(Dispatchers.IO) {
                database?.appLogDao()?.insertLog(appLog)
            }
        } catch (e: Exception) {
            Log.e("LogManager", "Failed to save log: ${e.message}")
        }
    }
}
