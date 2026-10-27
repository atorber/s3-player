<div align="center">

# 🎵 Aether S3 Audio Player

### 专为技术极客与音频发烧友打造的高保真云原生 S3 音频播放器

[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![S3 Compatible](https://img.shields.io/badge/Storage-S3%20Compatible-orange.svg)](https://aws.amazon.com/s3/)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)

*将任意兼容 S3 协议的对象存储（AWS S3、MinIO、阿里云 OSS、Cloudflare R2、浪潮云等）即刻变身您的私人专属高保真音乐库。*

[✨ 核心特性](#-核心特性) • [🚀 快速开始](#-快速开始) • [📱 产品功能与界面](#-产品功能与界面) • [⚙️ 高级功能](#-高级功能) • [📦 下载安装](#-下载安装)

---

</div>

## 🌟 产品定位

你是否拥有海量的无损音频资产，存放在各种私有云或对象存储中，却受限于没有一款专业好用的移动端播放器？

**Aether S3 Player** 专为云端音频发烧友、音频工程师、播客制作者与技术极客量身定制。它不仅能够直接通过 S3 SigV4 签名机制从云端高速流式传输无损音频，还内置了专业级母带控制台、动态波形渲染、实时遥测监控面板、AB循环与多段 EQ 均衡器。无需繁琐的自建流媒体服务端，直接连接你的云存储桶，随时随地畅享高保真纯净原音。

---

## ✨ 核心特性

### 1. 🌐 全兼容 S3 协议云存储直连
- **全厂商支持**：完美支持 AWS S3、MinIO、Cloudflare R2、阿里云 OSS、腾讯云 COS、浪潮云 OSS、Ceph 等任意遵循标准 S3 协议的对象存储。
- **安全免配置**：支持标准 S3 SigV4 鉴权认证（AK / SK / Session Token / Region / 自定义 Endpoint），同时支持公开免密存储桶快速挂载。
- **灵活寻址**：支持 Path-Style 与 Virtual-Hosted Style，自由切换 HTTP / HTTPS (TLS 加密)。

### 2. 🎧 母带级全格式无损流式播放
- **广泛格式原生解码**：全面支持 **FLAC、WAV、MP3、M4A、AAC、OGG、OPUS** 等主流与无损音频格式。
- **预签名无损直链**：在端侧生成高时效预签名直链，零中转、零重编码，保留 100% 原始采样率与动态细节。
- **智能缓存机制**：支持将高频聆听曲目一键缓存至本地，断网或离线环境下依旧从容畅听。

### 3. 🛡️ 类似主流音乐播放器的后台保活
- **后台与熄屏持续播放**：深度集成 Android `FOREGROUND_SERVICE_MEDIA_PLAYBACK` 前台保活服务与系统唤醒锁 (`WakeLock`)，退出应用界面、切换其他 App 或手机熄屏时，音乐持续流畅不中断。
- **原生 MediaSession 联动**：深度接入系统级媒体会话，锁屏界面展示完整媒体控制中心，完美支持蓝牙耳机、车载中控、智能手表的切歌/暂停/播放实体按键。
- **自然返回退出逻辑**：在播放状态下按系统返回键自动将任务置于后台 (`moveTaskToBack`)，防止意外误划退出。

### 4. 🎛️ 专业级控制台与调音系统
- **动态立体声峰值表 (Peak Meter)**：毫秒级双声道音频电平弹跳监测（-60 dB ~ 0 dB），直观呈现音频动态。
- **5 段硬件级 EQ 均衡器**：内置「原声平直」、「重低音增强 (Bass Boost)」、「Hi-Fi 母带」、「分轨清晰」、「遥测滤波」预设，支持各频段自定义增益微调。
- **高精度 A-B 区间循环 (A-B Repeat)**：支持任意时间段毫秒级精确打点循环，是乐器扒带、外语听力精听、音频剪辑校对的绝佳伴侣。
- **变速与微调引擎**：支持 0.5x ~ 2.0x 任意倍速播放、音高智能修正 (Pitch Correction) 与静音消除。

### 5. 📂 极客级云端层级文件探索 (S3 Browser)
- **多层级目录树交互**：支持严格逐级深入导航或全局递归穿透扫描，像浏览本地文件管理器一样探索数十万个云端对象。
- **快速定位当前曲目**：在播放任何歌曲时，一键直达其所在的云端多级子文件夹。
- **批量与插队管理**：支持目录级一键播放、批量勾选追加至播放列表、下一首优先插队 (UpNext)。
- **智能多维检索**：支持按名称、修改日期、文件大小、时长正反双向排序，支持格式快速过滤与全局即时模糊搜索。

### 6. 📡 实时遥测与云端自监控 (Telemetry & Auto-Enqueue)
- **S3 网络遥测**：实时监控连接延迟 (Latency)、吞吐量 (MB/s)、网络抖动 (Jitter)、TLS 加密套件与边缘节点信息。
- **新曲目静默轮询**：可开启子目录自动监控轮询（1~60秒自定义），远程云存储上传新音频后，自动发现并根据策略自动加入队列或自动播放。

### 7. 💾 升级无缝保留数据
- **Room 渐进式平滑迁移**：内置完备的数据库 Migration 策略，覆盖安装新版本永不丢失收藏列表、离线索引与历史数据。
- **偏好配置自动备份**：结合 Android 云备份与设备迁移规则，换机或升级免重复配置 Bucket 凭证。

---

## 📱 产品界面架构

Aether S3 采用现代赛博朋克极客风格与 Google Material Design 3 深度融合的设计语言：

| 模块 | 核心功能 |
| :--- | :--- |
| **主播放控制台 (Master Console)** | 黑胶旋转艺术、实时动态双声道跳灯、进度擦洗拉条、倍速/AB循环快捷开关、微型底栏 Dock |
| **云端文件探索 (S3 Browser)** | 云端目录面包屑、层级文件夹浏览、文件属性卡片、批量勾选面板、即时搜索与格式筛选 |
| **当前播放队列 (Playlist Queue)** | 当前播放高亮、优先插队管理、单曲移除、已播历史快速清理、新音频捕获状态条 |
| **存储桶管理 (Bucket Hub)** | 多云存储桶快速切换、挂载状态巡检、连接延迟测试、在线对象数量与总容量统计 |
| **同步与高级设置 (Sync Settings)** | AK/SK 敏感配置安全存储、轮询频率调节、自动插队策略、网络遥测监控面板 |

---

## 📦 下载安装

### 方式一：直接下载安装 Release APK (推荐)
前往项目的 [Releases 页面](https://github.com/atorber/s3-player/releases)，下载最新发布的安装包：
- **安装包命名规则**：`s3-player-v{版本号}.apk` *(例如：`s3-player-v1.0.3.apk`)*
- 支持搭载 Android 7.0 (API 24) 及以上版本的手机、车载系统或专业播放器硬件。

### 方式二：源码构建

1. **环境要求**：
   - Android Studio Ladybug (2024.2) 或更高版本
   - JDK 17
   - Android SDK 36

2. **克隆并编译**：
   ```bash
   git clone https://github.com/atorber/s3-player.git
   cd s3-player

   # 赋予编译权限
   chmod +x gradlew

   # 编译 Debug 版 APK
   ./gradlew assembleDebug

   # 安装至连接的 Android 设备
   ./gradlew installDebug
   ```

---

## 🚀 快速上手配置指南

只需 3 步，即可开始畅听：

1. **进入「设置」选项卡**：打开底栏的设置面板。
2. **填写 S3 连接参数**：
   - **存储桶名称 (Bucket Name)**：例如 `my-music-bucket`
   - **Endpoint**：例如 `s3.us-west-2.amazonaws.com` 或私有 MinIO 域名（无需填 `https://` 前缀）
   - **Region**：例如 `us-west-2`
   - **认证凭据**：填写 Access Key ID 与 Secret Access Key（若存储桶为公开只读，勾选「公开访问」即可免密连接）
   - **路径样式**：根据存储厂商选择开启/关闭 Path-Style（MinIO/私有对象存储通常需要开启）。
3. **点击「保存并启动同步」**：应用将瞬间握手远端 S3，自动探索并列出桶内的全部音频，即刻开启流媒体原音播放！

---

## 🛠️ 技术底座与开源库

- **核心架构**：MVVM 架构 + 单例音频处理引擎
- **UI 框架**：Jetpack Compose 1.7+ (Material 3)
- **响应式数据流**：Kotlin Coroutines + StateFlow / SharedFlow
- **本地持久化**：AndroidX Room (带严谨 Migration 迁移策略) + Encrypted SharedPreferences
- **网络与 S3 通信**：OkHttp 4.10 + Retrofit + XML 解析
- **系统集成**：Android Foreground Service + MediaSession + PowerManager WakeLock
- **CI / CD**：GitHub Actions 全自动化构建 Release APK 与发布

---

## 📄 开源许可证

本项目基于 [Apache License 2.0](LICENSE) 开源发布。欢迎提 Issue、PR 或分享使用心得！
