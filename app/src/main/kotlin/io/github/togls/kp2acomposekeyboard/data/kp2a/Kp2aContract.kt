package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.app.Activity
import android.content.Intent
import keepass2android.pluginsdk.Kp2aControl
import keepass2android.pluginsdk.Strings

object Kp2aContract {

    fun createQueryEntryIntent(searchText: String?): Intent {
        return Kp2aControl.getQueryEntryIntent(searchText)
    }

    fun isSuccessfulResult(
        resultCode: Int,
        data: Intent?,
    ): Boolean {
        return resultCode == Activity.RESULT_OK &&
                !data?.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA).isNullOrBlank()
    }
}