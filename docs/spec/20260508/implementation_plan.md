# kp2acomposekeyboard 实现计划

版本：v0.1  
状态：实施计划草案  
关联文档：

- `kp2acomposekeyboard 键盘布局需求 v0.3`
- `kp2acomposekeyboard 技术架构设计 v0.1`

---

## 1. 实施目标

本实现计划用于指导 `kp2acomposekeyboard` P0 阶段开发。

P0 目标：

1. 搭建现代 Android 工程基线。
2. 实现可用的 Compose 输入法键盘。
3. 实现默认布局：字母、数字、符号、大小写、删除、空格、换行。
4. 实现条目布局：固定字段、其余字段分页、展开模式。
5. 接入 Keepass2Android 条目选择流程。
6. 点击字段按钮后，通过 `InputConnection.commitText()` 输入字段值。
7. 不通过剪贴板传递密码。
8. 不显示敏感字段真实值。
9. 不持久化敏感字段 value。
10. 支持 60 秒自动清理和输入法销毁清理。
11. 支持 Material 3、动态颜色、浅色和深色模式。
12. 保持 KISS，优先可维护，不做复杂通用输入法能力。

---

## 2. 技术范围

### 2.1 P0 包含

```text
AGP 9.x
Version Catalog
Compose
Material 3
Hilt
KSP
ViewModel
StateFlow
SharedFlow
MVI / UDF
InputMethodService
EntryPickerActivity
SettingsActivity 基础骨架
KeyboardSessionRepository
Keepass2Android Plugin API 接入
```

### 2.2 P0 不包含

```text
拼音输入
联想词
自动纠错
候选词
词典
滑行输入
语音输入
剪贴板面板
表情面板
复杂主题编辑器
复杂字段搜索
复杂字段分组
多 module 拆分
```

---

## 3. 实施原则

1. **先骨架，后业务。**
2. **先假数据，后 KP2A 接入。**
3. **先保证输入链路可靠，再优化视觉体验。**
4. **UI State 不包含敏感字段 value。**
5. **Compose UI 不直接访问 Repository / InputConnection / KP2A。**
6. **IME Service 只负责平台能力。**
7. **Activity 负责选择页和设置页。**
8. **ViewModel 负责状态管理和业务决策。**
9. **Repository 负责内存 Session。**
10. **所有敏感值禁止日志输出。**

---

## 4. 总体阶段划分

```text
阶段 0：项目准备与工程基线
阶段 1：IME 最小骨架
阶段 2：MVI / ViewModel 状态框架
阶段 3：默认布局实现
阶段 4：条目布局假数据实现
阶段 5：Session 安全层实现
阶段 6：EntryPickerActivity 与 KP2A 接入
阶段 7：SettingsActivity 与 DataStore
阶段 8：主题与视觉体验
阶段 9：测试、验收与收尾
```

---

# 阶段 0：项目准备与工程基线

## 目标

建立可长期维护的现代 Android 工程基础。

## 任务清单

### 0.1 创建项目

- [ ] 创建 Android Application 项目。
- [ ] 设置包名，例如：`io.github.togls.kp2acomposekeyboard`。
- [ ] 设置 `minSdk`。
- [ ] 设置 `targetSdk`。
- [ ] 设置 `compileSdk`。
- [ ] 配置输入法相关 Manifest 基础结构。

### 0.2 配置 Version Catalog

- [ ] 创建 `gradle/libs.versions.toml`。
- [ ] 添加 AGP 9.x。
- [ ] 添加 Kotlin 2.x。
- [ ] 添加 Compose BOM。
- [ ] 添加 Compose Compiler Plugin。
- [ ] 添加 Hilt。
- [ ] 添加 KSP。
- [ ] 添加 Lifecycle。
- [ ] 添加 Activity Compose。
- [ ] 添加 DataStore。

### 0.3 配置 Gradle

- [ ] 配置 root `build.gradle.kts`。
- [ ] 配置 app `build.gradle.kts`。
- [ ] 使用 `alias(libs.plugins.android.application)`。
- [ ] 使用 `alias(libs.plugins.compose.compiler)`。
- [ ] 使用 `alias(libs.plugins.hilt)`。
- [ ] 使用 `alias(libs.plugins.ksp)`。
- [ ] 启用 Compose。
- [ ] 添加 Compose / Material 3 / Lifecycle / Hilt / DataStore 依赖。

### 0.4 接入 Hilt

- [ ] 创建 `Kp2aComposeKeyboardApp`。
- [ ] 添加 `@HiltAndroidApp`。
- [ ] 在 Manifest 中注册 Application。
- [ ] 创建基础 `di/` 包。
- [ ] 创建 `AppModule`。
- [ ] 验证 Hilt 编译通过。

## 验收标准

- [ ] 项目可以成功 Sync。
- [ ] 项目可以成功 Build。
- [ ] Hilt 代码生成正常。
- [ ] Compose Preview 依赖正常。
- [ ] Version Catalog 生效。
- [ ] 没有硬编码依赖版本散落在 `build.gradle.kts` 中。

---

# 阶段 1：IME 最小骨架

## 目标

实现最小可运行输入法，可以被系统识别并显示一个 Compose 键盘 View。

## 任务清单

### 1.1 创建输入法 Service

- [ ] 创建 `KeyboardImeService : InputMethodService`。
- [ ] 在 Manifest 中注册 service。
- [ ] 添加 `android.view.InputMethod` metadata。
- [ ] 创建 `res/xml/method.xml`。
- [ ] 确认系统输入法列表中能看到应用。

### 1.2 创建 Compose 输入视图

- [ ] 在 `onCreateInputView()` 中返回 `ComposeView`。
- [ ] 创建 `KeyboardInputView`。
- [ ] 显示一个静态测试布局。
- [ ] 验证输入法弹出时 UI 正常显示。
- [ ] 验证输入法切换、隐藏、重新显示不会崩溃。

### 1.3 创建 InputConnectionDispatcher

- [ ] 创建 `InputConnectionDispatcher`。
- [ ] 封装 `commitText(text)`。
- [ ] 封装 `deleteBackward()`。
- [ ] 封装 `sendEnter()`。
- [ ] 禁止在这些方法中打印 text value。

### 1.4 最小输入验证

- [ ] 添加一个测试按钮 `[a]`。
- [ ] 点击后调用 `commitText("a")`。
- [ ] 添加 `[⌫]`。
- [ ] 添加 `[空格]`。
- [ ] 添加 `[换行]`。

## 验收标准

- [ ] 系统能启用该输入法。
- [ ] 输入法能显示 Compose View。
- [ ] 点击 `[a]` 可以输入字符。
- [ ] 点击 `[⌫]` 可以删除字符。
- [ ] 点击 `[空格]` 可以输入空格。
- [ ] 点击 `[换行]` 可以发送 Enter。
- [ ] 输入法销毁、重建不崩溃。

---

# 阶段 2：MVI / ViewModel 状态框架

## 目标

建立键盘本体的 MVI / UDF 状态管理框架。

## 任务清单

### 2.1 创建状态模型

- [ ] 创建 `MainKeyboardLayout`。
- [ ] 创建 `DefaultInputMode`。
- [ ] 创建 `EntryFieldDisplayMode`。
- [ ] 创建 `KeyboardUiState`。
- [ ] 创建 `KeyboardIntent`。
- [ ] 创建 `KeyboardEffect`。
- [ ] 创建 `ScrollDirection`。

### 2.2 创建 KeyboardViewModel

- [ ] 创建 `KeyboardViewModel`。
- [ ] 使用 `MutableStateFlow<KeyboardUiState>`。
- [ ] 对外暴露 `StateFlow<KeyboardUiState>`。
- [ ] 使用 `MutableSharedFlow<KeyboardEffect>`。
- [ ] 对外暴露 `SharedFlow<KeyboardEffect>`。
- [ ] 创建 `onIntent(intent)` 入口。
- [ ] 先支持 `CommitText`、`DeleteBackward`、`Enter`。

### 2.3 创建 KeyboardViewModelFactory

- [ ] 创建 `KeyboardViewModelFactory`。
- [ ] 使用 Hilt 注入 Factory。
- [ ] `KeyboardImeService` 中通过 Factory 创建 `KeyboardViewModel`。
- [ ] 避免在 IME 中直接使用 `hiltViewModel()`。

### 2.4 IME 收集 State 和 Effect

- [ ] `ComposeView` 中收集 `uiState`。
- [ ] 将 `state` 传给 `KeyboardRoot`。
- [ ] 将 `viewModel::onIntent` 传给 UI。
- [ ] 在 IME Service 中收集 `effect`。
- [ ] 根据 `effect` 调用 `InputConnectionDispatcher`。
- [ ] 根据 `effect` 启动 Activity。

## 验收标准

- [ ] 所有按键事件都通过 `KeyboardIntent` 进入 ViewModel。
- [ ] 所有输入动作都通过 `KeyboardEffect` 返回 IME 执行。
- [ ] Compose UI 不直接调用 `InputConnection`。
- [ ] IME Service 不直接管理键盘 UI 状态。
- [ ] MVI 数据流可以跑通。

---

# 阶段 3：默认布局实现

## 目标

实现默认键盘布局：字母、数字、符号、大小写切换、删除、空格、选择条目、换行。

## 任务清单

### 3.1 创建 UI 组件

- [ ] 创建 `KeyboardRoot`。
- [ ] 创建 `DefaultKeyboardLayout`。
- [ ] 创建 `LetterKeyboard`。
- [ ] 创建 `NumberKeyboard`。
- [ ] 创建 `SymbolKeyboard`。
- [ ] 创建 `KeyboardKey`。
- [ ] 创建 `ExistingEntryHint`。

### 3.2 字母输入模式

- [ ] 实现 qwerty 字母布局。
- [ ] 实现字母点击输入。
- [ ] 实现 `⇧` 大小写切换。
- [ ] 实现 `⌫` 删除。
- [ ] 底部显示 `[123] [符号] [空格] [选择条目] [换行]`。
- [ ] 点击 `[123]` 切换到数字模式。
- [ ] 点击 `[符号]` 切换到符号模式。

### 3.3 数字输入模式

- [ ] 实现数字键布局。
- [ ] 实现常用标点。
- [ ] 点击 `[ABC]` 切回字母模式。
- [ ] 点击 `[符号]` 切换符号模式。
- [ ] 支持删除、空格、选择条目、换行。

### 3.4 符号输入模式

- [ ] 实现符号键布局。
- [ ] 点击 `[ABC]` 切回字母模式。
- [ ] 点击 `[123]` 切换数字模式。
- [ ] 支持删除、空格、选择条目、换行。

### 3.5 选择条目入口

- [ ] 点击 `[选择条目]` 发送 `KeyboardIntent.SelectEntry`。
- [ ] ViewModel 发出 `KeyboardEffect.LaunchEntryPicker`。
- [ ] IME Service 暂时可以先打印安全事件或打开空 Activity。
- [ ] 后续阶段接入真正 EntryPickerActivity。

## 验收标准

- [ ] 默认布局完整显示。
- [ ] 底部按钮始终固定为 `[123] [符号] [空格] [选择条目] [换行]`。
- [ ] 字母输入正常。
- [ ] 大小写切换正常。
- [ ] 数字输入正常。
- [ ] 符号输入正常。
- [ ] 删除、空格、换行正常。
- [ ] UI 不出现候选词、联想词、拼音相关能力。

---

# 阶段 4：条目布局假数据实现

## 目标

在未接入 KP2A 前，用假数据实现条目布局，先验证 UI 状态和交互闭环。

## 任务清单

### 4.1 创建领域模型

- [ ] 创建 `KeyboardFieldType`。
- [ ] 创建 `KeyboardField`。
- [ ] 创建 `KeyboardFieldUiModel`。
- [ ] 创建 `KeyboardSession`。
- [ ] 创建 `KeyboardSessionSnapshot`。

### 4.2 创建条目布局组件

- [ ] 创建 `EntryKeyboardLayout`。
- [ ] 创建 `EntryHeader`。
- [ ] 创建 `FixedFieldRow`。
- [ ] 创建 `ExtraFieldPagedPanel`。
- [ ] 创建 `AllFieldsExpandedPanel`。
- [ ] 创建 `EntryActionRows`。
- [ ] 创建 `FieldButton`。

### 4.3 分页模式

- [ ] 显示当前条目名称。
- [ ] 显示固定字段：Username、Password、TOTP。
- [ ] 固定字段不存在时不占位。
- [ ] 显示其余字段。
- [ ] 每页显示 3 个其余字段。
- [ ] 实现 `[prev]`。
- [ ] 实现 `[next]`。
- [ ] 实现 `[全部]`。
- [ ] 实现 `[切换默认布局]`。
- [ ] 实现 `[选择条目]`。
- [ ] 实现 `[⌫]`。

### 4.4 展开模式

- [ ] 点击 `[全部]` 进入展开模式。
- [ ] 按顺序显示全部字段。
- [ ] 字段区域内部滚动。
- [ ] 底部显示 `[prev] [next] [收起]`。
- [ ] 底部显示 `[切换默认布局] [选择条目] [⌫]`。
- [ ] 点击 `[收起]` 返回分页模式。
- [ ] 展开模式字段过多时，键盘高度不能无限增加。

### 4.5 默认布局顶部提示

- [ ] 有当前条目且处于默认布局时，显示 `ExistingEntryHint`。
- [ ] 显示 `当前已有条目：{entryName}`。
- [ ] 点击 `[切回条目布局]` 返回条目布局。
- [ ] 不重新查询 KP2A。
- [ ] 不清除当前条目。

## 验收标准

- [ ] 可以通过假数据进入条目布局。
- [ ] 分页模式显示正确。
- [ ] 展开模式显示正确。
- [ ] 分页和展开可以互相切换。
- [ ] 条目布局可以切换到默认布局。
- [ ] 默认布局可以切回条目布局。
- [ ] 字段按钮只显示 label，不显示 value。
- [ ] Password / TOTP 不显示真实值。
- [ ] 字段过多时键盘高度不无限增长。

---

# 阶段 5：Session 安全层实现

## 目标

实现真正的内存 Session 管理，建立敏感字段安全边界。

## 任务清单

### 5.1 创建 KeyboardSessionRepository

- [ ] 使用 `@Singleton`。
- [ ] 使用 `MutableStateFlow<KeyboardSession?>`。
- [ ] 对外暴露 `StateFlow<KeyboardSession?>`。
- [ ] 实现 `setSession(session)`。
- [ ] 实现 `clear()`。
- [ ] 实现 `getFieldValue(fieldId)`。
- [ ] 实现 `getSnapshot()`。
- [ ] `getSnapshot()` 不返回任何 value。

### 5.2 Session Snapshot

- [ ] 将 `KeyboardSession` 转成 `KeyboardSessionSnapshot`。
- [ ] Snapshot 只包含 entryName 和字段 UI 模型。
- [ ] Snapshot 不包含字段 value。
- [ ] ViewModel 只从 Snapshot 构造 `KeyboardUiState`。

### 5.3 ViewModel 观察 Session

- [ ] `KeyboardViewModel` 观察 `sessionRepository.session`。
- [ ] 有 Session 时更新为条目布局。
- [ ] 无 Session 时更新为默认布局。
- [ ] 保留默认布局顶部提示逻辑。
- [ ] Session 更新时重置分页页码。
- [ ] Session 更新时字段显示模式重置为分页模式。

### 5.4 字段输入

- [ ] 点击字段按钮发送 `KeyboardIntent.CommitField(fieldId)`。
- [ ] ViewModel 通过 `sessionRepository.getFieldValue(fieldId)` 获取 value。
- [ ] 获取成功后发送 `KeyboardEffect.CommitText(value)`。
- [ ] 获取失败时忽略或发送安全提示 Effect。
- [ ] 输入 Password 后可预留自动清理策略。

### 5.5 超时清理

- [ ] 创建 `SessionTimeoutController`。
- [ ] 默认 60 秒清理。
- [ ] 支持重新选择条目后重新计时。
- [ ] 支持输入法销毁时清理。
- [ ] 支持手动清理。
- [ ] 超时后 ViewModel 更新回默认布局。

### 5.6 安全日志

- [ ] 创建 `SecureLog`。
- [ ] 禁止打印 field value。
- [ ] 禁止打印 commitText text。
- [ ] 只允许打印安全事件，例如 `Session created`、`Session cleared`。
- [ ] Debug 日志也不能包含敏感 value。

## 验收标准

- [ ] `KeyboardUiState` 不包含字段 value。
- [ ] Compose UI 不接触字段 value。
- [ ] Password / TOTP 不显示真实值。
- [ ] 字段 value 不写 DataStore。
- [ ] 字段 value 不写 SharedPreferences。
- [ ] 字段 value 不写日志。
- [ ] 字段输入不经过剪贴板。
- [ ] 60 秒后 Session 自动清理。
- [ ] 输入法销毁时 Session 清理。
- [ ] 重新选择条目覆盖旧 Session。

---

# 阶段 6：EntryPickerActivity 与 KP2A 接入

## 目标

实现真正的 Keepass2Android 条目选择流程。

## 任务清单

### 6.1 创建 EntryPickerActivity

- [ ] 创建 `EntryPickerActivity : ComponentActivity`。
- [ ] 添加 `@AndroidEntryPoint`。
- [ ] 创建 `EntryPickerViewModel`。
- [ ] 使用 `@HiltViewModel`。
- [ ] 创建基本 Compose UI，可显示选择中、失败、取消状态。

### 6.2 启动 EntryPickerActivity

- [ ] `KeyboardViewModel` 处理 `SelectEntry`。
- [ ] 发送 `KeyboardEffect.LaunchEntryPicker`。
- [ ] `KeyboardImeService` 收到 Effect 后启动 `EntryPickerActivity`。
- [ ] 启动时添加合适 Intent flags。
- [ ] 避免输入法上下文启动 Activity 崩溃。

### 6.3 KP2A Contract

- [ ] 创建 `Kp2aContract`。
- [ ] 定义 KP2A action。
- [ ] 定义 request / result extra。
- [ ] 封装 KP2A 选择条目 Intent。
- [ ] 封装结果判断。

### 6.4 KP2A 启动与返回

- [ ] 在 `EntryPickerActivity` 中启动 KP2A。
- [ ] 使用 Activity Result API 或兼容方式接收返回。
- [ ] 用户成功选择时解析数据。
- [ ] 用户取消时不清除旧 Session。
- [ ] 用户失败时显示错误或关闭页面。
- [ ] 成功写入 Session 后 finish。

### 6.5 解析与映射

- [ ] 实现 `Kp2aEntryResultParser`。
- [ ] 提取 entryId。
- [ ] 提取 entryName。
- [ ] 提取字段 key / label / value。
- [ ] 实现 `KeyboardFieldClassifier`。
- [ ] 实现固定字段识别。
- [ ] 实现敏感字段识别。
- [ ] 实现字段排序。
- [ ] 创建 `KeyboardSession`。
- [ ] 写入 `KeyboardSessionRepository`。

### 6.6 返回键盘体验

- [ ] 选择条目完成后关闭 `EntryPickerActivity`。
- [ ] 用户回到目标输入框。
- [ ] IME 观察到 Session 变化。
- [ ] 自动显示条目布局。
- [ ] 分页页码重置为第一页。
- [ ] 字段显示模式重置为分页模式。

## 验收标准

- [ ] 点击 `[选择条目]` 可以打开 KP2A 选择流程。
- [ ] 用户选择条目后回到键盘。
- [ ] 键盘显示条目布局。
- [ ] 当前条目名称正确显示。
- [ ] 固定字段正确显示。
- [ ] 其余字段正确显示。
- [ ] 点击字段可以输入对应 value。
- [ ] 用户取消选择时保持原状态。
- [ ] 重新选择条目会覆盖旧 Session。
- [ ] 敏感字段 value 不进入 UI State。
- [ ] 敏感字段 value 不写日志。

---

# 阶段 7：SettingsActivity 与 DataStore

## 目标

实现基础设置页，用于管理非敏感配置。

## 任务清单

### 7.1 创建设置模型

- [ ] 创建 `KeyboardSettings`。
- [ ] 添加 `sessionTimeoutSeconds`。
- [ ] 添加 `clearAfterPasswordCommit`。
- [ ] 添加 `enableHapticFeedback`。
- [ ] 添加 `keyboardHeightLevel`。
- [ ] 添加 `keyDensityLevel`。
- [ ] 添加 `useDynamicColor`。

### 7.2 创建 SettingsRepository

- [ ] 创建 `KeyboardSettingsRepository`。
- [ ] 接入 DataStore Preferences。
- [ ] 读取设置。
- [ ] 更新超时时间。
- [ ] 更新输入 Password 后自动清理。
- [ ] 更新动态颜色开关。
- [ ] 更新震动反馈开关。

### 7.3 创建 SettingsActivity

- [ ] 创建 `SettingsActivity`。
- [ ] 添加 `@AndroidEntryPoint`。
- [ ] 创建 `SettingsViewModel`。
- [ ] 创建 `SettingsUiState`。
- [ ] 创建设置页面 Compose UI。

### 7.4 设置入口

- [ ] 在键盘合适位置预留设置入口。
- [ ] 发送 `KeyboardIntent.OpenSettings`。
- [ ] ViewModel 发送 `KeyboardEffect.LaunchSettings`。
- [ ] IME Service 启动 `SettingsActivity`。

## 验收标准

- [ ] 设置页可以打开。
- [ ] 设置页可以读取默认配置。
- [ ] 设置页可以修改非敏感配置。
- [ ] 设置保存到 DataStore。
- [ ] DataStore 中不保存任何字段 value。
- [ ] 动态颜色设置可以影响主题。
- [ ] 超时时间设置可以影响 Session 清理。

---

# 阶段 8：主题与视觉体验

## 目标

实现参考 Gboard 风格的现代 Android 键盘视觉体验。

## 任务清单

### 8.1 Material 3 主题

- [ ] 创建 `KeyboardTheme`。
- [ ] 使用 Material 3。
- [ ] 支持浅色模式。
- [ ] 支持深色模式。
- [ ] 支持 Android 12+ 动态颜色。
- [ ] 不支持动态颜色时回退默认 Material 3 色彩方案。

### 8.2 按键样式

- [ ] 创建统一 `KeyboardKey`。
- [ ] 创建统一 `FieldButton`。
- [ ] 支持圆角。
- [ ] 支持合适按键间距。
- [ ] 支持按压反馈。
- [ ] 支持禁用态。
- [ ] 支持操作按钮视觉层级。
- [ ] 敏感字段按钮使用谨慎视觉样式，但不显示 value。

### 8.3 键盘高度

- [ ] 限制整体键盘高度。
- [ ] 条目字段过多时不撑高键盘。
- [ ] 展开模式字段区域内部滚动。
- [ ] 当前条目区域固定。
- [ ] 底部操作栏固定。

### 8.4 横竖屏基础适配

- [ ] 竖屏布局可用。
- [ ] 横屏不崩溃。
- [ ] 横屏可以先使用相同布局压缩显示。
- [ ] P1 再做专门横屏布局。

## 验收标准

- [ ] 键盘视觉接近现代 Android 键盘体验。
- [ ] 支持动态颜色。
- [ ] 支持浅色模式。
- [ ] 支持深色模式。
- [ ] 按键有合理间距和圆角。
- [ ] 字段按钮和普通按键视觉层级一致。
- [ ] 展开模式不会撑高键盘。
- [ ] 不使用 Gboard 商标、图标或受版权保护资源。

---

# 阶段 9：测试、验收与收尾

## 目标

完成 P0 验收，确保输入链路、安全边界和主要状态切换可靠。

## 任务清单

### 9.1 单元测试

- [ ] 测试 `KeyboardFieldClassifier`。
- [ ] 测试固定字段识别。
- [ ] 测试敏感字段识别。
- [ ] 测试字段排序。
- [ ] 测试 `KeyboardSessionRepository`。
- [ ] 测试 Session Snapshot 不包含 value。
- [ ] 测试 `KeyboardViewModel` 状态切换。
- [ ] 测试分页 prev / next。
- [ ] 测试展开 / 收起。

### 9.2 手动测试

- [ ] 系统启用输入法。
- [ ] 输入法弹出和隐藏。
- [ ] 默认布局输入。
- [ ] 数字布局输入。
- [ ] 符号布局输入。
- [ ] 大小写切换。
- [ ] 删除、空格、换行。
- [ ] 选择 KP2A 条目。
- [ ] 取消选择 KP2A 条目。
- [ ] 重新选择条目。
- [ ] 点击 Username。
- [ ] 点击 Password。
- [ ] 点击 TOTP。
- [ ] 字段分页。
- [ ] 展开模式滚动。
- [ ] 超时清理。
- [ ] 输入法销毁清理。
- [ ] 深色模式。
- [ ] 动态颜色。

### 9.3 安全检查

- [ ] 全局搜索 `Log.`。
- [ ] 全局搜索 `println`。
- [ ] 全局搜索 `field.value`。
- [ ] 全局搜索 `password` 日志。
- [ ] 检查 DataStore 写入项。
- [ ] 检查 SharedPreferences 使用。
- [ ] 检查 SavedStateHandle 使用。
- [ ] 确认敏感字段不进入 UI State。
- [ ] 确认敏感字段不显示到 UI。
- [ ] 确认字段输入不经过剪贴板。

### 9.4 文档收尾

- [ ] 更新 README。
- [ ] 更新架构文档。
- [ ] 更新需求文档。
- [ ] 添加已知限制。
- [ ] 添加安全说明。
- [ ] 添加测试设备说明。
- [ ] 添加不保证适配所有 ROM 的声明。

## 验收标准

- [ ] P0 功能清单全部完成。
- [ ] P0 安全要求全部满足。
- [ ] 主流程无崩溃。
- [ ] 字段输入可用。
- [ ] Session 清理可靠。
- [ ] UI 状态切换可靠。
- [ ] 代码结构符合当前架构设计。
- [ ] 文档同步更新。

---

## 5. 关键实现顺序建议

实际开发时建议严格按以下顺序推进：

```text
1. 工程基线
2. IME 骨架
3. MVI 状态
4. 默认键盘
5. 条目布局假数据
6. Session Repository
7. 字段安全输入
8. KP2A 接入
9. 设置页
10. 主题体验
11. 测试和收尾
```

不要一开始就接入 KP2A。  
原因：

1. 输入法生命周期本身已经复杂。
2. Compose 键盘 UI 需要先稳定。
3. MVI 状态流需要先跑通。
4. 假数据更容易验证分页、展开、状态切换。
5. KP2A 接入失败时不会干扰基础键盘开发。

---

## 6. 风险点与处理策略

### 6.1 IME 中启动 Activity

风险：

```text
InputMethodService 不是普通 Activity Context。
启动 EntryPickerActivity 需要合适的 Intent flag。
```

处理策略：

```text
1. 统一由 ImeEffectHandler 处理 LaunchEntryPicker。
2. 使用 FLAG_ACTIVITY_NEW_TASK。
3. 对异常做安全日志。
4. 不在 ViewModel 中直接 startActivity。
```

---

### 6.2 Compose + InputMethodService 生命周期

风险：

```text
IME 生命周期和 Activity 不同。
ComposeView 重建、输入法切换、横竖屏可能导致状态异常。
```

处理策略：

```text
1. KeyboardViewModel 由 KeyboardViewModelFactory 创建。
2. Session 存在 Singleton Repository 中。
3. IME 销毁时清理 Session。
4. Compose UI 不直接持有敏感状态。
```

---

### 6.3 敏感字段泄漏

风险：

```text
密码可能进入日志、UI State、DataStore、崩溃上报、调试输出。
```

处理策略：

```text
1. UiState 只使用 KeyboardFieldUiModel。
2. KeyboardFieldUiModel 不包含 value。
3. Repository 提供 Snapshot，不返回 value。
4. commitText 不打印 text。
5. 增加安全检查清单。
6. Debug 也不打印 value。
```

---

### 6.4 KP2A 返回数据格式不稳定

风险：

```text
不同字段名、大小写、自定义字段可能不一致。
```

处理策略：

```text
1. Kp2aEntryResultParser 隔离外部协议。
2. KeyboardFieldClassifier 统一分类。
3. 字段识别允许大小写差异。
4. 未识别字段归为 Custom。
5. 字段 label 永远可显示，value 只在点击时输入。
```

---

### 6.5 键盘高度被字段撑高

风险：

```text
字段数量多时展开模式可能导致键盘无限增高。
```

处理策略：

```text
1. EntryKeyboardLayout 固定整体高度。
2. AllFieldsExpandedPanel 内部滚动。
3. Header 固定。
4. Bottom action rows 固定。
5. 只滚动字段区域。
```

---

## 7. 推荐分支策略

P0 可以按阶段分支开发：

```text
feat/project-baseline
feat/ime-skeleton
feat/keyboard-mvi
feat/default-keyboard
feat/entry-layout-fake-data
feat/session-security
feat/kp2a-entry-picker
feat/settings
feat/keyboard-theme
test/p0-validation
```

每个阶段合并前至少满足：

```text
1. 能编译
2. 能运行
3. 不破坏已有主流程
4. 不引入敏感日志
5. 当前阶段验收标准通过
```

---

## 8. 推荐 Commit 粒度

示例：

```text
build: configure agp 9 version catalog and compose
build: add hilt and ksp setup
feat: add keyboard ime service skeleton
feat: add input connection dispatcher
feat: introduce keyboard mvi state model
feat: implement default letter keyboard layout
feat: add number and symbol keyboard layouts
feat: add fake entry keyboard layout
feat: add keyboard session repository
feat: commit field values through input connection
feat: add entry picker activity skeleton
feat: integrate keepass2android entry result parser
feat: add keyboard settings datastore
feat: add material3 dynamic keyboard theme
test: add keyboard field classifier tests
test: add keyboard session repository tests
docs: add p0 implementation notes
```

---

## 9. P0 完成定义

P0 完成时，应满足：

```text
1. 输入法可以被系统启用。
2. 默认键盘可正常输入字母、数字、符号。
3. 删除、空格、换行可用。
4. 可以打开 Keepass2Android 选择条目。
5. 选择条目后键盘切换到条目布局。
6. 条目布局显示当前条目名称。
7. 固定字段和其余字段显示正确。
8. 分页模式可用。
9. 展开模式可用。
10. 字段点击后可以输入对应 value。
11. Password / TOTP 不显示真实值。
12. 字段输入不经过剪贴板。
13. 字段 value 不持久化。
14. 字段 value 不写日志。
15. 条目 60 秒后自动清理。
16. 输入法销毁时清理 Session。
17. 支持 Material 3。
18. 支持动态颜色。
19. 支持浅色和深色模式。
20. 架构符合 Activity + IME 分离、MVI/ViewModel、Hilt、Version Catalog 设计。
```

---

## 10. P0 后续建议

P0 完成后再考虑：

```text
1. 长按删除连续删除。
2. 手动清除条目按钮。
3. 输入 Password 后自动清理。
4. 字段搜索。
5. 字段分组。
6. 键盘高度设置。
7. 按键密度设置。
8. 震动反馈。
9. 横屏布局。
10. 平板布局。
11. UI Screenshot Test。
12. 多 module 拆分。
```
