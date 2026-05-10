package com.example.weathersnap.ui.screens.reports

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
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
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.weathersnap.ui.screen.reports.SavedReportsViewModel
import java.text.SimpleDateFormat
import java.util.*

// Design Tokens (matches WeatherScreen / CreateReportScreen)
private val ScreenBg       = Color(0xFF1A1A1A)
private val CardDark       = Color(0xFF252525)
private val HeaderGradient = Brush.horizontalGradient(listOf(Color(0xFFE8F0A0), Color(0xFF8BAA3A)))
private val BackBtnBg      = Color(0xFF2A3A10)
private val TempBadgeBg    = Color(0xFF4A5C1A)
private val TempBadgeText  = Color(0xFFD4E870)
private val ChipBlueTxt    = Color(0xFF4FC3F7)
private val ChipOrangeTxt  = Color(0xFFFFB74D)
private val ChipOrigBg     = Color(0xFF0D2A10)
private val ChipCompBg     = Color(0xFF0A1A2E)
private val NoteTagBg      = Color(0xFF2A3A10)
private val NoteTagText    = Color(0xFFB8D060)

//  Screen

@Composable
fun SavedReportsScreen(
    onBack   : () -> Unit,
    viewModel: SavedReportsViewModel = hiltViewModel()
) {
    val reports by viewModel.reports.collectAsState()

    SavedReportsContent(
        reports = reports,
        onBack  = onBack
    )
}

//  Content (previewable)

@Composable
fun SavedReportsContent(
    reports: List<WeatherReportUiModel>,
    onBack : () -> Unit
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
        item {
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
                        "Saved Reports",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF1A1A1A)
                        )
                    )
                    Text(
                        if (reports.isEmpty()) "No reports yet"
                        else "${reports.size} report${if (reports.size == 1) "" else "s"} stored locally",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1A1A1A).copy(alpha = 0.6f)
                    )
                }
                Surface(
                    shape   = RoundedCornerShape(8.dp),
                    color   = BackBtnBg,
                    onClick = onBack
                ) {
                    Text(
                        "Back",
                        modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        //Empty state
        if (reports.isEmpty()) {
            item { EmptyReportsState() }
        }

        //Report cards
        items(items = reports, key = { it.id }) { report ->
            AnimatedVisibility(
                visible = true,
                enter   = fadeIn(tween(300)) + expandVertically(tween(300))
            ) {
                ReportCard(report = report)
            }
        }
    }
}

//  Report Card

@Composable
private fun ReportCard(report: WeatherReportUiModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
    ) {
        Column {

            // Captured image
            SubcomposeAsyncImage(
                model              = report.imagePath,
                contentDescription = "Report photo",
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale       = ContentScale.Crop,
                error              = {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFF0D0D0D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No image",
                            color = Color.White.copy(alpha = 0.3f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            // Details
            Column(
                modifier            = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // City + condition + timestamp + temp badge
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            report.cityName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        )
                        Text(
                            report.condition,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            formatTimestamp(report.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f)
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
                            "${report.temperature}°C",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color      = TempBadgeText
                            )
                        )
                    }
                }

                // Image size chips
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SizeChip(
                        label    = "Original",
                        size     = report.originalImageSize,
                        txtColor = ChipBlueTxt,
                        bgColor  = ChipOrigBg,
                        modifier = Modifier.weight(1f)
                    )
                    SizeChip(
                        label    = "Compressed",
                        size     = report.compressedImageSize,
                        txtColor = ChipOrangeTxt,
                        bgColor  = ChipCompBg,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Notes tag (only if non-empty)
                if (report.notes.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NoteTagBg)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            report.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = NoteTagText
                        )
                    }
                }
            }
        }
    }
}

//  Size Chip

@Composable
private fun SizeChip(
    label   : String,
    size    : Long,
    txtColor: Color,
    bgColor : Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = txtColor.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                formatFileSize(size),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color      = txtColor
                )
            )
        }
    }
}

//  Empty State

@Composable
private fun EmptyReportsState() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier           = Modifier.size(64.dp),
                tint               = Color.White.copy(alpha = 0.2f)
            )
            Text(
                "No reports yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White.copy(alpha = 0.5f)
            )
            Text(
                "Search for a city, capture a photo, and save your first weather report.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.35f)
            )
        }
    }
}

//  Helpers

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024     -> "${bytes / 1_024} KB"
    else               -> "$bytes B"
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(millis))

//  UI Model

data class WeatherReportUiModel(
    val id                 : Long,
    val cityName           : String,
    val temperature        : String,
    val condition          : String,
    val humidity           : String,
    val windSpeed          : String,
    val pressure           : String,
    val imagePath          : String,
    val notes              : String,
    val originalImageSize  : Long,
    val compressedImageSize: Long,
    val timestamp          : Long
)

