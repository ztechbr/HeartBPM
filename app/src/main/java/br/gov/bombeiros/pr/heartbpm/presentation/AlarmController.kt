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

private const val TAG = "AlarmController"

/**
 * Controla o alarme sonoro e tátil do relógio.
 */
class AlarmController(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private var fallbackRingtone: Ringtone? = null
    private val vibrator: Vibrator by lazy { resolveVibrator() }

    @Volatile
    private var isRunning = false

    /** Inicia sirene sonora + vibração contínua. Move para Default para não travar a UI. */
    suspend fun start() = withContext(Dispatchers.Default) {
        if (isRunning) return@withContext
        isRunning = true
        Log.d(TAG, "Alarme INICIADO")

        toneGenerator = runCatching {
            ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME).also {
                it.startTone(ToneGenerator.TONE_CDMA_HIGH_L)
            }
        }.onFailure {
            Log.w(TAG, "ToneGenerator falhou, usando RingtoneManager: ${it.message}")
            runCatching {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                fallbackRingtone = RingtoneManager.getRingtone(context, uri)?.apply {
                    play()
                }
            }
        }.getOrNull()

        val pattern = longArrayOf(0L, 500L, 200L)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    /** Para sirene e vibração de forma síncrona e segura. */
    fun stop() {
        if (!isRunning) return
        isRunning = false
        Log.d(TAG, "Alarme PARADO")
        cleanupResources()
    }

    /** Libera todos os recursos. Deve ser chamado no onDestroy da Activity. */
    fun release() {
        stop()
    }

    private fun cleanupResources() {
        toneGenerator?.apply {
            runCatching { stopTone() }
            runCatching { release() }
        }
        toneGenerator = null

        fallbackRingtone?.apply {
            runCatching { if (isPlaying) stop() }
        }
        fallbackRingtone = null

        vibrator.cancel()
    }

    private fun resolveVibrator(): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(VibratorManager::class.java)).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
}
