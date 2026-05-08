# kp2acomposekeyboard

基于 Keepass2Android Plugin API 的现代 Compose 安全输入法。

当前进度：Plan 0.1 创建项目。

已完成：

- Android Application 单 app module 骨架。
- 包名：`io.github.togls.kp2acomposekeyboard`。
- `minSdk = 26`、`targetSdk = 36`、`compileSdk = 36`。
- 输入法 `KeyboardImeService` 最小类。
- Manifest 输入法 Service 注册。
- `res/xml/method.xml` 输入法 metadata。

后续：

- Plan 0.2：迁移到 Version Catalog。
- Plan 0.3：配置完整 Gradle / Compose / Material 3 / Lifecycle / Hilt / DataStore。
- Plan 0.4：接入 Hilt Application 与基础 DI。
