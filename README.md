# WeatherSnap 🌤️📸

WeatherSnap is a modern Android application that combines live 
weather data with camera-based field reporting. Users can search 
any city, view real-time weather conditions, capture a photo as 
evidence using a custom camera interface, add notes, and save 
the complete report locally.

## Features
- 🔍 Live city search with autocomplete suggestions
- 🌡️ Real-time weather data (temperature, humidity, wind, pressure)
- 📸 Custom camera screen built with CameraX (no system intent)
- 🗜️ Automatic image compression with size comparison
- 📝 Field notes for each weather report
- 💾 Local report storage using Room Database
- 📋 Saved reports viewer with full weather + photo details

## Tech Stack
- Kotlin + Jetpack Compose
- MVVM Architecture
- Hilt (Dependency Injection)
- Retrofit + Gson (REST API)
- Open-Meteo API (no API key needed)
- CameraX (custom camera)
- Room Database
- Coroutines + StateFlow
- Material 3
- Coil (image loading)
- Navigation Compose

## Setup
1. Clone the repository
2. Open in Android Studio Hedgehog or newer
3. Sync Gradle
4. Run on device or emulator (API 24+)
5. No API key required

## App Flow
Search City → Select Suggestion → View Live Weather → 
Create Report → Capture Photo → Add Notes → 
Save Report → View Saved Reports
