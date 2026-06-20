package br.gov.bombeiros.pr.heartbpm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.gov.bombeiros.pr.heartbpm.data.sensor.HeartRateSensor
import br.gov.bombeiros.pr.heartbpm.domain.model.HeartStatus
import br.gov.bombeiros.pr.heartbpm.domain.usecase.ClassifyHeartRateUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de monitoramento cardíaco.
 *
 * Responsabilidades:
 *  - Controlar o ciclo de vida do sensor via [HeartRateSensor]
 *  - Aplicar a regra de classificação via [ClassifyHeartRateUseCase]
 *  - Expor [HeartRateUiState] imutável para a UI por meio de [StateFlow]
 *  - Refletir eventos de permissão recebidos da [MainActivity]
 *
 * NÃO conhece Context, View, MediaPlayer ou qualquer framework Android —
 * apenas domínio e coroutines.
 */
class HeartRateViewModel(
    private val sensor: HeartRateSensor,
    private val classify: ClassifyHeartRateUseCase = ClassifyHeartRateUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HeartRateUiState())
    val uiState: StateFlow<HeartRateUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    // ── Callbacks de permissão (chamados pela Activity) ───────────────────────

    fun onPermissionGranted() {
        _uiState.update { it.copy(permissionState = PermissionState.GRANTED) }
        startObserving()
    }

    fun onPermissionDenied() {
        _uiState.update { it.copy(permissionState = PermissionState.DENIED) }
    }

    // ── Leitura do sensor ─────────────────────────────────────────────────────

    private fun startObserving() {
        if (observeJob?.isActive == true) return

        if (!sensor.isAvailable) {
            _uiState.update { it.copy(sensorAvailable = false) }
            return
        }

        observeJob = viewModelScope.launch {
            sensor.observe()
                .catch { e ->
                    // Sensor pode lançar exceção se não disponível;
                    // a UI continuará exibindo "--" até nova leitura.
                    _uiState.update { it.copy(sensorAvailable = false) }
                }
                .collect { reading ->
                    val status = classify(reading.bpm)
                    _uiState.update {
                        it.copy(
                            bpm         = reading.bpm,
                            status      = status,
                            alarmActive = status == HeartStatus.ALARME
                        )
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}
