package io.github.togls.kp2acomposekeyboard.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.togls.kp2acomposekeyboard.domain.policy.KeyboardFieldClassifier
import io.github.togls.kp2acomposekeyboard.domain.policy.SensitiveFieldPolicy
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettingsStore
import io.github.togls.kp2acomposekeyboard.data.settings.SettingsRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideKeyboardFieldClassifier(): KeyboardFieldClassifier {
        return KeyboardFieldClassifier()
    }

    @Provides
    fun provideSensitiveFieldPolicy(): SensitiveFieldPolicy {
        return SensitiveFieldPolicy()
    }

    @Provides
    fun provideKeyboardSettingsStore(
        settingsRepository: SettingsRepository,
    ): KeyboardSettingsStore {
        return settingsRepository
    }
}
