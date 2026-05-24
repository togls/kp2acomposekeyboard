package io.github.togls.kp2acomposekeyboard.feature.keyboard

import io.github.togls.kp2acomposekeyboard.application.keyboard.ClearKeyboardSessionUseCase
import io.github.togls.kp2acomposekeyboard.application.keyboard.CommitKeyboardFieldUseCase
import io.github.togls.kp2acomposekeyboard.application.keyboard.ObserveKeyboardSessionSnapshotUseCase
import io.github.togls.kp2acomposekeyboard.application.session.SessionTimeoutController
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardSubtype
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.MainKeyboardLayout

import io.github.togls.kp2acomposekeyboard.application.settings.KeyboardSettingsStore
import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class KeyboardViewModelSubtypeTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_defaultsToEntry() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(KeyboardSubtype.Entry, viewModel.uiState.value.currentSubtype)
        assertEquals(MainKeyboardLayout.Entry, viewModel.uiState.value.mainLayout)
    }

    @Test
    fun changeSubtypeToEnglish_selectsTextInput() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(KeyboardIntent.ChangeSubtype(KeyboardSubtype.EnglishUs))
        advanceUntilIdle()

        assertEquals(KeyboardSubtype.EnglishUs, viewModel.uiState.value.currentSubtype)
        assertEquals(MainKeyboardLayout.TextInput, viewModel.uiState.value.mainLayout)
    }

    @Test
    fun changeSubtypeToEntry_selectsEntryWithoutSession() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(KeyboardIntent.ChangeSubtype(KeyboardSubtype.Entry))
        advanceUntilIdle()

        assertEquals(KeyboardSubtype.Entry, viewModel.uiState.value.currentSubtype)
        assertEquals(MainKeyboardLayout.Entry, viewModel.uiState.value.mainLayout)
        assertEquals(false, viewModel.uiState.value.hasActiveSession)
    }

    @Test
    fun settingsFlow_updatesEnglishSubtypeEnabled() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore()
        val viewModel = createViewModel(settingsStore)

        settingsStore.settingsFlow.value = KeyboardSettings(englishUsSubtypeEnabled = true)
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.englishUsSubtypeEnabled)
    }

    @Test
    fun switchLanguageFromEntryWithEnglishEnabled_emitsSwitchToEnglishSubtype() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore(
            KeyboardSettings(englishUsSubtypeEnabled = true),
        )
        val viewModel = createViewModel(settingsStore)
        advanceUntilIdle()
        val effects = mutableListOf<KeyboardEffect>()
        val job = launch { viewModel.effect.collect { effect -> effects.add(effect) } }
        runCurrent()

        viewModel.onIntent(KeyboardIntent.ChangeSubtype(KeyboardSubtype.Entry))
        viewModel.onIntent(KeyboardIntent.SwitchLanguage)
        advanceUntilIdle()

        assertEquals(MainKeyboardLayout.TextInput, viewModel.uiState.value.mainLayout)
        assertEquals(listOf(KeyboardEffect.SwitchToSubtype(KeyboardSubtype.EnglishUs)), effects)
        job.cancel()
    }

    @Test
    fun switchLanguageFromEntryWithEnglishDisabled_emitsSwitchToNextInputMethod() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<KeyboardEffect>()
        val job = launch { viewModel.effect.collect { effect -> effects.add(effect) } }
        runCurrent()

        viewModel.onIntent(KeyboardIntent.ChangeSubtype(KeyboardSubtype.Entry))
        viewModel.onIntent(KeyboardIntent.SwitchLanguage)
        advanceUntilIdle()

        assertEquals(listOf(KeyboardEffect.SwitchToNextInputMethod), effects)
        job.cancel()
    }

    @Test
    fun switchLanguageFromEnglish_emitsSwitchToNextInputMethod() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore(
            KeyboardSettings(englishUsSubtypeEnabled = true),
        )
        val viewModel = createViewModel(settingsStore)
        advanceUntilIdle()
        val effects = mutableListOf<KeyboardEffect>()
        val job = launch { viewModel.effect.collect { effect -> effects.add(effect) } }
        runCurrent()

        viewModel.onIntent(KeyboardIntent.ChangeSubtype(KeyboardSubtype.EnglishUs))
        viewModel.onIntent(KeyboardIntent.SwitchLanguage)
        advanceUntilIdle()

        assertEquals(listOf(KeyboardEffect.SwitchToNextInputMethod), effects)
        job.cancel()
    }

    private fun createViewModel(
        settingsStore: FakeKeyboardSettingsStore = FakeKeyboardSettingsStore(),
    ): KeyboardViewModel {
        val sessionRepository = KeyboardSessionRepository()
        val clearKeyboardSession = ClearKeyboardSessionUseCase(sessionRepository)
        return KeyboardViewModel(
            observeKeyboardSessionSnapshot = ObserveKeyboardSessionSnapshotUseCase(sessionRepository),
            commitKeyboardField = CommitKeyboardFieldUseCase(sessionRepository),
            sessionTimeoutController = SessionTimeoutController(clearKeyboardSession),
            settingsStore = settingsStore,
        )
    }

    private class FakeKeyboardSettingsStore(
        initialSettings: KeyboardSettings = KeyboardSettings(),
    ) : KeyboardSettingsStore {
        val settingsFlow = MutableStateFlow(initialSettings)

        override val settings = settingsFlow

        override suspend fun updateQuickActionSlots(slots: KeyboardQuickActionSlots) {
            settingsFlow.value = settingsFlow.value.copy(quickActionSlots = slots)
        }
    }
}
