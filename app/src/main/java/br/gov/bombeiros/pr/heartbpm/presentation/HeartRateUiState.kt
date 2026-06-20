package br.gov.bombeiros.pr.heartbpm.presentation

import br.gov.bombeiros.pr.heartbpm.domain.model.HeartStatus

/**
 * Estado imutável da UI de monitoramento cardíaco.
 *
 * O ViewModel expõe um [kotlinx.coroutines.flow.StateFlow] deste tipo;
 * a UI não armazena estado próprio — apenas observa e renderiza.
 *
 * @param bpm               Último valor de BPM recebido do sensor (0 = aguardando).
 * @param status            Classificação clínica atual.
 * @param permissionState   Estado da solicitação de permissões de runtime.
 * @param sensorAvailable   `false` se o hardware não existe no dispositivo.
 * @param alarmActive       `true` quando a sirene deve estar tocando.
 */
data class HeartRateUiState(
    val bpm: Int                     = 0,
    val status: HeartStatus          = HeartStatus.NORMAL,
    val permissionState: PermissionState = PermissionState.UNKNOWN,
    val sensorAvailable: Boolean     = true,
    val alarmActive: Boolean         = false
)

enum class PermissionState {
    UNKNOWN,    // ainda não verificado
    GRANTED,    // concedido → sensor ativo
    DENIED      // negado → exibir tela de instrução
}
