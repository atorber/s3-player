# Aether S3 Audio Player

专为技术极客打造的高精度 S3 音频流播放器与云存储 Bucket 检查工具。

## 功能特性
- **S3 音频流播放**：直接从兼容 S3 的云存储中流式播放音频文件。
- **Bucket 检查**：检查云端 Bucket 存储桶并浏览探索其中的内容。
- **现代 UI**：基于 Jetpack Compose 构建，提供原生、流畅且响应式的 Android 交互体验。

## 技术栈
- **Android & Kotlin**
- **Jetpack Compose**：现代声明式 UI 工具包。
- **Coroutines & Flow**：用于异步编程与响应式数据流。
- **Retrofit & Moshi**：用于网络请求与 JSON 解析。
- **Room**：用于本地数据库管理。
- **Coil**：用于图片加载。

## 安装与运行指南

1. **配置环境变量**：
   复制项目根目录的 `.env.example` 文件并重命名为 `.env`，然后配置必要的 API 密钥。
   ```bash
   cp .env.example .env
   ```
   > **注意**：如果您需要使用 Gemini AI API，请务必在 `.env` 文件中取消注释并填入您的 `GEMINI_API_KEY`。

2. **编译与运行**：
   - **使用 Android Studio (推荐)**：在 Android Studio 中打开本项目，等待 Gradle 同步完成，选择您的调试设备（例如已连接的物理真机），然后点击 "Run" (运行) 按钮。
   - **使用命令行**：
     首先确保您的环境中已安装 Gradle，然后生成 Gradle Wrapper 脚本并运行安装：
     ```bash
     gradle wrapper
     ./gradlew installDebug
     ```

## 持续集成 (GitHub Actions)
本项目已配置 GitHub Actions 工作流，用于自动构建 Release 正式版 APK 并发布。
- **触发机制**：向仓库推送以 `v` 开头的 Tag（例如 `v1.0.0`），或者在 Actions 页面手动触发。
- **密钥配置**：请在仓库的 **Settings -> Secrets and variables -> Actions** 中配置好 `KEYSTORE_BASE64`、`STORE_PASSWORD` 和 `KEY_PASSWORD`，才能成功完成 APK 签名。
