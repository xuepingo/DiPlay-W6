# 稳定版 34

基于上游 DiPlay **v0.3.15**（`versionCode 135`），面向**威马 W6**（高通 QVM / Android 9）的有线 CarPlay 适配版。

## 本版修复

### Activity 重建后首连必然超时 4 秒

**现象**：在设置页改完选项（会触发 Activity 重建）后重新连接，首连固定卡 4 秒才出画面，第二次就正常。

**日志证据**

```
17:07:02.017  attempt=2 开始
17:07:06.552  Timed out waiting for the USBMUX version reply
              USB read queue wedged; next attempt will force vendor re-enumeration
17:07:08.565  失败 → 重试
17:07:08.608  attempt=3 → 1 秒内就绪
```

**根因**：`forcedReenumerationForAttach` 与 `reenumeratedThisAttach` 是 companion（静态）变量，只在真实 USB 拔插时重置。Activity 重建调 `close()` 时没有清它们，导致 `shouldForceReenumerationBeforeReuse()` 返回 false，走了 `reuse-descriptors`，复用半拆的 USBMUX 状态 → 版本握手超时 → 自愈重连。

**修法**：`close()` 中重置这两个标志，让 Activity 重建后的首次连接把 USB 当成新的 attach 周期，直接走强制重枚举（issue #275 路径），跳过注定失败的复用尝试。

改动：`CarPlayController.kt` +8 行。

## 相对上游 v0.3.15 的累积能力

| 版本 | 内容 |
|---|---|
| 34 | 本版：Activity 重建后首连强制 USB 重枚举 |
| 33 | 修 32 引入的 4 条回归；资源泄漏（媒体引擎 / IAP socket / NCM 看门狗线程）；热路径优化（O(n²) 缓冲、toHex 查表、BufferInfo 复用、LPCM 零拷贝） |
| 32 | 首帧 transform 时序修复（画面糊）；压低从硬切改 500ms 渐变 |
| 31 | 存量小项 9 项（废弃 API、原子性、光感滞回、常量提取等） |
| 30 | 导航音量根因修复（改按音频通道匹配，此前设置从未生效）+ 档位扩至 600% + 自适应软限幅 |
| 29 | 合并上游 v0.3.15（有线防重复重枚举、Dock 挤压黑边防御）；修 busy 门禁误判 |
| 28 | 合并上游 v0.3.13（97 提交）+ 移植主线 #1148 微信语音录音 MTU 修复 |
| 25–27 | W6 基础适配、音频焦点、日夜模式、17 项审查修复、测试债清零 |

完整调试记录（现象 / 日志证据 / 根因 / 修法）见 [README 第 4 章](README.md#4-调试过程)。

## 安装

1. 把 APK 拷到 U 盘，插入车机 USB 口，用车机文件管理器打开安装
2. 或 `adb install -r <apk>`
3. 覆盖安装即可升级，设置不会丢失

安装到**车机**，不是 iPhone。车机需允许安装第三方应用。

## 校验

| | |
|---|---|
| 文件 | `DiPlayLegacy-v0.3.15-威马W6-稳定版34-USBMUX重建自愈-含身份-debug.apk` |
| 大小 | 16,838,286 字节 |
| SHA256 | `a9180916118b473826a0b66f6f8221797588db0dc84b78c0ee79cde0019508da` |
| 包名 | `com.shihab.diplay` |
| 签名 | V2（debug keystore） |

## 上车验证建议

1. 改一次设置（如切换 HEVC 或帧率）让 Activity 重建，然后重连 —— 不应再出现 `Timed out waiting for the USBMUX version reply`，首连应在 1~2 秒内完成
2. 冷启动第一帧画面应立即清晰且比例正确
3. 开着"播报时压低媒体音量"，播报过程中插入来电或微信语音 —— 静音后音乐不应慢慢渗出
4. 紧邻两次导航播报 —— 第二次压低仍应生效

## 免责声明

本 APK **不是 Apple 认证产品**。内嵌的配件身份为实验性产物，来自公开的 Carlinkit 固件，非为 DiPlay 新签发，私钥可被提取。未来 iOS 更新后能否继续被接受、在不同车机上的可靠性、是否适合公开分发均无定论。仅供个人车辆兼容性研究与自用，使用者自行承担全部风险。

许可：GPL-3.0（派生自 shilapi/xcertplay → shihabal3amri/DiPlay → programmerguohuajing/DiPlay-Legacy-Android）。
