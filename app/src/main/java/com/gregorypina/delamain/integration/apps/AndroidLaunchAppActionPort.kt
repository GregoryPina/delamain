package com.gregorypina.delamain.integration.apps

import android.content.Context
import android.content.Intent
import com.gregorypina.delamain.domain.LocalAction
import com.gregorypina.delamain.domain.LocalActionPort
import com.gregorypina.delamain.domain.LocalActionResult

class AndroidLaunchAppActionPort private constructor(
    private val context: Context?,
) : LocalActionPort {
    override fun execute(action: LocalAction): LocalActionResult = when (action) {
        is LocalAction.OpenApp -> launch(action)
        else -> LocalActionResult.Unavailable(action)
    }

    private fun launch(action: LocalAction.OpenApp): LocalActionResult {
        val appContext = context ?: return LocalActionResult.Unavailable(action)

        return try {
            val launchIntent = appContext.packageManager
                .getLaunchIntentForPackage(action.packageName)
                ?: return LocalActionResult.NotInstalled(action, action.displayName)

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(launchIntent)
            LocalActionResult.Launched(action, action.displayName)
        } catch (_: RuntimeException) {
            LocalActionResult.Failure(action)
        }
    }

    companion object {
        fun from(context: Context): AndroidLaunchAppActionPort =
            AndroidLaunchAppActionPort(context.applicationContext)
    }
}
