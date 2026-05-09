package io.github.togls.kp2acomposekeyboard.security

object DebugLogEvent {
    const val EntryPickerLaunchRequested = "entry_picker.launch_requested"
    const val EntryPickerLaunchSkipped = "entry_picker.launch_skipped"

    const val Kp2aQueryLaunchRequested = "kp2a.query.launch_requested"
    const val Kp2aQueryIntentCreated = "kp2a.query.intent_created"
    const val Kp2aQueryLaunchCalled = "kp2a.query.launch_called"
    const val Kp2aQueryResultReceived = "kp2a.query.result_received"
    const val Kp2aQueryResultCancelled = "kp2a.query.result_cancelled"
    const val Kp2aQueryResultDataMissing = "kp2a.query.result_data_missing"
    const val Kp2aQueryResultParseFailed = "kp2a.query.result_parse_failed"
    const val Kp2aQueryResultParsed = "kp2a.query.result_parsed"

    const val Kp2aAccessNotGranted = "kp2a.access.not_granted"
    const val Kp2aPluginSettingsOpenRequested = "kp2a.plugin_settings.open_requested"
    const val Kp2aPluginSettingsReturned = "kp2a.plugin_settings.returned"
    const val Kp2aPluginSettingsFailed = "kp2a.plugin_settings.failed"

    const val Kp2aAccessReceiverReceived = "kp2a.access_receiver.received"
    const val Kp2aAccessReceiverGetScopes = "kp2a.access_receiver.get_scopes"
}