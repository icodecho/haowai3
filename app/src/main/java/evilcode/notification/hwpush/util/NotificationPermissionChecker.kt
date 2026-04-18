package evilcode.notification.hwpush.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils

object NotificationPermissionChecker {

    fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val componentName = android.content.ComponentName.unflattenFromString(name)
                if (componentName != null) {
                    if (TextUtils.equals(packageName, componentName.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * 检查并引导用户开启通知监听权限
     * 如果已开启返回true，否则跳转设置页并返回false
     */
    fun checkAndGuideNotificationPermission(context: Context): Boolean {
        return if (isNotificationListenerEnabled(context)) {
            true
        } else {
            openNotificationListenerSettings(context)
            false
        }
    }
}
