package br.gov.bombeiros.pr.heartbpm.domain.model

/**
 * Leitura instantânea do sensor de frequência cardíaca.
 *
 * @param bpm Batimentos por minuto capturados pelo sensor.
 */
data class HeartRateReading(val bpm: Int)
