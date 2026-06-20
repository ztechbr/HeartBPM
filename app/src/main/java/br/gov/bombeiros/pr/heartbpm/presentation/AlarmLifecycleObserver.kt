package br.gov.bombeiros.pr.heartbpm.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Observa [HeartRateUiState.alarmActive] vinculado ao [Lifecycle] da Activity.
 *
 * Separar essa responsabilidade da Activity mantém a [MainActivity] enxuta
 * e facilita testes unitários do comportamento do alarme de forma isolada.
 */
class AlarmLifecycleObserver(
    private val flow: StateFlow<HeartRateUiState>,
    private val alarmController: AlarmController,
    private val lifecycle: Lifecycle
) : DefaultLifecycleObserver {

    private var collectJob: Job? = null

    override fun onStart(owner: LifecycleOwner) {
        collectJob = lifecycle.coroutineScope.launch {
            flow
                .map { it.alarmActive }
                .distinctUntilChanged()
                .collect { active ->
                    if (active) alarmController.start()
                    else       alarmController.stop()
                }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        collectJob?.cancel()
        lifecycle.coroutineScope.launch {
            alarmController.stop()
        }
    }
}
