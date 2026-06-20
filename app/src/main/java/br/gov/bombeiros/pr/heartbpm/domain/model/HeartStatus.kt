package br.gov.bombeiros.pr.heartbpm.domain.model

/**
 * Status clínico da frequência cardíaca do bombeiro.
 *
 * Limiares definidos conforme diretrizes de segurança operacional:
 *  - NORMAL  : ≤ 100 bpm
 *  - ATENCAO : 101–140 bpm  (FC moderadamente elevada)
 *  - ALARME  : > 140 bpm    (FC perigosamente elevada → emite sirene)
 */
enum class HeartStatus(val label: String) {
    NORMAL("Normal"),
    ATENCAO("Atenção"),
    ALARME("ALARME")
}
