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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.jeanfit.app.ui.theme.OceanBlue
import com.jeanfit.app.ui.theme.TealAccent
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
    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(state.logSuccess) {
        if (state.logSuccess) onBarcodeFound()
    }

    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error)
        viewModel.clearError()
        // Reset scanning so user can try a different barcode
        scannedBarcode = null
        isLooking = false
    }

    LaunchedEffect(scannedBarcode) {
        val barcode = scannedBarcode ?: return@LaunchedEffect
        if (!isLooking) {
            isLooking = true
            viewModel.searchByBarcode(barcode)
        }
    }

    if (state.selectedItem != null) {
        FoodDetailSheet(
            item = state.selectedItem!!,
            servingSize = state.servingSize,
            mealType = mealType,
            onServingChange = viewModel::setServingSize,
            onLog = { viewModel.logFood(mealType) },
            onDismiss = {
                viewModel.clearSelection()
                scannedBarcode = null
                isLooking = false
            },
            isLogging = state.isLogging
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode scannen", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Zurück") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { padding ->
        if (!hasCameraPermission) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        "Kamera-Zugriff benötigt",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                    ) { Text("Berechtigung erteilen") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onBack,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) { Text("Manuell eingeben") }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val executor = remember { Executors.newSingleThreadExecutor() }
                val barcodeScanner = remember { BarcodeScanning.getClient() }

                // Camera preview
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
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            barcodes.firstOrNull {
                                                it.format == Barcode.FORMAT_EAN_13 ||
                                                it.format == Barcode.FORMAT_EAN_8 ||
                                                it.format == Barcode.FORMAT_UPC_A
                                            }?.rawValue?.let { scannedBarcode = it }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) { /* handle */ }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Animated scan line
                val infiniteTransition = rememberInfiniteTransition(label = "scan")
                val lineY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scanLine"
                )

                // Scan overlay: corners + animated line
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cornerLength = 60f
                    val cornerStroke = 8f
                    val margin = size.width * 0.1f
                    val top = size.height * 0.2f
                    val bottom = size.height * 0.8f
                    val left = margin
                    val right = size.width - margin
                    val cornerColor = Color(0xFF1565C0)

                    // Scan line (TealAccent, animates within the scan frame)
                    drawLine(
                        color = Color(0xFF00BCD4),
                        start = Offset(left, top + lineY * (bottom - top)),
                        end = Offset(right, top + lineY * (bottom - top)),
                        strokeWidth = 4f
                    )

                    // Top-left corner
                    drawLine(cornerColor, Offset(left, top + cornerLength), Offset(left, top), strokeWidth = cornerStroke)
                    drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth = cornerStroke)
                    // Top-right corner
                    drawLine(cornerColor, Offset(right - cornerLength, top), Offset(right, top), strokeWidth = cornerStroke)
                    drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLength), strokeWidth = cornerStroke)
                    // Bottom-left corner
                    drawLine(cornerColor, Offset(left, bottom - cornerLength), Offset(left, bottom), strokeWidth = cornerStroke)
                    drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth = cornerStroke)
                    // Bottom-right corner
                    drawLine(cornerColor, Offset(right - cornerLength, bottom), Offset(right, bottom), strokeWidth = cornerStroke)
                    drawLine(cornerColor, Offset(right, bottom - cornerLength), Offset(right, bottom), strokeWidth = cornerStroke)
                }

                // Status overlay (centered below the scan frame)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = (0.8f * 700).dp) // Positioned below scan area
                    ) {
                        if (scannedBarcode != null && state.isSearching) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Suche Produkt...",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    CircularProgressIndicator(
                                        color = TealAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Instruction text at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 48.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (scannedBarcode == null || !state.isSearching) {
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
