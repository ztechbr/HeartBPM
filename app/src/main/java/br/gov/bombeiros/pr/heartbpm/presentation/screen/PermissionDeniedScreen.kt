package br.gov.bombeiros.pr.heartbpm.presentation.screen

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import br.gov.bombeiros.pr.heartbpm.R

/**
 * Tela exibida quando o usuário nega as permissões de sensor corporal.
 * Instrui como habilitar manualmente nas configurações do relógio.
 */
@Composable
fun PermissionDeniedScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF212121))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_warning),
                contentDescription = null,
                tint               = Color(0xFFFFCC00),
                modifier           = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.permission_title),
                style     = MaterialTheme.typography.titleSmall.copy(color = Color.White),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = stringResource(R.string.permission_instruction),
                style = MaterialTheme.typography.bodySmall.copy(
                    color    = Color.White.copy(alpha = 0.75f),
                    fontSize = 9.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Tela exibida quando o hardware de sensor cardíaco não está disponível.
 */
@Composable
fun SensorUnavailableScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text      = stringResource(R.string.sensor_unavailable),
                style     = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                textAlign = TextAlign.Center
            )
        }
    }
}
