package io.github.togls.kp2acomposekeyboard.kp2a

import android.util.Log
import keepass2android.pluginsdk.PluginActionBroadcastReceiver

class Kp2aActionReceiver : PluginActionBroadcastReceiver() {

    override fun openEntry(openEntryAction: OpenEntryAction) {
        val fields = openEntryAction.entryFields

        Log.d("Kp2APlugin", "openEntry fields=${fields.keys}")

        // 下一步：
        // 1. 读取 Title/UserName/Password/TOTP 等字段
        // 2. 写入 KeyboardSessionRepository
        // 3. IME 从 repository 读取当前条目快照
    }

    override fun actionSelected(actionSelectedAction: ActionSelectedAction) {
        Log.d("Kp2APlugin", "actionSelected")
    }

    override fun entryOutputModified(entryOutputModifiedAction: EntryOutputModifiedAction) {
        Log.d("Kp2APlugin", "entryOutputModified")
    }
}