package com.example.weathersnap.ui.screens.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersnap.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State

sealed class WeatherUiState {
    object Idle    : WeatherUiState()
    object Loading : WeatherUiState()
    object Empty   : WeatherUiState()
    data class Success(val data: WeatherDisplayData) : WeatherUiState()
    data class Error(val message: String)            : WeatherUiState()
}

// Display Models

data class WeatherDisplayData(
    val cityName    : String,
    val temperature : String,
    val condition   : String,
    val humidity    : String,
    val windSpeed   : String,
    val pressure    : String,
    val latitude    : Double,
    val longitude   : Double
)

data class CitySuggestion(
    val id      : Int,
    val name    : String,
    val country : String,
    val admin1  : String?,
    val latitude : Double,
    val longitude: Double
)

// ViewModel stub (fill logic in code phase)
//
 @HiltViewModel
 class WeatherViewModel @Inject constructor(
     private val repository: WeatherRepository
 ) : ViewModel() {

     private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
     val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

     private val _cityQuery = MutableStateFlow("")
     val cityQuery: StateFlow<String> = _cityQuery.asStateFlow()

     private val _suggestions = MutableStateFlow<List<CitySuggestion>>(emptyList())
     val suggestions: StateFlow<List<CitySuggestion>> = _suggestions.asStateFlow()

     // suggestion cache: query -> list
     private val suggestionCache = mutableMapOf<String, List<CitySuggestion>>()

     fun onCityQueryChange(query: String) {
         _cityQuery.value = query
         if (query.length > 2) fetchSuggestions(query) else _suggestions.value = emptyList()
     }

     private fun fetchSuggestions(query: String) {
         viewModelScope.launch {
             suggestionCache[query]?.let { _suggestions.value = it; return@launch }
             val result = repository.getSuggestions(query)
             result.onSuccess { list ->
                 suggestionCache[query] = list
                 _suggestions.value = list
             }
         }
     }

     fun onSuggestionSelected(suggestion: CitySuggestion) {
         _cityQuery.value = suggestion.name
         _suggestions.value = emptyList()
         fetchWeather(suggestion.latitude, suggestion.longitude, suggestion.name)
     }

     fun searchWeather() { /* trigger from search button */ }

     private fun fetchWeather(lat: Double, lon: Double, cityName: String) {
         viewModelScope.launch {
             _uiState.value = WeatherUiState.Loading
             val result = repository.getWeather(lat, lon, cityName)
             _uiState.value = result.fold(
                 onSuccess = { WeatherUiState.Success(it) },
                 onFailure = { WeatherUiState.Error(it.message ?: "Unknown error") }
             )
         }
     }
 }
