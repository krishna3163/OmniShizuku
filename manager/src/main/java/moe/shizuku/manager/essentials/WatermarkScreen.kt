package moe.shizuku.manager.essentials

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sameerasw.essentials.domain.watermark.ColorMode
import com.sameerasw.essentials.domain.watermark.ExifData
import com.sameerasw.essentials.domain.watermark.MetadataProvider
import com.sameerasw.essentials.domain.watermark.WatermarkEngine
import com.sameerasw.essentials.domain.watermark.WatermarkOptions
import com.sameerasw.essentials.domain.watermark.WatermarkStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private suspend fun saveWatermarkedImage(
    context: Context,
    uri: Uri,
    options: WatermarkOptions,
): Boolean = withContext(Dispatchers.IO) {
    var outputUri: Uri? = null
    var temporaryFile: java.io.File? = null
    try {
        val appContext = context.applicationContext
        temporaryFile = WatermarkEngine(appContext, MetadataProvider(appContext)).processImage(uri, options)
        val resolver = appContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "OmniWatermark_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/OmniWatermark")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        outputUri = resolver.insert(collection, values) ?: return@withContext false
        resolver.openOutputStream(outputUri)?.use { output ->
            temporaryFile.inputStream().use { input -> input.copyTo(output) }
        } ?: throw IllegalStateException("Unable to open output image")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.update(outputUri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
        }
        true
    } catch (_: Exception) {
        outputUri?.let { runCatching { context.contentResolver.delete(it, null, null) } }
        false
    } finally {
        temporaryFile?.delete()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var exifInfo by remember { mutableStateOf(ExifData()) }
    var selectedStyle by remember { mutableStateOf(WatermarkStyle.FRAME) }
    var selectedColorMode by remember { mutableStateOf(ColorMode.LIGHT) }
    var photographerSignature by remember { mutableStateOf("Shot on Omni") }
    var showDeviceBrand by remember { mutableStateOf(true) }
    var showExif by remember { mutableStateOf(true) }
    var showDate by remember { mutableStateOf(false) }
    var showFocalLength by remember { mutableStateOf(true) }
    var showAperture by remember { mutableStateOf(true) }
    var showShutterSpeed by remember { mutableStateOf(true) }
    var showIso by remember { mutableStateOf(true) }
    var moveToTop by remember { mutableStateOf(false) }
    var leftAlignOverlay by remember { mutableStateOf(false) }
    var padding by remember { mutableFloatStateOf(50f) }
    var borderStroke by remember { mutableFloatStateOf(0f) }
    var borderCorner by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var isSaving by remember { mutableStateOf(false) }
    val previewBitmap = remember(selectedImageUri) {
        selectedImageUri?.let { uri ->
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            coroutineScope.launch {
                exifInfo = withContext(Dispatchers.IO) {
                    MetadataProvider(context.applicationContext).extractExif(uri)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Photo Watermark & EXIF Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Essentials frame and overlay engine", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Selection Card / Preview
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clickable { photoPickerLauncher.launch("image/*") }
            ) {
                if (selectedImageUri == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to Select Photo from Gallery", fontWeight = FontWeight.SemiBold)
                            Text("Automatic camera lens & EXIF metadata detection", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap.asImageBitmap(),
                                contentDescription = "Selected Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Unable to preview this image", modifier = Modifier.align(Alignment.Center))
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f))
                                .padding(8.dp)
                        ) {
                            Text("Tap to change photo", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }

            // Watermark Preset Style
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Watermark style", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedStyle == WatermarkStyle.FRAME,
                            onClick = { selectedStyle = WatermarkStyle.FRAME },
                            label = { Text("Frame") }
                        )
                        FilterChip(
                            selected = selectedStyle == WatermarkStyle.OVERLAY,
                            onClick = { selectedStyle = WatermarkStyle.OVERLAY },
                            label = { Text("Overlay") }
                        )
                    }
                    Text("Color", fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            ColorMode.LIGHT to "Light",
                            ColorMode.DARK to "Dark",
                            ColorMode.ACCENT_LIGHT to "Accent light",
                            ColorMode.ACCENT_DARK to "Accent dark",
                        ).forEach { (mode, label) ->
                            FilterChip(
                                selected = selectedColorMode == mode,
                                onClick = { selectedColorMode = mode },
                                label = { Text(label) },
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Show camera make and model")
                        Switch(checked = showDeviceBrand, onCheckedChange = { showDeviceBrand = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Show camera EXIF details")
                        Switch(checked = showExif, onCheckedChange = { showExif = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Show photo date")
                        Switch(checked = showDate, onCheckedChange = { showDate = it })
                    }
                    if (showExif) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Text("EXIF fields", fontWeight = FontWeight.SemiBold)
                        listOf(
                            "Focal length" to showFocalLength,
                            "Aperture" to showAperture,
                            "Shutter speed" to showShutterSpeed,
                            "ISO" to showIso,
                        ).forEachIndexed { index, (label, checked) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(label)
                                Switch(
                                    checked = checked,
                                    onCheckedChange = { value ->
                                        when (index) {
                                            0 -> showFocalLength = value
                                            1 -> showAperture = value
                                            2 -> showShutterSpeed = value
                                            3 -> showIso = value
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // These options are rendered by Essentials' WatermarkEngine.
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Frame layout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (selectedStyle == WatermarkStyle.OVERLAY) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Place overlay at top")
                            Switch(checked = moveToTop, onCheckedChange = { moveToTop = it })
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Align overlay to the left")
                            Switch(checked = leftAlignOverlay, onCheckedChange = { leftAlignOverlay = it })
                        }
                    }
                    WatermarkSlider("Padding", padding, 0f..100f) { padding = it }
                    WatermarkSlider("Border thickness", borderStroke, 0f..20f) { borderStroke = it }
                    WatermarkSlider("Corner radius", borderCorner, 0f..50f) { borderCorner = it }
                    WatermarkSlider("Rotation", rotation, -180f..180f) { rotation = it }
                }
            }

            // Detected Metadata Details
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Live EXIF Parameters", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "The exported image keeps source EXIF metadata, including GPS location tags if present. Check before sharing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Camera Device", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(exifInfo.model ?: "Unknown", fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Focal Length", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(exifInfo.focalLength ?: "—", fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Aperture", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(exifInfo.aperture ?: "—", fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shutter Speed", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(exifInfo.shutterSpeed ?: "—", fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ISO Sensitivity", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(exifInfo.iso ?: "—", fontWeight = FontWeight.SemiBold)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    OutlinedTextField(
                        value = photographerSignature,
                        onValueChange = { photographerSignature = it },
                        label = { Text("Photographer Signature") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Export Button
            Button(
                onClick = {
                    if (selectedImageUri == null) {
                        Toast.makeText(context, "Please select an image first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    coroutineScope.launch {
                        isSaving = true
                        val success = saveWatermarkedImage(
                            context = context,
                            uri = selectedImageUri!!,
                            options = WatermarkOptions(
                                style = selectedStyle,
                                colorMode = selectedColorMode,
                                showDeviceBrand = showDeviceBrand,
                                showExif = showExif,
                                showFocalLength = showFocalLength,
                                showAperture = showAperture,
                                showShutterSpeed = showShutterSpeed,
                                showIso = showIso,
                                showDate = showDate,
                                showCustomText = photographerSignature.isNotBlank(),
                                customText = photographerSignature,
                                moveToTop = moveToTop,
                                leftAlignOverlay = leftAlignOverlay,
                                padding = padding.toInt(),
                                borderStroke = borderStroke.toInt(),
                                borderCorner = borderCorner.toInt(),
                                rotation = rotation.toInt(),
                                outputQuality = 98,
                            ),
                        )
                        isSaving = false
                        if (success) {
                            Toast.makeText(context, "Watermarked photo saved to Pictures/OmniWatermark!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Failed to save watermarked image", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedImageUri != null && !isSaving
            ) {
                Icon(Icons.Outlined.SaveAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSaving) "Rendering High-Res EXIF Frame..." else "Export Watermarked Photo")
            }

            Spacer(modifier = Modifier.height(bottomPadding + 24.dp))
        }
    }
}

@Composable
private fun WatermarkSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(value.toInt().toString(), color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}
