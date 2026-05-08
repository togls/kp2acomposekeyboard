# kp2acomposekeyboard

基于 Keepass2Android Plugin API 的现代 Compose 安全输入法。

当前进度：Plan 0.4 接入 Hilt 基础骨架。

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

### Plan 0.3 配置 Gradle

- app module 应用 Android Application、Kotlin Android、Compose Compiler、KSP、Hilt 插件。
- 启用 Compose。
- 添加 Compose、Material 3、Lifecycle、Activity Compose、DataStore、Hilt 依赖。
- Hilt 编译器使用 KSP。
- 依赖版本继续集中在 Version Catalog 中管理。

### Plan 0.4 接入 Hilt

- 创建 `Kp2aComposeKeyboardApp`。
- 添加 `@HiltAndroidApp`。
- Manifest 注册 Application。
- 创建基础 `di/AppModule.kt`。
- `KeyboardImeService` 添加 `@AndroidEntryPoint`。

## 本地验证建议

```bash
./gradlew :app:kspDebugKotlin
./gradlew :app:assembleDebug
```

## 后续

- Plan 1.1：创建输入法 Service 的完整 Compose 宿主。
- Plan 1.2：在 `onCreateInputView()` 中返回 `ComposeView` 并显示静态测试布局。
