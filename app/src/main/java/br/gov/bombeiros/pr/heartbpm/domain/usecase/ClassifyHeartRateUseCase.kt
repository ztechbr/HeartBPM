package br.gov.bombeiros.pr.heartbpm.domain.usecase

import br.gov.bombeiros.pr.heartbpm.domain.model.HeartStatus

/**
 * Regra de negócio pura: classifica um valor de BPM como [HeartStatus].
 *
 * Sem dependências Android — 100% testável com JUnit simples.
 */
class ClassifyHeartRateUseCase {

    companion object {
        const val THRESHOLD_ATENCAO = 101   // bpm ≥ este valor → ATENÇÃO
        const val THRESHOLD_ALARME  = 141   // bpm ≥ este valor → ALARME
    }

    operator fun invoke(bpm: Int): HeartStatus = when {
        bpm >= THRESHOLD_ALARME  -> HeartStatus.ALARME
        bpm >= THRESHOLD_ATENCAO -> HeartStatus.ATENCAO
        else                     -> HeartStatus.NORMAL
    }
}
