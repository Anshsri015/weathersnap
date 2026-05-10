package com.example.weathersnap.ui.screens.createreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersnap.data.local.WeatherReportEntity
import com.example.weathersnap.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//UI State

sealed class CreateReportUiState {
    object Idle   : CreateReportUiState()
    object Saving : CreateReportUiState()
    object Saved  : CreateReportUiState()
    data class Error(val message: String) : CreateReportUiState()
}

 // ViewModel stub (fill logic in code phase)

 @HiltViewModel
 class CreateReportViewModel @Inject constructor(
     private val repository: ReportRepository
 ) : ViewModel() {

     private val _uiState = MutableStateFlow<CreateReportUiState>(CreateReportUiState.Idle)
     val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

     private val _notes = MutableStateFlow("")
     val notes: StateFlow<String> = _notes.asStateFlow()

     private val _imagePath = MutableStateFlow<String?>(null)
     val imagePath: StateFlow<String?> = _imagePath.asStateFlow()

     private val _originalSize = MutableStateFlow(0L)
     val originalSize: StateFlow<Long> = _originalSize.asStateFlow()

     private val _compressedSize = MutableStateFlow(0L)
     val compressedSize: StateFlow<Long> = _compressedSize.asStateFlow()

     fun onNotesChange(value: String) { _notes.value = value }

     fun onImageReceived(path: String, original: Long, compressed: Long) {
         _imagePath.value = path
         _originalSize.value = original
         _compressedSize.value = compressed
     }

     fun saveReport(
         cityName: String, temperature: String, condition: String,
         humidity: String, windSpeed: String, pressure: String,
         latitude: Double, longitude: Double
     ) {
         viewModelScope.launch(Dispatchers.IO) {
             _uiState.value = CreateReportUiState.Saving
             try {
                 repository.saveReport(
                     WeatherReportEntity(
                         cityName = cityName, temperature = temperature,
                         condition = condition, humidity = humidity,
                         windSpeed = windSpeed, pressure = pressure,
                         imagePath = _imagePath.value ?: "",
                         notes = _notes.value,
                         originalImageSize = _originalSize.value,
                         compressedImageSize = _compressedSize.value,
                         timestamp = System.currentTimeMillis()
                     )
                 )
                 _uiState.value = CreateReportUiState.Saved
             } catch (e: Exception) {
                 _uiState.value = CreateReportUiState.Error(e.message ?: "Save failed")
             }
         }
     }
 }
