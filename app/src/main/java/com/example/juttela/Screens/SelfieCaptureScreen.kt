package com.example.juttela.Screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.juttela.Utils.CloudinaryUploader
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────
//  Theme
// ─────────────────────────────────────────────────────────────
private val Orange = Color(0xFFFF6D00)
private val DarkBg = Color(0xFF0E0E10)
private val CardBg = Color(0xFF1B1B1F)
private val SuccessGreen = Color(0xFF2ECC71)
private val Scrim = Color(0xB3000000)
private val SoftBlack = Color(0xCC000000)

// ─────────────────────────────────────────────────────────────
//  Quality thresholds (tune these by watching Logcat tag "Quality")
// ─────────────────────────────────────────────────────────────
private const val MIN_BRIGHTNESS = 60f
private const val MAX_BRIGHTNESS = 215f
private const val SELFIE_MIN_SHARPNESS = 12f   // Laplacian variance
private const val ID_MIN_SHARPNESS = 35f       // ID cards have text -> higher
private const val STABLE_FRAMES_REQUIRED = 4   // must be good for N frames in a row
private const val ID_ASPECT = 1.586f           // standard card ratio (85.6 x 54 mm)
private const val MAX_UPLOAD_SIDE = 1600

private val mainHandler = Handler(Looper.getMainLooper())

/** Camera / ML Kit callbacks run on background threads. ALL Compose state writes must go through this. */
private fun onMain(block: () -> Unit) { mainHandler.post(block) }

private enum class Step { SELFIE, ID_CARD, REVIEW }

private class StreakTracker {
    @Volatile var count = 0
    fun hit(): Boolean { count++; return count >= STABLE_FRAMES_REQUIRED }
    fun reset() { count = 0 }
}

private data class Quality(val brightness: Float, val sharpness: Float)

private fun defaultStatus(step: Step) = when (step) {
    Step.SELFIE -> "Position your face inside the circle"
    Step.ID_CARD -> "Place your ID inside the frame"
    Step.REVIEW -> ""
}

// ─────────────────────────────────────────────────────────────
//  Main screen
// ─────────────────────────────────────────────────────────────
@Composable
fun SelfieCaptureScreen(
    navController: NavController,
    userId: String,
    onUploadSuccess: (selfieUrl: String, idUrl: String) -> Unit = { _, _ -> },
    onUploadFailed: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    var step by remember { mutableStateOf(Step.SELFIE) }
    var statusText by remember { mutableStateOf(defaultStatus(Step.SELFIE)) }
    var isReady by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    var selfieBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var idBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selfieUrl by remember { mutableStateOf<String?>(null) } // cached so retry won't re-upload
    var idUrl by remember { mutableStateOf<String?>(null) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf("") }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val faceDetector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.25f)
                .build()
        )
    }
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val tracker = remember(step) { StreakTracker() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasCameraPermission = it }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(step) {
        statusText = defaultStatus(step)
        isReady = false
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            faceDetector.close()
            textRecognizer.close()
        }
    }

    BackHandler(enabled = step == Step.ID_CARD) { step = Step.SELFIE }

    if (!hasCameraPermission) {
        PermissionScreen(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) })
        return
    }

    // ── Live analysis for the current step ──────────────────
    val onResult: (String, Boolean) -> Unit = { text, ready ->
        onMain {
            statusText = text
            isReady = ready
        }
    }
    val analyzer: (ImageProxy) -> Unit = { proxy ->
        if (isCapturing) {
            proxy.close()
        } else if (step == Step.SELFIE) {
            processSelfieFrame(proxy, faceDetector, tracker, onResult)
        } else {
            processIdFrame(proxy, textRecognizer, tracker, onResult)
        }
    }

    // ── Shutter handler ─────────────────────────────────────
    val onShutter: () -> Unit = shutter@{
        if (!isReady || isCapturing) return@shutter
        val capturingStep = step
        isCapturing = true
        statusText = "Capturing…"

        capturePhoto(imageCapture, cameraExecutor) { bitmap ->
            // runs on the camera executor thread (heavy work is OK here)
            if (bitmap == null) {
                onMain {
                    statusText = "Capture failed, please try again"
                    isCapturing = false
                    isReady = false
                    tracker.reset()
                }
                return@capturePhoto
            }

            val minSharp = if (capturingStep == Step.SELFIE) SELFIE_MIN_SHARPNESS else ID_MIN_SHARPNESS
            val problem = qualityProblem(bitmapQuality(bitmap), minSharp)
            if (problem != null) {
                onMain {
                    statusText = "$problem Try again."
                    isCapturing = false
                    isReady = false
                    tracker.reset()
                }
                return@capturePhoto
            }

            val finalBitmap = bitmap.scaleDown(MAX_UPLOAD_SIDE)

            // Authoritative check: the captured photo must really contain a government ID
            if (capturingStep == Step.ID_CARD) {
                val idType = try {
                    val text = Tasks.await(textRecognizer.process(InputImage.fromBitmap(finalBitmap, 0)))
                    detectIdType(text.text)
                } catch (e: Exception) {
                    Log.e("IdCheck", "OCR failed", e)
                    null
                }
                if (idType == null) {
                    onMain {
                        statusText = "No government ID found in the photo. Try again."
                        isCapturing = false
                        isReady = false
                        tracker.reset()
                    }
                    return@capturePhoto
                }
                Log.d("IdCheck", "Captured ID type: $idType")
            }

            onMain {
                if (capturingStep == Step.SELFIE) {
                    selfieBitmap = finalBitmap
                    selfieUrl = null
                    step = if (idBitmap != null) Step.REVIEW else Step.ID_CARD
                } else {
                    idBitmap = finalBitmap
                    idUrl = null
                    step = Step.REVIEW
                }
                isCapturing = false
            }
        }
    }

    // ── Submit handler (uploads BOTH images) ────────────────
    val onSubmit: () -> Unit = submit@{
        val selfie = selfieBitmap ?: return@submit
        val id = idBitmap ?: return@submit
        if (isUploading) return@submit

        coroutineScope.launch {
            isUploading = true
            uploadError = null
            try {
                if (selfieUrl == null) {
                    uploadMessage = "Uploading selfie (1/2)…"
                    selfieUrl = uploadBitmap(context, selfie, userId, "selfie")
                }
                if (selfieUrl != null && idUrl == null) {
                    uploadMessage = "Uploading ID card (2/2)…"
                    idUrl = uploadBitmap(context, id, userId, "id_card")
                }

                val s = selfieUrl
                val i = idUrl
                if (s != null && i != null) {
                    Log.d("VerificationUpload", "✅ Both uploaded\nSelfie: $s\nID: $i")

                    Toast.makeText(
                        context.applicationContext,
                        "Your ID has been sent for review ✅",
                        Toast.LENGTH_LONG
                    ).show()

                    onUploadSuccess(s, i)
                } else {
                    uploadError = "Upload failed. Check your internet and try again."
                    Toast.makeText(
                        context.applicationContext,
                        "Upload failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                    onUploadFailed()
                }
            } catch (e: Exception) {
                Log.e("VerificationUpload", "Upload error", e)
                uploadError = "Something went wrong. Please try again."
                Toast.makeText(
                    context.applicationContext,
                    "Something went wrong. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                onUploadFailed()
            } finally {
                isUploading = false
            }
        }
    }

    Box(Modifier.fillMaxSize().background(DarkBg)) {
        when (step) {
            Step.SELFIE, Step.ID_CARD -> {
                val isSelfie = step == Step.SELFIE
                CaptureStage(
                    isSelfie = isSelfie,
                    selfieDone = selfieBitmap != null,
                    status = statusText,
                    isReady = isReady,
                    isCapturing = isCapturing,
                    cameraExecutor = cameraExecutor,
                    analyzer = analyzer,
                    onImageCapture = { imageCapture = it },
                    onShutter = onShutter
                )
            }

            Step.REVIEW -> ReviewStage(
                selfie = selfieBitmap,
                idCard = idBitmap,
                isUploading = isUploading,
                uploadMessage = uploadMessage,
                error = uploadError,
                onRetakeSelfie = {
                    selfieBitmap = null; selfieUrl = null; uploadError = null
                    step = Step.SELFIE
                },
                onRetakeId = {
                    idBitmap = null; idUrl = null; uploadError = null
                    step = Step.ID_CARD
                },
                onSubmit = onSubmit
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Capture stage (selfie OR id card)
// ─────────────────────────────────────────────────────────────
@Composable
private fun CaptureStage(
    isSelfie: Boolean,
    selfieDone: Boolean,
    status: String,
    isReady: Boolean,
    isCapturing: Boolean,
    cameraExecutor: ExecutorService,
    analyzer: (ImageProxy) -> Unit,
    onImageCapture: (ImageCapture) -> Unit,
    onShutter: () -> Unit
) {
    val guideColor by animateColorAsState(
        targetValue = if (isReady) SuccessGreen else Orange,
        animationSpec = tween(300),
        label = "guide"
    )

    Box(Modifier.fillMaxSize()) {
        // Re-bind camera (front/back) whenever the step changes
        CameraLayer(
            useFront = isSelfie,
            executor = cameraExecutor,
            maximizeQuality = !isSelfie,
            onImageCapture = onImageCapture,
            analyzer = analyzer
        )

        GuideOverlay(isCircle = isSelfie, color = guideColor)

        // Header: steps + title
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepIndicator(currentStep = if (isSelfie) 1 else 2, selfieDone = selfieDone)
            Spacer(Modifier.height(18.dp))
            Text(
                text = if (isSelfie) "Take a selfie" else "Scan your government ID",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isSelfie) "Look straight at the camera in good light"
                else "Aadhaar, Passport, Voter ID or Driving Licence",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        // Footer: tips, status, shutter
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isSelfie) {
                Text(
                    text = "✓ Good light   ✓ No glare   ✓ All 4 corners visible",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(12.dp))
            }
            StatusPill(text = status, isGood = isReady)
            Spacer(Modifier.height(24.dp))
            ShutterButton(enabled = isReady && !isCapturing, isBusy = isCapturing, onClick = onShutter)
        }

        if (isCapturing) {
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.35f)))
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, selfieDone: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepDot(number = 1, label = "Selfie", active = currentStep == 1, done = selfieDone && currentStep > 1 || (selfieDone && currentStep == 2))
        Box(
            Modifier
                .padding(horizontal = 10.dp)
                .width(36.dp)
                .height(2.dp)
                .background(if (currentStep == 2) Orange else Color.White.copy(alpha = 0.3f))
        )
        StepDot(number = 2, label = "ID Card", active = currentStep == 2, done = false)
    }
}

@Composable
private fun StepDot(number: Int, label: String, active: Boolean, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    when {
                        done -> SuccessGreen
                        active -> Orange
                        else -> Color.White.copy(alpha = 0.2f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text("$number", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            color = if (active || done) Color.White else Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun StatusPill(text: String, isGood: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SoftBlack)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isGood) SuccessGreen else Orange)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ShutterButton(enabled: Boolean, isBusy: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(88.dp)
                .border(3.dp, Color.White.copy(alpha = if (enabled) 0.95f else 0.4f), CircleShape)
        )
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange,
                disabledContainerColor = Orange.copy(alpha = 0.45f)
            ),
            modifier = Modifier.size(68.dp)
        ) {
            if (isBusy) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(26.dp))
            }
        }
    }
}

// Dark scrim with a transparent circle (selfie) or card-shaped hole (ID)
@Composable
private fun GuideOverlay(isCircle: Boolean, color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        drawRect(Scrim)
        val stroke = Stroke(width = 4.dp.toPx())
        if (isCircle) {
            val radius = size.minDimension * 0.38f
            val center = Offset(size.width / 2f, size.height * 0.43f)
            drawCircle(Color.Black, radius, center, blendMode = BlendMode.Clear)
            drawCircle(color, radius, center, style = stroke)
        } else {
            val w = size.width * 0.88f
            val h = w / ID_ASPECT
            val topLeft = Offset((size.width - w) / 2f, size.height * 0.43f - h / 2f)
            val corner = CornerRadius(16.dp.toPx())
            drawRoundRect(Color.Black, topLeft, Size(w, h), corner, blendMode = BlendMode.Clear)
            drawRoundRect(color, topLeft, Size(w, h), corner, style = stroke)
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Review stage
// ─────────────────────────────────────────────────────────────
@Composable
private fun ReviewStage(
    selfie: Bitmap?,
    idCard: Bitmap?,
    isUploading: Boolean,
    uploadMessage: String,
    error: String?,
    onRetakeSelfie: () -> Unit,
    onRetakeId: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Review your photos", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Make sure your face and ID details are clearly visible.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        PhotoCard(title = "Selfie", bitmap = selfie, aspect = 1f, enabled = !isUploading, onRetake = onRetakeSelfie)
        Spacer(Modifier.height(16.dp))
        PhotoCard(title = "Government ID", bitmap = idCard, aspect = ID_ASPECT, enabled = !isUploading, onRetake = onRetakeId)

        Spacer(Modifier.height(20.dp))

        if (error != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FF5252))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Warning, null, tint = Color(0xFFFF8A80))
                Spacer(Modifier.width(10.dp))
                Text(error, color = Color.White, fontSize = 14.sp)
            }
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = onSubmit,
            enabled = !isUploading && selfie != null && idCard != null,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange,
                disabledContainerColor = Orange.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(uploadMessage, color = Color.White, fontWeight = FontWeight.SemiBold)
            } else {
                Text(
                    if (error != null) "Try again" else "Submit for verification",
                    color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PhotoCard(
    title: String,
    bitmap: Bitmap?,
    aspect: Float,
    enabled: Boolean,
    onRetake: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = onRetake, enabled = enabled) {
                Icon(Icons.Filled.Refresh, null, tint = Orange, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Retake", color = Orange, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(if (aspect == 1f) 0.6f else 1f)
                    .align(Alignment.CenterHorizontally)
                    .aspectRatio(aspect)
                    .clip(RoundedCornerShape(14.dp))
            )
        }
    }
}

@Composable
private fun PermissionScreen(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Camera access needed", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "We need your camera to take a selfie and photo of your ID for verification.",
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRequest,
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(14.dp)
        ) { Text("Allow camera", color = Color.White) }
    }
}

// ─────────────────────────────────────────────────────────────
//  CameraX
// ─────────────────────────────────────────────────────────────
@Composable
private fun CameraLayer(
    useFront: Boolean,
    executor: ExecutorService,
    maximizeQuality: Boolean,
    onImageCapture: (ImageCapture) -> Unit,
    analyzer: (ImageProxy) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentAnalyzer by rememberUpdatedState(analyzer)

    // One PreviewView for the whole lifetime. TextureView mode avoids SurfaceView teardown crashes.
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Re-bind only the camera (front/back) — the view itself is never destroyed.
    DisposableEffect(useFront) {
        var disposed = false
        var provider: ProcessCameraProvider? = null
        var analysisUseCase: ImageAnalysis? = null
        val future = ProcessCameraProvider.getInstance(context)

        future.addListener({
            if (disposed) return@addListener
            val cameraProvider = future.get()
            provider = cameraProvider

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(
                    if (maximizeQuality) ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
                    else ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                )
                .build()
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(executor) { proxy -> currentAnalyzer(proxy) } }
            analysisUseCase = analysis

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    if (useFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,
                    preview, capture, analysis
                )
                onImageCapture(capture)
            } catch (e: Exception) {
                Log.e("CameraX", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            disposed = true
            try {
                analysisUseCase?.clearAnalyzer()
                provider?.unbindAll()
            } catch (e: Exception) {
                Log.e("CameraX", "Unbind failed", e)
            }
        }
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}

private fun capturePhoto(
    imageCapture: ImageCapture?,
    executor: ExecutorService,
    onResult: (Bitmap?) -> Unit
) {
    val capture = imageCapture ?: run { onResult(null); return }
    capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val bitmap = try {
                image.toBitmap().rotate(image.imageInfo.rotationDegrees)
            } catch (e: Exception) {
                Log.e("Capture", "Bitmap conversion failed", e)
                null
            } finally {
                image.close()
            }
            onResult(bitmap)
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("Capture", "Capture failed", exception)
            onResult(null)
        }
    })
}

// ─────────────────────────────────────────────────────────────
//  Live frame analysis
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalGetImage::class)
private fun processSelfieFrame(
    proxy: ImageProxy,
    detector: FaceDetector,
    tracker: StreakTracker,
    onResult: (String, Boolean) -> Unit
) {
    val mediaImage = proxy.image ?: run { proxy.close(); return }
    val rotation = proxy.imageInfo.rotationDegrees
    val image = InputImage.fromMediaImage(mediaImage, rotation)
    val upright = rotation % 180 == 0
    val imgW = if (upright) proxy.width else proxy.height
    val imgH = if (upright) proxy.height else proxy.width

    detector.process(image)
        .addOnSuccessListener { faces ->
            // Luma plane is still valid here (proxy closes in onComplete)
            val problem = evaluateFaces(faces, imgW, imgH)
                ?: qualityProblem(frameQuality(proxy), SELFIE_MIN_SHARPNESS)
            publish(problem, tracker, onResult, "Perfect! Tap the button")
        }
        .addOnFailureListener {
            tracker.reset()
            onResult("Detection error", false)
        }
        .addOnCompleteListener { proxy.close() }
}

@OptIn(ExperimentalGetImage::class)
private fun processIdFrame(
    proxy: ImageProxy,
    recognizer: TextRecognizer,
    tracker: StreakTracker,
    onResult: (String, Boolean) -> Unit
) {
    // 1) cheap checks first: light + sharpness
    val q = frameQuality(proxy)
    Log.d("Quality", "ID brightness=${q.brightness} sharpness=${q.sharpness}")
    val qualityIssue = qualityProblem(q, ID_MIN_SHARPNESS)
    if (qualityIssue != null) {
        tracker.reset()
        onResult(qualityIssue, false)
        proxy.close()
        return
    }

    // 2) is there actually a government ID in front of the camera?
    val mediaImage = proxy.image ?: run { proxy.close(); return }
    val rotation = proxy.imageInfo.rotationDegrees
    val imgW = if (rotation % 180 == 0) proxy.width else proxy.height
    val image = InputImage.fromMediaImage(mediaImage, rotation)

    recognizer.process(image)
        .addOnSuccessListener { text ->
            val type = detectIdType(text.text)
            val problem = idTextProblem(text, imgW, type)
            publish(problem, tracker, onResult, "${type ?: "ID"} detected! Tap the button")
        }
        .addOnFailureListener {
            tracker.reset()
            onResult("Could not read the ID, try again", false)
        }
        .addOnCompleteListener { proxy.close() }
}

private fun idTextProblem(text: Text, imgW: Int, type: String?): String? {
    if (text.text.isBlank()) return "No ID detected — place your ID inside the frame"
    if (type == null) return "This doesn't look like a government ID"

    // The ID should fill most of the frame width
    val boxes = text.textBlocks.mapNotNull { it.boundingBox }
    if (boxes.isNotEmpty()) {
        val span = boxes.maxOf { it.right } - boxes.minOf { it.left }
        if (span.toFloat() / imgW < 0.5f) return "Move closer — fill the frame with your ID"
    }
    return null
}

// ─────────────────────────────────────────────────────────────
//  Government ID detection (Indian IDs) from OCR text
// ─────────────────────────────────────────────────────────────
private val AADHAAR_NUMBER = Regex("""\b\d{4}\s\d{4}\s\d{4}\b""")
private val PAN_NUMBER = Regex("""\b[A-Z]{5}[0-9]{4}[A-Z]\b""")
private val PASSPORT_MRZ = Regex("""P[<«][A-Z]{3}""")
private val VOTER_EPIC = Regex("""\b[A-Z]{3}[0-9]{7}\b""")
private val DL_NUMBER = Regex("""\b[A-Z]{2}[-\s]?\d{2}[-\s]?\d{4}[-\s]?\d{7}\b""")

/** Returns the ID type name, or null when the text does not look like a government ID. */
private fun detectIdType(raw: String): String? {
    if (raw.isBlank()) return null
    val t = raw.lowercase()
    val up = raw.uppercase()

    // Aadhaar
    if ("aadhaar" in t || "aadhar" in t || "uidai" in t || "unique identification" in t) return "Aadhaar"
    if (AADHAAR_NUMBER.containsMatchIn(raw) &&
        ("government of india" in t || "year of birth" in t || "dob" in t || "female" in t || "male" in t)
    ) return "Aadhaar"

    // PAN card
    if (("income tax" in t || "permanent account" in t) && PAN_NUMBER.containsMatchIn(up)) return "PAN card"

    // Passport
    if (PASSPORT_MRZ.containsMatchIn(up)) return "Passport"
    if ("passport" in t && ("republic of india" in t || "nationality" in t)) return "Passport"

    // Driving licence
    if ("driving licen" in t) return "Driving Licence"
    if ("union of india" in t && DL_NUMBER.containsMatchIn(up)) return "Driving Licence"

    // Voter ID
    if ("election commission" in t) return "Voter ID"
    if (VOTER_EPIC.containsMatchIn(up) && "elector" in t) return "Voter ID"

    return null
}

private fun publish(
    problem: String?,
    tracker: StreakTracker,
    onResult: (String, Boolean) -> Unit,
    readyText: String
) {
    if (problem != null) {
        tracker.reset()
        onResult(problem, false)
    } else if (tracker.hit()) {
        onResult(readyText, true)
    } else {
        onResult("Hold still…", false)
    }
}

/** Returns a human message when the face is not good enough, or null when it's fine. */
private fun evaluateFaces(faces: List<Face>, imgW: Int, imgH: Int): String? {
    if (faces.isEmpty()) return "No face detected"
    if (faces.size > 1) return "Only one face allowed"

    val face = faces[0]
    val box = face.boundingBox

    val ratio = (box.width() * box.height()).toFloat() / (imgW * imgH)
    if (ratio < 0.18f) return "Move closer"
    if (ratio > 0.65f) return "Move a little back"

    val cx = box.exactCenterX() / imgW
    val cy = box.exactCenterY() / imgH
    if (abs(cx - 0.5f) > 0.15f || abs(cy - 0.45f) > 0.18f) return "Center your face in the circle"

    if (abs(face.headEulerAngleY) > 12) return "Look straight at the camera"
    if (abs(face.headEulerAngleX) > 15) return "Keep your head straight"

    val left = face.leftEyeOpenProbability ?: 0f
    val right = face.rightEyeOpenProbability ?: 0f
    if (left < 0.4f || right < 0.4f) return "Open your eyes"

    return null
}

// ─────────────────────────────────────────────────────────────
//  Image quality (brightness + sharpness via variance of Laplacian)
// ─────────────────────────────────────────────────────────────
private fun qualityProblem(q: Quality, minSharpness: Float): String? = when {
    q.brightness < MIN_BRIGHTNESS -> "Too dark — move to a brighter place"
    q.brightness > MAX_BRIGHTNESS -> "Too bright — avoid glare"
    q.sharpness < minSharpness -> "Image is blurry — hold steady"
    else -> null
}

private inline fun computeQuality(w: Int, h: Int, luma: (Int, Int) -> Int): Quality {
    val x0 = w / 4; val x1 = w * 3 / 4
    val y0 = h / 4; val y1 = h * 3 / 4
    val step = 2
    var sum = 0L
    var n = 0
    var lapSum = 0.0
    var lapSqSum = 0.0

    var y = y0
    while (y < y1) {
        var x = x0
        while (x < x1) {
            val c = luma(x, y)
            sum += c
            val lap = 4 * c - luma(x - step, y) - luma(x + step, y) - luma(x, y - step) - luma(x, y + step)
            lapSum += lap
            lapSqSum += lap.toDouble() * lap
            n++
            x += step
        }
        y += step
    }
    if (n == 0) return Quality(0f, 0f)
    val mean = lapSum / n
    val variance = (lapSqSum / n - mean * mean).toFloat()
    return Quality(brightness = sum.toFloat() / n, sharpness = variance)
}

private fun frameQuality(proxy: ImageProxy): Quality {
    val plane = proxy.planes[0]
    val buffer = plane.buffer
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride
    return computeQuality(proxy.width, proxy.height) { x, y ->
        buffer.get(y * rowStride + x * pixelStride).toInt() and 0xFF
    }
}

private fun bitmapQuality(bitmap: Bitmap): Quality {
    val w = 640
    val h = (w.toFloat() * bitmap.height / bitmap.width).toInt().coerceAtLeast(1)
    val small = Bitmap.createScaledBitmap(bitmap, w, h, true)
    val pixels = IntArray(w * h)
    small.getPixels(pixels, 0, w, 0, 0, w, h)
    if (small !== bitmap) small.recycle()
    val q = computeQuality(w, h) { x, y -> (pixels[y * w + x] shr 8) and 0xFF } // green channel ≈ luma
    Log.d("Quality", "Captured brightness=${q.brightness} sharpness=${q.sharpness}")
    return q
}

// ─────────────────────────────────────────────────────────────
//  Bitmap + upload helpers
// ─────────────────────────────────────────────────────────────
private fun Bitmap.rotate(degrees: Int): Bitmap {
    if (degrees == 0) return this
    val m = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, m, true)
}

private fun Bitmap.scaleDown(maxSide: Int): Bitmap {
    val longest = maxOf(width, height)
    if (longest <= maxSide) return this
    val scale = maxSide.toFloat() / longest
    return Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
}

private suspend fun uploadBitmap(
    context: android.content.Context,
    bitmap: Bitmap,
    userId: String,
    label: String
): String? {
    val uri = withContext(Dispatchers.IO) { bitmapToUri(context, bitmap, label) }
    Log.d("VerificationUpload", "Uploading $label for userId=$userId uri=$uri")
    val url = CloudinaryUploader.uploadVerificationImage(
        context = context,
        imageUri = uri,
        userId = userId,
        imageName = "${label}_${System.currentTimeMillis()}"
    )
    withContext(Dispatchers.IO) { uri.path?.let { File(it).delete() } } // clean temp file
    return url
}

private fun bitmapToUri(context: android.content.Context, bitmap: Bitmap, label: String): Uri {
    val file = File(context.cacheDir, "temp_${label}_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    return Uri.fromFile(file)
}