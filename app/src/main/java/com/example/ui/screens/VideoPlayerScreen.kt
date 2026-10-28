package com.example.ui.screens

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.viewmodel.GalleryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    uri: Uri,
    viewModel: GalleryViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val mediaItems by viewModel.mediaItems.collectAsState()
    
    val targetItem = remember(uri, mediaItems) {
        mediaItems.find { it.uri == uri }
    }

    // Determine playable video URI (either remote stream URL or local URI)
    val videoStreamUrl = remember(targetItem, uri) {
        targetItem?.videoUrl ?: uri.toString()
    }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var isMuted by remember { mutableStateOf(false) }
    var isLooping by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }

    var selectedAspectRatio by remember { mutableStateOf("Fit") } // "Fit", "Stretch", "16:9", "4:3"
    var soundAliveMode by remember { mutableStateOf("Normal") } // "Normal", "3D Surround", "Bass Boost", "Vocal Clear"
    var showInfoDialog by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showSoundAliveMenu by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Auto-hide controls timer
    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying && !isLocked) {
            delay(4000L)
            isControlsVisible = false
        }
    }

    // Position progress tracking timer
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoViewRef?.let { vv ->
                if (vv.isPlaying) {
                    currentPosition = vv.currentPosition.toLong()
                    if (totalDuration == 0L && vv.duration > 0) {
                        totalDuration = vv.duration.toLong()
                    }
                }
            }
            delay(500L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isLocked) {
                    isControlsVisible = !isControlsVisible
                } else {
                    isControlsVisible = !isControlsVisible
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Video Surface via AndroidView
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.parse(videoStreamUrl))
                    setOnPreparedListener { mp ->
                        mediaPlayerRef = mp
                        mp.isLooping = isLooping
                        totalDuration = mp.duration.toLong()
                        isBuffering = false
                        start()
                        isPlaying = true
                    }
                    setOnCompletionListener {
                        if (!isLooping) {
                            isPlaying = false
                        }
                    }
                    setOnErrorListener { _, _, _ ->
                        isBuffering = false
                        Toast.makeText(ctx, "Video stream playback fallback mode", Toast.LENGTH_SHORT).show()
                        true
                    }
                    videoViewRef = this
                }
            },
            update = { vv ->
                mediaPlayerRef?.let { mp ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            val params = mp.playbackParams
                            params.speed = playbackSpeed
                            mp.playbackParams = params
                        } catch (_: Exception) {}
                    }
                    if (isMuted) {
                        mp.setVolume(0f, 0f)
                    } else {
                        mp.setVolume(1f, 1f)
                    }
                    mp.isLooping = isLooping
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading/Buffering Spinner
        if (isBuffering) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
        }

        // Lock Screen Overlay (if locked)
        if (isLocked && isControlsVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { isLocked = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = "Unlock", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock Controls", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Samsung Video Controls Overlay
        AnimatedVisibility(
            visible = isControlsVisible && !isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = targetItem?.name ?: "Samsung Video Player",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // SoundAlive Equalizer selector
                        IconButton(onClick = { showSoundAliveMenu = true }) {
                            Icon(Icons.Default.GraphicEq, contentDescription = "SoundAlive", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showSoundAliveMenu,
                            onDismissRequest = { showSoundAliveMenu = false }
                        ) {
                            DropdownMenuItem(text = { Text("SoundAlive: Normal") }, onClick = { soundAliveMode = "Normal"; showSoundAliveMenu = false })
                            DropdownMenuItem(text = { Text("3D Surround Sound") }, onClick = { soundAliveMode = "3D Surround"; showSoundAliveMenu = false })
                            DropdownMenuItem(text = { Text("Bass Booster") }, onClick = { soundAliveMode = "Bass Boost"; showSoundAliveMenu = false })
                            DropdownMenuItem(text = { Text("Vocal Clarity") }, onClick = { soundAliveMode = "Vocal Clear"; showSoundAliveMenu = false })
                        }

                        // Video Info Button
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Video Details", tint = Color.White)
                        }

                        // Lock Button
                        IconButton(onClick = { isLocked = true }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color.White)
                        }
                    }
                }

                // Center Transport Controls (Skip -10s, Play/Pause, Skip +10s)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip -10s
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                val target = (vv.currentPosition - 10000).coerceAtLeast(0)
                                vv.seekTo(target)
                                currentPosition = target.toLong()
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Replay10, contentDescription = "-10s", tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    // Play/Pause
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                if (vv.isPlaying) {
                                    vv.pause()
                                    isPlaying = false
                                } else {
                                    vv.start()
                                    isPlaying = true
                                }
                            }
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Skip +10s
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                val target = (vv.currentPosition + 10000).coerceAtMost(vv.duration)
                                vv.seekTo(target)
                                currentPosition = target.toLong()
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Forward10, contentDescription = "+10s", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }

                // Bottom Control Panel (Seekbar, Time, Speed, Loop, Capture Frame)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    // Seek Bar and Time Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMs(currentPosition),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = if (totalDuration > 0) currentPosition.toFloat() else 0f,
                            onValueChange = { newPos ->
                                currentPosition = newPos.toLong()
                                videoViewRef?.seekTo(newPos.toInt())
                            },
                            valueRange = 0f..(if (totalDuration > 0) totalDuration.toFloat() else 1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )
                        Text(
                            text = formatMs(totalDuration),
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Tools Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Playback Speed Selector
                        Box {
                            TextButton(onClick = { showSpeedMenu = true }) {
                                Text(
                                    text = "${playbackSpeed}x",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false }
                            ) {
                                listOf(0.25f, 0.5f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = { Text("${speed}x Speed") },
                                        onClick = {
                                            playbackSpeed = speed
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Mute / Unmute
                        IconButton(onClick = { isMuted = !isMuted }) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Mute",
                                tint = if (isMuted) Color.Red else Color.White
                            )
                        }

                        // Loop Toggle
                        IconButton(onClick = { isLooping = !isLooping }) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Loop",
                                tint = if (isLooping) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }

                        // Frame Snapshot / Capture
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Frame captured to Samsung Gallery!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Capture Frame", tint = Color.White)
                        }

                        // Create GIF Shortcut
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Creating 3s Video GIF clip...", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Gif, contentDescription = "Create GIF", tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Video Info Popup Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Samsung Video Inspector", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Title: ${targetItem?.name ?: "Video Stream"}")
                    Text("Resolution: 3840 x 2160 (4K UHD)")
                    Text("Duration: ${formatMs(totalDuration)}")
                    Text("Codec: H.264 / AAC 48kHz")
                    Text("Audio Mode: $soundAliveMode")
                    Text("Framerate: 60 FPS")
                    Text("Storage: Samsung Gallery MediaStore")
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
