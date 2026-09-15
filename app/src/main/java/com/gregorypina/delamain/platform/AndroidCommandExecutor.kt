package com.gregorypina.delamain.platform

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import com.gregorypina.delamain.domain.command.DelamainCommand
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface CommandResult {
    data class Success(val message: String) : CommandResult
    data class Failure(val message: String) : CommandResult
}

class AndroidCommandExecutor(private val context: Context) {
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun execute(command: DelamainCommand): CommandResult = when (command) {
        is DelamainCommand.SetVolume -> setVolume(command.percent)
        DelamainCommand.VolumeUp -> adjustVolume(AudioManager.ADJUST_RAISE)
        DelamainCommand.VolumeDown -> adjustVolume(AudioManager.ADJUST_LOWER)
        DelamainCommand.MediaPlayPause -> dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, "play/pause")
        DelamainCommand.MediaNext -> dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT, "próxima faixa")
        DelamainCommand.MediaPrevious -> dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS, "faixa anterior")
        is DelamainCommand.OpenApp -> openApp(command.packageName, command.displayName)
        DelamainCommand.CurrentTime -> currentTime()
        DelamainCommand.Unknown -> CommandResult.Failure("Não entendi o comando")
    }

    private fun setVolume(percent: Int): CommandResult {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = ((percent.coerceIn(0, 100) / 100f) * max).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        return CommandResult.Success("Volume em $percent%")
    }

    private fun adjustVolume(direction: Int): CommandResult {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val percent = ((current / max.toFloat()) * 100).toInt()
        return CommandResult.Success("Volume em $percent%")
    }

    private fun dispatchMediaKey(keyCode: Int, label: String): CommandResult {
        val down = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val up = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager.dispatchMediaKeyEvent(down)
        audioManager.dispatchMediaKeyEvent(up)
        return CommandResult.Success("Comando de mídia: $label")
    }

    private fun openApp(packageName: String, displayName: String): CommandResult {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return CommandResult.Failure("$displayName não está instalado")

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return CommandResult.Success("Abrindo $displayName")
    }

    private fun currentTime(): CommandResult {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        return CommandResult.Success("Agora são $time")
    }
}
