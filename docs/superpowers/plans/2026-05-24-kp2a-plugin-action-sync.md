# KP2A Plugin Action Session Sync Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 接入 KP2A plugin action 广播，让键盘 session 跟随 KP2A 打开、修改、关闭条目和锁库事件同步，并把自动清空改为较长兜底。

**Architecture:** 新增薄 receiver 适配 KP2A 广播，实际同步逻辑放入可单元测试的 `Kp2aEntrySyncHandler`。handler 复用现有 parser、mapper 和 in-memory session repository，UI 继续只观察安全 snapshot，敏感值不进入 UI state、日志或持久化。

**Tech Stack:** Kotlin, Android BroadcastReceiver, Keepass2Android Plugin SDK2, Hilt EntryPoint, JUnit, Robolectric, Gradle.

---

## 文件结构

- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aEntrySyncHandler.kt`
  - 负责解析 KP2A action intent、替换或清空 `KeyboardSessionRepository`、做 entryId 匹配、输出安全日志。
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginActionReceiver.kt`
  - 继承 `PluginActionBroadcastReceiver`，用 Hilt entry point 获取 handler，按 action 路由事件。
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginScopes.kt`
  - 统一 KP2A plugin access scopes，避免 access receiver 与 token 检查逻辑漂移。
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/session/KeyboardSessionRepository.kt`
  - 增加只读当前 session accessor，供 sync handler 做匹配判断。
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginAccessReceiver.kt`
  - 改为使用统一 scopes，并包含 `SCOPE_DATABASE_ACTIONS`。
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginAccess.kt`
  - token 检查改为使用统一 scopes。
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/session/SessionTimeoutController.kt`
  - 将运行时兜底 timeout 从 60 秒改为 300 秒。
- Modify: `app/src/main/AndroidManifest.xml`
  - 注册 KP2A plugin action receiver 和 action intent-filter。
- Test: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aEntrySyncHandlerTest.kt`
  - 覆盖 open、modified、close、lock、close database、open/unlock database 语义。
- Test: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginScopesTest.kt`
  - 覆盖 database action scope 纳入授权范围。
- Test: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/application/session/SessionTimeoutControllerTest.kt`
  - 覆盖 300 秒兜底 timeout 常量。
- Modify: `docs/architecture.md`
  - 增加 plugin action sync flow。
- Modify: `docs/security.md`
  - 更新 session cleanup trigger 和 timeout 说明。
- Modify: `docs/testing.md`
  - 增加 plugin action sync 测试项。

## Task 1: 为 KP2A action session 同步写失败测试

**Files:**
- Create: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aEntrySyncHandlerTest.kt`
- Implement in Task 2: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/session/KeyboardSessionRepository.kt`
- Implement in Task 2: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aEntrySyncHandler.kt`

- [ ] **Step 1: 写失败测试**

创建 `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aEntrySyncHandlerTest.kt`：

```kotlin
package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Intent
import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.domain.policy.KeyboardFieldClassifier
import io.github.togls.kp2acomposekeyboard.domain.policy.SensitiveFieldPolicy
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import keepass2android.pluginsdk.KeepassDefs
import keepass2android.pluginsdk.Strings
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Kp2aEntrySyncHandlerTest {

    @Test
    fun openEntry_createsSessionFromValidOutput() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)

        handler.openEntry(entryIntent(entryId = "entry-1", username = "open-user"))

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("Example Entry", session?.entryName)
        assertEquals("open-user", session?.usernameValue())
    }

    @Test
    fun openEntry_keepsPreviousSessionWhenOutputIsInvalid() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.openEntry(
            Intent().putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, "{ invalid json"),
        )

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("previous-user", session?.usernameValue())
    }

    @Test
    fun entryOutputModified_createsSessionWhenNoActiveSessionExists() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)

        handler.entryOutputModified(entryIntent(entryId = "entry-1", username = "modified-user"))

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("modified-user", session?.usernameValue())
    }

    @Test
    fun entryOutputModified_replacesSessionWhenEntryIdsMatch() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.entryOutputModified(entryIntent(entryId = "entry-1", username = "updated-user"))

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("updated-user", session?.usernameValue())
    }

    @Test
    fun entryOutputModified_replacesSessionWhenCurrentSessionHasNoEntryId() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = null, username = "previous-user"))

        handler.entryOutputModified(entryIntent(entryId = "entry-1", username = "updated-user"))

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("updated-user", session?.usernameValue())
    }

    @Test
    fun entryOutputModified_ignoresDifferentEntryId() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.entryOutputModified(entryIntent(entryId = "entry-2", username = "other-user"))

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("previous-user", session?.usernameValue())
    }

    @Test
    fun entryOutputModified_keepsPreviousSessionWhenOutputIsInvalid() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.entryOutputModified(
            Intent().putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, "{ invalid json"),
        )

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("previous-user", session?.usernameValue())
    }

    @Test
    fun closeEntryView_clearsWhenEntryIdsMatch() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.closeEntryView(closedEntryId = "entry-1")

        assertNull(repository.currentSession())
    }

    @Test
    fun closeEntryView_clearsWhenCurrentSessionHasNoEntryId() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = null, username = "previous-user"))

        handler.closeEntryView(closedEntryId = "entry-1")

        assertNull(repository.currentSession())
    }

    @Test
    fun closeEntryView_clearsWhenClosedEntryIdIsMissing() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.closeEntryView(closedEntryId = null)

        assertNull(repository.currentSession())
    }

    @Test
    fun closeEntryView_ignoresDifferentEntryId() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.closeEntryView(closedEntryId = "entry-2")

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("previous-user", session?.usernameValue())
    }

    @Test
    fun lockDatabase_clearsSessionUnconditionally() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.lockDatabase()

        assertNull(repository.currentSession())
    }

    @Test
    fun closeDatabase_clearsSessionUnconditionally() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.closeDatabase()

        assertNull(repository.currentSession())
    }

    @Test
    fun openDatabase_doesNotCreateSession() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)

        handler.openDatabase()

        assertNull(repository.currentSession())
    }

    @Test
    fun unlockDatabase_doesNotCreateSession() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)

        handler.unlockDatabase()

        assertNull(repository.currentSession())
    }

    private fun createHandler(
        repository: KeyboardSessionRepository,
    ): Kp2aEntrySyncHandler {
        return Kp2aEntrySyncHandler(
            sessionRepository = repository,
            resultParser = Kp2aEntryResultParser(),
            entryMapper = Kp2aEntryMapper(
                fieldClassifier = KeyboardFieldClassifier(),
                sensitiveFieldPolicy = SensitiveFieldPolicy(),
            ),
        )
    }

    private fun KeyboardSession.usernameValue(): String? {
        return fields.firstOrNull { field ->
            field.key == KeepassDefs.UserNameField
        }?.value
    }

    private fun entryIntent(
        entryId: String?,
        username: String,
    ): Intent {
        val fields = mapOf(
            KeepassDefs.TitleField to "Example Entry",
            KeepassDefs.UserNameField to username,
            KeepassDefs.PasswordField to "password-value",
        )

        return Intent().apply {
            if (entryId != null) {
                putExtra(Strings.EXTRA_ENTRY_ID, entryId)
            }
            putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, JSONObject(fields).toString())
            putExtra(
                Strings.EXTRA_PROTECTED_FIELDS_LIST,
                JSONArray(listOf(KeepassDefs.PasswordField)).toString(),
            )
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*Kp2aEntrySyncHandlerTest"
```

Expected: FAIL，原因包含 `Unresolved reference 'Kp2aEntrySyncHandler'` 和 `Unresolved reference 'currentSession'`。

## Task 2: 实现 session repository accessor 与 sync handler

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/session/KeyboardSessionRepository.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aEntrySyncHandler.kt`
- Test: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aEntrySyncHandlerTest.kt`

- [ ] **Step 1: 给 repository 增加当前 session accessor**

在 `KeyboardSessionRepository` 的 `clear()` 和 `getFieldValue()` 之间加入：

```kotlin
    fun currentSession(): KeyboardSession? {
        return _session.value
    }
```

- [ ] **Step 2: 新增 sync handler**

创建 `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aEntrySyncHandler.kt`：

```kotlin
package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Intent
import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import javax.inject.Inject

class Kp2aEntrySyncHandler @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
    private val resultParser: Kp2aEntryResultParser,
    private val entryMapper: Kp2aEntryMapper,
) {

    fun openEntry(intent: Intent) {
        val parsedEntry = parseEntry(intent)

        if (parsedEntry == null) {
            logSyncResult(source = SOURCE_OPEN_ENTRY, result = RESULT_IGNORED)
            return
        }

        sessionRepository.setSession(parsedEntry.session)
        logSyncResult(
            source = SOURCE_OPEN_ENTRY,
            result = RESULT_APPLIED,
            parsedEntry = parsedEntry,
        )
    }

    fun entryOutputModified(intent: Intent) {
        val parsedEntry = parseEntry(intent)

        if (parsedEntry == null) {
            logSyncResult(source = SOURCE_ENTRY_OUTPUT_MODIFIED, result = RESULT_IGNORED)
            return
        }

        if (!shouldApplyModifiedSession(parsedEntry.session)) {
            logSyncResult(
                source = SOURCE_ENTRY_OUTPUT_MODIFIED,
                result = RESULT_IGNORED,
                parsedEntry = parsedEntry,
            )
            return
        }

        sessionRepository.setSession(parsedEntry.session)
        logSyncResult(
            source = SOURCE_ENTRY_OUTPUT_MODIFIED,
            result = RESULT_APPLIED,
            parsedEntry = parsedEntry,
        )
    }

    fun closeEntryView(closedEntryId: String?) {
        val currentSession = sessionRepository.currentSession()

        if (currentSession == null) {
            logSyncResult(source = SOURCE_CLOSE_ENTRY_VIEW, result = RESULT_IGNORED)
            return
        }

        if (!shouldClearClosedSession(currentSession, closedEntryId)) {
            logSyncResult(source = SOURCE_CLOSE_ENTRY_VIEW, result = RESULT_IGNORED)
            return
        }

        sessionRepository.clear()
        logSyncResult(source = SOURCE_CLOSE_ENTRY_VIEW, result = RESULT_CLEARED)
    }

    fun lockDatabase() {
        clearForDatabaseAction(source = SOURCE_LOCK_DATABASE)
    }

    fun closeDatabase() {
        clearForDatabaseAction(source = SOURCE_CLOSE_DATABASE)
    }

    fun openDatabase() {
        logSyncResult(source = SOURCE_OPEN_DATABASE, result = RESULT_IGNORED)
    }

    fun unlockDatabase() {
        logSyncResult(source = SOURCE_UNLOCK_DATABASE, result = RESULT_IGNORED)
    }

    private fun parseEntry(intent: Intent): ParsedEntry? {
        val result = resultParser.parse(intent)
        val session = entryMapper.mapToSession(result) ?: return null
        return ParsedEntry(
            result = result,
            session = session,
        )
    }

    private fun shouldApplyModifiedSession(
        incomingSession: KeyboardSession,
    ): Boolean {
        val currentSession = sessionRepository.currentSession() ?: return true
        val currentEntryId = currentSession.entryId
        val incomingEntryId = incomingSession.entryId

        if (currentEntryId.isNullOrBlank() || incomingEntryId.isNullOrBlank()) {
            return true
        }

        return currentEntryId == incomingEntryId
    }

    private fun shouldClearClosedSession(
        currentSession: KeyboardSession,
        closedEntryId: String?,
    ): Boolean {
        val currentEntryId = currentSession.entryId

        if (currentEntryId.isNullOrBlank() || closedEntryId.isNullOrBlank()) {
            return true
        }

        return currentEntryId == closedEntryId
    }

    private fun clearForDatabaseAction(source: String) {
        sessionRepository.clear()
        logSyncResult(source = source, result = RESULT_CLEARED)
    }

    private fun logSyncResult(
        source: String,
        result: String,
        parsedEntry: ParsedEntry? = null,
    ) {
        SecureLog.d(
            message = "kp2a entry sync handled",
            "source" to source,
            "result" to result,
            "hasEntryId" to !parsedEntry?.session?.entryId.isNullOrBlank(),
            "fieldCount" to (parsedEntry?.result?.fields?.size ?: 0),
            "protectedFieldCount" to (parsedEntry?.result?.protectedFields?.size ?: 0),
        )
    }

    private data class ParsedEntry(
        val result: Kp2aEntryResult,
        val session: KeyboardSession,
    )

    private companion object {
        const val SOURCE_OPEN_ENTRY = "open_entry"
        const val SOURCE_ENTRY_OUTPUT_MODIFIED = "entry_output_modified"
        const val SOURCE_CLOSE_ENTRY_VIEW = "close_entry_view"
        const val SOURCE_LOCK_DATABASE = "lock_database"
        const val SOURCE_CLOSE_DATABASE = "close_database"
        const val SOURCE_OPEN_DATABASE = "open_database"
        const val SOURCE_UNLOCK_DATABASE = "unlock_database"

        const val RESULT_APPLIED = "applied"
        const val RESULT_CLEARED = "cleared"
        const val RESULT_IGNORED = "ignored"
    }
}
```

- [ ] **Step 3: 运行 handler 测试确认通过**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*Kp2aEntrySyncHandlerTest"
```

Expected: PASS，输出包含 `BUILD SUCCESSFUL`。

- [ ] **Step 4: 提交 handler 变更**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/session/KeyboardSessionRepository.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aEntrySyncHandler.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aEntrySyncHandlerTest.kt
git commit -m "feat(kp2a): sync keyboard sessions from plugin actions"
```

## Task 3: 统一 KP2A plugin scopes 并纳入 database action scope

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginScopes.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginAccessReceiver.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginAccess.kt`
- Create: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginScopesTest.kt`

- [ ] **Step 1: 写失败测试**

创建 `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginScopesTest.kt`：

```kotlin
package io.github.togls.kp2acomposekeyboard.data.kp2a

import keepass2android.pluginsdk.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Kp2aPluginScopesTest {

    @Test
    fun requiredScopes_includeCurrentEntryQueryCredentialsAndDatabaseActions() {
        assertEquals(
            listOf(
                Strings.SCOPE_CURRENT_ENTRY,
                Strings.SCOPE_QUERY_CREDENTIALS,
                Strings.SCOPE_DATABASE_ACTIONS,
            ),
            Kp2aPluginScopes.REQUIRED_SCOPES,
        )
    }

    @Test
    fun requiredScopesForAccessManager_returnsMutableArrayListCopy() {
        val scopes = Kp2aPluginScopes.requiredScopesForAccessManager()
        scopes.add("test-scope")

        assertTrue("test-scope" !in Kp2aPluginScopes.REQUIRED_SCOPES)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*Kp2aPluginScopesTest"
```

Expected: FAIL，原因包含 `Unresolved reference 'Kp2aPluginScopes'`。

- [ ] **Step 3: 创建统一 scopes**

创建 `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginScopes.kt`：

```kotlin
package io.github.togls.kp2acomposekeyboard.data.kp2a

import keepass2android.pluginsdk.Strings

object Kp2aPluginScopes {

    val REQUIRED_SCOPES = listOf(
        Strings.SCOPE_CURRENT_ENTRY,
        Strings.SCOPE_QUERY_CREDENTIALS,
        Strings.SCOPE_DATABASE_ACTIONS,
    )

    fun requiredScopesForAccessManager(): ArrayList<String> {
        return ArrayList(REQUIRED_SCOPES)
    }
}
```

- [ ] **Step 4: 修改 access receiver 使用统一 scopes**

将 `Kp2aPluginAccessReceiver` 更新为：

```kotlin
package io.github.togls.kp2acomposekeyboard.data.kp2a

import io.github.togls.kp2acomposekeyboard.security.SecureLog
import keepass2android.pluginsdk.PluginAccessBroadcastReceiver

class Kp2aPluginAccessReceiver : PluginAccessBroadcastReceiver() {

    override fun getScopes(): ArrayList<String> {
        SecureLog.d(
            message = "kp2a access scopes requested",
            "scopeCount" to REQUIRED_SCOPES.size,
        )

        return Kp2aPluginScopes.requiredScopesForAccessManager()
    }

    companion object {
        val REQUIRED_SCOPES = Kp2aPluginScopes.REQUIRED_SCOPES
    }
}
```

- [ ] **Step 5: 修改 token 检查使用统一 scopes**

将 `Kp2aPluginAccess` 更新为：

```kotlin
package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Context
import keepass2android.pluginsdk.AccessManager

object Kp2aPluginAccess {

    fun hasRequiredAccess(context: Context): Boolean {
        return findAccessibleHostPackage(context) != null
    }

    fun findAccessibleHostPackage(context: Context): String? {
        return hostPackages.firstOrNull { hostPackage ->
            AccessManager.tryGetAccessToken(
                context,
                hostPackage,
                Kp2aPluginScopes.requiredScopesForAccessManager(),
            ) != null
        }
    }

    private val hostPackages = listOf(
        "keepass2android.keepass2android",
        "keepass2android.keepass2android_nonet",
    )
}
```

- [ ] **Step 6: 运行 scope 测试确认通过**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*Kp2aPluginScopesTest"
```

Expected: PASS，输出包含 `BUILD SUCCESSFUL`。

- [ ] **Step 7: 提交 scope 变更**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginScopes.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginAccessReceiver.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginAccess.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginScopesTest.kt
git commit -m "feat(kp2a): request database action plugin scope"
```

## Task 4: 注册并实现 KP2A plugin action receiver

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginActionReceiver.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: 创建 action receiver**

创建 `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginActionReceiver.kt`：

```kotlin
package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import keepass2android.pluginsdk.PluginActionBroadcastReceiver
import keepass2android.pluginsdk.Strings

class Kp2aPluginActionReceiver : PluginActionBroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        runCatching {
            val handler = entryPoint(context).kp2aEntrySyncHandler()
            routeAction(
                handler = handler,
                intent = intent,
            )
        }.onFailure { error ->
            SecureLog.w(
                message = "kp2a action handling failed",
                throwable = error,
                "errorType" to error::class.java.simpleName,
            )
        }
    }

    private fun routeAction(
        handler: Kp2aEntrySyncHandler,
        intent: Intent,
    ) {
        when (intent.action) {
            Strings.ACTION_OPEN_ENTRY -> handler.openEntry(intent)
            Strings.ACTION_ENTRY_OUTPUT_MODIFIED -> handler.entryOutputModified(intent)
            Strings.ACTION_CLOSE_ENTRY_VIEW -> {
                handler.closeEntryView(intent.getStringExtra(Strings.EXTRA_ENTRY_ID))
            }
            Strings.ACTION_LOCK_DATABASE -> handler.lockDatabase()
            Strings.ACTION_CLOSE_DATABASE -> handler.closeDatabase()
            Strings.ACTION_OPEN_DATABASE -> handler.openDatabase()
            Strings.ACTION_UNLOCK_DATABASE -> handler.unlockDatabase()
            else -> {
                SecureLog.d(
                    message = "kp2a action ignored",
                    "hasAction" to !intent.action.isNullOrBlank(),
                )
            }
        }
    }

    private fun entryPoint(context: Context): Kp2aPluginActionReceiverEntryPoint {
        val appContext = context.applicationContext ?: context

        return EntryPointAccessors.fromApplication(
            appContext,
            Kp2aPluginActionReceiverEntryPoint::class.java,
        )
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface Kp2aPluginActionReceiverEntryPoint {
    fun kp2aEntrySyncHandler(): Kp2aEntrySyncHandler
}
```

- [ ] **Step 2: 注册 manifest receiver**

在 `app/src/main/AndroidManifest.xml` 中现有 `Kp2aPluginAccessReceiver` 后加入：

```xml
        <receiver
            android:name=".data.kp2a.Kp2aPluginActionReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="keepass2android.ACTION_OPEN_ENTRY" />
                <action android:name="keepass2android.ACTION_ENTRY_OUTPUT_MODIFIED" />
                <action android:name="keepass2android.ACTION_CLOSE_ENTRY_VIEW" />
                <action android:name="keepass2android.ACTION_LOCK_DATABASE" />
                <action android:name="keepass2android.ACTION_CLOSE_DATABASE" />
                <action android:name="keepass2android.ACTION_OPEN_DATABASE" />
                <action android:name="keepass2android.ACTION_UNLOCK_DATABASE" />
            </intent-filter>
        </receiver>
```

- [ ] **Step 3: 编译 debug APK 验证 manifest 与 Hilt entry point**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS，输出包含 `BUILD SUCCESSFUL`。

- [ ] **Step 4: 提交 receiver 变更**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aPluginActionReceiver.kt app/src/main/AndroidManifest.xml
git commit -m "feat(kp2a): receive plugin action broadcasts"
```

## Task 5: 将自动清空改为 300 秒兜底

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/session/SessionTimeoutController.kt`
- Create: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/application/session/SessionTimeoutControllerTest.kt`

- [ ] **Step 1: 写失败测试**

创建 `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/application/session/SessionTimeoutControllerTest.kt`：

```kotlin
package io.github.togls.kp2acomposekeyboard.application.session

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionTimeoutControllerTest {

    @Test
    fun defaultTimeoutMillis_isFiveMinuteFallback() {
        assertEquals(300_000L, SessionTimeoutController.DEFAULT_TIMEOUT_MILLIS)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*SessionTimeoutControllerTest"
```

Expected: FAIL，原因是当前 `DEFAULT_TIMEOUT_MILLIS` 仍为 `60_000L`。

- [ ] **Step 3: 修改 timeout 常量与注释**

将 `SessionTimeoutController` 的 companion object 更新为：

```kotlin
    companion object {
        // KP2A close, lock, and database events are the primary cleanup path.
        const val DEFAULT_TIMEOUT_MILLIS = 300_000L
    }
```

- [ ] **Step 4: 运行 timeout 测试确认通过**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*SessionTimeoutControllerTest"
```

Expected: PASS，输出包含 `BUILD SUCCESSFUL`。

- [ ] **Step 5: 提交 timeout 变更**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/session/SessionTimeoutController.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/application/session/SessionTimeoutControllerTest.kt
git commit -m "fix(session): extend session fallback timeout"
```

## Task 6: 更新 canonical 文档

**Files:**
- Modify: `docs/architecture.md`
- Modify: `docs/security.md`
- Modify: `docs/testing.md`

- [ ] **Step 1: 更新 architecture**

在 `docs/architecture.md` 的 `Entry Selection Flow` 后加入：

````markdown
## KP2A Plugin Action Sync Flow

Keepass2Android can notify the plugin when the current entry or database state changes:

```text
Keepass2Android plugin action broadcast
    -> Kp2aPluginActionReceiver
    -> Kp2aEntrySyncHandler
    -> Kp2aEntryResultParser
    -> Kp2aEntryMapper
    -> KeyboardSessionRepository
    -> KeyboardViewModel observes Session
    -> KeyboardUiState
```

`ACTION_OPEN_ENTRY` maps full entry output into a new keyboard session. `ACTION_ENTRY_OUTPUT_MODIFIED` maps the full modified output and replaces the active session when the entry identity is matching or unavailable. `ACTION_CLOSE_ENTRY_VIEW` clears the matching active session, and missing entry identity is treated conservatively. `ACTION_LOCK_DATABASE` and `ACTION_CLOSE_DATABASE` clear the session unconditionally. `ACTION_OPEN_DATABASE` and `ACTION_UNLOCK_DATABASE` never restore entry data.

The receiver must not log raw entry JSON, field values, entry IDs, field IDs, database paths, or database display names.
````

- [ ] **Step 2: 更新 security**

在 `docs/security.md` 的 `Session Timeout` 小节替换现有默认与触发器内容为：

````markdown
## Session Timeout

Default runtime fallback timeout:

```text
300 seconds
```

Keepass2Android action broadcasts are the primary cleanup path. The timeout is a fallback for missed broadcasts or process leftovers.

Clear triggers:

- Keepass2Android closes the active entry view.
- Keepass2Android locks the database.
- Keepass2Android closes the database.
- Timeout fallback.
- Manual clear.
- Normal IME destruction.
- New successful entry selection or open-entry broadcast replacing the old session.

Do not clear the old session when:

- Launching Keepass2Android selection temporarily hides or destroys the IME.
- The user cancels entry selection.
- Entry selection fails before a new session is created.
- A modified or closed entry broadcast clearly belongs to a different entry.
````

- [ ] **Step 3: 更新 testing**

在 `docs/testing.md` 的 unit test coverage 列表中加入：

```markdown
- KP2A plugin action session synchronization.
- KP2A plugin scope registration.
- Session fallback timeout.
```

在测试命令示例区域加入：

```bash
./gradlew :app:testDebugUnitTest --tests "*Kp2aEntrySyncHandlerTest"
./gradlew :app:testDebugUnitTest --tests "*Kp2aPluginScopesTest"
./gradlew :app:testDebugUnitTest --tests "*SessionTimeoutControllerTest"
```

- [ ] **Step 4: 提交文档变更**

```bash
git add docs/architecture.md docs/security.md docs/testing.md
git commit -m "docs(kp2a): document plugin action session sync"
```

## Task 7: 全量验证与最终检查

**Files:**
- Verify all changed files from previous tasks.

- [ ] **Step 1: 运行单元测试**

Run:

```bash
./gradlew :app:testDebugUnitTest
```

Expected: PASS，输出包含 `BUILD SUCCESSFUL`。

- [ ] **Step 2: 运行 debug 构建**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS，输出包含 `BUILD SUCCESSFUL`。

- [ ] **Step 3: 检查敏感日志模式**

Run:

```bash
rg -n "Log\\.|println\\(|printStackTrace\\(|EXTRA_ENTRY_OUTPUT_DATA|EXTRA_ACCESS_TOKEN|EXTRA_DATABASE_FILEPATH|EXTRA_DATABASE_FILE_DISPLAYNAME" app/src/main/kotlin
```

Expected:

- 不出现新增的 `Log.`、`println(` 或 `printStackTrace(`。
- `EXTRA_ENTRY_OUTPUT_DATA` 只出现在 parser 或安全 intent 读取路径中。
- `EXTRA_ACCESS_TOKEN` 不出现在新增代码中。
- `EXTRA_DATABASE_FILEPATH` 和 `EXTRA_DATABASE_FILE_DISPLAYNAME` 不出现在新增代码中。

- [ ] **Step 4: 查看 git 状态**

Run:

```bash
git status --short
```

Expected: 只剩用户已有的未跟踪构建产物或空状态；不得有未提交的实现、测试或文档改动。

## 自检记录

- Spec coverage: open、modified、close、lock、close database、open/unlock database、scope、timeout、日志限制、测试和文档均有对应任务。
- Red-flag scan: 计划不包含未完成标记、延后实现占位或未定义的函数名。
- Type consistency: handler API 在测试、receiver 和实现任务中一致，方法名为 `openEntry`、`entryOutputModified`、`closeEntryView`、`lockDatabase`、`closeDatabase`、`openDatabase`、`unlockDatabase`。
