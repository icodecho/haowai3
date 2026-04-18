# Add project specific ProGuard rules here.
# Huawei Push SDK
-keep class com.huawei.hms.** {*;}
-dontwarn com.huawei.hms.**
-keep class com.hianalytics.android.** {*;}
-dontwarn com.hianalytics.android.**

# Keep RemoteMessage class
-keep class com.huawei.hms.push.RemoteMessage { *; }
-keep class com.huawei.hms.push.HmsMessageService { *; }
-keep class com.huawei.hms.push.HmsInstanceId { *; }
-keep class com.huawei.hms.push.HmsMessaging { *; }

# Keep data models
-keep class evilcode.notification.hwpush.model.** { *; }
