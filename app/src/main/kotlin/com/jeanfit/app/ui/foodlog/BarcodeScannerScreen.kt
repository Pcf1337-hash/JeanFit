package com.jeanfit.app.ui.foodlog

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.jeanfit.app.ui.theme.JeanFitTheme
import com.jeanfit.app.ui.theme.SunsetOrange
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(
    mealType: String,
    onBack: () -> Unit,
    onBarcodeFound: () -> Unit,
    viewModel: FoodSearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var isLooking by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(state.logSuccess) {
        if (state.logSuccess) onBarcodeFound()
    }

    LaunchedEffect(scannedBarcode) {
        val barcode = scannedBarcode ?: return@LaunchedEffect
        if (!isLooking) {
            isLooking = true
            // Try to find the food item and select it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode scannen", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Zurück") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (!hasCameraPermission) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Kamera-Zugriff benötigt",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
                    ) { Text("Berechtigung erteilen") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onBack,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) { Text("Manuell eingeben") }
                }
            }
        } else {
            Box(Modifier.fillMaxSize().padding(padding)) {
                val executor = remember { Executors.newSingleThreadExecutor() }
                val barcodeScanner = remember { BarcodeScanning.getClient() }

                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && scannedBarcode == null) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            barcodes.firstOrNull { it.format == Barcode.FORMAT_EAN_13 || it.format == Barcode.FORMAT_EAN_8 || it.format == Barcode.FORMAT_UPC_A }
                                                ?.rawValue?.let { scannedBarcode = it }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                            } catch (e: Exception) { /* handle */ }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Viewfinder overlay
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(280.dp, 140.dp).background(Color.White.copy(alpha = 0.1f)))
                        Spacer(Modifier.height(24.dp))
                        if (scannedBarcode != null) {
                            Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Gefunden: $scannedBarcode", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(8.dp))
                                    CircularProgressIndicator(color = SunsetOrange, modifier = Modifier.size(24.dp))
                                }
                            }
                        } else {
                            Text(
                                "Barcode in den Rahmen halten",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
