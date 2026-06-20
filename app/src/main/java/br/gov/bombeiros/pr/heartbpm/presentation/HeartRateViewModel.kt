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
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview

/**
 * ViewModel de monitoramento cardíaco.
 */
@OptIn(FlowPreview::class)
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
                .conflate() 
                .sample(3000) // ATUALIZAÇÃO A CADA 3 SEGUNDOS: Alivia muito o processamento
                .distinctUntilChanged { old, new -> old.bpm == new.bpm }
                .catch { e ->
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
