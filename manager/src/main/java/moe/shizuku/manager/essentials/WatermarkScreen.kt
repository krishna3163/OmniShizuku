package moe.shizuku.manager.essentials

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

data class ExifInfo(
    val make: String = "Google",
    val model: String = "Pixel 9 Pro",
    val focalLength: String = "24mm",
    val aperture: String = "f/1.7",
    val shutterSpeed: String = "1/1000s",
    val iso: String = "ISO 50",
    val date: String = "2026.10.25 14:32"
)

enum class WatermarkPreset {
    LEICA,
    HASSELBLAD,
    MINIMAL_STRIP
}

/**
 * Photo Watermark & EXIF Studio Engine
 * Authoritative port from Essentials (com.sameerasw.essentials.domain.watermark.WatermarkEngine)
 *
 * Implements lossless EXIF reading and luxury Leica red-dot & Hasselblad framing
 */
object WatermarkEngine {
    fun extractExif(context: Context, uri: Uri): ExifInfo {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val make = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim() ?: Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                val model = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim() ?: Build.MODEL
                val focal = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)
                val focalStr = if (focal > 0) "${focal.toInt()}mm" else "24mm"
                val fNumber = exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, 0.0)
                val fStr = if (fNumber > 0) "f/%.1f".format(fNumber) else "f/1.8"
                val exp = exif.getAttributeDouble(ExifInterface.TAG_EXPOSURE_TIME, 0.0)
                val expStr = if (exp > 0) {
                    if (exp < 1.0) "1/%d".format((1.0 / exp).toInt()) else "%.1fs".format(exp)
                } else "1/250s"
                val iso = exif.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, 0)
                val isoStr = if (iso > 0) "ISO $iso" else "ISO 100"
                val date = exif.getAttribute(ExifInterface.TAG_DATETIME)?.replace(":", ".")?.take(16) ?: "2026.10.25"

                ExifInfo(make, model, focalStr, fStr, expStr, isoStr, date)
            } ?: ExifInfo()
        } catch (_: Exception) {
            ExifInfo()
        }
    }

    suspend fun renderAndSaveWatermark(
        context: Context,
        uri: Uri,
        exif: ExifInfo,
        preset: WatermarkPreset,
        customAuthor: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val originalStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
            val original = BitmapFactory.decodeStream(originalStream) ?: return@withContext false
            originalStream.close()

            val w = original.width
            val h = original.height

            // Calculate banner height: ~12% of image height
            val bannerHeight = (h * 0.12f).toInt().coerceAtLeast(140)
            val outputBitmap = Bitmap.createBitmap(w, h + bannerHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(outputBitmap)

            // Draw original image
            canvas.drawBitmap(original, 0f, 0f, null)

            // Draw banner background
            val bannerPaint = Paint().apply {
                color = when (preset) {
                    WatermarkPreset.LEICA -> Color.WHITE
                    WatermarkPreset.HASSELBLAD -> Color.BLACK
                    WatermarkPreset.MINIMAL_STRIP -> Color.WHITE
                }
                style = Paint.Style.FILL
            }
            val bannerRect = RectF(0f, h.toFloat(), w.toFloat(), (h + bannerHeight).toFloat())
            canvas.drawRect(bannerRect, bannerPaint)

            // Text paints
            val primaryTextColor = if (preset == WatermarkPreset.HASSELBLAD) Color.WHITE else Color.BLACK
            val secondaryTextColor = if (preset == WatermarkPreset.HASSELBLAD) Color.LTGRAY else Color.DKGRAY

            val titlePaint = Paint().apply {
                color = primaryTextColor
                textSize = bannerHeight * 0.28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = secondaryTextColor
                textSize = bannerHeight * 0.19f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val marginX = w * 0.05f
            val centerY = h + (bannerHeight / 2f)

            // Draw Device Model + Author on Left
            val deviceText = if (customAuthor.isNotBlank()) "${exif.model} | $customAuthor" else exif.model
            canvas.drawText(deviceText, marginX, centerY - 6f, titlePaint)
            canvas.drawText(exif.date, marginX, centerY + (bannerHeight * 0.22f), subtitlePaint)

            // Draw EXIF stats on Right
            val exifStats = "${exif.focalLength}  ${exif.aperture}  ${exif.shutterSpeed}  ${exif.iso}"
            val statsWidth = titlePaint.measureText(exifStats)
            val statsX = w - marginX - statsWidth

            canvas.drawText(exifStats, statsX, centerY - 6f, titlePaint)

            // Draw Leica Red Dot or Hasselblad branding
            if (preset == WatermarkPreset.LEICA) {
                val dotRadius = bannerHeight * 0.16f
                val dotX = statsX - (dotRadius * 2.5f)
                val redDotPaint = Paint().apply {
                    color = Color.parseColor("#E50012") // Leica Red
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(dotX, centerY - 6f - (dotRadius * 0.3f), dotRadius, redDotPaint)
            }

            // Save to MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "OmniWatermark_${System.currentTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/OmniWatermark")
                }
            }

            val outputUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (outputUri != null) {
                val outStream: OutputStream? = context.contentResolver.openOutputStream(outputUri)
                outStream?.use { outputBitmap.compress(Bitmap.CompressFormat.JPEG, 98, it) }
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
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
    var exifInfo by remember { mutableStateOf(ExifInfo()) }
    var selectedPreset by remember { mutableStateOf(WatermarkPreset.LEICA) }
    var photographerSignature by remember { mutableStateOf("Shot on Omni") }
    var isSaving by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            exifInfo = WatermarkEngine.extractExif(context, uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Photo Watermark & EXIF Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Luxury Leica Frame & Lens EXIF", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Image(
                            bitmap = remember(selectedImageUri) {
                                val stream = context.contentResolver.openInputStream(selectedImageUri!!)
                                BitmapFactory.decodeStream(stream).asImageBitmap()
                            },
                            contentDescription = "Selected Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
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
                    Text("Watermark Frame Style", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedPreset == WatermarkPreset.LEICA,
                            onClick = { selectedPreset = WatermarkPreset.LEICA },
                            label = { Text("Leica Red Dot") },
                            leadingIcon = {
                                Box(modifier = Modifier.size(10.dp).background(androidx.compose.ui.graphics.Color(0xFFE50012), CircleShape))
                            }
                        )
                        FilterChip(
                            selected = selectedPreset == WatermarkPreset.HASSELBLAD,
                            onClick = { selectedPreset = WatermarkPreset.HASSELBLAD },
                            label = { Text("Hasselblad Dark") }
                        )
                        FilterChip(
                            selected = selectedPreset == WatermarkPreset.MINIMAL_STRIP,
                            onClick = { selectedPreset = WatermarkPreset.MINIMAL_STRIP },
                            label = { Text("Minimal White") }
                        )
                    }
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

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Camera Device", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(exifInfo.model, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Focal Length", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(exifInfo.focalLength, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Aperture", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(exifInfo.aperture, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shutter Speed", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(exifInfo.shutterSpeed, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ISO Sensitivity", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(exifInfo.iso, fontWeight = FontWeight.SemiBold)
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
                        val success = WatermarkEngine.renderAndSaveWatermark(
                            context = context,
                            uri = selectedImageUri!!,
                            exif = exifInfo,
                            preset = selectedPreset,
                            customAuthor = photographerSignature
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
