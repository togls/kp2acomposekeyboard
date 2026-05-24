package io.github.togls.kp2acomposekeyboard.feature.keyboard

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.SettingsUtilityItemId

import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettingsStore
import io.github.togls.kp2acomposekeyboard.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.session.SessionTimeoutController
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
class KeyboardViewModelUtilityTest {

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
    fun toggleUtilityPanel_togglesExpandedState() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(KeyboardIntent.ToggleUtilityPanel)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isUtilityPanelExpanded)
    }

    @Test
    fun closeUtilityPanel_setsExpandedStateFalse() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(KeyboardIntent.ToggleUtilityPanel)
        viewModel.onIntent(KeyboardIntent.CloseUtilityPanel)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isUtilityPanelExpanded)
    }

    @Test
    fun clickSettings_emitsLaunchSettings() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<KeyboardEffect>()
        val job = launch {
            viewModel.effect.collect { effect -> effects.add(effect) }
        }
        runCurrent()

        viewModel.onIntent(KeyboardIntent.ClickUtilityItem(SettingsUtilityItemId))
        advanceUntilIdle()

        assertEquals(listOf(KeyboardEffect.LaunchSettings), effects)
        job.cancel()
    }

    @Test
    fun moveUtilityItemToRight_updatesSettingsStore() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore()
        val viewModel = createViewModel(settingsStore)

        viewModel.onIntent(KeyboardIntent.MoveUtilityItemToRight(SettingsUtilityItemId))
        advanceUntilIdle()

        assertEquals(
            KeyboardUtilitySlots(
                centerItemIds = emptyList(),
                rightItemId = SettingsUtilityItemId,
            ),
            settingsStore.savedSlots,
        )
    }

    @Test
    fun removeUtilityItem_updatesSettingsStore() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore()
        settingsStore.settingsFlow.value = KeyboardSettings(
            utilitySlots = KeyboardUtilitySlots(rightItemId = SettingsUtilityItemId),
        )
        val viewModel = createViewModel(settingsStore)
        advanceUntilIdle()

        viewModel.onIntent(KeyboardIntent.RemoveUtilityItem(SettingsUtilityItemId))
        advanceUntilIdle()

        assertEquals(KeyboardUtilitySlots(centerItemIds = emptyList()), settingsStore.savedSlots)
    }

    @Test
    fun settingsFlow_updatesUiStateUtilitySlots() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore()
        val viewModel = createViewModel(settingsStore)

        settingsStore.settingsFlow.value = KeyboardSettings(
            utilitySlots = KeyboardUtilitySlots(
                centerItemIds = emptyList(),
                rightItemId = SettingsUtilityItemId,
            ),
        )
        advanceUntilIdle()

        assertEquals(
            KeyboardUtilitySlots(
                centerItemIds = emptyList(),
                rightItemId = SettingsUtilityItemId,
            ),
            viewModel.uiState.value.utilitySlots,
        )
    }

    private fun createViewModel(
        settingsStore: FakeKeyboardSettingsStore = FakeKeyboardSettingsStore(),
    ): KeyboardViewModel {
        val sessionRepository = KeyboardSessionRepository()
        return KeyboardViewModel(
            sessionRepository = sessionRepository,
            sessionTimeoutController = SessionTimeoutController(sessionRepository),
            settingsStore = settingsStore,
        )
    }

    private class FakeKeyboardSettingsStore : KeyboardSettingsStore {
        val settingsFlow = MutableStateFlow(KeyboardSettings())
        var savedSlots: KeyboardUtilitySlots? = null

        override val settings = settingsFlow

        override suspend fun updateUtilitySlots(slots: KeyboardUtilitySlots) {
            savedSlots = slots
            settingsFlow.value = settingsFlow.value.copy(utilitySlots = slots)
        }
    }
}
