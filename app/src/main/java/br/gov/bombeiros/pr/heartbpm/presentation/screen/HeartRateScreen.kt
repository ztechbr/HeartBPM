package br.gov.bombeiros.pr.heartbpm.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import br.gov.bombeiros.pr.heartbpm.R
import br.gov.bombeiros.pr.heartbpm.domain.model.HeartStatus
import br.gov.bombeiros.pr.heartbpm.presentation.HeartRateUiState

// ── Paleta Corpo de Bombeiros do Paraná ──────────────────────────────────────
private val ColorVerdeNormal   = Color(0xFF1B5E20)
private val ColorAmbarAtencao  = Color(0xFFE65100)
private val ColorVermelhoAlarme = Color(0xFFB71C1C)
private val ColorTextoPrimario = Color.White
private val ColorTextoSecundario = Color.White.copy(alpha = 0.78f)

/**
 * Tela principal de monitoramento cardíaco.
 *
 * Composable puro — recebe [HeartRateUiState] e não chama nenhuma
 * API Android diretamente. Fácil de Previews e testes de composição.
 */
@Composable
fun HeartRateScreen(uiState: HeartRateUiState) {

    val bgColor by animateColorAsState(
        targetValue = when (uiState.status) {
            HeartStatus.NORMAL  -> ColorVerdeNormal
            HeartStatus.ATENCAO -> ColorAmbarAtencao
            HeartStatus.ALARME  -> ColorVermelhoAlarme
        },
        animationSpec = tween(durationMillis = 600),
        label = "bgColorAnimation"
    )

    // Ícone de coração pulsa quando está em ALARME
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = if (uiState.alarmActive) 1.45f else 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(380),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartScaleAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Cabeçalho ─────────────────────────────────────────────────────
            Text(
                text  = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleSmall.copy(
                    color      = ColorTextoPrimario,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 12.sp
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text  = stringResource(R.string.subtitle_cbpr),
                style = MaterialTheme.typography.bodySmall.copy(
                    color    = ColorTextoSecundario,
                    fontSize = 9.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            // ── Ícone de coração ──────────────────────────────────────────────
            Icon(
                painter            = painterResource(R.drawable.ic_heart),
                contentDescription = null,
                tint               = ColorTextoPrimario,
                modifier           = Modifier
                    .size(30.dp)
                    .scale(heartScale)
            )

            Spacer(Modifier.height(4.dp))

            // ── Valor BPM ─────────────────────────────────────────────────────
            Text(
                text  = if (uiState.bpm == 0) "--" else "${uiState.bpm}",
                style = MaterialTheme.typography.titleLarge.copy(
                    color      = ColorTextoPrimario,
                    fontWeight = FontWeight.Black,
                    fontSize   = 44.sp
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text  = stringResource(R.string.unit_bpm),
                style = MaterialTheme.typography.bodySmall.copy(
                    color    = ColorTextoSecundario,
                    fontSize = 11.sp
                )
            )

            Spacer(Modifier.height(6.dp))

            // ── Rótulo de status ──────────────────────────────────────────────
            val statusLabel = when (uiState.status) {
                HeartStatus.NORMAL  -> stringResource(R.string.status_normal)
                HeartStatus.ATENCAO -> stringResource(R.string.status_atencao)
                HeartStatus.ALARME  -> stringResource(R.string.status_alarme)
            }
            Text(
                text  = statusLabel,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color      = ColorTextoPrimario,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp
                ),
                textAlign = TextAlign.Center
            )

            // ── Mensagem de ação no alarme ────────────────────────────────────
            if (uiState.alarmActive) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = stringResource(R.string.alarm_message),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color    = ColorTextoPrimario,
                        fontSize = 9.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
