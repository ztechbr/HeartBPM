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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "AlarmController"

/**
 * Controla o alarme com foco em estabilidade e não bloqueio da UI Thread.
 */
class AlarmController(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private var fallbackRingtone: Ringtone? = null
    private val vibrator: Vibrator by lazy { resolveVibrator() }
    
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile
    private var isRunning = false

    /** Inicia sirene. Usa Mutex para evitar condições de corrida. */
    suspend fun start() = mutex.withLock {
        if (isRunning) return@withLock
        
        isRunning = true
        Log.d(TAG, "Alarme INICIADO")

        withContext(Dispatchers.Default) {
            val gen = toneGenerator ?: runCatching {
                ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME)
            }.getOrNull()
            
            toneGenerator = gen

            if (gen != null) {
                val started = runCatching { 
                    gen.startTone(ToneGenerator.TONE_CDMA_HIGH_L)
                    true
                }.getOrDefault(false)
                
                if (!started) {
                    Log.e(TAG, "Falha ao iniciar tom no ToneGenerator")
                    playFallbackRingtone()
                }
            } else {
                Log.e(TAG, "ToneGenerator é nulo, usando fallback")
                playFallbackRingtone()
            }

            // Vibração simplificada
            runCatching {
                val pattern = longArrayOf(0L, 500L, 500L)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
            }
        }
    }

    private fun playFallbackRingtone() {
        if (fallbackRingtone == null) {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            fallbackRingtone = RingtoneManager.getRingtone(context, uri)
        }
        runCatching { if (fallbackRingtone?.isPlaying == false) fallbackRingtone?.play() }
    }

    /** Para o alarme de forma segura. */
    suspend fun stop() = mutex.withLock {
        if (!isRunning) return@withLock
        isRunning = false
        Log.d(TAG, "Alarme PARADO")
        
        runCatching { vibrator.cancel() }

        withContext(Dispatchers.Default) {
            runCatching { toneGenerator?.stopTone() }
            runCatching { if (fallbackRingtone?.isPlaying == true) fallbackRingtone?.stop() }
        }
    }

    /** Libera recursos permanentemente. */
    fun release() {
        isRunning = false
        // Lançamos no GlobalScope ou em um escopo que não seja cancelado imediatamente
        // para garantir que o cleanup ocorra mesmo se o componente que chamou for destruído.
        // Como o cleanup é rápido (apenas release de hardware), não há risco de leak.
        scope.launch {
            try {
                mutex.withLock {
                    runCatching { vibrator.cancel() }
                    toneGenerator?.let {
                        runCatching { it.stopTone() }
                        runCatching { it.release() }
                        toneGenerator = null
                    }
                    fallbackRingtone?.let {
                        runCatching { it.stop() }
                        fallbackRingtone = null
                    }
                }
            } finally {
                scope.cancel()
            }
        }
    }


    private fun resolveVibrator(): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(VibratorManager::class.java)).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
}
