package br.gov.bombeiros.pr.heartbpm.presentation.screen

import androidx.compose.animation.animateColorAsState
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

private val ColorVerdeNormal   = Color(0xFF1B5E20)
private val ColorAmbarAtencao  = Color(0xFFE65100)
private val ColorVermelhoAlarme = Color(0xFFB71C1C)
private val ColorTextoPrimario = Color.White
private val ColorTextoSecundario = Color.White.copy(alpha = 0.78f)

@Composable
fun HeartRateScreen(uiState: HeartRateUiState) {

    // Mantemos apenas a transição de cor, que é menos custosa
    val bgColor by animateColorAsState(
        targetValue = when (uiState.status) {
            HeartStatus.NORMAL  -> ColorVerdeNormal
            HeartStatus.ATENCAO -> ColorAmbarAtencao
            HeartStatus.ALARME  -> ColorVermelhoAlarme
        },
        animationSpec = tween(durationMillis = 1000), // Mais lenta para poupar CPU
        label = "bgColorAnimation"
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
            Text(
                text  = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleSmall.copy(
                    color      = ColorTextoPrimario,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 12.sp
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(6.dp))

            Icon(
                painter            = painterResource(R.drawable.ic_heart),
                contentDescription = null,
                tint               = ColorTextoPrimario,
                modifier           = Modifier.size(32.dp)
            )

            Spacer(Modifier.height(4.dp))

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
        }
    }
}
