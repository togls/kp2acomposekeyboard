# kp2acomposekeyboard 技术架构设计

版本：v0.1  
状态：架构草案  
项目定位：基于 Keepass2Android Plugin API 的现代 Compose 安全输入法  
技术基线：AGP 9.x + Version Catalog + Compose + Material 3 + Hilt + MVI/ViewModel

---

## 1. 架构目标

`kp2acomposekeyboard` 是一个独立 Android 输入法应用，同时作为 Keepass2Android 插件使用。

本架构设计目标：

1. 使用现代 Android 官方推荐的工程实践。
2. 使用 Jetpack Compose 构建键盘 UI。
3. 使用 ViewModel + StateFlow + SharedFlow 实现 MVI / UDF 单向数据流。
4. 使用 Hilt 进行依赖注入。
5. 使用 Version Catalog 统一管理依赖版本。
6. 使用 Activity 承载条目选择页和设置页。
7. `InputMethodService` 只负责输入法宿主和输入能力。
8. 敏感字段只短时间保存在内存中。
9. 不通过剪贴板传递密码。
10. 不把敏感字段写入磁盘、日志、崩溃上报或 UI State。
11. 保持 P0 架构简单、清晰、可维护，避免过度模块化。

---

## 2. 技术基线

P0 推荐技术基线：

```text
AGP: 9.x，要求大于等于 9.0
Gradle: 与 AGP 9.x 匹配
JDK: 17
Kotlin: 2.x
Compose: Compose BOM + Compose Compiler Gradle Plugin
UI: Jetpack Compose + Material 3
DI: Hilt + KSP
Architecture: MVI / UDF + ViewModel
Module: 单 app module
```

建议版本示例：

```text
AGP: 9.2.0
Kotlin: 2.3.21
Compose BOM: 使用当前稳定 BOM
Hilt: 使用当前稳定版本
KSP: 与 Kotlin 版本匹配
```

说明：

1. 版本号应统一写入 `libs.versions.toml`。
2. Compose 依赖建议使用 BOM 管理。
3. Kotlin 2.x 项目使用 `org.jetbrains.kotlin.plugin.compose` 配置 Compose Compiler。
4. Hilt 编译器建议使用 KSP，不建议新项目继续使用 kapt。
5. P0 暂时保持单 app module，不拆 `core`、`domain`、`data` 等多 module。

---

## 3. 总体架构

当前推荐架构：

```text
Activity + IME 分离架构
        +
MVI / UDF + ViewModel
        +
Hilt 依赖注入
        +
内存 Session 安全边界
```

核心职责：

```text
IME Service
    只负责输入法宿主、Compose 键盘展示、InputConnection 输入、Effect 执行

EntryPickerActivity
    负责 Keepass2Android 条目选择流程

SettingsActivity
    负责设置页

KeyboardViewModel
    负责键盘状态和业务决策

KeyboardSessionRepository
    负责当前条目内存会话

Compose UI
    只负责渲染 UI State 和发送 Intent
```

核心数据流：

```text
KeyboardIntent
        ↓
KeyboardViewModel
        ↓
KeyboardUiState
        ↓
Compose UI

KeyboardViewModel
        ↓
KeyboardEffect
        ↓
KeyboardImeService
        ↓
InputConnection / Activity 跳转 / 平台能力
```

---

## 4. 工程目录结构

P0 推荐单 app module：

```text
app/
├─ di/
│  ├─ AppModule.kt
│  ├─ SessionModule.kt
│  ├─ Kp2aModule.kt
│  └─ KeyboardModule.kt
│
├─ ime/
│  ├─ KeyboardImeService.kt
│  ├─ KeyboardInputView.kt
│  ├─ InputConnectionDispatcher.kt
│  ├─ ImeEffectHandler.kt
│  └─ KeyboardViewModelFactory.kt
│
├─ feature/
│  ├─ keyboard/
│  │  ├─ KeyboardViewModel.kt
│  │  ├─ KeyboardUiState.kt
│  │  ├─ KeyboardIntent.kt
│  │  ├─ KeyboardEffect.kt
│  │  └─ KeyboardReducer.kt
│  │
│  ├─ entrypicker/
│  │  ├─ EntryPickerActivity.kt
│  │  ├─ EntryPickerViewModel.kt
│  │  ├─ EntryPickerUiState.kt
│  │  ├─ EntryPickerIntent.kt
│  │  └─ EntryPickerEffect.kt
│  │
│  └─ settings/
│     ├─ SettingsActivity.kt
│     ├─ SettingsViewModel.kt
│     ├─ SettingsUiState.kt
│     └─ SettingsIntent.kt
│
├─ kp2a/
│  ├─ Kp2aContract.kt
│  ├─ Kp2aEntryLauncher.kt
│  ├─ Kp2aEntryResultParser.kt
│  └─ Kp2aEntryMapper.kt
│
├─ session/
│  ├─ KeyboardSessionRepository.kt
│  ├─ KeyboardSession.kt
│  ├─ KeyboardSessionSnapshot.kt
│  └─ SessionTimeoutController.kt
│
├─ domain/
│  ├─ KeyboardField.kt
│  ├─ KeyboardFieldUiModel.kt
│  ├─ KeyboardFieldType.kt
│  ├─ KeyboardFieldClassifier.kt
│  └─ SensitiveFieldPolicy.kt
│
├─ settings/
│  ├─ KeyboardSettingsRepository.kt
│  ├─ KeyboardSettings.kt
│  └─ SettingsDataStore.kt
│
└─ ui/
   ├─ theme/
   │  ├─ KeyboardTheme.kt
   │  ├─ KeyboardColors.kt
   │  └─ KeyboardDimensions.kt
   │
   └─ keyboard/
      ├─ KeyboardRoot.kt
      ├─ DefaultKeyboardLayout.kt
      ├─ ExistingEntryHint.kt
      ├─ LetterKeyboard.kt
      ├─ NumberKeyboard.kt
      ├─ SymbolKeyboard.kt
      ├─ EntryKeyboardLayout.kt
      ├─ EntryHeader.kt
      ├─ FixedFieldRow.kt
      ├─ ExtraFieldPagedPanel.kt
      ├─ AllFieldsExpandedPanel.kt
      ├─ EntryActionRows.kt
      ├─ KeyboardKey.kt
      └─ FieldButton.kt
```

---

## 5. 模块职责说明

### 5.1 `ime/`

`ime/` 是输入法宿主层。

职责：

1. 创建输入法 Compose View。
2. 收集 `KeyboardUiState`。
3. 收集 `KeyboardEffect`。
4. 调用 `InputConnection.commitText()` 输入普通文本和字段值。
5. 调用 `InputConnection.deleteSurroundingText()` 删除字符。
6. 发送 Enter。
7. 启动 `EntryPickerActivity`。
8. 启动 `SettingsActivity`。
9. 输入法销毁时触发 Session 清理。

禁止：

1. 不直接解析 KP2A 返回结果。
2. 不做字段分类。
3. 不保存敏感字段。
4. 不把密码写入日志。
5. 不把敏感字段写入持久化存储。

---

### 5.2 `feature/keyboard/`

键盘本体功能层。

职责：

1. 管理默认布局和条目布局切换。
2. 管理字母、数字、符号输入模式。
3. 管理大小写切换。
4. 管理分页模式和展开模式。
5. 处理字段按钮点击。
6. 从 `KeyboardSessionRepository` 获取字段值。
7. 输出 `KeyboardEffect` 给 IME Service 执行。
8. 维护不含敏感 value 的 `KeyboardUiState`。

---

### 5.3 `feature/entrypicker/`

条目选择功能层。

职责：

1. 承载 Keepass2Android 条目选择流程。
2. 调用 KP2A 选择接口。
3. 接收 KP2A 返回结果。
4. 使用 `Kp2aEntryResultParser` 解析数据。
5. 使用 `KeyboardFieldClassifier` 分类字段。
6. 创建 `KeyboardSession`。
7. 写入 `KeyboardSessionRepository`。
8. 选择完成后关闭 Activity。

---

### 5.4 `feature/settings/`

设置功能层。

职责：

1. 展示设置页。
2. 修改普通配置。
3. 将非敏感配置写入 DataStore。
4. 支持动态主题、超时时间、键盘高度、震动反馈等设置。

允许持久化：

```text
Session 超时时间
主题偏好
键盘高度
按键密度
震动开关
输入 Password 后是否自动清理
```

禁止持久化：

```text
Password
TOTP
Token
Secret
Recovery Code
任何 KP2A 字段 value
```

---

### 5.5 `kp2a/`

Keepass2Android 接入层。

职责：

1. 定义 KP2A Intent / Extra / Result 常量。
2. 启动 KP2A 条目选择。
3. 解析 KP2A 返回数据。
4. 映射为项目内部领域模型。
5. 隔离外部插件协议变化。

---

### 5.6 `session/`

安全会话层。

职责：

1. 在内存中保存当前条目。
2. 保存字段真实 value。
3. 提供字段 value 查询。
4. 提供不含 value 的 UI Snapshot。
5. 支持 60 秒超时清理。
6. 支持手动清理。
7. 支持输入法销毁时清理。
8. 支持重新选择条目时覆盖旧条目。

关键原则：

```text
KeyboardSessionRepository 可以持有 value
KeyboardUiState 不能持有 value
Compose UI 不能持有 value
SavedStateHandle 不能持有 value
DataStore 不能持有 value
日志不能持有 value
```

---

### 5.7 `domain/`

领域规则层。

职责：

1. 定义字段模型。
2. 定义字段类型。
3. 识别固定字段。
4. 识别敏感字段。
5. 字段排序。
6. 生成 UI 字段模型。

固定字段：

```text
Username
Password
TOTP
```

敏感字段包括：

```text
Password
TOTP
Recovery Code
Token
Secret
其他可疑敏感自定义字段
```

---

### 5.8 `ui/`

Compose UI 层。

职责：

1. 只接收 `KeyboardUiState`。
2. 只通过 `onIntent` 上报用户事件。
3. 不访问 Repository。
4. 不访问 InputConnection。
5. 不访问 KP2A。
6. 不接触字段真实 value。

---

## 6. MVI / UDF 状态模型

### 6.1 数据流原则

```text
State 向下流动
Intent 向上流动
Effect 单次消费
```

```text
用户点击
    ↓
KeyboardIntent
    ↓
KeyboardViewModel
    ↓
KeyboardUiState
    ↓
Compose UI 重组
```

需要执行一次性平台动作时：

```text
KeyboardViewModel
    ↓
KeyboardEffect
    ↓
KeyboardImeService
    ↓
InputConnection / startActivity
```

---

### 6.2 KeyboardIntent

```kotlin
sealed interface KeyboardIntent {
    data object SelectEntry : KeyboardIntent
    data object OpenSettings : KeyboardIntent
    data object ClearEntry : KeyboardIntent

    data object SwitchToDefaultLayout : KeyboardIntent
    data object SwitchToEntryLayout : KeyboardIntent

    data object SwitchToLetters : KeyboardIntent
    data object SwitchToNumbers : KeyboardIntent
    data object SwitchToSymbols : KeyboardIntent

    data object ToggleUppercase : KeyboardIntent

    data class CommitText(val text: String) : KeyboardIntent
    data class CommitField(val fieldId: String) : KeyboardIntent

    data object DeleteBackward : KeyboardIntent
    data object Enter : KeyboardIntent

    data object PrevExtraFieldPage : KeyboardIntent
    data object NextExtraFieldPage : KeyboardIntent

    data object ExpandFields : KeyboardIntent
    data object CollapseFields : KeyboardIntent

    data object ScrollExpandedFieldsUp : KeyboardIntent
    data object ScrollExpandedFieldsDown : KeyboardIntent
}
```

---

### 6.3 KeyboardUiState

`KeyboardUiState` 只用于渲染 UI，不得包含敏感字段 value。

```kotlin
data class KeyboardUiState(
    val mainLayout: MainKeyboardLayout = MainKeyboardLayout.Default,
    val defaultInputMode: DefaultInputMode = DefaultInputMode.Letters,
    val entryFieldDisplayMode: EntryFieldDisplayMode = EntryFieldDisplayMode.Paged,

    val currentEntryName: String? = null,
    val hasActiveSession: Boolean = false,

    val fixedFields: List<KeyboardFieldUiModel> = emptyList(),
    val extraFields: List<KeyboardFieldUiModel> = emptyList(),
    val allFields: List<KeyboardFieldUiModel> = emptyList(),

    val extraFieldPageIndex: Int = 0,
    val extraFieldPageSize: Int = 3,

    val isUppercase: Boolean = false,
)
```

---

### 6.4 KeyboardEffect

`KeyboardEffect` 表示一次性副作用。

```kotlin
sealed interface KeyboardEffect {
    data class CommitText(val text: String) : KeyboardEffect
    data object DeleteBackward : KeyboardEffect
    data object SendEnter : KeyboardEffect

    data object LaunchEntryPicker : KeyboardEffect
    data object LaunchSettings : KeyboardEffect

    data class ScrollExpandedFields(
        val direction: ScrollDirection,
    ) : KeyboardEffect
}
```

说明：

1. 普通字符输入通过 `CommitText`。
2. 字段值输入也通过 `CommitText`，但禁止日志打印 text。
3. 打开条目选择页使用 `LaunchEntryPicker`。
4. 打开设置页使用 `LaunchSettings`。
5. 展开模式滚动使用 `ScrollExpandedFields`。

---

## 7. 核心流程

### 7.1 普通字符输入

```text
用户点击字母键
    ↓
KeyboardIntent.CommitText("a")
    ↓
KeyboardViewModel
    ↓
KeyboardEffect.CommitText("a")
    ↓
KeyboardImeService
    ↓
InputConnection.commitText("a", 1)
```

---

### 7.2 删除字符

```text
用户点击 ⌫
    ↓
KeyboardIntent.DeleteBackward
    ↓
KeyboardViewModel
    ↓
KeyboardEffect.DeleteBackward
    ↓
KeyboardImeService
    ↓
InputConnection.deleteSurroundingText(1, 0)
```

---

### 7.3 发送 Enter

```text
用户点击 换行
    ↓
KeyboardIntent.Enter
    ↓
KeyboardViewModel
    ↓
KeyboardEffect.SendEnter
    ↓
KeyboardImeService
    ↓
InputConnection.sendKeyEvent(KEYCODE_ENTER)
```

---

### 7.4 选择 Keepass2Android 条目

```text
用户点击 [选择条目]
    ↓
KeyboardIntent.SelectEntry
    ↓
KeyboardViewModel
    ↓
KeyboardEffect.LaunchEntryPicker
    ↓
KeyboardImeService
    ↓
startActivity(EntryPickerActivity)
    ↓
EntryPickerActivity 启动 KP2A
    ↓
用户选择条目
    ↓
KP2A 返回字段数据
    ↓
EntryPickerViewModel 解析并分类字段
    ↓
KeyboardSessionRepository.setSession()
    ↓
KeyboardViewModel 观察到 Session 变化
    ↓
KeyboardUiState 更新为条目布局
```

---

### 7.5 点击字段按钮

```text
用户点击 [Password]
    ↓
KeyboardIntent.CommitField(fieldId)
    ↓
KeyboardViewModel
    ↓
KeyboardSessionRepository.getFieldValue(fieldId)
    ↓
KeyboardEffect.CommitText(passwordValue)
    ↓
KeyboardImeService
    ↓
InputConnection.commitText(passwordValue, 1)
```

安全要求：

```text
passwordValue 不进入 UiState
passwordValue 不进入 Compose UI
passwordValue 不进入 SavedStateHandle
passwordValue 不进入 DataStore
passwordValue 不写日志
passwordValue 不经过剪贴板
```

---

### 7.6 条目超时清理

```text
SessionTimeoutController 定时检查
    ↓
超过超时时间
    ↓
KeyboardSessionRepository.clear()
    ↓
KeyboardViewModel 观察到 Session 为空
    ↓
KeyboardUiState 更新为默认布局
```

---

## 8. Hilt 设计

### 8.1 Application

```kotlin
@HiltAndroidApp
class Kp2aComposeKeyboardApp : Application()
```

---

### 8.2 Hilt Module

```text
di/
├─ AppModule.kt
├─ SessionModule.kt
├─ Kp2aModule.kt
└─ KeyboardModule.kt
```

---

### 8.3 SessionModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideKeyboardSessionRepository(): KeyboardSessionRepository {
        return KeyboardSessionRepository()
    }

    @Provides
    @Singleton
    fun provideSessionTimeoutController(
        sessionRepository: KeyboardSessionRepository,
    ): SessionTimeoutController {
        return SessionTimeoutController(sessionRepository)
    }
}
```

---

### 8.4 KeyboardModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object KeyboardModule {

    @Provides
    @Singleton
    fun provideKeyboardFieldClassifier(): KeyboardFieldClassifier {
        return KeyboardFieldClassifier()
    }

    @Provides
    @Singleton
    fun provideSensitiveFieldPolicy(): SensitiveFieldPolicy {
        return SensitiveFieldPolicy()
    }
}
```

---

### 8.5 Kp2aModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object Kp2aModule {

    @Provides
    @Singleton
    fun provideKp2aEntryResultParser(): Kp2aEntryResultParser {
        return Kp2aEntryResultParser()
    }

    @Provides
    @Singleton
    fun provideKp2aEntryMapper(
        fieldClassifier: KeyboardFieldClassifier,
    ): Kp2aEntryMapper {
        return Kp2aEntryMapper(fieldClassifier)
    }
}
```

---

## 9. ViewModel 设计

### 9.1 KeyboardViewModel

`KeyboardViewModel` 是键盘本体状态持有者。

```kotlin
class KeyboardViewModel(
    private val sessionRepository: KeyboardSessionRepository,
    private val settingsRepository: KeyboardSettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<KeyboardUiState>

    val effect: SharedFlow<KeyboardEffect>

    fun onIntent(intent: KeyboardIntent) {
        // Handle intent.
    }
}
```

说明：

1. `KeyboardViewModel` 不直接持有 `InputConnection`。
2. `KeyboardViewModel` 不直接持有 `InputMethodService`。
3. `KeyboardViewModel` 不直接调用 `startActivity()`。
4. `KeyboardViewModel` 不把敏感 value 放入 `KeyboardUiState`。
5. `KeyboardViewModel` 可以通过 `KeyboardSessionRepository` 查询字段 value，并立即发送 `KeyboardEffect.CommitText`。

---

### 9.2 EntryPickerViewModel

```kotlin
@HiltViewModel
class EntryPickerViewModel @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
    private val resultParser: Kp2aEntryResultParser,
    private val fieldClassifier: KeyboardFieldClassifier,
) : ViewModel()
```

职责：

1. 处理 KP2A 返回结果。
2. 解析条目名称和字段。
3. 字段分类。
4. 写入 `KeyboardSessionRepository`。
5. 通知 Activity 结束。

---

### 9.3 SettingsViewModel

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: KeyboardSettingsRepository,
) : ViewModel()
```

职责：

1. 读取设置。
2. 更新设置。
3. 写入 DataStore。
4. 不接触任何敏感字段 value。

---

## 10. IME Service 设计

### 10.1 KeyboardImeService

`KeyboardImeService` 使用 Hilt 注入依赖，但不建议直接使用 `hiltViewModel()`。

原因：

1. `hiltViewModel()` 更适合 Activity / Fragment / Navigation Compose 场景。
2. `InputMethodService` 是特殊宿主。
3. P0 使用自定义 `KeyboardViewModelFactory` 更清晰可控。

示例：

```kotlin
@AndroidEntryPoint
class KeyboardImeService : InputMethodService() {

    @Inject
    lateinit var viewModelFactory: KeyboardViewModelFactory

    private lateinit var viewModel: KeyboardViewModel

    override fun onCreate() {
        super.onCreate()
        viewModel = viewModelFactory.create()
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            setContent {
                KeyboardTheme {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    KeyboardRoot(
                        state = state,
                        onIntent = viewModel::onIntent,
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        viewModel.onIntent(KeyboardIntent.ClearEntry)
        super.onDestroy()
    }
}
```

---

### 10.2 InputConnectionDispatcher

```kotlin
class InputConnectionDispatcher(
    private val inputConnectionProvider: () -> InputConnection?,
) {
    fun commitText(text: String) {
        inputConnectionProvider()?.commitText(text, 1)
    }

    fun deleteBackward() {
        inputConnectionProvider()?.deleteSurroundingText(1, 0)
    }

    fun sendEnter() {
        val inputConnection = inputConnectionProvider() ?: return

        inputConnection.sendKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER),
        )
        inputConnection.sendKeyEvent(
            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER),
        )
    }
}
```

注意：

```text
commitText(text) 中的 text 可能是密码，禁止在这里打印日志。
```

---

## 11. Version Catalog 设计

### 11.1 `gradle/libs.versions.toml`

```toml
[versions]
agp = "9.2.0"
kotlin = "2.3.21"
ksp = "2.3.7"
hilt = "2.59.2"
composeBom = "2026.04.01"

androidxCore = "1.18.0"
activityCompose = "1.12.0"
lifecycle = "2.10.0"
datastore = "1.2.0"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "androidxCore" }

androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activityCompose" }

androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }

androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
androidx-compose-foundation = { module = "androidx.compose.foundation:foundation" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3" }
androidx-compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
androidx-compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }

hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

---

### 11.2 Root `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

---

### 11.3 App `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "io.github.togls.kp2acomposekeyboard"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.togls.kp2acomposekeyboard"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }
}

hilt {
    enableAggregatingTask = true
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

---

## 12. 安全架构

### 12.1 敏感字段处理原则

敏感字段包括但不限于：

```text
Password
TOTP
Token
Secret
Recovery Code
自定义敏感字段
```

必须禁止：

```text
写入 SharedPreferences
写入 DataStore
写入数据库
写入日志
写入崩溃上报
写入剪贴板
在 UI 显示明文
放入 KeyboardUiState
放入 SavedStateHandle
```

允许：

```text
短时间保存在 KeyboardSessionRepository 内存中
用户点击字段按钮后通过 InputConnection.commitText() 输入
超时后自动清理
手动清理
重新选择条目时覆盖旧条目
```

---

### 12.2 Repository 安全边界

```kotlin
@Singleton
class KeyboardSessionRepository @Inject constructor() {

    private val _session = MutableStateFlow<KeyboardSession?>(null)
    val session: StateFlow<KeyboardSession?> = _session.asStateFlow()

    fun setSession(session: KeyboardSession) {
        _session.value = session
    }

    fun clear() {
        _session.value = null
    }

    fun getFieldValue(fieldId: String): String? {
        return _session.value
            ?.fields
            ?.firstOrNull { it.id == fieldId }
            ?.value
    }

    fun getSnapshot(): KeyboardSessionSnapshot? {
        val currentSession = _session.value ?: return null

        return KeyboardSessionSnapshot(
            entryName = currentSession.entryName,
            fixedFields = currentSession.fixedFieldsWithoutValue(),
            extraFields = currentSession.extraFieldsWithoutValue(),
            allFields = currentSession.allFieldsWithoutValue(),
        )
    }
}
```

---

## 13. UI 架构

### 13.1 KeyboardRoot

```kotlin
@Composable
fun KeyboardRoot(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
) {
    when (state.mainLayout) {
        MainKeyboardLayout.Default -> {
            DefaultKeyboardLayout(
                state = state,
                onIntent = onIntent,
            )
        }

        MainKeyboardLayout.Entry -> {
            EntryKeyboardLayout(
                state = state,
                onIntent = onIntent,
            )
        }
    }
}
```

---

### 13.2 DefaultKeyboardLayout

职责：

1. 显示字母、数字、符号布局。
2. 有当前条目时显示顶部提示。
3. 底部始终显示 `[123] [符号] [空格] [选择条目] [换行]`。
4. 不直接调用 `InputConnection`。

---

### 13.3 EntryKeyboardLayout

职责：

1. 显示当前条目名称。
2. 显示固定字段。
3. 显示其余字段分页。
4. 显示展开模式。
5. 显示底部操作栏。
6. 点击字段按钮时只发送 `KeyboardIntent.CommitField(fieldId)`。

---

### 13.4 KeyboardTheme

职责：

1. Material 3 主题。
2. Android 12+ 动态颜色。
3. 深色模式。
4. 浅色模式。
5. Gboard 风格的圆角、间距、低干扰视觉层级。
6. 不使用 Gboard 商标、图标或受版权保护的资源。

---

## 14. 设置架构

### 14.1 KeyboardSettings

```kotlin
data class KeyboardSettings(
    val sessionTimeoutSeconds: Int = 60,
    val clearAfterPasswordCommit: Boolean = false,
    val enableHapticFeedback: Boolean = false,
    val keyboardHeightLevel: Int = 1,
    val keyDensityLevel: Int = 1,
    val useDynamicColor: Boolean = true,
)
```

---

### 14.2 KeyboardSettingsRepository

```kotlin
class KeyboardSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val settings: Flow<KeyboardSettings>

    suspend fun updateSessionTimeoutSeconds(value: Int)

    suspend fun updateClearAfterPasswordCommit(value: Boolean)

    suspend fun updateEnableHapticFeedback(value: Boolean)

    suspend fun updateUseDynamicColor(value: Boolean)
}
```

设置允许持久化，因为它们不包含敏感字段 value。

---

## 15. P0 实施顺序

### 阶段 1：工程基础

```text
1. 创建 AGP 9.x 项目
2. 配置 Version Catalog
3. 接入 Compose
4. 接入 Hilt
5. 接入 KSP
6. 创建 Application 并添加 @HiltAndroidApp
```

---

### 阶段 2：IME 骨架

```text
1. 创建 KeyboardImeService
2. 注册输入法 service
3. 创建 ComposeView
4. 显示静态键盘
5. 实现 InputConnectionDispatcher
6. 支持字母输入、删除、空格、换行
```

---

### 阶段 3：MVI 键盘状态

```text
1. 创建 KeyboardUiState
2. 创建 KeyboardIntent
3. 创建 KeyboardEffect
4. 创建 KeyboardViewModel
5. 默认布局支持 Letters / Numbers / Symbols
6. 支持大小写切换
```

---

### 阶段 4：假数据条目布局

```text
1. 创建 fake KeyboardSession
2. 显示条目布局
3. 显示固定字段 Username / Password / TOTP
4. 显示其余字段
5. 实现分页模式
6. 实现展开模式
7. 实现切换默认布局和切回条目布局
```

---

### 阶段 5：Session 安全层

```text
1. 实现 KeyboardSessionRepository
2. 实现 KeyboardSessionSnapshot
3. UiState 不再直接接触 value
4. 字段点击时通过 fieldId 查询 value
5. 实现超时清理
6. 实现手动清理
7. 实现输入法销毁清理
```

---

### 阶段 6：EntryPickerActivity + KP2A 接入

```text
1. 创建 EntryPickerActivity
2. 创建 EntryPickerViewModel
3. 启动 Keepass2Android 条目选择
4. 解析 KP2A 返回字段
5. 字段分类
6. 写入 KeyboardSessionRepository
7. 选择完成后关闭 Activity
8. IME 观察 session 后切换条目布局
```

---

### 阶段 7：SettingsActivity

```text
1. 创建 SettingsActivity
2. 创建 SettingsViewModel
3. 接入 DataStore
4. 支持超时时间设置
5. 支持动态颜色设置
6. 支持输入 Password 后自动清理设置
```

---

### 阶段 8：UI 风格与体验

```text
1. Material 3
2. 动态颜色
3. 深色模式
4. 浅色模式
5. 圆角按键
6. 按压反馈
7. 键盘高度限制
8. 展开模式内部滚动
```

---

## 16. P0 验收标准

P0 架构层面至少满足：

1. 使用 AGP 9.x。
2. 使用 Version Catalog 管理依赖版本。
3. 使用 Compose 构建键盘 UI。
4. 使用 Material 3。
5. 使用 Hilt 依赖注入。
6. 使用 KSP 编译 Hilt。
7. 键盘本体采用 MVI / UDF。
8. 键盘状态由 ViewModel 管理。
9. IME Service 不直接处理复杂业务。
10. EntryPickerActivity 负责 KP2A 条目选择。
11. SettingsActivity 负责设置。
12. KeyboardSessionRepository 只在内存中保存字段 value。
13. KeyboardUiState 不包含字段 value。
14. Compose UI 不接触字段 value。
15. 字段输入通过 InputConnection.commitText()。
16. 字段输入不经过剪贴板。
17. 敏感字段不持久化。
18. 敏感字段不写日志。
19. 条目超时后自动清理。
20. 输入法销毁时清理当前条目。

---

## 17. 后续可选演进

P1 / P2 可以考虑：

1. 拆分多 module。
2. 增加测试专用 fake Repository。
3. 增加 UI screenshot test。
4. 增加 SettingsActivity 的完整配置项。
5. 支持键盘高度自定义。
6. 支持字段搜索。
7. 支持字段分组。
8. 支持横屏布局。
9. 支持平板布局。
10. 支持输入 Password 后自动切回默认输入法提示。
11. 支持更精细的敏感字段策略。
12. 支持 crash-safe 的日志脱敏策略。

---

## 18. 架构总结

当前最终架构：

```text
kp2acomposekeyboard 使用 AGP 9.x + Version Catalog + Compose + Material 3 + Hilt + KSP 作为现代 Android 工程基线。

项目采用 Activity + IME 分离架构：
KeyboardImeService 只负责输入法宿主、Compose 键盘展示、InputConnection 输入和 Effect 执行；
EntryPickerActivity 负责 Keepass2Android 条目选择；
SettingsActivity 负责设置页。

键盘本体采用 MVI / UDF + ViewModel：
Compose UI 只渲染 KeyboardUiState 并发送 KeyboardIntent；
KeyboardViewModel 处理状态和业务决策，并发出 KeyboardEffect；
KeyboardImeService 消费 Effect 并执行平台能力。

安全边界：
敏感字段 value 只存在 KeyboardSessionRepository 的内存会话中；
不进入 UiState；
不进入 SavedStateHandle；
不进入 DataStore；
不写日志；
不经过剪贴板；
超时或输入法销毁后清理。
```
