package io.github.togls.kp2acomposekeyboard.feature.keyboard

import io.github.togls.kp2acomposekeyboard.application.keyboard.ClearKeyboardSessionUseCase
import io.github.togls.kp2acomposekeyboard.application.keyboard.CommitKeyboardFieldUseCase
import io.github.togls.kp2acomposekeyboard.application.keyboard.ObserveKeyboardSessionSnapshotUseCase
import io.github.togls.kp2acomposekeyboard.application.session.SessionTimeoutController
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.SettingsQuickActionId

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class KeyboardViewModelQuickActionTest {

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
    fun toggleQuickActionPanel_togglesExpandedState() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(KeyboardIntent.ToggleQuickActionPanel)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isQuickActionPanelExpanded)
    }

    @Test
    fun closeQuickActionPanel_setsExpandedStateFalse() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(KeyboardIntent.ToggleQuickActionPanel)
        viewModel.onIntent(KeyboardIntent.CloseQuickActionPanel)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isQuickActionPanelExpanded)
    }

    @Test
    fun clickSettings_emitsLaunchSettings() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<KeyboardEffect>()
        val job = launch {
            viewModel.effect.collect { effect -> effects.add(effect) }
        }
        runCurrent()

        viewModel.onIntent(KeyboardIntent.ClickQuickAction(SettingsQuickActionId))
        advanceUntilIdle()

        assertEquals(listOf(KeyboardEffect.LaunchSettings), effects)
        job.cancel()
    }

    @Test
    fun moveQuickActionToRight_updatesSettingsStore() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore()
        val viewModel = createViewModel(settingsStore)

        viewModel.onIntent(KeyboardIntent.MoveQuickActionToRight(SettingsQuickActionId))
        advanceUntilIdle()

        assertEquals(
            KeyboardQuickActionSlots(
                centerItemIds = emptyList(),
                rightItemId = SettingsQuickActionId,
            ),
            settingsStore.savedSlots,
        )
    }

    @Test
    fun removeQuickAction_updatesSettingsStore() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore()
        settingsStore.settingsFlow.value = KeyboardSettings(
            quickActionSlots = KeyboardQuickActionSlots(rightItemId = SettingsQuickActionId),
        )
        val viewModel = createViewModel(settingsStore)
        advanceUntilIdle()

        viewModel.onIntent(KeyboardIntent.RemoveQuickAction(SettingsQuickActionId))
        advanceUntilIdle()

        assertEquals(KeyboardQuickActionSlots(centerItemIds = emptyList()), settingsStore.savedSlots)
    }

    @Test
    fun settingsFlow_updatesUiStateQuickActionSlots() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore()
        val viewModel = createViewModel(settingsStore)

        settingsStore.settingsFlow.value = KeyboardSettings(
            quickActionSlots = KeyboardQuickActionSlots(
                centerItemIds = emptyList(),
                rightItemId = SettingsQuickActionId,
            ),
        )
        advanceUntilIdle()

        assertEquals(
            KeyboardQuickActionSlots(
                centerItemIds = emptyList(),
                rightItemId = SettingsQuickActionId,
            ),
            viewModel.uiState.value.quickActionSlots,
        )
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

    private class FakeKeyboardSettingsStore : KeyboardSettingsStore {
        val settingsFlow = MutableStateFlow(KeyboardSettings())
        var savedSlots: KeyboardQuickActionSlots? = null

        override val settings = settingsFlow

        override suspend fun updateQuickActionSlots(slots: KeyboardQuickActionSlots) {
            savedSlots = slots
            settingsFlow.value = settingsFlow.value.copy(quickActionSlots = slots)
        }
    }
}
