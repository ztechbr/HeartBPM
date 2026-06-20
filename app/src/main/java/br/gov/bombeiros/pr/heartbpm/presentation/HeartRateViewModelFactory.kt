package br.gov.bombeiros.pr.heartbpm.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.gov.bombeiros.pr.heartbpm.data.sensor.WearHeartRateSensor
import br.gov.bombeiros.pr.heartbpm.domain.usecase.ClassifyHeartRateUseCase

/**
 * Factory manual para [HeartRateViewModel].
 *
 * Elimina a necessidade de Hilt/Koin mantendo DI explícita e testável.
 * Para adicionar injeção de dependência completa no futuro, basta
 * substituir esta factory por um módulo Hilt.
 */
class HeartRateViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeartRateViewModel::class.java)) {
            return HeartRateViewModel(
                sensor   = WearHeartRateSensor(context.applicationContext),
                classify = ClassifyHeartRateUseCase()
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
}
