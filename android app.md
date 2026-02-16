# Weather Station Android App

## Open in Android Studio
 Launch **Android Studio**.
 Go to **File > Open**.
 Navigate to and select the `firmware/AndroidApp` folder.
 Wait for Gradle to sync.

## Configure the Backend URL
 Open `app/src/main/java/com/z0b1/weatherstation/MainActivity.kt`.
 Locate the `baseUrl` variable.
 Replace the placeholder with your **zrok** reserved public URL:
   ```kotlin
   val baseUrl = "https://your-name.share.zrok.io"
   ```

### Build & Install
 Connect your Android phone via USB (with Developer Mode and USB Debugging enabled).
 Click the Run (green triangle) button in Android Studio.
 The app will install and start fetching live data from your Raspberry Pi.
 Or download the APK from the APK folder.

## Features
- Live Monitoring: Real-time Temperature and Humidity.
- Wind Analytics: Visual compass for wind direction and speed.
- Satellite Gallery: View the latest images captured by your RTL-SDR.
- Frost Alerts: Automatic notifications when temperature drops below 1°C.
- Background Sync: Updates every 10 seconds while active.

## Internet Access (zrok)
1. Install **zrok** on your Raspberry Pi.
2. Run the following command to share your API (port 8000) publicly:
   ```bash
   zrok share public http://localhost:8000 --backend-mode proxy
   ```
3. Copy the URL provided by zrok and paste it into `MainActivity.kt`.


---
## NAPRED ZVEZDO
