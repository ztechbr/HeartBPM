package br.gov.bombeiros.pr.heartbpm.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import br.gov.bombeiros.pr.heartbpm.domain.model.HeartRateReading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "WearHeartRateSensor"

/**
 * Implementação otimizada para baixo consumo de processamento.
 */
class WearHeartRateSensor(context: Context) : HeartRateSensor {

    private val sensorManager: SensorManager = context
        .createAttributionContext("heartRateSensor")
        .getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val hardwareSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    override val isAvailable: Boolean
        get() = hardwareSensor != null

    override fun observe(): Flow<HeartRateReading> = callbackFlow {
        if (hardwareSensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            private var lastEventTime = 0L

            override fun onSensorChanged(event: SensorEvent?) {
                val currentTime = System.currentTimeMillis()
                // FILTRO DE HARDWARE: Ignora eventos em intervalos menores que 1.2s
                if (currentTime - lastEventTime < 1200) return
                
                val bpm = event?.values?.firstOrNull()?.toInt() ?: return
                if (bpm > 0) {
                    lastEventTime = currentTime
                    trySend(HeartRateReading(bpm))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener,
            hardwareSensor,
            SensorManager.SENSOR_DELAY_UI // Delay mais amigável para a UI
        )

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
