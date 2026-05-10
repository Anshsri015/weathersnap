package com.example.weathersnap.data.repository

import com.example.weathersnap.data.remote.GeocodingApiService
import com.example.weathersnap.data.remote.WeatherApiService
import com.example.weathersnap.ui.screens.weather.CitySuggestion
import com.example.weathersnap.ui.screens.weather.WeatherDisplayData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val geocodingApi : GeocodingApiService,
    private val weatherApi   : WeatherApiService
) {

    //City Suggestions
    suspend fun getSuggestions(query: String): Result<List<CitySuggestion>> = runCatching {
        val response = geocodingApi.searchCities(name = query)
        response.results?.map { result ->
            CitySuggestion(
                id        = result.id,
                name      = result.name,
                country   = result.country,
                admin1    = result.admin1,
                latitude  = result.latitude,
                longitude = result.longitude
            )
        } ?: emptyList()
    }

    //Current Weather
    suspend fun getWeather(
        latitude  : Double,
        longitude : Double,
        cityName  : String
    ): Result<WeatherDisplayData> = runCatching {
        val response = weatherApi.getWeather(latitude, longitude)
        val current  = response.current ?: error("No weather data returned")
        WeatherDisplayData(
            cityName    = cityName,
            temperature = "%.1f".format(current.temperature),
            condition   = weatherCodeToCondition(current.weatherCode),
            humidity    = current.humidity.toString(),
            windSpeed   = "%.2f".format(current.windSpeed),
            pressure    = "%.0f".format(current.pressure),
            latitude    = latitude,
            longitude   = longitude
        )
    }

    //WMO Weather Code → Human-readable condition
    private fun weatherCodeToCondition(code: Int): String = when (code) {
        0            -> "Clear sky"
        1            -> "Mainly clear"
        2            -> "Partly cloudy"
        3            -> "Overcast"
        45, 48       -> "Foggy"
        51, 53, 55   -> "Drizzle"
        61, 63, 65   -> "Rain"
        71, 73, 75   -> "Snow"
        77           -> "Snow grains"
        80, 81, 82   -> "Rain showers"
        85, 86       -> "Snow showers"
        95           -> "Thunderstorm"
        96, 99       -> "Thunderstorm with hail"
        else         -> "Unknown"
    }
}
