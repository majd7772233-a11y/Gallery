package com.example.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.PhotoAlbum
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.ui.viewmodel.GalleryViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    viewModel: GalleryViewModel = viewModel(),
    settingsViewModel: com.example.ui.viewmodel.SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()
    
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    
    val multiplePermissionsState = rememberMultiplePermissionsState(permissionsToRequest)

    if (multiplePermissionsState.allPermissionsGranted) {
        LaunchedEffect(Unit) {
            viewModel.loadMedia()
        }
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val isBottomBarVisible = currentRoute == "pictures" || currentRoute == "albums" || currentRoute == "stories" || currentRoute == "shared"

        Scaffold(
            bottomBar = {
                if (isBottomBarVisible) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (currentRoute == "pictures") Icons.Filled.PhotoLibrary else Icons.Outlined.PhotoLibrary,
                                    contentDescription = stringResource(R.string.tab_pictures)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_pictures)) },
                            selected = currentRoute == "pictures",
                            onClick = {
                                navController.navigate("pictures") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (currentRoute == "albums") Icons.Filled.PhotoAlbum else Icons.Outlined.PhotoAlbum,
                                    contentDescription = stringResource(R.string.tab_albums)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_albums)) },
                            selected = currentRoute == "albums",
                            onClick = {
                                navController.navigate("albums") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (currentRoute == "stories") Icons.Filled.PhotoAlbum else Icons.Outlined.PhotoAlbum,
                                    contentDescription = stringResource(R.string.tab_stories)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_stories)) },
                            selected = currentRoute == "stories",
                            onClick = {
                                navController.navigate("stories") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (currentRoute == "shared") Icons.Filled.PhotoAlbum else Icons.Outlined.PhotoAlbum,
                                    contentDescription = stringResource(R.string.tab_shared)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_shared)) },
                            selected = currentRoute == "shared",
                            onClick = {
                                navController.navigate("shared") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "pictures",
                modifier = Modifier.padding(if (isBottomBarVisible) innerPadding else PaddingValues(0.dp))
            ) {
                composable("pictures") {
                    PicturesScreen(
                        viewModel = viewModel,
                        onImageClick = { uri, isVideo ->
                            if (isVideo) {
                                navController.navigate("videoPlayer/${Uri.encode(uri.toString())}")
                            } else {
                                navController.navigate("viewer/${Uri.encode(uri.toString())}?isVideo=false")
                            }
                        },
                        onSettingsClick = {
                            navController.navigate("settings")
                        },
                        onAlbumClick = { albumName ->
                            navController.navigate("album/${Uri.encode(albumName)}")
                        }
                    )
                }
                composable("albums") {
                    AlbumsScreen(
                        viewModel = viewModel,
                        onAlbumClick = { albumName ->
                            navController.navigate("album/${Uri.encode(albumName)}")
                        },
                        onSettingsClick = {
                            navController.navigate("settings")
                        }
                    )
                }
                composable("stories") {
                    StoriesScreen(
                        viewModel = viewModel,
                        onSettingsClick = {
                            navController.navigate("settings")
                        }
                    )
                }
                composable("shared") {
                    SharedScreen(
                        viewModel = viewModel,
                        onAlbumClick = { albumName ->
                            navController.navigate("album/${Uri.encode(albumName)}")
                        }
                    )
                }
                composable(
                    "album/{albumName}",
                    arguments = listOf(navArgument("albumName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val albumName = backStackEntry.arguments?.getString("albumName")
                    if (albumName != null) {
                        AlbumDetailScreen(
                            albumName = Uri.decode(albumName),
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onImageClick = { uri, isVideo ->
                                if (isVideo) {
                                    navController.navigate("videoPlayer/${Uri.encode(uri.toString())}")
                                } else {
                                    navController.navigate("viewer/${Uri.encode(uri.toString())}?isVideo=false")
                                }
                            }
                        )
                    }
                }
                composable("settings") {
                    com.example.ui.screens.SettingsScreen(
                        viewModel = settingsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    "viewer/{uri}?isVideo={isVideo}",
                    arguments = listOf(
                        navArgument("uri") { type = NavType.StringType },
                        navArgument("isVideo") { type = NavType.BoolType; defaultValue = false }
                    )
                ) { backStackEntry ->
                    val uriString = backStackEntry.arguments?.getString("uri")
                    val isVideo = backStackEntry.arguments?.getBoolean("isVideo") ?: false
                    if (uriString != null) {
                        ViewerScreen(
                            uri = Uri.parse(Uri.decode(uriString)),
                            isVideo = isVideo,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onEdit = { navController.navigate("editor/${Uri.encode(uriString)}") },
                            onPlayVideo = { videoUri ->
                                navController.navigate("videoPlayer/${Uri.encode(videoUri.toString())}")
                            }
                        )
                    }
                }
                composable(
                    "videoPlayer/{uri}",
                    arguments = listOf(navArgument("uri") { type = NavType.StringType })
                ) { backStackEntry ->
                    val uriString = backStackEntry.arguments?.getString("uri")
                    if (uriString != null) {
                        VideoPlayerScreen(
                            uri = Uri.parse(Uri.decode(uriString)),
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(
                    "editor/{uri}",
                    arguments = listOf(navArgument("uri") { type = NavType.StringType })
                ) { backStackEntry ->
                    val uriString = backStackEntry.arguments?.getString("uri")
                    if (uriString != null) {
                        EditorScreen(
                            uri = Uri.parse(Uri.decode(uriString)),
                            viewModel = viewModel,
                            onDismiss = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Text(stringResource(R.string.permission_required))
                Button(onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }) {
                    Text(stringResource(R.string.grant_permission))
                }
            }
        }
    }
}
