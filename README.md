# 号外号外 - 华为推送服务客户端

一款基于华为推送服务（Push Kit）开发的 Android 客户端应用，采用纯黑深色 Material Design 设计风格。

## 功能特性

### 基础推送能力
- 获取和注销华为推送 Token
- 接收推送消息数据（通知栏消息和透传消息）

### 消息记录管理
- 查看推送消息记录，包括消息内容、接收时间等信息
- 支持复制消息记录内容到剪贴板
- 支持删除选定的消息记录
- 支持清空所有消息记录

### 运行日志
- 记录所有应用运行日志
- 支持查看和清空日志
- 日志级别分类显示（INFO/WARN/ERROR）

## 技术栈

- **开发语言**: Kotlin
- **最低支持版本**: Android 7.0 (API 24)
- **目标版本**: Android 14 (API 34)
- **UI框架**: Material Design 3 (纯黑深色主题)
- **数据存储**: Room Database
- **异步处理**: Kotlin Coroutines
- **华为SDK**: HMS Push SDK 6.12.0.300

## 项目结构

```
haowai3/
├── app/
│   ├── src/main/
│   │   ├── java/evilcode/notification/hwpush/
│   │   │   ├── adapter/          # RecyclerView适配器
│   │   │   ├── database/         # Room数据库
│   │   │   ├── model/            # 数据模型
│   │   │   ├── service/          # 华为推送服务
│   │   │   ├── util/             # 工具类
│   │   │   ├── MainActivity.kt   # 主界面
│   │   │   ├── MessageListActivity.kt  # 消息列表
│   │   │   ├── LogListActivity.kt      # 日志列表
│   │   │   └── HaoWaiApplication.kt    # Application类
│   │   ├── res/                  # 资源文件
│   │   └── AndroidManifest.xml   # 应用配置
│   ├── build.gradle              # 模块构建配置
│   ├── proguard-rules.pro        # 混淆配置
│   └── agconnect-services.json   # AGC配置文件
├── doc/
│   └── evilcode.jks              # 签名证书
├── .github/workflows/
│   └── build-apk.yml             # GitHub Actions CI/CD
├── build.gradle                  # 项目构建配置
├── settings.gradle               # 项目设置
└── gradle.properties             # Gradle属性
```

## 编译说明

### 环境要求
- JDK 17 或以上
- Android Studio Hedgehog (2023.1.1) 或以上
- Android SDK 34
- Gradle 8.2

### 本地编译

1. 克隆项目到本地
2. 确保以下文件存在:
   - `app/agconnect-services.json` (从华为AGC下载)
   - `doc/evilcode.jks` (签名证书)
3. 使用 Android Studio 打开项目
4. 同步 Gradle 依赖
5. 点击 Build > Build Bundle(s) / APK(s) > Build APK(s)

或使用命令行:
```bash
./gradlew assembleDebug    # 编译Debug APK
./gradlew assembleRelease  # 编译Release APK
```

### GitHub Actions 自动化编译

项目已配置 GitHub Actions 工作流，推送代码到 main/master 分支或手动触发 workflow 时会自动编译 APK。

编译产物:
- Release APK 文件
- ProGuard mapping 文件 (用于反混淆)

## 签名配置

应用使用以下签名信息:
- 签名文件: `doc/evilcode.jks`
- keyAlias: `evilcode`
- keyPassword: `@evilcode1024`
- storePassword: `@evilcode1024`

## 包名信息

- 应用包名: `evilcode.notification.hwpush`
- 应用名称: 号外号外

## 开发指南参考

本项目开发参考以下华为官方文档:
- 华为推送服务开发指南 (D:\01app\code\trae2\doc\华为推送服务文档.md)
- HMS Push Client Demo 示例代码 (D:\01app\code\trae2\doc\hms-push-clientdemo)

## 主要功能实现

### Token 管理
```kotlin
// 获取Token
HmsInstanceId.getInstance(context).getToken(appId, "HCM")

// 注销Token
HmsInstanceId.getInstance(context).deleteToken(appId, "HCM")
```

### 消息接收
```kotlin
class HwPushService : HmsMessageService() {
    override fun onMessageReceived(message: RemoteMessage?) {
        // 处理接收到的消息
    }
    
    override fun onNewToken(token: String?) {
        // 处理Token更新
    }
}
```

### 数据存储
使用 Room 数据库持久化存储推送消息和应用日志:
- `push_messages` 表: 存储推送消息
- `app_logs` 表: 存储应用日志

## 注意事项

1. 确保在华为 AppGallery Connect 中已开通推送服务
2. agconnect-services.json 文件必须放置在 app 模块根目录
3. 签名证书指纹需在 AGC 中配置
4. 测试建议使用华为手机（EMUI 10及以上）以获得最佳推送体验
5. 非华为手机需安装 HMS Core 才能使用推送功能

## 许可证

本项目仅供参考学习使用。
