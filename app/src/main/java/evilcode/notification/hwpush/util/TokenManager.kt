package evilcode.notification.hwpush.util

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "haowai_prefs"
    private const val KEY_TOKEN = "push_token"
    private const val KEY_TOKEN_TIME = "push_token_time"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putLong(KEY_TOKEN_TIME, System.currentTimeMillis())
            apply()
        }
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getTokenTime(): Long {
        return prefs.getLong(KEY_TOKEN_TIME, 0)
    }

    fun clearToken() {
        prefs.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_TOKEN_TIME)
            apply()
        }
    }
}
