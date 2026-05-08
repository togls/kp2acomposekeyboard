# kp2acomposekeyboard

基于 Keepass2Android Plugin API 的现代 Compose 安全输入法。

当前进度：Plan 0.2 配置 Version Catalog。

## 已完成

### Plan 0.1 创建项目

- Android Application 单 app module 骨架。
- 包名：`io.github.togls.kp2acomposekeyboard`。
- `minSdk = 26`、`targetSdk = 36`、`compileSdk = 36`。
- 输入法 `KeyboardImeService` 最小类。
- Manifest 输入法 Service 注册。
- `res/xml/method.xml` 输入法 metadata。

### Plan 0.2 配置 Version Catalog

- 创建 `gradle/libs.versions.toml`。
- 添加 AGP、Kotlin、Compose BOM、Compose Compiler、Hilt、KSP、Lifecycle、Activity Compose、DataStore 版本声明。
- root `build.gradle.kts` 使用 plugin alias。
- app `build.gradle.kts` 的现有插件使用 plugin alias。

## 后续

- Plan 0.3：配置完整 Gradle / Compose / Material 3 / Lifecycle / Hilt / DataStore。
- Plan 0.4：接入 Hilt Application 与基础 DI。
