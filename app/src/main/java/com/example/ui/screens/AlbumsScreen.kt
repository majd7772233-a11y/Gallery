package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun AlbumsScreen(viewModel: GalleryViewModel, onAlbumClick: (String) -> Unit, onSettingsClick: () -> Unit) {
    val albums by viewModel.albums.collectAsState()
    val scrollState = rememberLazyGridState()

    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewAlbumDialog by remember { mutableStateOf(false) }
    var newAlbumNameInput by remember { mutableStateOf("") }

    val filteredAlbums = remember(albums, searchQuery) {
        if (searchQuery.isBlank()) albums
        else albums.filterKeys { it.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        var expanded by remember { mutableStateOf(false) }

        if (isSearching) {
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
                title = stringResource(R.string.tab_albums),
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
                                text = { Text(stringResource(R.string.new_album)) },
                                onClick = {
                                    expanded = false
                                    showNewAlbumDialog = true
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Access Category Cards
            if (!isSearching) {
                item(span = { GridItemSpan(2) }) {
                    Column {
                        Text(
                            text = stringResource(R.string.special_albums),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SpecialAlbumCard(
                                title = stringResource(R.string.favorites),
                                icon = Icons.Default.Favorite,
                                color = Color(0xFFE91E63),
                                modifier = Modifier.weight(1f),
                                onClick = { onAlbumClick("Favorites") }
                            )
                            SpecialAlbumCard(
                                title = stringResource(R.string.videos),
                                icon = Icons.Default.PlayArrow,
                                color = Color(0xFF2196F3),
                                modifier = Modifier.weight(1f),
                                onClick = { onAlbumClick("Videos") }
                            )
                            SpecialAlbumCard(
                                title = stringResource(R.string.menu_trash),
                                icon = Icons.Default.Delete,
                                color = Color(0xFFFF9800),
                                modifier = Modifier.weight(1f),
                                onClick = { onAlbumClick("Trash") }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.custom_albums),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }

            // Albums Grid
            items(filteredAlbums.entries.toList()) { (albumName, items) ->
                Column(
                    modifier = Modifier.clickable { onAlbumClick(albumName) }
                ) {
                    val firstItem = items.firstOrNull()
                    if (firstItem != null) {
                        AsyncImage(
                            model = firstItem.uri,
                            contentDescription = albumName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(24.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = albumName.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = albumName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${items.size} items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // New Album Creation Dialog
    if (showNewAlbumDialog) {
        AlertDialog(
            onDismissRequest = { showNewAlbumDialog = false },
            title = { Text(stringResource(R.string.new_album), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newAlbumNameInput,
                    onValueChange = { newAlbumNameInput = it },
                    label = { Text("Album Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAlbumNameInput.isNotBlank()) {
                            viewModel.createCustomAlbum(newAlbumNameInput)
                            newAlbumNameInput = ""
                            showNewAlbumDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewAlbumDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SpecialAlbumCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
