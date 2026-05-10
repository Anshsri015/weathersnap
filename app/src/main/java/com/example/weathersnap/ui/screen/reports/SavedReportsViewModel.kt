package com.example.weathersnap.ui.screen.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersnap.data.local.WeatherReportEntity
import com.example.weathersnap.data.repository.ReportRepository
import com.example.weathersnap.ui.screens.reports.WeatherReportUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.collections.emptyList

//ViewModel stub (fill logic in code phase)

@HiltViewModel
class SavedReportsViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    val reports: StateFlow<List<WeatherReportUiModel>> = repository
        .getAllReports()
        .map { list -> list.map { it.toUiModel() } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
}


// ADD THIS BELOW THE VIEWMODEL

private fun WeatherReportEntity.toUiModel(): WeatherReportUiModel {
    return WeatherReportUiModel(
        id = id,
        cityName = cityName,
        temperature = temperature,
        condition = condition,
        humidity = humidity,
        windSpeed = windSpeed,
        pressure = pressure,
        imagePath = imagePath,
        notes = notes,
        originalImageSize = originalImageSize,
        compressedImageSize = compressedImageSize,
        timestamp = timestamp
    )
}
