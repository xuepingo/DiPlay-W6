# DiPlay · 威马 W6 适配版

简体中文 | [English summary](#english-summary)

在**威马 W6**（Weltmeister W6）车机上运行**有线 CarPlay** 的适配分支。

上游 [DiPlay](https://github.com/shihabal3amri/DiPlay) 面向 BYD DiLink 与现代 Android 车机；本项目针对 W6 的高通 **QVM 虚拟化平台**（QNX 车控 + Android 9 客户机双系统）重做了 USB 数据通道、音频路由、视频渲染与显示协商，并把调通过程中定位到的每个问题的**现象、日志证据、根因、修法**完整记录在 [第 4 章](#4-调试过程)。

> **本项目的 APK 不是 Apple 认证产品。** 内嵌的实验性配件身份来自公开的 Carlinkit 固件，不是为 DiPlay 新签发的 MFi 身份，其私钥可被提取。详见 [免责声明](#免责声明)。

| | |
|---|---|
| 当前版本 | **稳定版 37**（基于上游 v0.3.15，`versionCode 135`） |
| 下载 | [Releases](../../releases/latest) |
| 包名 | `com.shihab.diplay` |
| APK SHA256 | `a9180916118b473826a0b66f6f8221797588db0dc84b78c0ee79cde0019508da` |
| 许可证 | GPL-3.0（派生作品，必须保留） |

---

## 目录

- [1. 支持范围](#1-支持范围)
- [2. 安装](#2-安装)
- [3. 使用方法](#3-使用方法)
- [4. 调试过程](#4-调试过程)
- [5. 从源码构建](#5-从源码构建)
- [6. 已知限制](#6-已知限制)
- [7. 版本历史](#7-版本历史)
- [8. 许可证与致谢](#8-许可证与致谢)

---

## 1. 支持范围

### 已在实车验证的环境

| 项目 | 值 |
|---|---|
| 车型 | 威马 W6 |
| 车机架构 | 高通 QVM 虚拟化：QNX 宿主 + Android 9 客户机 |
| Android | 9（API 28） |
| 屏幕 | 1920×720 横屏 |
| 连接方式 | **有线 USB**（NCM 网卡 + USBMUX 通道） |
| iPhone | iOS 17 及以上 |

### 未经本项目验证

- **无线 CarPlay**（Wi-Fi Direct / 车载热点）——上游实现完整，但 W6 上未做验证，见 [已知限制](#6-已知限制)
- 其他威马车型（EX5、EX6、M7 等）与 W6 硬件相近但未逐项测试
- 方向盘按键（原因见 [3.3](#33-方向盘按键为什么在-w6-上不可用)）

---

## 2. 安装

### 2.1 前提

- 车机允许安装第三方 APK（W6 默认允许未知来源，若被锁需在车机设置中放开）
- 一条**能传数据**的 USB 线。很多短线与纯充电线没有数据线芯，这是"插上没反应"最常见的原因
- iPhone 已升级到 iOS 17+，并在手机上点过"信任此电脑"

### 2.2 装到车机

任选其一。

**方式 A：U 盘（推荐，无需电脑）**

1. 把 `DiPlayLegacy-v0.3.15-威马W6-稳定版34-USBMUX重建自愈-含身份-debug.apk` 拷到 U 盘根目录
2. U 盘插入车机 USB 口，用车机文件管理器打开该 APK
3. 按提示完成安装

**方式 B：ADB（车机已开 USB 调试时）**

```bash
adb install -r "DiPlayLegacy-v0.3.15-威马W6-稳定版34-USBMUX重建自愈-含身份-debug.apk"
```

**覆盖安装即可升级**，不需要先卸载。设置项保存在应用私有目录，覆盖安装不会丢失。

> 若从上游 BYD 版或其他分支切过来出现 `INSTALL_FAILED_UPDATE_INCOMPATIBLE`，是签名不一致导致的，先卸载旧版再装。

### 2.3 首次连接

1. 车机启动后打开 **DiPlay** 主界面
2. USB 线连接 iPhone 与车机数据口
3. iPhone 弹出"要信任此电脑吗" → 点**信任**并输入锁屏密码
4. 车机端显示"正在连接"，通常 **3~8 秒**出画面

首次连接比后续慢（要走完整配对并保存配对记录），第二次起复用保存的记录，一般 1~2 秒建链。

### 2.4 日常使用

- 上车插线即连，不需要每次打开 App
- 拔线后 App 停在"等待 iPhone"状态，重新插线自动恢复，不必重启车机
- 长时间不用建议退出 App，避免后台占用

---

## 3. 使用方法

### 3.1 连接方式

本项目验证的是**有线**。上游支持的"车载热点 / Wi-Fi Direct / 现有 Wi-Fi 同一局域网"三种无线方式代码完整保留，但 W6 上没有实测结论，遇到问题请先回到有线验证。

### 3.2 设置项说明

设置分两类：**立即生效** 与 **下次连接生效**。改完没反应时，先看这一列。

| 设置项 | 取值 | 说明 | 生效时机 |
|---|---|---|---|
| 分辨率 | 自动 / 各档位 | CarPlay 握手分辨率 | 下次连接 |
| 帧率 | 自动 / 30 / 60 | 越高越顺滑，越吃解码性能 | 下次连接 |
| 使用 HEVC | 开 / 关 | 关闭可兼容更多车机；W6 硬解 H.264/HEVC 均正常 | 下次连接 |
| 媒体音量 | 25% – 300% | DiPlay 内部对媒体流的增益 | 立即 |
| **导航音量** | **25% – 600%** | 导航与语音提示的独立增益（见 [案例一](#42-案例一导航音量旋钮是死的)） | 立即 |
| 播报时压低媒体音量（实验性） | 开 / 关 | 播报时把媒体压到 20%，结束约 1 秒后恢复，走 500ms 渐变 | 下次连接 |
| 日夜模式 | 跟随系统 / 光感 / 白天 / 夜间 / 定时 | 光感模式带滞回带，避免阈值附近来回闪 | 立即 |
| 光感阈值 | 可调 | 日夜切换的照度阈值（lux） | 立即 |
| 桌面底栏置顶（实验性） | 开 / 关 | 桌面底栏挤压 CarPlay 时主动置前，让底栏收起，消除黑边 | 下次连接 |
| 仪表投射（实验性） | 自动 / 主屏左半区 | 把 CarPlay 画面投到仪表区域 | 下次连接 |
| 车机蓝牙音频 | 开 / 关 | 关闭后音频改由 iPhone 直接输出 | 下次连接 |
| 主屏幕地图与仪表盘同步 | 开 / 关 | 中控卡片镜像仪表地图 | 立即 |

**导航音量为什么能到 600%**：W6 的主音量只有一个总控，CarPlay 导航提示的实际电平又偏低，常规 100%~300% 档在音乐同时播放时几乎听不出差别。本项目把导航档位独立扩展到 600%，并对高增益档做**自适应软限幅**（增益 ≥500% 时拐点降到 88% 满量程，≥300% 时 82%，其余 70%），避免数字溢出变成破音。

### 3.3 方向盘按键为什么在 W6 上不可用

上游有一个 `WheelKeyService`，通过监听 `KeyEvent` 接管方向盘按键。**W6 上这条路走不通**：

W6 的方向盘按键由 **CAN 总线直接送到 QNX 车控域**，音量与切歌在 QNX 侧处理，根本不会作为 `KeyEvent` 下发到 Android 客户机。因此任何基于 `KeyEvent` 的监听都收不到事件——这不是权限问题，也不是 App 没启动。

本项目因此**删除了 `WheelKeyService` 及相关代码**（`CarPlayHostActivity.kt` 中有 `W6 note` 注释说明），而不是留一个永远不触发的空实现。如果你在设置里看到与方向盘相关的条目，那是上游文案残留，在 W6 上无效。

**结论**：W6 上用触摸屏操作 CarPlay 界面，或用 iPhone 本身控制。

### 3.4 导出诊断报告

定位问题几乎全靠这个。

1. 车机打开 DiPlay → **设置 → 诊断 → 保存诊断报告**
2. Android 9 上会走系统文件选择器，选一个能访问的位置（U 盘最方便）
3. 得到 `DiPlay-YYYYMMDD-HHMMSS-mmm.txt`

报告包含：连接阶段耗时、USB 枚举与重枚举记录、视频解码协商与帧统计、音频通道/音量/写入延迟/underrun、压低状态机日志、Activity 生命周期。**不含**热点密码，但含设备型号，外发前请自己看一眼。

---

## 4. 调试过程

这一章是本分支真正的价值所在。上游代码在 W6 上"能跑"和"能用"之间隔了 10 个版本，每个版本对应一批具体症状。

### 4.1 演进总览

| 版本 | 主题 | 解决的核心问题 |
|---|---|---|
| 25 | 音频焦点 + 日夜模式 | 媒体被其他播放器抢焦点后不恢复；日夜模式在阈值附近反复闪 |
| 26 | 全面审查 | 17 项逻辑/资源/边界问题 |
| 27 | 测试债清零 | 14 类存量测试失败、6 个语言包缺失、选择对话框"取消"按钮不渲染 |
| 28 | 合并上游 v0.3.13 | 97 个提交：解码器自愈、播报压低、NCM 恢复；移植主线 #1148 微信语音录音修复 |
| 29 | 合并上游 v0.3.15 | 有线防重复重枚举、Dock 挤压黑边防御；修 busy 门禁误判 |
| 30 | **导航音量根因修复** | 导航音量设置从未生效（见案例一）；档位扩至 600% |
| 31 | 存量小项清零 | 废弃 API、原子性、光感滞回、常量提取等 9 项 |
| 32 | 画面糊 + 平滑压低 | 首帧 transform 时序；压低从硬切改渐变（并引入 4 条回归） |
| 33 | 全量审查修复 | 修掉 32 引入的 4 条回归 + 资源泄漏 + 热路径效率 |
| 34 | USBMUX 重建自愈 | Activity 重建后首连必然超时 4 秒（见案例四） |

### 4.2 案例一：导航音量旋钮是死的

**现象**
音乐 100% 播放时，导航音量选 300% 完全听不到，选更高也只是"蚊子叫"；把车机自带导航音量拉到最大也没用。

**日志证据**
诊断报告里导航提示帧的统计一切正常（帧数在涨、`writeErrors=0`、`underrun=+0`），说明音频**确实在播放**，只是电平极低。

**根因**
增益匹配的判定条件是：

```kotlin
// 修复前
val gain = if (format.audioType == "navigation") navigationPercent else 100
```

而 CarPlay 协议**从来不会发 `audioType == "navigation"`**——导航提示实际以 `default` 或 `alert` 类型到达。这个 `if` 永远为假，导航音量设置**从一开始就没生效过**，无论选 100% 还是 300%，走的都是 100%。

典型的"死代码"型 bug：读代码看不出错，跑起来也看不出错，因为分支两边都是"正常"行为，只是设置项形同虚设。

**修法**
改为按**音频通道**而非类型字面量匹配，因为通道映射本身可靠：

```kotlin
// AudioChannelMapping.volumePercentFor(...)
val channel = map(audioType, payloadType, ...).channel
return when (channel) {
    AudioChannel.MEDIA -> mediaVolumePercent
    AudioChannel.NAVIGATION -> navigationVolumePercent
    else -> 100
}
```

同时把导航档位独立扩展到 600%，并加软限幅防溢出。补了回归测试 `volumePercentFollowsTheMappedChannelNotTheAudioTypeLiteral`，用 `default`/`alert`/`compatibility`/未知四种类型断言必须命中导航档。

**教训**：设置项"看起来能调"不等于"真的生效"。凡是与协议字段字面量挂钩的判断，都要用真实流量验证一遍。

### 4.3 案例二：刚连上画面糊，一会儿自己好

**现象**
刚装好或刚连上时，画面整个铺满规律的斜向网点（像打了马赛克），跑几十秒自己变清晰。

**日志证据**
解码器协商完全正常：`OMX.qcom.video.decoder.avc size=1920x720`，输出 `coded=1920x720 crop=0,0,1919,719`；渲染走 `Video output mode=TEXTURE`。也就是说，**不是解码分辨率的问题**。

**根因（分两层，修完第一层才暴露第二层）**

第一层：`onSurfaceTextureAvailable` 回调时 `view.width/height` 还是 0（View 尚未完成 layout），`updateVideoLayout()` 被 `if (viewWidth <= 0) return` 拦掉，**transform 矩阵没设置就开始渲染**，首帧以默认 1:1 映射到纹理，出现规律性拉伸伪影。等后续 layout pass 触发重算才恢复。

第二层：为抢在首帧前设好 transform，第一版补丁用了回调自带的 **SurfaceTexture 缓冲尺寸**（`width`/`height`）去调 `updateVideoLayout()`。但这个方法要的是 **View 尺寸**——内部 `setScale` 的分母、居中平移量都基于错误坐标系，SurfaceView 分支还会污染持久化的显示基线。

**最终修法**
调整顺序，让 transform 在 surface 挂载**之前**算好，用的仍是 View 尺寸：

```kotlin
// 先按 View 尺寸设好 transform，再挂 surface 并请求首帧
updateVideoLayout(view.width, view.height)   // 此时尺寸已由布局确定
attachSurface(surface)
sink?.refreshPicture(SCREEN_TYPE_MAIN)
reapplyVideoLayout()                          // 两段式兜底：立刻一次 + 尺寸稳定后一次
```

修复后日志证据：每次 `Texture surface created` 之后 50~130ms 内必然出现
`Display: video layout reapplied view=1920x720 content=1920x720`。

**教训**：`SurfaceTexture` 回调给的尺寸和 `View` 尺寸是两个坐标系，混用不会报错，只会让画面比例和位置悄悄出错。

### 4.4 案例三：压低开关关了还压低，开了又很生硬

**现象**
"播报时压低媒体音量"开关**关闭**时，导航播报依然会把音乐压小，而且这个压低非常**顺滑**，用户反而很喜欢。把开关**打开**后，压低变成"啪"一下掉下去，非常生硬。

**根因：存在两条完全独立的压低路径**

| 路径 | 触发源 | 受开关控制 | 过渡方式 |
|---|---|---|---|
| 系统焦点压低 | 导航音轨请求 `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` | 否 | **HAL 混音器级渐变**（好听的那个） |
| 应用层压低 | 播报 PCM 帧活动（issue #364） | 是 | `minOf(volume, 0.2f)` **硬切**（难听的那个） |

用户喜欢的顺滑感来自 **QNX/HAL 侧的系统级 duck**，与应用代码无关；开关打开后变生硬，是因为应用层又叠了一次瞬时跳变。

**修法**
没有去关掉系统焦点路径（那正是用户要的效果），而是把应用层的硬切换成渐变：

```kotlin
// 50ms 一步、每步收敛 15%，约 500ms 到位
private fun smoothVolumeTo(target: Float)
```

- 开关**关**：只有系统 HAL 顺滑压低，原样保留
- 开关**开**：系统压低 + 应用层 500ms 渐变加深，不再跳变

**关键约束**：焦点路径 `setFocusVolume` **必须保持同步直设**，不能也改渐变。一是它的注释契约写明"瞬时失焦静音（0f）必须压过 duck 电平"，异步渐变会让静音被旧目标拉回；二是 `AudioFocusAutoYieldTest` 的 16 个断言在 PAUSED Looper 下依赖同步语义。第一版把焦点路径也改成渐变，直接挂了 16 个测试——这个失败是有效信号，不是测试写错了。

### 4.5 案例四：改设置后重连，要多等 4 秒

**现象**
在设置页改完选项（会导致 Activity 重建），重连时首连必然卡 4 秒才出画面，第二次就好。

**日志证据**

```
17:07:02.017  attempt=2 开始
17:07:06.552  Timed out waiting for the USBMUX version reply
              USB read queue wedged; next attempt will force vendor re-enumeration
17:07:08.565  失败 → 重试
17:07:08.608  attempt=3 → 1 秒内就绪（走强制重枚举）
```

自愈逻辑是对的，只是**浪费了一次注定失败的尝试**。

**根因**
`forcedReenumerationForAttach` 和 `reenumeratedThisAttach` 是 **companion（静态）** 变量，只在**真实 USB 拔插**时重置。Activity 重建调 `close()` 时没清它们——上一次连接已做过一次重枚举（`reenumeratedThisAttach = true`），于是 `shouldForceReenumerationBeforeReuse()` 返回 false，走了 `reuse-descriptors`，复用半拆的 USBMUX 状态 → 握手超时。

**修法**
`close()` 中重置这两个标志，让 Activity 重建后的首次连接把 USB 当成新的 attach 周期：

```kotlin
// CarPlayController.close()
forcedReenumerationForAttach = false
reenumeratedThisAttach = false
```

**教训**：进程级静态状态 + 组件级生命周期，是最容易漏的组合。凡是 `companion` 里的标志，都要问一句"组件重建时该不该清"。

### 4.6 案例五：自审抓出自己引入的 4 条回归

稳定版 32 交付后做了一轮纯只读审查，发现**上一轮补丁自己带进了 4 个问题**（当时用户已经在车上装了）：

| | 问题 | 后果 |
|---|---|---|
| R1 | `volumeRampStep` 漏了 `synchronized`，同文件其余 Runnable 都持锁，唯独新加的渐变回调无锁遍历非线程安全的 `active` 表 | 可能 `ConcurrentModificationException` 崩到主线程 |
| R2 | `setFocusVolume` 既不取消也不重定向在途渐变 | duck 渐变中来电 → 瞬间静音后 50ms 被旧目标拉回 0.2f；回升渐变中失焦更会被拉回**满音量** |
| R3 | `smoothVolumeTo` 的 `if (mediaVolume == target) return` 早返回 | 释放渐变刚启动、音量还在 0.2f 时又来一帧播报，重新 engage 直接返回 → 日志说"已压低"，实际音量往满格走 |
| R4 | 把 SurfaceTexture 缓冲尺寸传给期望 View 尺寸的 `updateVideoLayout` | 首帧比例失真（见案例二第二层） |

R1–R3 同源，最终收敛成"**单一权威目标 + 单一渐变状态机 + 统一锁**"，而不是打三个补丁。稳定版 33 一并修掉，并在诊断报告里验证：4 组 `announcement duck engaged → released` 全部干净释放，3 次 teardown 的 `resource=media engine completed=true timedOut=false`。

**教训**：修 A 引入 B 是常态。补丁落地后要么补测试，要么补一次只读审查——这次是后者救回来的。

### 4.7 怎么读诊断报告

拿到 `.txt` 后按这个顺序看，比从头读到尾快得多：

| 想确认什么 | 搜什么 |
|---|---|
| 连接卡在哪一步 | `attempt=`、`phase=`、`teardown begin` |
| USB 是否异常 | `USBMUX`、`wedged`、`reenumeration`、`usbfs` |
| 画面糊/比例错 | `video layout reapplied`、`content=`、`Texture surface created` |
| 解码是否降级 | `decoder=`、`coded=`、`crop=`、`recoveries=` |
| 音量是否真生效 | `channel=NAVIGATION`、`volume=`、`announcement duck` |
| 音频卡顿 | `underrun`、`maxWriteMs`、`writeErrors` |
| 焦点/压低异常 | `AudioFocus`、`duck engaged`、`duck released` |

几个 W6 上的经验值：视频 `p50=59~75ms`、`p90=81~150ms` 属正常；媒体流 `maxWriteMs≈650ms` 是 AudioTrack 缓冲深度导致的，只要不持续上涨就没事；导航流 `maxWriteMs≈427ms`。

### 4.8 怎么自己抓日志

```bash
# 实时看连接过程
adb logcat -s xcertplay-usb AirPlayServer AudioTrack

# 抓完整一次连接到文件
adb logcat -c && adb logcat > diplay-full.log
# 复现问题后 Ctrl+C 结束
```

应用内诊断报告比 logcat 更有用——它是结构化统计而不是裸日志，优先用它。

---

## 5. 从源码构建

### 5.1 环境

| 工具 | 版本 | 说明 |
|---|---|---|
| JDK | **17**（Corretto 17 实测通过） | 上游 `docs/BUILD.md` 写的是 JDK 25，本分支在 17 上验证可用 |
| Android SDK Platform | 37 | `compileSdk = 37` |
| Build-Tools | 37.0.0 | 验签也用这个版本 |
| Gradle | 仓库自带 wrapper | 不要用系统 Gradle |

`minSdk = 17`，`targetSdk = 37`。

### 5.2 构建

```bash
export JAVA_HOME="/path/to/jdk-17"
export ANDROID_HOME="/path/to/Android/Sdk"
# 可选：指向放有 offline-mfi 身份资产的目录（见 5.3）
export DIPLAY_AUTH_ASSETS_DIR="/path/to/runtime-assets"

# 跑单测（shared + common，约 730 个用例）
./gradlew :shared:testDebugUnitTest :common:testDebugUnitTest

# 构建 APK
./gradlew :mobile:clean :mobile:assembleStandaloneDebug
```

产物：`mobile/build/outputs/apk/debug/mobile-debug.apk`

> **务必先 `:mobile:clean` 再 assemble**。脏构建会产出 21MB 左右且语言包缺失的异常包（本项目踩过这个坑），正常产物约 16.8MB。

### 5.3 身份资产（重要）

CarPlay 认证需要配件身份文件：

```
runtime-assets/offline-mfi/
├── certificate.p7b    # 证书链
└── identity.pk8       # 私钥
```

**这些文件不在本仓库内**（`.gitignore` 已排除 `*.pk8` / `*.p7b` / `auth-assets/`）。原因见 [免责声明](#免责声明)：内嵌私钥可被提取，公开分发 APK 本体已属灰色地带，仓库层面不再重复提供。

构建时通过 `DIPLAY_AUTH_ASSETS_DIR` 指向包含它们的目录。没有这些文件，代码能编译通过，但装到车机上 CarPlay 无法完成认证（表现为握手阶段失败）。

### 5.4 验签

```bash
"$ANDROID_HOME/build-tools/37.0.0/apksigner" verify --print-certs \
  mobile/build/outputs/apk/debug/mobile-debug.apk
```

需要看到 **V2 签名验证通过**。debug 包用 Android 默认 debug keystore 签名。

### 5.5 关于 CI

上游仓库自带的 4 个 GitHub Actions 工作流（`android.yml` / `pages.yml` / `verify-release-apk-version.yml` / `verify-release-signature.yml`）**没有**放在 `.github/workflows/`，而是作为参考收在 [`docs/ci-examples/`](docs/ci-examples/)。

原因有两个：

- 它们是为上游的 source-only 构建写的（JDK 25 + lint + 单测），不产出带身份的 APK，在本项目的手工构建流程下推上去只会长期报红；
- 提交 `.github/workflows/` 需要 OAuth token 具备 `workflow` scope，本仓库的发布流程用不到它。

想启用 CI 的话，把需要的文件复制回 `.github/workflows/` 再提交即可（届时需给 token 补 `workflow` 权限）。

---

## 6. 已知限制

| 限制 | 原因 | 状态 |
|---|---|---|
| 无线 CarPlay 未验证 | W6 的 Wi-Fi Direct 实现与上游目标机型差异大 | 未做，代码保留 |
| 方向盘按键不可用 | 按键走 CAN→QNX，不产生 `KeyEvent` | 无解，见 [3.3](#33-方向盘按键为什么在-w6-上不可用) |
| 播报压低仅在应用层可控 | 系统 HAL 级 duck 由 QNX 侧决定 | 已兼容，见案例三 |
| 仪表投射为实验性 | 依赖车机仪表窗口句柄 | 可用但不保证各固件一致 |
| Dock 置顶为实验性 | 部分桌面会挤压 CarPlay 窗口 | 稳定版 35 修复了「置顶操作实际未生效」，稳定版 36 让重排不再遮挡画面；但仍依赖车机 launcher 配合 |
| 只测过简体中文 | 其余 6 个语言包为回填 | 功能可用，文案可能有偏差 |
| 上游 `WheelSiriKey` 相关字段未移植 | 同上，W6 上无意义 | 有意删除 |

---

## 7. 版本历史

| 版本 | 基线 | 要点 |
|---|---|---|
| 稳定版 37 | v0.3.15 | 修强制重枚举在同一 attach 上叠加第二次请求：`reenumeratedThisAttach` 闩只加在 `shouldForceReenumerationBeforeReuse`，probe 的强制分支漏了它，导致 `UsbManager could not open the iPhone`（attempt 5/6 失败、空等 2-4 秒）。置位改到真正发出 bounce 处 |
| 稳定版 36 | v0.3.15 | 35 的自查修正：**Dock 重排改为静默请求**（原来会把主界面盖在投影上，且停在设置页时回不来）、**停滞看门狗补入站活跃条件**（否则空闲会话会被误判为数据面卡死而踢掉）、看门狗线程回收改并行 join |
| 稳定版 35 | v0.3.15 | 移植上游 v0.3.16 的 5 项：**Dock 置顶改用 launcher + NEW_TASK**（原实现是 no-op）、挤压守卫改用基线尺寸、bulk-out 停滞看门狗、NCM 每帧日志改采样、阶段与倒车 age 改单调时钟、诊断补开关状态 |
| 稳定版 34 | v0.3.15 | Activity 重建后首连强制 USB 重枚举，消除 4 秒超时 |
| 稳定版 33 | v0.3.15 | 修 32 引入的 4 条回归；资源泄漏（媒体引擎 / IAP socket / 看门狗线程）；热路径（O(n²) 缓冲、toHex 查表、BufferInfo 复用、LPCM 零拷贝） |
| 稳定版 32 | v0.3.15 | 首帧 transform 时序；压低硬切改 500ms 渐变 |
| 稳定版 31 | v0.3.15 | 存量小项 9 项（废弃 API、原子性、光感滞回、常量提取等） |
| 稳定版 30 | v0.3.15 | **导航音量根因修复**（按通道匹配）+ 档位扩至 600% + 自适应软限幅 |
| 稳定版 29 | v0.3.15 | 合并上游 v0.3.15（含 0.3.14）；修 busy 门禁误判、播报计时器未摘除 |
| 稳定版 28 | v0.3.13 | 合并上游 v0.3.13（97 提交）+ 移植主线 #1148 微信语音录音 MTU 修复 |
| 稳定版 27 | v0.3.0 | 存量测试失败清零、6 语言包回填、选择对话框取消按钮修复 |
| 稳定版 26 | v0.3.0 | 17 项审查修复 |
| 稳定版 25 及更早 | v0.3.0 | W6 基础适配：USB NCM、音频路由、日夜模式、仪表左半区、触控与视频卡死修复 |

回滚标签：`w6-stable27-premerge-20261102`（合并上游前的完整可用状态）。

---

## 8. 许可证与致谢

### 许可证

**GNU General Public License v3.0** —— 完整文本见 [LICENSE](LICENSE)。

本仓库的派生链条如下，修改与再分发必须遵守对应许可证：

```
shilapi/xcertplay                    GPL-3.0     接收器核心
        ↓
shihabal3amri/DiPlay                 GPL-3.0     加入 DiAuto 风格界面
        ↓  界面部分源自 shihabal3amri/DiAuto  AGPL-3.0
programmerguohuajing/DiPlay-Legacy-Android       旧版 Android 兼容重构（原仓库已不可访问）
        ↓
本仓库（威马 W6 适配分支）             GPL-3.0
```

> **关于提交历史**：`programmerguohuajing/DiPlay-Legacy-Android` 已被删除或转为私有（GitHub 返回 404），本项目是在该仓库尚可用时浅克隆拉取的。浅克隆缺少早期祖先对象，直接推送会被 GitHub 的 `index-pack` 拒绝，因此推送前在对象层重放了一遍历史：**保留了每个提交的作者、时间、提交信息与完整目录树，只有提交 SHA 变化，两个已无法追溯的祖先边界被切断为根提交**。本分支自己的 W6 适配提交（稳定版 1 → 34）全部完整。

原始版权归各作者所有，本分支的修改同样以 GPL-3.0 发布。分发修改版时必须保留全部上游版权声明与许可证文本，详见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) 与 `docs/licenses/`。

### 致谢

- [shilapi](https://github.com/shilapi/xcertplay) —— xcertplay 接收器核心
- [shihabal3amri](https://github.com/shihabal3amri) —— DiPlay 与 DiAuto 界面
- [programmerguohuajing](https://github.com/programmerguohuajing) —— Legacy 分支的旧版 Android 兼容重构，本项目多数上游能力来自这里（原仓库现已不可访问）
- 上游 issue 贡献者 —— #290（surface 重建）、#363（Dock 挤压）、#364（播报压低）、#1148（麦克风 MTU）、#275（USB 重枚举）等修复的原始思路

### 相关文档

本仓库保留了上游完整文档，通用功能仍以它们为准：

- [docs/BUILD.md](docs/BUILD.md) —— 上游构建说明（JDK 25 版本）
- [docs/INSTALL.md](docs/INSTALL.md) —— 安装
- [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md) —— 兼容性与故障排查
- [docs/CONNECTION_RELIABILITY.md](docs/CONNECTION_RELIABILITY.md) —— 连接可靠性
- [docs/ANDROID9_WIFI_DIRECT.md](docs/ANDROID9_WIFI_DIRECT.md) —— Android 9 无线
- [docs/PRIVACY.md](docs/PRIVACY.md) —— 诊断报告与隐私
- [docs/LEGACY-README.md](docs/LEGACY-README.md) —— 上一级 Legacy 分支 README 备份
- [docs/UPSTREAM-README.md](docs/UPSTREAM-README.md) —— xcertplay 原始 README

---

## English summary

A fork of [DiPlay](https://github.com/shihabal3amri/DiPlay) (via [DiPlay-Legacy-Android](https://github.com/programmerguohuajing/DiPlay-Legacy-Android)) adapted for the **Weltmeister W6** head unit — a Qualcomm QVM platform running Android 9 as a guest OS on QNX.

Scope of this fork: USB NCM + USBMUX transport hardening, audio channel routing and per-channel gain, video render timing, display negotiation, and lifecycle recovery. Upstream targets BYD DiLink hardware; this fork documents every W6-specific defect found, with log evidence and root cause analysis, in [section 4](#4-调试过程) (Chinese).

**Verified**: wired CarPlay only, 1920×720, Android 9, iOS 17+. Wireless is untested. Steering-wheel keys are unusable on W6 by vehicle bus design (CAN → QNX, no `KeyEvent` reaches Android).

**Not an Apple-certified product.** The bundled accessory identity is an experimental one recovered from public Carlinkit firmware, not a newly provisioned MFi identity for DiPlay, and its private key is extractable. See the disclaimer below.

Licensed **GPL-3.0**, as required by the upstream chain. UI portions derive from DiAuto (AGPL-3.0).

---

## 免责声明

- 本项目**不是** Apple 认证产品，与 Apple Inc. 无任何关联。CarPlay 是 Apple Inc. 的商标。
- APK 内嵌的配件身份为**实验性**产物，来源为公开可得的 Carlinkit 固件，并非为 DiPlay 签发。其私钥可被提取，因此**不构成任何安全保证**。
- 该身份在未来 iOS 更新后能否继续被接受、在不同车机上的可靠性、以及是否适合公开分发，均**未有定论**。
- 本项目仅用于个人车辆上的兼容性研究与自用。使用者需自行承担在车机上安装第三方软件的全部风险（包括但不限于车机异常、保修影响、数据丢失）。
- 请勿在驾驶过程中操作车机界面。遵守当地交通法规。
- 如权利人认为本仓库内容侵犯其权益，请通过 issue 联系，会及时处理。
