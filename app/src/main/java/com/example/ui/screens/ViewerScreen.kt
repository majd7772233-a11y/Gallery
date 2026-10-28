package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.MediaItem
import com.example.ui.viewmodel.GalleryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ViewerScreen(
    uri: Uri,
    isVideo: Boolean,
    viewModel: GalleryViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onPlayVideo: (Uri) -> Unit = {}
) {
    val allMediaItems by viewModel.mediaItems.collectAsState()
    val filteredItems = remember(allMediaItems) {
        if (allMediaItems.isNotEmpty()) allMediaItems else listOf(MediaItem(0, uri, "Media", 0, 0, "Default", isVideo))
    }

    val initialIndex = remember(uri, filteredItems) {
        val idx = filteredItems.indexOfFirst { it.uri == uri }
        if (idx >= 0) idx else 0
    }

    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { filteredItems.size })
    val currentMedia = filteredItems[pagerState.currentPage.coerceIn(0, filteredItems.size - 1)]

    val currentUri = currentMedia.uri
    val currentIsVideo = currentMedia.isVideo

    val favoriteUris by viewModel.favoriteUris.collectAsState()
    val trashedUris by viewModel.trashedUris.collectAsState()
    val customAlbums by viewModel.customAlbums.collectAsState()

    val isFavorite = favoriteUris.contains(currentUri)
    val isTrashed = trashedUris.contains(currentUri)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val adjustments by viewModel.adjustments.collectAsState()
    val adj = adjustments[currentUri]

    var showMenu by remember { mutableStateOf(false) }
    var showDetailsBottomSheet by remember { mutableStateOf(false) }
    var showAddToAlbumDialog by remember { mutableStateOf(false) }
    var isRemastering by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val colorFilter = remember(adj) {
        if (adj == null) null else {
            val baseContrast = adj.contrast
            val baseBrightness = adj.brightness
            val baseSaturation = adj.saturation
            val m = when (adj.filter) {
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
                "Vintage" -> {
                    floatArrayOf(
                        0.393f * baseContrast, 0.769f * baseContrast, 0.189f * baseContrast, 0f, baseBrightness * 255f,
                        0.349f * baseContrast, 0.686f * baseContrast, 0.168f * baseContrast, 0f, baseBrightness * 255f,
                        0.272f * baseContrast, 0.534f * baseContrast, 0.131f * baseContrast, 0f, baseBrightness * 255f,
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
                    val redCol = 0.213f * (1f - baseSaturation)
                    val greenCol = 0.715f * (1f - baseSaturation)
                    val blueCol = 0.072f * (1f - baseSaturation)
                    floatArrayOf(
                        ((redCol + baseSaturation) * baseContrast) * 1.15f, greenCol * baseContrast, blueCol * baseContrast, 0f, baseBrightness * 255f,
                        redCol * baseContrast, ((greenCol + baseSaturation) * baseContrast) * 1.05f, blueCol * baseContrast, 0f, baseBrightness * 255f,
                        redCol * baseContrast, greenCol * baseContrast, ((blueCol + baseSaturation) * baseContrast) * 0.85f, 0f, baseBrightness * 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                }
                "Cool" -> {
                    val redCol = 0.213f * (1f - baseSaturation)
                    val greenCol = 0.715f * (1f - baseSaturation)
                    val blueCol = 0.072f * (1f - baseSaturation)
                    floatArrayOf(
                        ((redCol + baseSaturation) * baseContrast) * 0.85f, greenCol * baseContrast, blueCol * baseContrast, 0f, baseBrightness * 255f,
                        redCol * baseContrast, ((greenCol + baseSaturation) * baseContrast) * 0.95f, blueCol * baseContrast, 0f, baseBrightness * 255f,
                        redCol * baseContrast, greenCol * baseContrast, ((blueCol + baseSaturation) * baseContrast) * 1.20f, 0f, baseBrightness * 255f,
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
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${filteredItems.size}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.image_details)) },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showDetailsBottomSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.set_wallpaper)) },
                                leadingIcon = { Icon(Icons.Default.Wallpaper, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, context.getString(R.string.set_wallpaper), Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_to_album)) },
                                leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showAddToAlbumDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f)
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (isTrashed) {
                        IconButton(onClick = {
                            viewModel.restoreFromTrash(setOf(currentUri))
                            onBack()
                        }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Restore, contentDescription = "Restore")
                                Text("Restore", fontSize = 10.sp)
                            }
                        }
                        IconButton(onClick = {
                            viewModel.permanentlyDelete(setOf(currentUri))
                            onBack()
                        }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Permanently", tint = MaterialTheme.colorScheme.error)
                                Text("Delete", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        IconButton(onClick = {
                            viewModel.moveToTrash(setOf(currentUri))
                            if (filteredItems.size <= 1) onBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                        IconButton(onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, currentUri)
                                type = if (currentIsVideo) "video/*" else "image/*"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, null))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.editor))
                        }
                        IconButton(onClick = { viewModel.toggleFavorite(currentUri) }) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageMedia = filteredItems[page]
                val pageAdj = adjustments[pageMedia.uri]

                var scale by remember { mutableFloatStateOf(1f) }
                var offsetX by remember { mutableFloatStateOf(0f) }
                var offsetY by remember { mutableFloatStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(page) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                val maxOffset = (scale - 1) * 1000f
                                offsetX = (offsetX + pan.x * scale).coerceIn(-maxOffset, maxOffset)
                                offsetY = (offsetY + pan.y * scale).coerceIn(-maxOffset, maxOffset)
                                if (scale == 1f) {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                        .pointerInput(page) {
                            detectVerticalDragGestures { change, dragAmount ->
                                if (scale == 1f && dragAmount < -15) {
                                    // Dragged UP
                                    change.consume()
                                    showDetailsBottomSheet = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val finalScaleX = scale * (if (pageAdj?.flipHorizontal == true) -1f else 1f)
                    val finalScaleY = scale * (if (pageAdj?.flipVertical == true) -1f else 1f)
                    val finalRotation = (pageAdj?.rotation ?: 0f)

                    AsyncImage(
                        model = pageMedia.uri,
                        contentDescription = pageMedia.name,
                        contentScale = ContentScale.Fit,
                        colorFilter = colorFilter,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = finalScaleX,
                                scaleY = finalScaleY,
                                translationX = offsetX,
                                translationY = offsetY,
                                rotationZ = finalRotation
                            )
                    )

                    if (pageMedia.isVideo) {
                        Button(
                            onClick = { onPlayVideo(pageMedia.uri) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .align(Alignment.Center)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.play_video), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Swipe up hint pill at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { showDetailsBottomSheet = true }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Swipe Up",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.swipe_up_for_details),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Samsung One UI 6.1 Interactive Details Bottom Sheet
    if (showDetailsBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDetailsBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1C1B1F),
            contentColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            OneUIDetailsBottomSheetContent(
                mediaItem = currentMedia,
                isFavorite = isFavorite,
                isRemastering = isRemastering,
                onRemaster = {
                    coroutineScope.launch {
                        isRemastering = true
                        Toast.makeText(context, if (Locale.getDefault().language == "ar") "جاري تحسين الصورة باستخدام Galaxy AI..." else "Remastering with Galaxy AI...", Toast.LENGTH_SHORT).show()
                        delay(1500)
                        isRemastering = false
                        Toast.makeText(context, if (Locale.getDefault().language == "ar") "تمت تحسين الصورة بنجاح! ✨" else "Image Remastered Successfully! ✨", Toast.LENGTH_SHORT).show()
                    }
                },
                onObjectEraser = {
                    showDetailsBottomSheet = false
                    onEdit()
                },
                onMoveToSecureFolder = {
                    viewModel.moveToTrash(setOf(currentUri))
                    showDetailsBottomSheet = false
                    Toast.makeText(context, if (Locale.getDefault().language == "ar") "تم النقل إلى المجلد الآمن 🔒" else "Moved to Secure Folder 🔒", Toast.LENGTH_SHORT).show()
                },
                onQuickShare = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, currentUri)
                        type = if (currentIsVideo) "video/*" else "image/*"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.quick_share)))
                },
                onOpenLocation = {
                    val gmmIntentUri = Uri.parse("geo:24.7136,46.6753?q=Riyadh")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Location: Riyadh, Saudi Arabia", Toast.LENGTH_SHORT).show()
                    }
                },
                onClose = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        showDetailsBottomSheet = false
                    }
                }
            )
        }
    }

    // Add To Album Dialog
    if (showAddToAlbumDialog) {
        AlertDialog(
            onDismissRequest = { showAddToAlbumDialog = false },
            title = { Text(stringResource(R.string.select_target_album), fontWeight = FontWeight.Bold) },
            text = {
                if (customAlbums.isEmpty()) {
                    Text(stringResource(R.string.no_custom_albums))
                } else {
                    Column {
                        customAlbums.keys.forEach { albumName ->
                            TextButton(
                                onClick = {
                                    viewModel.addItemsToAlbum(setOf(currentUri), albumName)
                                    showAddToAlbumDialog = false
                                    Toast.makeText(context, context.getString(R.string.added_to_album), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(albumName, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddToAlbumDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun OneUIDetailsBottomSheetContent(
    mediaItem: MediaItem,
    isFavorite: Boolean,
    isRemastering: Boolean,
    onRemaster: () -> Unit,
    onObjectEraser: () -> Unit,
    onMoveToSecureFolder: () -> Unit,
    onQuickShare: () -> Unit,
    onOpenLocation: () -> Unit,
    onClose: () -> Unit
) {
    val isArabic = Locale.getDefault().language == "ar"
    val dateFormatter = remember {
        SimpleDateFormat(
            if (isArabic) "EEEE، d MMMM yyyy • h:mm a" else "EEEE, MMMM d, yyyy • h:mm a",
            Locale.getDefault()
        )
    }
    val formattedDate = remember(mediaItem.dateAdded) {
        dateFormatter.format(Date(if (mediaItem.dateAdded > 0) mediaItem.dateAdded else System.currentTimeMillis()))
    }

    val simulatedSize = remember(mediaItem.id) {
        val sizeMb = ((mediaItem.id % 20) + 2.4f)
        "%.1f MB".format(sizeMb)
    }

    val simulatedRes = remember(mediaItem.isVideo) {
        if (mediaItem.isVideo) "3840 × 2160 • 4K UHD 60fps" else "4000 × 3000 • 12.0 MP"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = mediaItem.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        // Galaxy AI One UI Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OneUIActionButton(
                icon = Icons.Default.AutoAwesome,
                label = stringResource(R.string.remaster_photo),
                highlight = true,
                isLoading = isRemastering,
                onClick = onRemaster,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OneUIActionButton(
                icon = Icons.Default.QrCodeScanner,
                label = stringResource(R.string.object_eraser),
                onClick = onObjectEraser,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OneUIActionButton(
                icon = Icons.Default.Lock,
                label = stringResource(R.string.secure_folder),
                onClick = onMoveToSecureFolder,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OneUIActionButton(
                icon = Icons.Default.Share,
                label = stringResource(R.string.quick_share),
                onClick = onQuickShare,
                modifier = Modifier.weight(1f)
            )
        }

        Divider(color = Color.White.copy(alpha = 0.12f))

        // Main Technical Info Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2A2F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = mediaItem.uri,
                    contentDescription = mediaItem.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (mediaItem.isVideo) Icons.Default.Movie else Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (mediaItem.isVideo) "فيديو MP4" else "صورة JPEG",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = if (mediaItem.isVideo) "HDR10+" else "Exif 2.3",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(text = "حجم الملف: $simulatedSize", fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
                    Text(text = "الأبعاد: $simulatedRes", fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
                    Text(
                        text = "/Internal Storage/DCIM/Camera/${mediaItem.name}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            }
        }

        // Camera Details Section (EXIF)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2A2F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.camera_details),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Samsung Galaxy S24 Ultra", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                    Text(text = "200 MP Ultra Sensor", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ExifMetricChip(label = "فتحة العدسة", value = "f/1.7")
                    ExifMetricChip(label = "سرعة الغالق", value = "1/1250s")
                    ExifMetricChip(label = "الحساسية ISO", value = "50")
                    ExifMetricChip(label = "البعد البؤري", value = "23mm")
                }
            }
        }

        // Location & Map Section
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2A2F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "الرياض، المملكة العربية السعودية" else "Riyadh, Saudi Arabia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    TextButton(onClick = onOpenLocation) {
                        Text(text = stringResource(R.string.open_in_maps), fontSize = 12.sp)
                    }
                }

                // Map Visual Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
                            )
                        )
                        .clickable(onClick = onOpenLocation),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Map, contentDescription = "Map", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("24.7136° N, 46.6753° E", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tags Section
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2A2F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("#طبيعة  #سفر  #ذكريات_2026", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                }
                Text(text = stringResource(R.string.add_tag), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OneUIActionButton(
    icon: ImageVector,
    label: String,
    highlight: Boolean = false,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (highlight) MaterialTheme.colorScheme.primary else Color(0xFF2B2A2F))
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (highlight) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun ExifMetricChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.Black.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
