package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore

    private val THEME_KEY = stringPreferencesKey("theme")
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    private val GRID_COLUMNS_KEY = intPreferencesKey("grid_columns")
    private val AUTOPLAY_VIDEOS_KEY = booleanPreferencesKey("autoplay_videos")
    private val TRASH_RETENTION_KEY = stringPreferencesKey("trash_retention")
    private val CLOUD_SYNC_KEY = booleanPreferencesKey("cloud_sync")
    private val ULTRA_HD_THUMBS_KEY = booleanPreferencesKey("ultra_hd_thumbs")
    private val HIDE_HIDDEN_ALBUMS_KEY = booleanPreferencesKey("hide_hidden_albums")

    private val _theme = MutableStateFlow("system")
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _language = MutableStateFlow("ar")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _gridColumns = MutableStateFlow(3)
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _autoPlayVideos = MutableStateFlow(true)
    val autoPlayVideos: StateFlow<Boolean> = _autoPlayVideos.asStateFlow()

    private val _trashRetention = MutableStateFlow("30")
    val trashRetention: StateFlow<String> = _trashRetention.asStateFlow()

    private val _cloudSync = MutableStateFlow(true)
    val cloudSync: StateFlow<Boolean> = _cloudSync.asStateFlow()

    private val _ultraHdThumbs = MutableStateFlow(true)
    val ultraHdThumbs: StateFlow<Boolean> = _ultraHdThumbs.asStateFlow()

    private val _hideHiddenAlbums = MutableStateFlow(false)
    val hideHiddenAlbums: StateFlow<Boolean> = _hideHiddenAlbums.asStateFlow()

    private val _cacheSize = MutableStateFlow("12.4 MB")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            _theme.value = prefs[THEME_KEY] ?: "system"
            _language.value = prefs[LANGUAGE_KEY] ?: "ar"
            _gridColumns.value = prefs[GRID_COLUMNS_KEY] ?: 3
            _autoPlayVideos.value = prefs[AUTOPLAY_VIDEOS_KEY] ?: true
            _trashRetention.value = prefs[TRASH_RETENTION_KEY] ?: "30"
            _cloudSync.value = prefs[CLOUD_SYNC_KEY] ?: true
            _ultraHdThumbs.value = prefs[ULTRA_HD_THUMBS_KEY] ?: true
            _hideHiddenAlbums.value = prefs[HIDE_HIDDEN_ALBUMS_KEY] ?: false
            updateCacheSize()
        }
    }

    fun updateCacheSize() {
        viewModelScope.launch {
            val size = getDirSize(getApplication<Application>().cacheDir)
            val sizeMb = size.toDouble() / (1024 * 1024)
            _cacheSize.value = if (sizeMb > 0) "%.2f MB".format(sizeMb) else "0.00 MB"
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            deleteDir(getApplication<Application>().cacheDir)
            _cacheSize.value = "0.00 MB"
        }
    }

    private fun getDirSize(dir: java.io.File): Long {
        var size: Long = 0
        val files = dir.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    size += getDirSize(file)
                } else {
                    size += file.length()
                }
            }
        }
        return size
    }

    private fun deleteDir(dir: java.io.File): Boolean {
        if (dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(java.io.File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
        }
        return dir.delete()
    }

    fun setTheme(theme: String) {
        _theme.value = theme
        viewModelScope.launch { dataStore.edit { it[THEME_KEY] = theme } }
    }

    fun setLanguage(language: String) {
        _language.value = language
        viewModelScope.launch { dataStore.edit { it[LANGUAGE_KEY] = language } }
    }

    fun setGridColumns(cols: Int) {
        _gridColumns.value = cols
        viewModelScope.launch { dataStore.edit { it[GRID_COLUMNS_KEY] = cols } }
    }

    fun setAutoPlayVideos(enabled: Boolean) {
        _autoPlayVideos.value = enabled
        viewModelScope.launch { dataStore.edit { it[AUTOPLAY_VIDEOS_KEY] = enabled } }
    }

    fun setTrashRetention(days: String) {
        _trashRetention.value = days
        viewModelScope.launch { dataStore.edit { it[TRASH_RETENTION_KEY] = days } }
    }

    fun setCloudSync(enabled: Boolean) {
        _cloudSync.value = enabled
        viewModelScope.launch { dataStore.edit { it[CLOUD_SYNC_KEY] = enabled } }
    }

    fun setUltraHdThumbs(enabled: Boolean) {
        _ultraHdThumbs.value = enabled
        viewModelScope.launch { dataStore.edit { it[ULTRA_HD_THUMBS_KEY] = enabled } }
    }

    fun setHideHiddenAlbums(enabled: Boolean) {
        _hideHiddenAlbums.value = enabled
        viewModelScope.launch { dataStore.edit { it[HIDE_HIDDEN_ALBUMS_KEY] = enabled } }
    }
}
