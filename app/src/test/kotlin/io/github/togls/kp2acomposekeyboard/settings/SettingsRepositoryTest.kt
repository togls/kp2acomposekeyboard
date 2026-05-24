package io.github.togls.kp2acomposekeyboard.settings

import io.github.togls.kp2acomposekeyboard.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsRepositoryTest {

    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() = runTest {
        repository = SettingsRepository(RuntimeEnvironment.getApplication())
        repository.resetToDefault()
    }

    @After
    fun tearDown() = runTest {
        repository.resetToDefault()
    }

    @Test
    fun settings_defaultsEnglishSubtypeDisabled() = runTest {
        assertFalse(repository.settings.first().englishUsSubtypeEnabled)
    }

    @Test
    fun updateEnglishUsSubtypeEnabled_persistsEnabledState() = runTest {
        repository.updateEnglishUsSubtypeEnabled(true)

        assertTrue(repository.settings.first().englishUsSubtypeEnabled)
    }

    @Test
    fun updateEnglishUsSubtypeEnabled_persistsDisabledState() = runTest {
        repository.updateEnglishUsSubtypeEnabled(true)
        repository.updateEnglishUsSubtypeEnabled(false)

        assertFalse(repository.settings.first().englishUsSubtypeEnabled)
    }
}
