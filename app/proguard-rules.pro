# Regras ProGuard para HeartBPM

# Mantém classes do Wear OS
-keep class com.google.android.wearable.** { *; }

# Mantém ViewModel (evita remoção pelo R8 em release)
-keep class br.gov.bombeiros.pr.heartbpm.presentation.HeartRateViewModel { *; }
-keep class br.gov.bombeiros.pr.heartbpm.presentation.HeartRateViewModelFactory { *; }

# Mantém modelos do domínio
-keep class br.gov.bombeiros.pr.heartbpm.domain.** { *; }
