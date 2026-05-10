package com.example.weathersnap.ui.screens.weather

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

//Design Tokens
private val ScreenBg        = Color(0xFF1A1A1A)
private val CardDark        = Color(0xFF252525)
private val HeaderGradient  = Brush.horizontalGradient(listOf(Color(0xFFE8F0A0), Color(0xFF8BAA3A)))
private val TempBadgeBg     = Color(0xFF4A5C1A)
private val BtnBg           = Color(0xFFC8DC78)
private val BtnText         = Color(0xFF1A1A1A)
private val ReportsBtnBg    = Color(0xFF2A3A10)
private val ChipBlueTxt     = Color(0xFF4FC3F7)
private val ChipOrangeTxt   = Color(0xFFFFB74D)
private val ChipDarkBg      = Color(0xFF0D2A10)
private val ChipDarkBg2     = Color(0xFF0A1A2E)
private val ChipDarkBg3     = Color(0xFF2A1000)
private val ReadinessBg     = Color(0xFF1E1E1E)
private val ReadinessTag    = Color(0xFF2A3A10)

// Screen

@Composable
fun WeatherScreen(
    onNavigateToCreateReport: (
        cityName: String, temperature: String, condition: String,
        humidity: String, windSpeed: String, pressure: String,
        latitude: Double, longitude: Double
    ) -> Unit,
    onNavigateToSavedReports: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val cityQuery   by viewModel.cityQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val focusManager = LocalFocusManager.current

    WeatherScreenContent(
        uiState              = uiState,
        cityQuery            = cityQuery,
        suggestions          = suggestions,
        onQueryChange        = viewModel::onCityQueryChange,
        onSearch             = {
            focusManager.clearFocus()
            viewModel.searchWeather()
        },
        onSuggestionClick    = { suggestion ->
            focusManager.clearFocus()
            viewModel.onSuggestionSelected(suggestion)
        },
        onCreateReport       = { data ->
            onNavigateToCreateReport(
                data.cityName, data.temperature, data.condition,
                data.humidity, data.windSpeed, data.pressure,
                data.latitude, data.longitude
            )
        },
        onNavigateToReports  = onNavigateToSavedReports
    )
}

// Content (previewable)

@Composable
fun WeatherScreenContent(
    uiState            : WeatherUiState,
    cityQuery          : String,
    suggestions        : List<CitySuggestion>,
    onQueryChange      : (String) -> Unit,
    onSearch           : () -> Unit,
    onSuggestionClick  : (CitySuggestion) -> Unit,
    onCreateReport     : (WeatherDisplayData) -> Unit,
    onNavigateToReports: () -> Unit
) {
    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding      = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
    ) {
        //Header
        item { HeaderCard(onNavigateToReports = onNavigateToReports) }

        //Search
        item {
            SearchCard(
                query         = cityQuery,
                onQueryChange = onQueryChange,
                onSearch      = onSearch
            )
        }

        //Suggestions dropdown
        if (suggestions.isNotEmpty()) {
            item {
                SuggestionsCard(
                    suggestions       = suggestions,
                    onSuggestionClick = onSuggestionClick
                )
            }
        }

        //Weather state
        item {
            AnimatedContent(
                targetState    = uiState,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label          = "weather_state"
            ) { state ->
                when (state) {
                    is WeatherUiState.Idle    -> IdleHint()
                    is WeatherUiState.Loading -> WeatherLoadingCard()
                    is WeatherUiState.Empty   -> EmptyState()
                    is WeatherUiState.Error   -> ErrorState(state.message)
                    is WeatherUiState.Success -> WeatherSuccessCard(
                        data           = state.data,
                        onCreateReport = { onCreateReport(state.data) }
                    )
                }
            }
        }
    }
}

// Header Card

@Composable
private fun HeaderCard(onNavigateToReports: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HeaderGradient)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "WeatherSnap",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1A1A1A)
                )
            )
            Text(
                "Live weather reports with camera evidence",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1A1A1A).copy(alpha = 0.6f)
            )
        }
        Surface(
            shape   = RoundedCornerShape(8.dp),
            color   = ReportsBtnBg,
            onClick = onNavigateToReports
        ) {
            Text(
                "Reports",
                modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color      = Color.White,
                fontWeight = FontWeight.SemiBold,
                style      = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Search Card

@Composable
private fun SearchCard(
    query        : String,
    onQueryChange: (String) -> Unit,
    onSearch     : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value         = query,
                    onValueChange = onQueryChange,
                    label         = { Text("City", color = Color.White.copy(alpha = 0.5f)) },
                    singleLine    = true,
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White,
                        focusedBorderColor   = Color.White.copy(alpha = 0.4f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor          = Color.White
                    )
                )
                Surface(
                    shape   = RoundedCornerShape(10.dp),
                    color   = BtnBg,
                    onClick = onSearch
                ) {
                    Text(
                        "Search",
                        modifier   = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        fontWeight = FontWeight.SemiBold,
                        color      = BtnText,
                        style      = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Text(
                "Enter more than 2 letters to start city suggestions.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.45f)
            )
        }
    }
}

// Suggestions Card

@Composable
private fun SuggestionsCard(
    suggestions      : List<CitySuggestion>,
    onSuggestionClick: (CitySuggestion) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            suggestions.forEachIndexed { index, suggestion ->
                if (index > 0) HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color    = Color.White.copy(alpha = 0.08f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint     = BtnBg,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            suggestion.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color      = Color.White
                            )
                        )
                        val sub = listOfNotNull(suggestion.admin1, suggestion.country)
                            .filter { it.isNotEmpty() }.joinToString(", ")
                        if (sub.isNotEmpty()) {
                            Text(
                                sub,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Weather Success Card

@Composable
private fun WeatherSuccessCard(
    data          : WeatherDisplayData,
    onCreateReport: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // City + Temperature
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column {
                    Text(
                        data.cityName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    )
                    Text(
                        data.condition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                // Temperature badge
                Box(
                    modifier         = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TempBadgeBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${data.temperature}°C",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFFD4E870)
                        )
                    )
                }
            }

            // Stats row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip("Humidity",  "${data.humidity}%",      ChipBlueTxt,   ChipDarkBg,  Modifier.weight(1f))
                StatChip("Wind",      "${data.windSpeed} m/s",  ChipBlueTxt,   ChipDarkBg2, Modifier.weight(1f))
                StatChip("Pressure",  "${data.pressure} hPa",   ChipOrangeTxt, ChipDarkBg3, Modifier.weight(1f))
            }

            // Report readiness row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ReadinessBg)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Report readiness",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ReadinessTag)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Camera and Room DB enabled",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB8D060)
                    )
                }
            }

            // Create Report button
            Button(
                onClick  = onCreateReport,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = BtnBg,
                    contentColor   = BtnText
                )
            ) {
                Text("Create Report", fontWeight = FontWeight.SemiBold, color = BtnText)
            }
        }
    }
}

@Composable
private fun StatChip(
    label    : String,
    value    : String,
    textColor: Color,
    bgColor  : Color,
    modifier : Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f))
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold, color = textColor))
        }
    }
}

// State Composables

@Composable
private fun WeatherLoadingCard() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = BtnBg)
            Spacer(Modifier.height(12.dp))
            Text("Fetching weather...", color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No results found for that city.",
            color = Color.White.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF3A1010))
            .padding(20.dp)
    ) {
        Text(message, color = Color(0xFFFF6B6B),
            style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun IdleHint() {
    Box(
        modifier         = Modifier.fillMaxWidth().height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Search a city to see live weather.",
            color = Color.White.copy(alpha = 0.4f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

