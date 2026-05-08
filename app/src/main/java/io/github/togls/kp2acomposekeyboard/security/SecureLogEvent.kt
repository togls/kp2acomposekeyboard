package io.github.togls.kp2acomposekeyboard.security

/**
 * 这些事件名只表达“发生了什么”，不带字段名、不带字段值、不带输入文本。
 * 需求里明确禁止密码写入日志、禁止调试日志打印字段 value，字段按钮也只能显示 label，不能显示真实 value。
 */
sealed interface SecureLogEvent {
    val message: String

    data object ImeCreated : SecureLogEvent {
        override val message = "IME created"
    }

    data object ImeDestroyed : SecureLogEvent {
        override val message = "IME destroyed"
    }

    data object InputViewCreated : SecureLogEvent {
        override val message = "Input view created"
    }

    data object InputViewStarted : SecureLogEvent {
        override val message = "Input view started"
    }

    data object InputViewFinished : SecureLogEvent {
        override val message = "Input view finished"
    }

    data object SessionCreated : SecureLogEvent {
        override val message = "Session created"
    }

    data object SessionCleared : SecureLogEvent {
        override val message = "Session cleared"
    }

    data object SessionTimeoutScheduled : SecureLogEvent {
        override val message = "Session timeout scheduled"
    }

    data object SessionTimeoutCanceled : SecureLogEvent {
        override val message = "Session timeout canceled"
    }

    data object FieldCommitRequested : SecureLogEvent {
        override val message = "Field commit requested"
    }

    data object FieldCommitIgnored : SecureLogEvent {
        override val message = "Field commit ignored"
    }

    data object TextCommitRequested : SecureLogEvent {
        override val message = "Text commit requested"
    }

    data object DeleteBackwardRequested : SecureLogEvent {
        override val message = "Delete backward requested"
    }

    data object EnterRequested : SecureLogEvent {
        override val message = "Enter requested"
    }

    data object LaunchEntryPickerRequested : SecureLogEvent {
        override val message = "Launch entry picker requested"
    }

    data object LaunchSettingsRequested : SecureLogEvent {
        override val message = "Launch settings requested"
    }

    data object EntryPickerActivityLaunchRequested : SecureLogEvent {
        override val message = "Entry picker activity launch requested"
    }

    data object EntryPickerActivityLaunchFailed : SecureLogEvent {
        override val message = "Entry picker activity launch failed"
    }
}