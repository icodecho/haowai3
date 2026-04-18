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

# BouncyCastle for Huawei Secure SDK
-dontwarn org.bouncycastle.**
-keep class org.bouncycastle.** { *; }

# Huawei Secure Android Common
-dontwarn com.huawei.secure.android.**
-keep class com.huawei.secure.android.** { *; }

# AGC
-dontwarn com.huawei.agconnect.**
-keep class com.huawei.agconnect.** { *; }
-keep class com.huawei.hms.aaid.** { *; }
