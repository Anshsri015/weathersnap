package com.example.weathersnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weathersnap.ui.screens.reports.WeatherReportUiModel

@Entity(tableName = "weather_reports")
data class WeatherReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id                  : Long   = 0,
    val cityName            : String,
    val temperature         : String,
    val condition           : String,
    val humidity            : String,
    val windSpeed           : String,
    val pressure            : String,
    val imagePath           : String,
    val notes               : String,
    val originalImageSize   : Long,
    val compressedImageSize : Long,
    val timestamp           : Long
)
