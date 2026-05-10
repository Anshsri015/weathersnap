package com.example.weathersnap.ui.screens.camera

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.weathersnap.util.ImageCompressor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//Colors
private val BtnBg   = Color(0xFFA8B84A)
private val BtnText = Color(0xFF1A1A1A)

//Screen (with permissions + CameraX)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onImageCaptured: (imagePath: String, originalSize: Long, compressedSize: Long) -> Unit,
    onClose        : () -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (cameraPermission.status.isGranted) {
            CameraPreviewContent(
                onImageCaptured = onImageCaptured,
                onClose         = onClose
            )
        } else {
            PermissionDeniedContent(
                onRequestPermission = { cameraPermission.launchPermissionRequest() },
                onClose             = onClose
            )
        }
    }
}

//Camera Preview (with CameraX)

@Composable
private fun CameraPreviewContent(
    onImageCaptured: (String, Long, Long) -> Unit,
    onClose        : () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing  by remember { mutableStateOf(false) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType          = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            imageCapture = capture
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
            } catch (e: Exception) {
                Log.e("CameraScreen", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Live camera preview
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // UI overlay on top of camera
        CameraScreenContent(
            isCapturing = isCapturing,
            onClose     = onClose,
            onCapture   = {
                isCapturing = true
                captureImage(
                    context      = context,
                    imageCapture = imageCapture,
                    executor     = cameraExecutor,
                    onSuccess    = { path, orig, comp ->
                        isCapturing = false
                        onImageCaptured(path, orig, comp)
                    },
                    onError = { isCapturing = false }
                )
            }
        )
    }
}

//Content (no CameraX — previewable)

@Composable
fun CameraScreenContent(
    isCapturing: Boolean = false,
    onClose    : () -> Unit = {},
    onCapture  : () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Top bar: Title + Close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                "Custom Camera",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            )
            Surface(
                shape   = RoundedCornerShape(50),
                color   = Color.White.copy(alpha = 0.18f),
                onClick = onClose
            ) {
                Text(
                    "Close",
                    modifier   = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    color      = Color.White,
                    fontWeight = FontWeight.Medium,
                    style      = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Bottom: Capture button
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCapturing) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Button(
                    onClick  = onCapture,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(50),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = BtnBg,
                        contentColor   = BtnText
                    )
                ) {
                    Text("Capture", fontWeight = FontWeight.SemiBold, color = BtnText)
                }
            }
        }
    }
}

//Image Capture Logic

private fun captureImage(
    context     : Context,
    imageCapture: ImageCapture?,
    executor    : ExecutorService,
    onSuccess   : (path: String, originalSize: Long, compressedSize: Long) -> Unit,
    onError     : () -> Unit
) {
    val capture = imageCapture ?: run { onError(); return }

    val outputFile = File(
        context.filesDir,
        "weathersnap_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.jpg"
    )

    capture.takePicture(
        ImageCapture.OutputFileOptions.Builder(outputFile).build(),
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val originalSize   = outputFile.length()
                val compressedFile = compressImage(context, outputFile)
                val compressedSize = compressedFile?.length() ?: originalSize
                val finalPath      = compressedFile?.absolutePath ?: outputFile.absolutePath

                // ✅ YAHI FIX HAI — Main thread pe bhejo
                ContextCompat.getMainExecutor(context).execute {
                    onSuccess(finalPath, originalSize, compressedSize)
                }
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Capture failed: ${exception.message}", exception)

                // ✅ Yeh bhi fix karo
                ContextCompat.getMainExecutor(context).execute {
                    onError()
                }
            }
        }
    )
}

private fun compressImage(context: Context, originalFile: File): File? {
    return try {
        val (compressedFile,_) = ImageCompressor.compress(context,originalFile)
        File(compressedFile)
        // val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
        // FileOutputStream(compressedFile).use { out ->
        //     bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        // }

    } catch (e: Exception) {
        Log.e("CameraScreen", "Compression failed", e)
        null
    }
}

//Permission Denied

@Composable
private fun PermissionDeniedContent(
    onRequestPermission: () -> Unit,
    onClose            : () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.Camera, contentDescription = null,
                modifier = Modifier.size(64.dp), tint = Color.White)
            Text("Camera Permission Required",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White, fontWeight = FontWeight.Bold))
            Text("This screen needs camera access to capture weather evidence photos.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f)))
            Button(onClick = onRequestPermission) { Text("Grant Permission") }
            TextButton(onClick = onClose) {
                Text("Go Back", color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

