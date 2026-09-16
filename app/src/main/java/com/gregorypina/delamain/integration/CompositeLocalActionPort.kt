package com.gregorypina.delamain.integration

import com.gregorypina.delamain.domain.LocalAction
import com.gregorypina.delamain.domain.LocalActionPort
import com.gregorypina.delamain.domain.LocalActionResult

class CompositeLocalActionPort(
    private val volumePort: LocalActionPort,
    private val mediaKeyPort: LocalActionPort,
    private val launchAppPort: LocalActionPort,
) : LocalActionPort {
    override fun execute(action: LocalAction): LocalActionResult = when (action) {
        LocalAction.VolumeUp,
        LocalAction.VolumeDown,
        -> volumePort.execute(action)
        LocalAction.MediaNext,
        LocalAction.MediaPrevious,
        -> mediaKeyPort.execute(action)
        is LocalAction.OpenApp -> launchAppPort.execute(action)
    }
}
