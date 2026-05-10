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
            getString(io.github.togls.kp2acomposekeyboard.R.string.kp2a_plugin_status_enabled)
        } else {
            getString(io.github.togls.kp2acomposekeyboard.R.string.kp2a_plugin_status_disabled)
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
