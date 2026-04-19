# 号外号外 - 华为推送服务客户端

一款基于华为推送服务（Push Kit）开发的 Android 客户端应用，采用纯黑深色 Material Design 设计风格。

## 功能特性

### 基础推送能力
- 获取和注销华为推送 Token
- 接收推送消息数据（通知栏消息和透传消息）
- 支持显示/隐藏 Token，复制 Token 到剪贴板
- 支持三种消息类型自动识别：通知消息、通知消息(含透传数据)、透传消息

### 消息记录管理
- 查看推送消息记录，包括消息类型标签、消息标题、消息内容、透传数据、接收时间、消息ID等信息
- 消息列表区分显示消息类型标签（通知消息/透传消息），采用不同颜色标识
- 支持自定义选择复制字段（标题、内容、数据、时间、消息ID），可全选或部分选择
- 支持复制消息记录内容到剪贴板
- 支持删除选定的消息记录
- 支持清空所有消息记录

### 运行日志
- 记录所有应用运行日志
- 支持查看和清空日志
- 日志级别分类显示（INFO/WARN/ERROR），不同级别使用不同颜色标识

### 推送消息本地通知
- 透传消息接收后自动弹出系统本地通知
- 点击本地通知可打开应用主界面

## 技术栈

- **开发语言**: Kotlin
- **最低支持版本**: Android 7.0 (API 24)
- **目标版本**: Android 14 (API 34)
- **UI框架**: Material Design 3 (纯黑深色主题)
- **数据存储**: Room Database
- **异步处理**: Kotlin Coroutines
- **华为SDK**: HMS Push SDK 6.12.0.300
- **本地通知**: NotificationCompat

## 项目结构

```
haowai3/
├── app/
│   ├── src/main/
│   │   ├── java/evilcode/notification/hwpush/
│   │   │   ├── adapter/                    # RecyclerView适配器
│   │   │   │   ├── MessageAdapter.kt       # 消息列表适配器
│   │   │   │   └── LogAdapter.kt           # 日志列表适配器
│   │   │   ├── database/                   # Room数据库
│   │   │   │   ├── HaoWaiDatabase.kt       # 数据库实例
│   │   │   │   ├── PushMessageDao.kt       # 消息数据访问对象
│   │   │   │   └── AppLogDao.kt            # 日志数据访问对象
│   │   │   ├── model/                      # 数据模型
│   │   │   │   ├── PushMessage.kt          # 推送消息实体
│   │   │   │   └── AppLog.kt               # 日志实体
│   │   │   ├── service/                    # 服务类
│   │   │   │   └── HwPushService.kt        # 华为推送服务
│   │   │   ├── util/                       # 工具类
│   │   │   │   ├── LogManager.kt           # 日志管理器
│   │   │   │   ├── TokenManager.kt         # Token管理器
│   │   │   │   └── NotificationHelper.kt   # 本地通知辅助类
│   │   │   ├── MainActivity.kt             # 主界面
│   │   │   ├── MessageListActivity.kt      # 消息列表
│   │   │   ├── LogListActivity.kt          # 日志列表
│   │   │   ├── PushNotifyReceiverActivity.kt # 点击通知接收Activity
│   │   │   └── HaoWaiApplication.kt        # Application类
│   │   ├── res/                            # 资源文件
│   │   │   ├── layout/                     # 布局文件
│   │   │   │   ├── activity_main.xml       # 主界面布局
│   │   │   │   ├── activity_message_list.xml # 消息列表布局
│   │   │   │   ├── activity_log_list.xml   # 日志列表布局
│   │   │   │   ├── item_message.xml        # 消息列表项布局
│   │   │   │   ├── item_log.xml            # 日志列表项布局
│   │   │   │   └── dialog_copy_options.xml # 复制选项对话框
│   │   │   ├── drawable/                   # 可绘制资源
│   │   │   │   ├── bg_message_type.xml     # 通知消息类型标签背景
│   │   │   │   ├── bg_message_type_data.xml # 透传消息类型标签背景
│   │   │   │   └── ic_notification.xml     # 通知图标
│   │   │   └── values/                     # 值资源
│   │   │       ├── colors.xml              # 颜色定义
│   │   │       ├── strings.xml             # 字符串定义
│   │   │       └── themes.xml              # 主题定义
│   │   └── AndroidManifest.xml             # 应用配置
│   ├── build.gradle                        # 模块构建配置
│   ├── proguard-rules.pro                  # 混淆配置
│   └── agconnect-services.json             # AGC配置文件
├── doc/
│   └── evilcode.jks                        # 签名证书
├── .github/workflows/
│   └── build-apk.yml                       # GitHub Actions CI/CD
├── build.gradle                            # 项目构建配置
├── settings.gradle                         # 项目设置
└── gradle.properties                       # Gradle属性
```

## 消息类型说明

应用自动识别并标识三种推送消息类型：

| 类型 | 标识 | 说明 |
|------|------|------|
| 通知消息 | 绿色标签 | 仅包含通知栏标题和内容的消息 |
| 通知消息(含透传数据) | 绿色标签 | 同时包含通知栏内容和透传数据的消息 |
| 透传消息 | 紫色标签 | 仅包含透传数据的消息，数据中包含 title/body 时自动解析 |

### 透传消息处理逻辑
1. 数据为 JSON 格式时，自动解析 `title` 和 `body` 字段
2. 若 JSON 中无 `title`/`body`，则使用应用名称作为标题，原始数据作为内容
3. 数据为非 JSON 格式时，使用应用名称作为标题，原始数据作为内容
4. 透传消息接收后会弹出系统本地通知提醒用户

### 点击通知消息处理逻辑（PushNotifyReceiverActivity）
当用户点击通知栏消息时，应用会通过 `PushNotifyReceiverActivity` 接收并保存消息记录：
1. 首先尝试从 `data` 字段解析 JSON 获取标题和内容
2. 其次尝试从多种常见字段名直接获取标题和内容（如 `title`、`push_title`、`body` 等）
3. 最后遍历所有 extras 字段，查找 JSON 格式数据并解析
4. 消息保存后自动打开应用主界面

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
        // 自动识别通知消息和透传消息
        // 通知消息：notification.title 不为空
        // 透传消息：data 不为空且 notification.title 为空
        // 通知消息含透传数据：notification.title 不为空且 data 不为空
    }
    
    override fun onNewToken(token: String?) {
        // 处理Token更新
    }
}
```

### 本地通知
```kotlin
// 透传消息接收后弹出本地通知
NotificationHelper.showNotification(context, notifyId, title, body, messageId)
```

### 数据存储
使用 Room 数据库持久化存储推送消息和应用日志:
- `push_messages` 表: 存储推送消息（包含 messageId、title、body、data、token、collapseKey、sentTime、receivedTime 字段）
- `app_logs` 表: 存储应用日志（包含 level、tag、message、timestamp 字段）

## 注意事项

1. 确保在华为 AppGallery Connect 中已开通推送服务
2. agconnect-services.json 文件必须放置在 app 模块根目录
3. 签名证书指纹需在 AGC 中配置
4. 测试建议使用华为手机（EMUI 10及以上）以获得最佳推送体验
5. 非华为手机需安装 HMS Core 才能使用推送功能

## 推送端消息体格式

### 方案一：通知栏消息 + click_action（点击通知后保存）

通知栏消息由系统NC展示，用户点击后触发 PushNotifyReceiverActivity 保存记录：

```json
{
    "message": {
        "notification": {
            "title": "消息标题",
            "body": "消息内容"
        },
        "android": {
            "notification": {
                "click_action": {
                    "type": 1,
                    "action": "evilcode.notification.hwpush.ACTION_OPEN_MESSAGE"
                }
            }
        },
        "token": ["用户PushToken"]
    }
}
```

### 方案二：透传消息（实时自动保存）

使用透传消息，onMessageReceived实时接收并保存。透传消息的 data 字段如果包含 title 和 body，会自动解析并显示：

```json
{
    "message": {
        "data": "{\"title\":\"消息标题\",\"body\":\"消息内容\"}",
        "android": {},
        "token": ["用户PushToken"]
    }
}
```

### 方案三：通知栏消息 + 透传数据

同时下发通知栏消息和透传数据，onMessageReceived 实时接收并保存：

```json
{
    "message": {
        "notification": {
            "title": "消息标题",
            "body": "消息内容"
        },
        "data": "{\"custom_key\":\"custom_value\"}",
        "android": {},
        "token": ["用户PushToken"]
    }
}
```

### 方案四：前台通知处理（应用在前台时自动保存）

设置 foreground_show=false，应用在前台时走 onMessageReceived：

```json
{
    "message": {
        "notification": {
            "title": "消息标题",
            "body": "消息内容"
        },
        "android": {
            "notification": {
                "foreground_show": false,
                "click_action": {
                    "type": 3
                }
            }
        },
        "token": ["用户PushToken"]
    }
}
```

## 许可证

本项目仅供参考学习使用。
