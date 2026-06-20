package br.gov.bombeiros.pr.heartbpm.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

/**
 * Tema base do HeartBPM.
 *
 * O Wear Compose Material3 já vem pré-configurado para telas redondas/
 * quadradas de relógio. Extender aqui com ColorScheme customizado
 * quando necessário (ex: branding completo do CBPR).
 */
@Composable
fun HeartBPMTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
