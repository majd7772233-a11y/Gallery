package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.MediaItem
import com.example.ui.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumName: String,
    viewModel: GalleryViewModel,
    onBack: () -> Unit,
    onImageClick: (Uri, Boolean) -> Unit
) {
    val albums by viewModel.albums.collectAsState()
    val favoriteItems by viewModel.favoriteItems.collectAsState()
    val videoItems by viewModel.videoItems.collectAsState()
    val trashedItems by viewModel.trashedItems.collectAsState()
    val trashedUris by viewModel.trashedUris.collectAsState()
    val mediaItems by viewModel.mediaItems.collectAsState()
    val adjustments by viewModel.adjustments.collectAsState()

    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var selectedTrashItem by remember { mutableStateOf<MediaItem?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf(albumName) }
    var showDeleteAlbumDialog by remember { mutableStateOf(false) }
    var expandedMenu by remember { mutableStateOf(false) }

    val isTrash = albumName == "Trash" || albumName == "سلة المحذوفات"

    val albumItems = when (albumName) {
        "Favorites" -> favoriteItems
        "Videos" -> videoItems
        "Trash", "سلة المحذوفات" -> trashedItems
        "Recent" -> mediaItems.filter { !trashedUris.contains(it.uri) }.take(8)
        "Suggested" -> mediaItems.filter { !trashedUris.contains(it.uri) && (it.bucketName == "Nature" || it.bucketName == "Travel") }
        else -> albums[albumName]?.filter { !trashedUris.contains(it.uri) } ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = {
                Column {
                    Text(albumName, fontWeight = FontWeight.Bold)
                    Text("${albumItems.size} items", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (isTrash) {
                    if (albumItems.isNotEmpty()) {
                        TextButton(onClick = { showEmptyTrashDialog = true }) {
                            Text("Empty Trash", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Box {
                        IconButton(onClick = { expandedMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Rename Album") },
                                onClick = {
                                    expandedMenu = false
                                    showRenameDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Album") },
                                onClick = {
                                    expandedMenu = false
                                    showDeleteAlbumDialog = true
                                }
                            )
                        }
                    }
                }
            }
        )

        if (albumItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isTrash) Icons.Default.Delete else Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isTrash) "Trash is Empty" else "No photos in $albumName",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(albumItems, key = { it.id }) { item ->
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

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(1.dp)
                            .clickable {
                                if (isTrash) {
                                    selectedTrashItem = item
                                } else {
                                    onImageClick(item.uri, item.isVideo)
                                }
                            }
                    ) {
                        AsyncImage(
                            model = item.uri,
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            colorFilter = colorFilter,
                            modifier = Modifier.fillMaxSize().graphicsLayer(
                                rotationZ = itemAdj?.rotation ?: 0f,
                                scaleX = if (itemAdj?.flipHorizontal == true) -1f else 1f,
                                scaleY = if (itemAdj?.flipVertical == true) -1f else 1f
                            )
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
                    }
                }
            }
        }
    }

    // Dialog for Trashed Item action (Restore or Delete Permanently)
    if (selectedTrashItem != null) {
        val targetItem = selectedTrashItem!!
        AlertDialog(
            onDismissRequest = { selectedTrashItem = null },
            title = { Text("Trashed Photo Options", fontWeight = FontWeight.Bold) },
            text = { Text("Would you like to restore this item or delete it permanently?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreFromTrash(setOf(targetItem.uri))
                        selectedTrashItem = null
                    }
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.permanentlyDelete(setOf(targetItem.uri))
                        selectedTrashItem = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Permanently")
                }
            }
        )
    }

    // Empty Trash Dialog
    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("Empty Trash?", fontWeight = FontWeight.Bold) },
            text = { Text("All items in trash will be permanently deleted. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyTrashDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Empty Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Album Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Album", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Album Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameAlbum(albumName, renameInput)
                            showRenameDialog = false
                            onBack()
                        }
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Album Dialog
    if (showDeleteAlbumDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAlbumDialog = false },
            title = { Text("Delete Album?", fontWeight = FontWeight.Bold) },
            text = { Text("This will delete the album '$albumName'. Your photos will remain in the main gallery.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAlbum(albumName)
                        showDeleteAlbumDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAlbumDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
