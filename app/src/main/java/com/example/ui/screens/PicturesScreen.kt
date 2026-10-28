package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import com.example.R
import com.example.ui.components.OneUiTopAppBar
import com.example.ui.viewmodel.GalleryViewModel

import android.content.Intent
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PicturesScreen(viewModel: GalleryViewModel, onImageClick: (android.net.Uri, Boolean) -> Unit, onSettingsClick: () -> Unit, onAlbumClick: (String) -> Unit) {
    val groupedMedia by viewModel.groupedMedia.collectAsState()
    val scrollState = rememberLazyGridState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedItems by viewModel.selectedItems.collectAsState()
    val context = LocalContext.current
    val adjustments by viewModel.adjustments.collectAsState()

    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val displayedGroupedMedia = remember(groupedMedia, searchQuery) {
        if (searchQuery.isBlank()) {
            groupedMedia
        } else {
            groupedMedia.mapValues { (_, items) ->
                items.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }.filterValues { it.isNotEmpty() }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        var expanded by remember { mutableStateOf(false) }

        if (isSelectionMode) {
            TopAppBar(
                title = { Text(text = if (selectedItems.isEmpty()) stringResource(R.string.action_select) else "${selectedItems.size}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.clearSelection() }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.selectAll() }) {
                        Text(stringResource(R.string.menu_select_all), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        } else if (isSearching) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    isSearching = false 
                    searchQuery = ""
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Close Search")
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.action_search)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }
        } else {
            OneUiTopAppBar(
                title = stringResource(R.string.tab_pictures),
                lazyScrollState = scrollState,
                actions = {
                    IconButton(onClick = { isSearching = true }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                    }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_more))
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_camera)) },
                                onClick = { 
                                    expanded = false
                                    val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                                    try {
                                        context.startActivity(cameraIntent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Camera not available", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_edit)) },
                                onClick = { 
                                    expanded = false
                                    viewModel.setSelectionMode(true)
                                    android.widget.Toast.makeText(context, "Select an item to edit", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_select)) },
                                onClick = { 
                                    expanded = false
                                    viewModel.setSelectionMode(true)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_group_similar)) },
                                onClick = {
                                    expanded = false
                                    viewModel.toggleGroupingSimilar()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_trash)) },
                                onClick = { 
                                    expanded = false
                                    onAlbumClick("Trash")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings)) },
                                onClick = { 
                                    expanded = false
                                    onSettingsClick()
                                }
                            )
                        }
                    }
                }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (!isSelectionMode && !isSearching) {
                    item(span = { GridItemSpan(3) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TopIconItem(Icons.Default.AutoAwesome, stringResource(R.string.suggested), onClick = { onAlbumClick("Suggested") })
                            TopIconItem(Icons.Default.Schedule, stringResource(R.string.recent), onClick = { onAlbumClick("Recent") })
                            TopIconItem(Icons.Default.FavoriteBorder, stringResource(R.string.favorites), onClick = { onAlbumClick("Favorites") })
                            TopIconItem(Icons.Default.PlayCircleOutline, stringResource(R.string.videos), onClick = { onAlbumClick("Videos") })
                        }
                    }
                }

                displayedGroupedMedia.forEach { (dateStr, items) ->
                    item(span = { GridItemSpan(3) }) {
                        val dateTitle = when(dateStr) {
                            "اليوم", "Today" -> stringResource(R.string.today)
                            "أمس", "Yesterday" -> stringResource(R.string.yesterday)
                            else -> dateStr
                        }
                        Text(
                            text = dateTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                        )
                    }

                    items(items, key = { it.id }) { item ->
                        val isSelected = selectedItems.contains(item.uri)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            viewModel.setSelectionMode(true)
                                            viewModel.toggleSelection(item.uri)
                                        },
                                        onTap = {
                                            if (isSelectionMode) {
                                                viewModel.toggleSelection(item.uri)
                                            } else {
                                                onImageClick(item.uri, item.isVideo)
                                            }
                                        }
                                    )
                                }
                        ) {
                            val itemAdj = adjustments[item.uri]
                            val colorFilter = remember(itemAdj) {
                                if (itemAdj == null) null else {
                                    val baseContrast = itemAdj.contrast
                                    val baseBrightness = itemAdj.brightness
                                    val baseSaturation = itemAdj.saturation
                                    val m = when (itemAdj.filter) {
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

                            AsyncImage(
                                model = item.uri,
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                colorFilter = colorFilter,
                                modifier = Modifier.fillMaxSize().graphicsLayer(
                                    rotationZ = itemAdj?.rotation ?: 0f,
                                    scaleX = if (itemAdj?.flipHorizontal == true) -1f else 1f,
                                    scaleY = if (itemAdj?.flipVertical == true) -1f else 1f
                                ).let {
                                    if (isSelected) it.padding(12.dp).clip(MaterialTheme.shapes.medium) else it
                                }
                            )
                            if (item.isVideo) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = item.durationStr,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (isSelectionMode) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                    contentDescription = "Select",
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (isSelectionMode && selectedItems.isNotEmpty()) {
            var showMoreSelectionMenu by remember { mutableStateOf(false) }
            var showAddToAlbumDialog by remember { mutableStateOf(false) }

            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = {
                        val uris = ArrayList(selectedItems)
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND_MULTIPLE
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                            type = "*/*"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, null))
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = stringResource(R.string.menu_share))
                            Text(stringResource(R.string.menu_share))
                        }
                    }
                    TextButton(onClick = { viewModel.moveToTrash(selectedItems) }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.menu_trash))
                            Text(stringResource(R.string.menu_trash))
                        }
                    }
                    Box {
                        TextButton(onClick = { showMoreSelectionMenu = true }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_more))
                                Text(stringResource(R.string.action_more))
                            }
                        }
                        DropdownMenu(
                            expanded = showMoreSelectionMenu,
                            onDismissRequest = { showMoreSelectionMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add to Custom Album") },
                                onClick = {
                                    showMoreSelectionMenu = false
                                    showAddToAlbumDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add to Favorites") },
                                onClick = {
                                    showMoreSelectionMenu = false
                                    selectedItems.forEach { viewModel.toggleFavorite(it) }
                                    viewModel.clearSelection()
                                    android.widget.Toast.makeText(context, "Added to Favorites", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Create GIF / Collage") },
                                onClick = {
                                    showMoreSelectionMenu = false
                                    viewModel.clearSelection()
                                    android.widget.Toast.makeText(context, "Collage created successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            if (showAddToAlbumDialog) {
                val customAlbums by viewModel.customAlbums.collectAsState()
                AlertDialog(
                    onDismissRequest = { showAddToAlbumDialog = false },
                    title = { Text("Select Target Album", fontWeight = FontWeight.Bold) },
                    text = {
                        if (customAlbums.isEmpty()) {
                            Text("No custom albums created yet. Create an album in the Albums tab first!")
                        } else {
                            Column {
                                customAlbums.keys.forEach { albumName ->
                                    TextButton(
                                        onClick = {
                                            viewModel.addItemsToAlbum(selectedItems, albumName)
                                            showAddToAlbumDialog = false
                                            android.widget.Toast.makeText(context, "Added items to $albumName", android.widget.Toast.LENGTH_SHORT).show()
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
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TopIconItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
