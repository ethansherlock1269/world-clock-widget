# 世界时钟挂件

欧式摆钟风格的 Android 桌面小部件（2×2，可拖动调整大小）。

## 特点

- 奶白开裂漆面表壳、金色串珠表圈、黑色阿拉伯数字，参照传统欧式摆钟
- 表盘里的小鸡按秒啄米，表壳下方的摆盘持续摆动
- 单个时钟，时区可自由选择：内置常用城市，也可输入任意时区 ID（如 `Europe/Rome`）
- 点击挂件可随时重新选择时区
- 指针按所选时区的当地时间每分钟刷新

## 使用

1. 安装 APK 后，在桌面长按，选择小部件，添加“世界时钟挂件”。
2. 添加时选择城市，点“保存”；之后点击挂件可重新选择。
3. 国产系统（如 ColorOS/OxygenOS）请把本应用的电池设置为“不受限制”并允许自启动，
   否则时针可能停摆。Android 13 及以上建议在“闹钟和提醒”里授权本应用，指针刷新更准时。

## 构建

- 用 Android Studio 打开本目录，等待 Gradle 同步（需联网）。
- Gradle JDK 选 17 或 21（Gradle 8.7 不支持 JDK 25）。
- SDK：compileSdk 34，minSdk 24。
- 打包：`Build > Generate App Bundles or APKs > Generate APKs`，
  输出位置 `app/build/outputs/apk/debug/app-debug.apk`（调试签名）。

本仓库未包含 `gradlew` / `gradle-wrapper.jar`，请使用 Android Studio 构建。

## 目录说明

```
app/src/main/
├─ AndroidManifest.xml
├─ java/com/example/worldclock/
│  ├─ ClockWidgetProvider.kt   挂件刷新（指针、城市名、每分钟定时）
│  ├─ ConfigActivity.kt        时区选择页
│  └─ Prefs.kt                 城市列表与时区保存
└─ res/
   ├─ drawable-nodpi/          表壳、摆盘图片
   ├─ drawable/                指针、小鸡帧、摆盘帧（旋转资源）
   ├─ layout/widget_clock.xml  挂件布局
   └─ xml/clock_widget_info.xml 挂件规格
```

想增加常用城市，在 `Prefs.kt` 的 `CITIES` 里加一行即可。
