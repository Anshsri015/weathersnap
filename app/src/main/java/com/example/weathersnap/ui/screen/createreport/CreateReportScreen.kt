package com.example.weathersnap.ui.screens.createreport

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import coil.compose.AsyncImage

//Colors
private val ScreenBg      = Color(0xFF1A1A1A)
private val HeaderText    = Color(0xFF1A1A1A)
private val BackBtnBg     = Color(0xFF2A2A2A)
private val CardBg        = Color(0xFF252525)
private val PhotoAreaBg   = Brush.verticalGradient(listOf(Color(0xFF1E2A0A), Color(0xFF0D1205)))
private val BtnBg         = Color(0xFFA8B84A)
private val BtnText       = Color(0xFF1A1A1A)
private val TempYellow    = Color(0xFFD4C84A)
private val ChipBlueTxt   = Color(0xFF4FC3F7)
private val ChipOrangeTxt = Color(0xFFFFB74D)
private val ChipBg        = Color(0xFF333333)

//Screen (with ViewModel)

@Composable
fun CreateReportScreen(
    cityName                : String,
    temperature             : String,
    condition               : String,
    humidity                : String,
    windSpeed               : String,
    pressure                : String,
    latitude                : Double,
    longitude               : Double,
    onNavigateToCamera      : () -> Unit,
    onNavigateToSavedReports: () -> Unit,
    onBack                  : () -> Unit,
    savedStateHandle        : SavedStateHandle?,
    viewModel               : CreateReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notes   by viewModel.notes.collectAsState()

    val imagePath      = savedStateHandle?.get<String>("imagePath")
    val originalSize   = savedStateHandle?.get<Long>("originalSize")
    val compressedSize = savedStateHandle?.get<Long>("compressedSize")

    LaunchedEffect(imagePath) {
        imagePath?.let { viewModel.onImageReceived(it, originalSize ?: 0L, compressedSize ?: 0L) }
    }
    LaunchedEffect(uiState) {
        if (uiState is CreateReportUiState.Saved) onNavigateToSavedReports()
    }

    CreateReportScreenContent(
        cityName       = cityName,
        temperature    = temperature,
        condition      = condition,
        humidity       = humidity,
        windSpeed      = windSpeed,
        pressure       = pressure,
        notes          = notes,
        imagePath      = viewModel.imagePath.collectAsState().value,
        originalSize   = viewModel.originalSize.collectAsState().value,
        compressedSize = viewModel.compressedSize.collectAsState().value,
        isSaving       = uiState is CreateReportUiState.Saving,
        isError        = uiState is CreateReportUiState.Error,
        errorMsg       = (uiState as? CreateReportUiState.Error)?.message ?: "",
        onNotesChange  = viewModel::onNotesChange,
        onCaptureClick = onNavigateToCamera,
        onSaveClick    = {
            viewModel.saveReport(
                cityName, temperature, condition,
                humidity, windSpeed, pressure, latitude, longitude
            )
        },
        onBack         = onBack
    )
}

//Content (no ViewModel — previewable)

@Composable
fun CreateReportScreenContent(
    cityName      : String,
    temperature   : String,
    condition     : String,
    humidity      : String,
    windSpeed     : String,
    pressure      : String,
    notes         : String,
    imagePath     : String?,
    originalSize  : Long,
    compressedSize: Long,
    isSaving      : Boolean,
    isError       : Boolean,
    errorMsg      : String,
    onNotesChange : (String) -> Unit,
    onCaptureClick: () -> Unit,
    onSaveClick   : () -> Unit,
    onBack        : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Header card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFE8F0A0), Color(0xFFA8B84A))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Create Report",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color      = HeaderText
                    )
                )
                Text(
                    "Capture, compress, annotate",
                    style = MaterialTheme.typography.bodySmall,
                    color = HeaderText.copy(alpha = 0.55f)
                )
            }
            Surface(
                shape   = RoundedCornerShape(50),
                color   = BackBtnBg,
                onClick = onBack
            ) {
                Text(
                    "Back",
                    modifier   = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color      = Color.White,
                    fontWeight = FontWeight.Medium,
                    style      = MaterialTheme.typography.bodyMedium
                )
            }
        }

        //Weather card
        WeatherSnapshotCard(cityName, temperature, condition, humidity, windSpeed, pressure)

        //Photo section
        PhotoSection(
            capturedImagePath = imagePath,
            originalSize      = originalSize,
            compressedSize    = compressedSize,
            onCaptureClick    = onCaptureClick
        )

        //Field Notes
        FieldNotesSection(notes = notes, onNotesChange = onNotesChange)

        //Save Report button
        Button(
            onClick  = onSaveClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(10.dp),
            enabled  = !isSaving,
            colors   = ButtonDefaults.buttonColors(containerColor = BtnBg, contentColor = BtnText)
        ) {
            if (isSaving) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = BtnText)
                Spacer(Modifier.width(8.dp))
                Text("Saving...", fontWeight = FontWeight.SemiBold, color = BtnText)
            } else {
                Text("Save Report", fontWeight = FontWeight.SemiBold, color = BtnText)
            }
        }

        // Error state
        if (isError) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(errorMsg, modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// Weather Snapshot Card

@Composable
private fun WeatherSnapshotCard(
    cityName: String, temperature: String, condition: String,
    humidity: String, windSpeed: String, pressure: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column {
                    Text(cityName, style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold, color = Color.White))
                    Text(condition, style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f))
                }
                Text("$temperature°C", style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold, color = TempYellow))
            }

            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("Humidity", "$humidity%",     ChipBlueTxt,   Color(0xFF0D2E2A), Modifier.weight(1f))
                StatChip("Wind",     "$windSpeed m/s", ChipBlueTxt,   Color(0xFF0D1E3A), Modifier.weight(1f))
                StatChip("Pressure", "$pressure hPa",  ChipOrangeTxt, Color(0xFF2E1500), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, valueColor: Color, bgColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = valueColor.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold, color = valueColor))
        }
    }
}

//  Photo Section

@Composable
private fun PhotoSection(
    capturedImagePath: String?,
    originalSize     : Long,
    compressedSize   : Long,
    onCaptureClick   : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            AnimatedContent(
                targetState    = capturedImagePath,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                label          = "image_preview"
            ) { path ->
                if (path != null) {
                    AsyncImage(
                        model              = path,
                        contentDescription = "Captured photo",
                        modifier           = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PhotoAreaBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Photo preview", color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            AnimatedVisibility(
                visible = capturedImagePath != null,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SizeChip("Original",   originalSize,   Modifier.weight(1f))
                    SizeChip("Compressed", compressedSize, Modifier.weight(1f))
                }
            }

            Button(
                onClick  = onCaptureClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = BtnBg, contentColor = BtnText)
            ) {
                Text("Capture Photo", fontWeight = FontWeight.SemiBold, color = BtnText)
            }
        }
    }
}

@Composable
private fun SizeChip(label: String, size: Long, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ChipBg)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f))
            Text(formatFileSize(size), style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold, color = ChipBlueTxt))
        }
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024     -> "${bytes / 1_024} KB"
    else               -> "$bytes B"
}

//Field Notes

@Composable
private fun FieldNotesSection(notes: String, onNotesChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        Column {
            Text("Field Notes", style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold, color = Color.White))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value         = notes,
                onValueChange = onNotesChange,
                placeholder   = { Text("Notes", color = Color.White.copy(alpha = 0.4f)) },
                modifier      = Modifier.fillMaxWidth().height(120.dp),
                shape         = RoundedCornerShape(10.dp),
                maxLines      = 5,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedTextColor     = Color.White,
                    unfocusedTextColor   = Color.White,
                    focusedBorderColor   = Color.White.copy(alpha = 0.4f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    cursorColor          = Color.White
                )
            )
        }
    }
}

