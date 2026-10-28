package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.viewmodel.GalleryViewModel
import com.example.ui.viewmodel.ImageAdjustments
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(uri: Uri, viewModel: GalleryViewModel, onDismiss: () -> Unit) {
    val adjustmentsState by viewModel.adjustments.collectAsState()
    val initialAdjustments = adjustmentsState[uri] ?: ImageAdjustments()

    // Editor tools: "Crop", "Transform", "Filters", "Adjust"
    var selectedTool by remember { mutableStateOf("Crop") }

    // uCrop Aspect Ratios: "Free", "1:1", "3:4", "4:3", "9:16", "16:9", "Original"
    var selectedAspectRatio by remember { mutableStateOf("Free") }

    // Transform states
    var rotationDegrees by remember { mutableFloatStateOf(initialAdjustments.rotation) }
    var fineAngle by remember { mutableFloatStateOf(0f) } // -45 to +45
    var flipHorizontal by remember { mutableStateOf(initialAdjustments.flipHorizontal) }
    var flipVertical by remember { mutableStateOf(initialAdjustments.flipVertical) }

    // Adjust states
    var brightness by remember { mutableFloatStateOf(initialAdjustments.brightness) } // -0.5 to 0.5
    var contrast by remember { mutableFloatStateOf(initialAdjustments.contrast) } // 0.5 to 2.0
    var saturation by remember { mutableFloatStateOf(initialAdjustments.saturation) } // 0.0 to 2.0
    var temperature by remember { mutableFloatStateOf(0f) } // -0.3 to 0.3
    var vignette by remember { mutableFloatStateOf(0f) } // 0 to 1

    // Filter selection
    var activeFilter by remember { mutableStateOf(initialAdjustments.filter) }

    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    val aspectRatios = listOf("Free", "1:1", "3:4", "4:3", "9:16", "16:9", "Original")
    val filters = listOf("Original", "Warm", "Cool", "Vintage", "B&W", "Negative", "Sepia", "Cinema", "Vivid", "Noir")

    // ColorFilter computed with brightness, contrast, saturation, temperature, filter
    val colorFilter = remember(brightness, contrast, saturation, temperature, activeFilter) {
        val baseContrast = contrast
        val baseBrightness = brightness
        val baseSaturation = saturation

        val m = when (activeFilter) {
            "B&W" -> {
                val redCol = 0.213f
                val greenCol = 0.715f
                val blueCol = 0.072f
                floatArrayOf(
                    redCol * baseContrast, greenCol * baseContrast, blueCol * baseContrast, 0f, baseBrightness * 255f,
                    redCol * baseContrast, greenCol * baseContrast, blueCol * baseContrast, 0f, baseBrightness * 255f,
                    redCol * baseContrast, greenCol * baseContrast, blueCol * baseContrast, 0f, baseBrightness * 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            }
            "Sepia" -> {
                floatArrayOf(
                    0.393f * baseContrast, 0.769f * baseContrast, 0.189f * baseContrast, 0f, baseBrightness * 255f,
                    0.349f * baseContrast, 0.686f * baseContrast, 0.168f * baseContrast, 0f, baseBrightness * 255f,
                    0.272f * baseContrast, 0.534f * baseContrast, 0.131f * baseContrast, 0f, baseBrightness * 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            }
            "Vintage" -> {
                floatArrayOf(
                    0.4f * baseContrast, 0.6f * baseContrast, 0.2f * baseContrast, 0f, (baseBrightness + 0.05f) * 255f,
                    0.2f * baseContrast, 0.7f * baseContrast, 0.2f * baseContrast, 0f, baseBrightness * 255f,
                    0.1f * baseContrast, 0.3f * baseContrast, 0.6f * baseContrast, 0f, baseBrightness * 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            }
            "Negative" -> {
                floatArrayOf(
                    -1f * baseContrast, 0f, 0f, 0f, 255f + (baseBrightness * 255f),
                    0f, -1f * baseContrast, 0f, 0f, 255f + (baseBrightness * 255f),
                    0f, 0f, -1f * baseContrast, 0f, 255f + (baseBrightness * 255f),
                    0f, 0f, 0f, 1f, 0f
                )
            }
            "Warm" -> {
                val r = 1.15f + temperature
                val b = 0.85f - temperature
                floatArrayOf(
                    r * baseContrast, 0f, 0f, 0f, baseBrightness * 255f,
                    0f, 1.05f * baseContrast, 0f, 0f, baseBrightness * 255f,
                    0f, 0f, b * baseContrast, 0f, baseBrightness * 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            }
            "Cool" -> {
                val r = 0.85f - temperature
                val b = 1.20f + temperature
                floatArrayOf(
                    r * baseContrast, 0f, 0f, 0f, baseBrightness * 255f,
                    0f, 0.95f * baseContrast, 0f, 0f, baseBrightness * 255f,
                    0f, 0f, b * baseContrast, 0f, baseBrightness * 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            }
            "Cinema" -> {
                floatArrayOf(
                    1.1f * baseContrast, 0f, 0f, 0f, baseBrightness * 255f,
                    0f, 1.2f * baseContrast, 0f, 0f, baseBrightness * 255f,
                    0f, 0f, 1.3f * baseContrast, 0f, baseBrightness * 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            }
            "Vivid" -> {
                val sat = baseSaturation * 1.3f
                val redCol = 0.213f * (1f - sat)
                val greenCol = 0.715f * (1f - sat)
                val blueCol = 0.072f * (1f - sat)
                floatArrayOf(
                    (redCol + sat) * baseContrast * 1.1f, greenCol * baseContrast, blueCol * baseContrast, 0f, baseBrightness * 255f,
                    redCol * baseContrast, (greenCol + sat) * baseContrast * 1.1f, blueCol * baseContrast, 0f, baseBrightness * 255f,
                    redCol * baseContrast, greenCol * baseContrast, (blueCol + sat) * baseContrast * 1.1f, 0f, baseBrightness * 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            }
            "Noir" -> {
                floatArrayOf(
                    0.25f * baseContrast * 1.4f, 0.65f * baseContrast * 1.4f, 0.1f * baseContrast * 1.4f, 0f, (baseBrightness - 0.1f) * 255f,
                    0.25f * baseContrast * 1.4f, 0.65f * baseContrast * 1.4f, 0.1f * baseContrast * 1.4f, 0f, (baseBrightness - 0.1f) * 255f,
                    0.25f * baseContrast * 1.4f, 0.65f * baseContrast * 1.4f, 0.1f * baseContrast * 1.4f, 0f, (baseBrightness - 0.1f) * 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            }
            else -> {
                val redCol = 0.213f * (1f - baseSaturation)
                val greenCol = 0.715f * (1f - baseSaturation)
                val blueCol = 0.072f * (1f - baseSaturation)
                floatArrayOf(
                    (redCol + baseSaturation) * baseContrast, greenCol * baseContrast, blueCol * baseContrast, 0f, baseBrightness * 255f,
                    redCol * baseContrast, (greenCol + baseSaturation) * baseContrast, blueCol * baseContrast, 0f, baseBrightness * 255f,
                    redCol * baseContrast, greenCol * baseContrast, (blueCol + baseSaturation) * baseContrast, 0f, baseBrightness * 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            }
        }
        ColorFilter.colorMatrix(ColorMatrix(m))
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0C0C0C))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color(0xFF222222), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                }

                Text(
                    text = stringResource(R.string.editor),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Reset Button
                    IconButton(
                        onClick = {
                            rotationDegrees = 0f
                            fineAngle = 0f
                            flipHorizontal = false
                            flipVertical = false
                            brightness = 0f
                            contrast = 1f
                            saturation = 1f
                            temperature = 0f
                            vignette = 0f
                            activeFilter = "Original"
                            selectedAspectRatio = "Free"
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(Color(0xFF222222), CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                    }

                    // Save Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSaving = true
                                delay(1000L)
                                viewModel.saveAdjustments(
                                    uri = uri,
                                    adj = ImageAdjustments(
                                        rotation = rotationDegrees + fineAngle,
                                        flipHorizontal = flipHorizontal,
                                        flipVertical = flipVertical,
                                        brightness = brightness,
                                        contrast = contrast,
                                        saturation = saturation,
                                        filter = activeFilter
                                    )
                                )
                                isSaving = false
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Image Preview area with uCrop Overlay Frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val totalRotation = rotationDegrees + fineAngle
                val scaleX = if (flipHorizontal) -1f else 1f
                val scaleY = if (flipVertical) -1f else 1f

                // Image Aspect Ratio Box based on selectedAspectRatio
                val aspectModifier = when (selectedAspectRatio) {
                    "1:1" -> Modifier.aspectRatio(1f)
                    "3:4" -> Modifier.aspectRatio(3f / 4f)
                    "4:3" -> Modifier.aspectRatio(4f / 3f)
                    "9:16" -> Modifier.aspectRatio(9f / 16f)
                    "16:9" -> Modifier.aspectRatio(16f / 9f)
                    else -> Modifier.fillMaxSize()
                }

                Box(
                    modifier = aspectModifier,
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        colorFilter = colorFilter,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                rotationZ = totalRotation,
                                scaleX = scaleX,
                                scaleY = scaleY
                            )
                    )

                    // uCrop Grid Overlay canvas when cropping tool is selected
                    if (selectedTool == "Crop") {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw 3x3 uCrop Grid lines
                            val gridColor = Color.White.copy(alpha = 0.6f)
                            val strokeWidth = 1.dp.toPx()

                            // Vertical lines
                            drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = strokeWidth)
                            drawLine(gridColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), strokeWidth = strokeWidth)

                            // Horizontal lines
                            drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = strokeWidth)
                            drawLine(gridColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), strokeWidth = strokeWidth)

                            // Draw Border Frame
                            drawRect(
                                color = Color.White,
                                topLeft = Offset.Zero,
                                size = size,
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Draw Corner Handles (uCrop Style)
                            val handleLen = 20.dp.toPx()
                            val handleWidth = 4.dp.toPx()
                            val handleColor = Color.White

                            // Top-Left
                            drawLine(handleColor, Offset(0f, 0f), Offset(handleLen, 0f), strokeWidth = handleWidth)
                            drawLine(handleColor, Offset(0f, 0f), Offset(0f, handleLen), strokeWidth = handleWidth)

                            // Top-Right
                            drawLine(handleColor, Offset(w, 0f), Offset(w - handleLen, 0f), strokeWidth = handleWidth)
                            drawLine(handleColor, Offset(w, 0f), Offset(w, handleLen), strokeWidth = handleWidth)

                            // Bottom-Left
                            drawLine(handleColor, Offset(0f, h), Offset(handleLen, h), strokeWidth = handleWidth)
                            drawLine(handleColor, Offset(0f, h), Offset(0f, h - handleLen), strokeWidth = handleWidth)

                            // Bottom-Right
                            drawLine(handleColor, Offset(w, h), Offset(w - handleLen, h), strokeWidth = handleWidth)
                            drawLine(handleColor, Offset(w, h), Offset(w, h - handleLen), strokeWidth = handleWidth)
                        }
                    }
                }
            }

            // Controls & Panels Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161616), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(20.dp)
            ) {
                // 1. CROP TOOL PANEL
                AnimatedVisibility(visible = selectedTool == "Crop") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            "Aspect Ratio / uCrop Presets",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(aspectRatios) { ratio ->
                                val isSelected = selectedAspectRatio == ratio
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2C))
                                        .clickable { selectedAspectRatio = ratio }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = ratio,
                                        color = Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. TRANSFORM TOOL PANEL
                AnimatedVisibility(visible = selectedTool == "Transform") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButtonWithLabel(
                                icon = Icons.Default.RotateRight,
                                label = "Rotate 90°",
                                onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f }
                            )
                            IconButtonWithLabel(
                                icon = Icons.Default.Flip,
                                label = "Flip Horiz",
                                onClick = { flipHorizontal = !flipHorizontal }
                            )
                            IconButtonWithLabel(
                                icon = Icons.Default.Crop,
                                label = "Flip Vert",
                                onClick = { flipVertical = !flipVertical }
                            )
                        }

                        // Fine rotation angle slider (-45 to +45)
                        TuneSliderRow(
                            label = "Fine Angle Rotation",
                            value = fineAngle,
                            valueRange = -45f..45f,
                            onValueChange = { fineAngle = it }
                        )
                    }
                }

                // 3. FILTERS PANEL
                AnimatedVisibility(visible = selectedTool == "Filters") {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filters) { filterName ->
                            val isSelected = activeFilter == filterName
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { activeFilter = filterName }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF333333))
                                        .border(2.dp, if (isSelected) Color.White else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filterName.take(2).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = filterName,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // 4. ADJUST TUNING PANEL
                AnimatedVisibility(visible = selectedTool == "Adjust") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TuneSliderRow(
                            label = "Brightness",
                            value = brightness,
                            valueRange = -0.5f..0.5f,
                            onValueChange = { brightness = it }
                        )
                        TuneSliderRow(
                            label = "Contrast",
                            value = contrast,
                            valueRange = 0.5f..2.0f,
                            onValueChange = { contrast = it }
                        )
                        TuneSliderRow(
                            label = "Saturation",
                            value = saturation,
                            valueRange = 0.0f..2.0f,
                            onValueChange = { saturation = it }
                        )
                        TuneSliderRow(
                            label = "Temperature / Warmth",
                            value = temperature,
                            valueRange = -0.3f..0.3f,
                            onValueChange = { temperature = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Tool Navigation selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EditorNavButton(
                        icon = Icons.Default.Crop,
                        label = "Crop",
                        isSelected = selectedTool == "Crop",
                        onClick = { selectedTool = "Crop" }
                    )
                    EditorNavButton(
                        icon = Icons.Default.RotateRight,
                        label = "Transform",
                        isSelected = selectedTool == "Transform",
                        onClick = { selectedTool = "Transform" }
                    )
                    EditorNavButton(
                        icon = Icons.Default.Filter,
                        label = "Filters",
                        isSelected = selectedTool == "Filters",
                        onClick = { selectedTool = "Filters" }
                    )
                    EditorNavButton(
                        icon = Icons.Default.Tune,
                        label = "Adjust",
                        isSelected = selectedTool == "Adjust",
                        onClick = { selectedTool = "Adjust" }
                    )
                }
            }
        }

        // Saving Progress Indicator Dialog
        if (isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Processing uCrop edits...",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IconButtonWithLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF2B2B2B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun TuneSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = String.format("%.2f", value),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}

@Composable
fun EditorNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

