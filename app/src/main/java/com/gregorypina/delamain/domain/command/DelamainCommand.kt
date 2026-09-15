package com.gregorypina.delamain.domain.command

sealed interface DelamainCommand {
    data class SetVolume(val percent: Int) : DelamainCommand
    data object VolumeUp : DelamainCommand
    data object VolumeDown : DelamainCommand
    data object MediaPlayPause : DelamainCommand
    data object MediaNext : DelamainCommand
    data object MediaPrevious : DelamainCommand
    data class OpenApp(val packageName: String, val displayName: String) : DelamainCommand
    data object CurrentTime : DelamainCommand
    data object Unknown : DelamainCommand
}
