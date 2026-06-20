package br.gov.bombeiros.pr.heartbpm.presentation

import android.content.Context
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

private const val TAG = "AlarmController"

/**
 * Controla o alarme com foco em não bloquear a UI Thread.
 */
class AlarmController(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private var fallbackRingtone: Ringtone? = null
    private val vibrator: Vibrator by lazy { resolveVibrator() }

    @Volatile
    private var isRunning = false

    /** Inicia sirene. Move para IO e evita múltiplas instâncias. */
    suspend fun start() = withContext(Dispatchers.Default) {
        if (isRunning) return@withContext
        isRunning = true
        Log.d(TAG, "Alarme INICIADO")

        toneGenerator = runCatching {
            ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME).also {
                it.startTone(ToneGenerator.TONE_CDMA_HIGH_L)
            }
        }.onFailure {
            runCatching {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                fallbackRingtone = RingtoneManager.getRingtone(context, uri)?.apply { play() }
            }
        }.getOrNull()

        // Vibração simplificada (500ms ligado, 500ms desligado) para economizar CPU
        val pattern = longArrayOf(0L, 500L, 500L)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    /** Para o alarme sem bloquear a thread chamadora. */
    fun stop() {
        if (!isRunning) return
        isRunning = false
        Log.d(TAG, "Alarme PARADO")
        
        // Para a vibração imediatamente (é rápido)
        vibrator.cancel()

        // Libera o ToneGenerator e Ringtone em uma thread separada para não travar a UI
        val gen = toneGenerator
        val ring = fallbackRingtone
        toneGenerator = null
        fallbackRingtone = null

        thread(start = true, isDaemon = true) {
            runCatching { gen?.stopTone() }
            runCatching { gen?.release() }
            runCatching { if (ring?.isPlaying == true) ring.stop() }
        }
    }

    fun release() {
        stop()
    }

    private fun resolveVibrator(): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(VibratorManager::class.java)).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
}
