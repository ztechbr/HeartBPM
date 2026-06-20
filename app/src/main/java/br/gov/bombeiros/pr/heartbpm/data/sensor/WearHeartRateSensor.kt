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
 * Implementação concreta de [HeartRateSensor] usando [SensorManager].
 *
 * Usa [callbackFlow] para converter o callback de hardware em um Flow
 * reativo. O sensor é registrado quando o Flow começa a ser coletado
 * e desregistrado automaticamente ao cancelar (awaitClose).
 *
 * O atributo de contexto "heartRateSensor" é exigido pelo Android
 * para acesso com rastreabilidade de privacidade (API 30+).
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
            Log.e(TAG, "Sensor TYPE_HEART_RATE não encontrado neste dispositivo.")
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val bpm = event?.values?.firstOrNull()?.toInt() ?: return
                if (bpm > 0) trySend(HeartRateReading(bpm))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d(TAG, "Acurácia do sensor alterada: $accuracy")
            }
        }

        val registered = sensorManager.registerListener(
            listener,
            hardwareSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        if (!registered) {
            Log.e(TAG, "Falha ao registrar listener do sensor.")
            close()
        }

        // Desregistra o listener quando o Flow for cancelado/fechado
        awaitClose {
            sensorManager.unregisterListener(listener)
            Log.d(TAG, "Listener do sensor desregistrado.")
        }
    }
}
