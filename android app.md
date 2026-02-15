# Weather Station Android App

This is a native Android application built using Kotlin and Jetpack Compose. 

## Open in Android Studio
 Launch **Android Studio**.
 Go to **File > Open**.
 Navigate to and select the `firmware/AndroidApp` folder.
 Wait for Gradle to sync.

## Configure the Backend URL
 Open `app/src/main/java/com/z0b1/weatherstation/MainActivity.kt`.
 Locate the `baseUrl` variable.
 Replace the placeholder with your DuckDNS or public URL:
   ```kotlin
   val baseUrl = "http://yourname.duckdns.org:8000"
   ```

### Build & Install
 Connect your Android phone via USB (with Developer Mode and USB Debugging enabled).
 Click the Run (green triangle) button in Android Studio.
 The app will install and start fetching live data from your Raspberry Pi.

## Features
- Live Monitoring: Real-time Temperature and Humidity.
- Wind Analytics: Visual compass for wind direction and speed.
- Satellite Gallery: View the latest images captured by your RTL-SDR.
- Background Sync: Updates every 10 seconds while active.

## Internet Access (DuckDNS)
To access your station from anywhere:
- Run the `duck.sh` script on your Raspberry Pi.
- Ensure Port 8000 is forwarded on your router to the RPi's local IP.

---
NAPRED ZVEZDO
