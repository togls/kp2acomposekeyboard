package io.github.togls.kp2acomposekeyboard.feature.plugin

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import keepass2android.pluginsdk.AccessManager

class Kp2aPluginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val enabled = AccessManager.getAllHostPackages(this).isNotEmpty()

        val text = if (enabled) {
            "KP2A Compose Keyboard 已启用为 Keepass2Android 插件"
        } else {
            "KP2A Compose Keyboard 尚未启用为 Keepass2Android 插件"
        }

        setContentView(
            TextView(this).apply {
                this.text = text
                textSize = 18f
                setPadding(48, 48, 48, 48)
            },
        )
    }
}