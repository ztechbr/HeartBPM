package br.gov.bombeiros.pr.heartbpm.data.sensor

import br.gov.bombeiros.pr.heartbpm.domain.model.HeartRateReading
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acesso ao sensor de frequência cardíaca.
 *
 * A abstração permite substituir a implementação real por um fake
 * em testes sem tocar no ViewModel.
 */
interface HeartRateSensor {

    /** `true` se o hardware de sensor cardíaco está disponível no dispositivo. */
    val isAvailable: Boolean

    /**
     * Emite [HeartRateReading] continuamente enquanto o Flow estiver ativo.
     * O sensor é registrado ao coletar e desregistrado ao cancelar.
     */
    fun observe(): Flow<HeartRateReading>
}
