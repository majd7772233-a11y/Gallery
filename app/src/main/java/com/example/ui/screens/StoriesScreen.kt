package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.OneUiTopAppBar
import com.example.ui.viewmodel.GalleryViewModel
import kotlinx.coroutines.delay

@Composable
fun StoriesScreen(viewModel: GalleryViewModel, onSettingsClick: () -> Unit) {
    val mediaItems by viewModel.mediaItems.collectAsState()
    val scrollState = rememberLazyGridState()

    // Filter items to make pre-compiled stories
    val natureStory = mediaItems.filter { it.bucketName == "Nature" }
    val foodStory = mediaItems.filter { it.bucketName == "Food" }
    val travelStory = mediaItems.filter { it.bucketName == "Travel" }

    val storiesList = remember(mediaItems) {
        listOf(
            StoryData("Nature Escape", natureStory, "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600"),
            StoryData("Gourmet Diaries", foodStory, "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600"),
            StoryData("Wanderlust Trips", travelStory, "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=600")
        ).filter { it.items.isNotEmpty() }
    }

    var activeStory by remember { mutableStateOf<StoryData?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            OneUiTopAppBar(
                title = stringResource(R.string.tab_stories),
                lazyScrollState = scrollState,
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Transparent // Spacer or secondary
                        )
                    }
                }
            )

            if (storiesList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No stories available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(storiesList) { story ->
                        StoryCard(story = story, onClick = { activeStory = story })
                    }
                }
            }
        }

        // Slideshow overlay when a story is clicked
        AnimatedVisibility(
            visible = activeStory != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            activeStory?.let { story ->
                StorySlideshow(story = story, onDismiss = { activeStory = null })
            }
        }
    }
}

data class StoryData(
    val title: String,
    val items: List<com.example.data.MediaItem>,
    val coverUrl: String
)

@Composable
fun StoryCard(story: StoryData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = story.coverUrl,
                contentDescription = story.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 300f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = story.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${story.items.size} Photos",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun StorySlideshow(story: StoryData, onDismiss: () -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    val currentItem = story.items[currentIndex]

    // Automatically transition to next slide every 3 seconds
    LaunchedEffect(currentIndex) {
        delay(3000L)
        if (currentIndex < story.items.size - 1) {
            currentIndex++
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Image Slideshow
        AsyncImage(
            model = currentItem.uri,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Title and progress
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            // Segmented Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in story.items.indices) {
                    val progress = when {
                        i < currentIndex -> 1f
                        i == currentIndex -> 1f // In production, animate float from 0 to 1
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = story.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}
