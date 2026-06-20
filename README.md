# HeartBPM - Wear OS Heart Rate Monitor

HeartBPM is a specialized Wear OS application designed for real-time heart rate monitoring, optimized for performance and stability on limited hardware.

## Key Features

- **Real-time Heart Rate Tracking**: Monitors heart rate via on-device sensors.
- **Aggressive Throttling**: Implements 3-second sampling and hardware-level event filtering (1.2s) to reduce CPU load and prevent system freezes.
- **Resource-Efficient UI**: Simplified Jetpack Compose UI without heavy animations to conserve battery and processing power.
- **Safe Hardware Interaction**: Off-loads `ToneGenerator` and `Vibrator` operations to background threads to ensure the UI remains responsive.

## Performance Optimizations

This app has been specifically tuned to avoid system hangs common in high-frequency sensor applications on Wear OS:

- **Sensor Filtering**: Uses `SENSOR_DELAY_UI` and a 1.2s minimum interval between hardware events in `WearHeartRateSensor`.
- **Data Conflation**: The `HeartRateViewModel` samples data every 3 seconds and uses `conflate()` to ensure only the latest reading is processed.
- **Background Cleanup**: `AlarmController` manages hardware resources using daemon threads to prevent blocking the Main Thread during stop operations.
- **Memory Management**: Optimized Gradle and Kotlin daemon configurations for stable builds.

## Technical Requirements

- **Platform**: Wear OS
- **Framework**: Jetpack Compose for Wear OS
- **Language**: Kotlin
- **Sensors**: Heart Rate Sensor (Permission: `BODY_SENSORS`)

## License

[Specify License Here]
