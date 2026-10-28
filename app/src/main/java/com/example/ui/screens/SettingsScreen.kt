package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val theme by viewModel.theme.collectAsState()
    val language by viewModel.language.collectAsState()
    val gridColumns by viewModel.gridColumns.collectAsState()
    val autoPlayVideos by viewModel.autoPlayVideos.collectAsState()
    val trashRetention by viewModel.trashRetention.collectAsState()
    val cloudSync by viewModel.cloudSync.collectAsState()
    val ultraHdThumbs by viewModel.ultraHdThumbs.collectAsState()
    val hideHiddenAlbums by viewModel.hideHiddenAlbums.collectAsState()
    val cacheSize by viewModel.cacheSize.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showGridDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Display & Language
            SettingsCategoryCard(title = if (language == "ar") "العرض واللغة" else "Display & Language") {
                // Theme
                SettingsClickableItem(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.theme),
                    subtitle = when (theme) {
                        "light" -> stringResource(R.string.theme_light)
                        "dark" -> stringResource(R.string.theme_dark)
                        else -> stringResource(R.string.theme_system)
                    },
                    onClick = { showThemeDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                
                // Language Switch
                SettingsClickableItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.language),
                    subtitle = if (language == "ar") "العربية (Arabic)" else "English (الإنجليزية)",
                    onClick = {
                        viewModel.setLanguage(if (language == "ar") "en" else "ar")
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Grid Columns
                SettingsClickableItem(
                    icon = Icons.Default.GridView,
                    title = stringResource(R.string.grid_columns),
                    subtitle = "$gridColumns ${if (language == "ar") "أعمدة" else "Columns"}",
                    onClick = { showGridDialog = true }
                )
            }

            // Section 2: Media Playback & Visuals
            SettingsCategoryCard(title = if (language == "ar") "التشغيل وجودة العرض" else "Playback & Visuals") {
                // Auto-play videos
                SettingsSwitchItem(
                    icon = Icons.Default.PlayCircle,
                    title = stringResource(R.string.autoplay_videos),
                    subtitle = if (language == "ar") "تشغيل المعاينة للمقاطع تلقائياً" else "Automatically play video previews",
                    checked = autoPlayVideos,
                    onCheckedChange = { viewModel.setAutoPlayVideos(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Ultra HD Thumbnails
                SettingsSwitchItem(
                    icon = Icons.Default.HighQuality,
                    title = stringResource(R.string.ultra_hd_thumbs),
                    subtitle = if (language == "ar") "تحسين جودة معالجة المصغرات" else "Enhance thumbnail rendering quality",
                    checked = ultraHdThumbs,
                    onCheckedChange = { viewModel.setUltraHdThumbs(it) }
                )
            }

            // Section 3: Storage & Cloud
            SettingsCategoryCard(title = if (language == "ar") "التخزين والسحابة" else "Storage & Cloud") {
                // Trash Retention
                SettingsSwitchItem(
                    icon = Icons.Default.DeleteSweep,
                    title = stringResource(R.string.trash_retention),
                    subtitle = if (language == "ar") "حذف العناصر تلقائياً بعد 30 يوماً" else "Automatically purge trashed items after 30 days",
                    checked = trashRetention == "30",
                    onCheckedChange = { viewModel.setTrashRetention(if (it) "30" else "0") }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Samsung Cloud Sync
                SettingsSwitchItem(
                    icon = Icons.Default.CloudSync,
                    title = stringResource(R.string.cloud_sync),
                    subtitle = if (language == "ar") "مزامنة الاستوديو مع Samsung Cloud" else "Sync gallery assets with Samsung Cloud",
                    checked = cloudSync,
                    onCheckedChange = { viewModel.setCloudSync(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Hide Protected Albums
                SettingsSwitchItem(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.hide_hidden_albums),
                    subtitle = if (language == "ar") "تشفير وإخفاء الألبومات الخاصة" else "Encrypt & lock protected albums",
                    checked = hideHiddenAlbums,
                    onCheckedChange = { viewModel.setHideHiddenAlbums(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Cache Manager
                SettingsClickableItem(
                    icon = Icons.Default.Storage,
                    title = stringResource(R.string.cache_manager),
                    subtitle = "${if (language == "ar") "المساحة المستهلكة:" else "Occupied space:"} $cacheSize",
                    onClick = { viewModel.clearCache() }
                )
            }

            // Section 4: About
            SettingsCategoryCard(title = if (language == "ar") "حول التطبيق" else "About") {
                SettingsClickableItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.about_gallery),
                    subtitle = stringResource(R.string.version),
                    onClick = { showAboutDialog = true }
                )
            }
        }
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.theme), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("system" to R.string.theme_system, "light" to R.string.theme_light, "dark" to R.string.theme_dark).forEach { (key, res) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setTheme(key); showThemeDialog = false }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = theme == key, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(res))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Grid Columns Dialog
    if (showGridDialog) {
        AlertDialog(
            onDismissRequest = { showGridDialog = false },
            title = { Text(stringResource(R.string.grid_columns), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(3, 4, 5).forEach { cols ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setGridColumns(cols); showGridDialog = false }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = gridColumns == cols, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("$cols ${if (language == "ar") "أعمدة" else "Columns"}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGridDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = { Text(stringResource(R.string.about_gallery), fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.version), fontWeight = FontWeight.SemiBold)
                    Text(
                        if (language == "ar")
                            "تطبيق الأستوديو المستوحى من واجهة Samsung One UI 6.1 مع كامل المميزات المتقدمة وتجربة تصفح سلسة."
                        else
                            "Samsung One UI 6.1 inspired Gallery application with full capabilities and seamless user experience.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
