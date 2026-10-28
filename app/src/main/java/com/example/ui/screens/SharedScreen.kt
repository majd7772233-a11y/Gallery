package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.OneUiTopAppBar
import com.example.ui.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedScreen(viewModel: GalleryViewModel, onAlbumClick: (String) -> Unit) {
    val scrollState = rememberLazyGridState()

    // State for local shared albums to make the creation 100% working and interactive
    val sharedAlbums = remember {
        mutableStateListOf(
            SharedAlbumData("Family Cloud", 24, "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=600", "Mom, Dad, Me"),
            SharedAlbumData("Summer Trip '26", 112, "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=600", "Sarah, Alex, Jake")
        )
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }
    var newAlbumMembers by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            OneUiTopAppBar(
                title = stringResource(R.string.tab_shared),
                lazyScrollState = scrollState,
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Shared Album")
                    }
                }
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sharedAlbums) { album ->
                    Column(
                        modifier = Modifier
                            .clickable { onAlbumClick(album.name) }
                            .padding(bottom = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                            AsyncImage(
                                model = album.coverUrl,
                                contentDescription = album.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp))
                            )
                            // Small group icon overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = album.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = album.members,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${album.itemsCount} items",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Dialog to create a new shared album
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("New Shared Album", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newAlbumName,
                            onValueChange = { newAlbumName = it },
                            label = { Text("Album Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newAlbumMembers,
                            onValueChange = { newAlbumMembers = it },
                            label = { Text("Invited Members (comma separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newAlbumName.isNotBlank()) {
                                // Add new interactive shared album with a gorgeous stock cover photo
                                sharedAlbums.add(
                                    SharedAlbumData(
                                        name = newAlbumName,
                                        itemsCount = 0,
                                        coverUrl = "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=600",
                                        members = if (newAlbumMembers.isBlank()) "Just Me" else newAlbumMembers
                                    )
                                )
                                newAlbumName = ""
                                newAlbumMembers = ""
                                showCreateDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

data class SharedAlbumData(
    val name: String,
    val itemsCount: Int,
    val coverUrl: String,
    val members: String
)
