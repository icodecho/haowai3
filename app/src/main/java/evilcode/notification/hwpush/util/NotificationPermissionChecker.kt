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
                        LogManager.i("NotificationPermissionChecker", "Notification listener is enabled for $packageName")
                        return true
                    }
                }
            }
        }
        LogManager.i("NotificationPermissionChecker", "Notification listener is NOT enabled for $packageName")
        return false
    }

    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        LogManager.i("NotificationPermissionChecker", "Opened notification listener settings")
    }

    /**
     * 检查并引导用户开启通知监听权限
     * 如果已开启返回true，否则跳转设置页并返回false
     */
    fun checkAndGuideNotificationPermission(context: Context): Boolean {
        LogManager.i("NotificationPermissionChecker", "Checking notification permission")
        return if (isNotificationListenerEnabled(context)) {
            LogManager.i("NotificationPermissionChecker", "Permission already granted")
            true
        } else {
            LogManager.w("NotificationPermissionChecker", "Permission not granted, opening settings")
            openNotificationListenerSettings(context)
            false
        }
    }
}
