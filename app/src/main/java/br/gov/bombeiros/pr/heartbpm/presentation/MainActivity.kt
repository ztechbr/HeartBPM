package br.gov.bombeiros.pr.heartbpm.presentation

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.gov.bombeiros.pr.heartbpm.presentation.screen.HeartRateScreen
import br.gov.bombeiros.pr.heartbpm.presentation.screen.PermissionDeniedScreen
import br.gov.bombeiros.pr.heartbpm.presentation.screen.SensorUnavailableScreen
import br.gov.bombeiros.pr.heartbpm.presentation.theme.HeartBPMTheme

private const val TAG = "HeartBPM.MainActivity"

/**
 * Única Activity do app.
 *
 * Responsabilidades:
 *  1. Instalar Splash Screen.
 *  2. Verificar e solicitar permissões de runtime (BODY_SENSORS + ACTIVITY_RECOGNITION).
 *  3. Notificar o [HeartRateViewModel] sobre o resultado das permissões.
 *  4. Observar [HeartRateUiState.alarmActive] e acionar/parar [AlarmController].
 *  5. Renderizar a UI correta conforme o estado.
 *
 * O ViewModel nunca conhece Context, Activity ou AlarmController —
 * toda lógica de hardware fica aqui ou em classes específicas.
 */
class MainActivity : ComponentActivity() {

    // ── Dependências ──────────────────────────────────────────────────────────

    private val viewModel: HeartRateViewModel by lazy {
        ViewModelProvider(
            this,
            HeartRateViewModelFactory(applicationContext)
        )[HeartRateViewModel::class.java]
    }

    private val alarmController: AlarmController by lazy {
        AlarmController(applicationContext)
    }

    // ── Permissões ────────────────────────────────────────────────────────────

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.BODY_SENSORS,
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )

    /**
     * Launcher moderno para solicitação de múltiplas permissões.
     * Substitui o deprecated [onRequestPermissionsResult].
     */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        Log.d(TAG, "Resultado de permissões: $results → allGranted=$allGranted")

        if (allGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    private fun arePermissionsGranted(): Boolean =
        requiredPermissions.all { perm ->
            ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
        }

    private fun checkAndRequestPermissions() {
        if (arePermissionsGranted()) {
            Log.d(TAG, "Permissões já concedidas.")
            viewModel.onPermissionGranted()
        } else {
            Log.d(TAG, "Solicitando permissões: ${requiredPermissions.toList()}")
            permissionLauncher.launch(requiredPermissions)
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        setContent {
            HeartBPMTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Controla alarme como efeito colateral do estado — fora do Compose
                // (feito no LaunchedEffect via lifecycleScope no bloco abaixo)

                when {
                    !uiState.sensorAvailable -> SensorUnavailableScreen()

                    uiState.permissionState == PermissionState.DENIED ->
                        PermissionDeniedScreen()

                    uiState.permissionState == PermissionState.GRANTED ->
                        HeartRateScreen(uiState = uiState)

                    // UNKNOWN: aguardando resultado do dialog de permissão
                    else -> Unit
                }
            }
        }

        // Observa alarmActive fora do Compose para controlar hardware
        observeAlarmState()
    }

    override fun onStop() {
        super.onStop()
        alarmController.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmController.release()
    }

    // ── Alarme ───────────────────────────────────────────────────────────────

    /**
     * Coleta [HeartRateUiState.alarmActive] no lifecycleScope da Activity
     * e aciona [AlarmController] de forma síncrona com o ciclo de vida.
     */
    private fun observeAlarmState() {
        lifecycle.addObserver(
            AlarmLifecycleObserver(
                flow             = viewModel.uiState,
                alarmController  = alarmController,
                lifecycle        = lifecycle
            )
        )
    }
}
