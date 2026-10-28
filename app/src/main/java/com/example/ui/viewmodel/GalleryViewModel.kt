package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MediaItem
import com.example.data.MediaStoreHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ImageAdjustments(
    val rotation: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val brightness: Float = 0f, // -0.5 to 0.5
    val contrast: Float = 1f, // 0.5 to 2.0
    val saturation: Float = 1f, // 0.0 to 2.0
    val filter: String = "Original" // Original, B&W, Warm, Cool, Vintage, Negative
)

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaStoreHelper = MediaStoreHelper(application)

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

    private val _adjustments = MutableStateFlow<Map<Uri, ImageAdjustments>>(emptyMap())
    val adjustments: StateFlow<Map<Uri, ImageAdjustments>> = _adjustments.asStateFlow()

    private val _albums = MutableStateFlow<Map<String, List<MediaItem>>>(emptyMap())
    val albums: StateFlow<Map<String, List<MediaItem>>> = _albums.asStateFlow()

    private val _groupedMedia = MutableStateFlow<Map<String, List<MediaItem>>>(emptyMap())
    val groupedMedia: StateFlow<Map<String, List<MediaItem>>> = _groupedMedia.asStateFlow()

    private val _videoItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val videoItems: StateFlow<List<MediaItem>> = _videoItems.asStateFlow()

    private val _favoriteItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val favoriteItems: StateFlow<List<MediaItem>> = _favoriteItems.asStateFlow()

    private val _trashedItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val trashedItems: StateFlow<List<MediaItem>> = _trashedItems.asStateFlow()

    private val _selectedItems = MutableStateFlow<Set<Uri>>(emptySet())
    val selectedItems: StateFlow<Set<Uri>> = _selectedItems.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _isGroupingSimilar = MutableStateFlow(false)
    val isGroupingSimilar: StateFlow<Boolean> = _isGroupingSimilar.asStateFlow()

    private val _trashedUris = MutableStateFlow<Set<Uri>>(emptySet())
    val trashedUris: StateFlow<Set<Uri>> = _trashedUris.asStateFlow()

    private val _customAlbums = MutableStateFlow<Map<String, List<Uri>>>(emptyMap())
    val customAlbums: StateFlow<Map<String, List<Uri>>> = _customAlbums.asStateFlow()

    private val _favoriteUris = MutableStateFlow<Set<Uri>>(emptySet())
    val favoriteUris: StateFlow<Set<Uri>> = _favoriteUris.asStateFlow()

    init {
        loadMedia()
    }

    fun loadMedia() {
        viewModelScope.launch {
            val items = mediaStoreHelper.getMedia()
            _mediaItems.value = items
            updateFilteredMedia()
        }
    }

    fun createCustomAlbum(name: String) {
        if (name.isBlank()) return
        val current = _customAlbums.value.toMutableMap()
        if (!current.containsKey(name)) {
            current[name] = emptyList()
            _customAlbums.value = current
            updateFilteredMedia()
        }
    }

    fun addItemsToAlbum(uris: Set<Uri>, albumName: String) {
        val current = _customAlbums.value.toMutableMap()
        val existing = current[albumName]?.toMutableList() ?: mutableListOf()
        existing.addAll(uris)
        current[albumName] = existing.distinct()
        _customAlbums.value = current
        clearSelection()
        updateFilteredMedia()
    }

    fun renameAlbum(oldName: String, newName: String) {
        if (newName.isBlank() || oldName == newName) return
        val current = _customAlbums.value.toMutableMap()
        val items = current.remove(oldName) ?: emptyList()
        current[newName] = items
        _customAlbums.value = current
        updateFilteredMedia()
    }

    fun deleteAlbum(albumName: String) {
        val current = _customAlbums.value.toMutableMap()
        current.remove(albumName)
        _customAlbums.value = current
        updateFilteredMedia()
    }

    fun permanentlyDelete(uris: Set<Uri>) {
        val currentMedia = _mediaItems.value.filter { !uris.contains(it.uri) }
        _mediaItems.value = currentMedia
        val currentTrash = _trashedUris.value.toMutableSet()
        currentTrash.removeAll(uris)
        _trashedUris.value = currentTrash
        clearSelection()
        updateFilteredMedia()
    }

    fun emptyTrash() {
        permanentlyDelete(_trashedUris.value)
    }

    private fun groupItemsByDate(items: List<MediaItem>): Map<String, List<MediaItem>> {
        val sorted = items.sortedByDescending { it.dateAdded }
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.timeInMillis

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val locale = Locale.getDefault()
        val sameYearFormat = SimpleDateFormat("d MMMM", locale)
        val diffYearFormat = SimpleDateFormat("d MMMM yyyy", locale)

        val isArabic = locale.language == "ar"
        val todayStr = if (isArabic) "اليوم" else "Today"
        val yesterdayStr = if (isArabic) "أمس" else "Yesterday"

        val groupedMap = LinkedHashMap<String, MutableList<MediaItem>>()

        for (item in sorted) {
            val itemCal = Calendar.getInstance().apply { timeInMillis = item.dateAdded }
            val itemYear = itemCal.get(Calendar.YEAR)

            val header = when {
                isSameDay(item.dateAdded, today) -> todayStr
                isSameDay(item.dateAdded, yesterday) -> yesterdayStr
                itemYear == currentYear -> sameYearFormat.format(Date(item.dateAdded))
                else -> diffYearFormat.format(Date(item.dateAdded))
            }
            groupedMap.getOrPut(header) { mutableListOf() }.add(item)
        }

        return groupedMap
    }

    private fun isSameDay(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun toggleSelection(uri: android.net.Uri) {
        val current = _selectedItems.value.toMutableSet()
        if (current.contains(uri)) {
            current.remove(uri)
        } else {
            current.add(uri)
        }
        _selectedItems.value = current
        if (current.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun selectAll() {
        _selectedItems.value = _mediaItems.value.map { it.uri }.toSet()
        _isSelectionMode.value = true
    }

    fun clearSelection() {
        _selectedItems.value = emptySet()
        _isSelectionMode.value = false
    }

    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _selectedItems.value = emptySet()
        }
    }

    fun toggleFavorite(uri: Uri) {
        val current = _favoriteUris.value.toMutableSet()
        if (current.contains(uri)) {
            current.remove(uri)
        } else {
            current.add(uri)
        }
        _favoriteUris.value = current
        updateFilteredMedia()
    }

    fun moveToTrash(uris: Set<Uri>) {
        val current = _trashedUris.value.toMutableSet()
        current.addAll(uris)
        _trashedUris.value = current
        clearSelection()
        updateFilteredMedia()
    }

    fun toggleGroupingSimilar() {
        _isGroupingSimilar.value = !_isGroupingSimilar.value
        updateFilteredMedia()
    }

    private fun updateFilteredMedia() {
        val filteredItems = _mediaItems.value.filter { !_trashedUris.value.contains(it.uri) }
        val bucketMap = filteredItems.groupBy { it.bucketName }.toMutableMap()
        
        // Add custom user albums to album list
        for ((customName, uris) in _customAlbums.value) {
            val itemsInCustom = filteredItems.filter { uris.contains(it.uri) }
            bucketMap[customName] = itemsInCustom
        }

        _albums.value = bucketMap
        
        var itemsForDisplay = filteredItems
        if (_isGroupingSimilar.value) {
            val grouped = mutableListOf<MediaItem>()
            var lastDateAdded: Long = -1
            for (item in itemsForDisplay.sortedByDescending { it.dateAdded }) {
                // Group items within 5 seconds of each other
                if (lastDateAdded == -1L || Math.abs(item.dateAdded - lastDateAdded) > 5000) {
                    grouped.add(item)
                    lastDateAdded = item.dateAdded
                }
            }
            itemsForDisplay = grouped.sortedByDescending { it.dateAdded }
        }
        
        _groupedMedia.value = groupItemsByDate(itemsForDisplay)
        _videoItems.value = filteredItems.filter { it.isVideo }
        _favoriteItems.value = filteredItems.filter { _favoriteUris.value.contains(it.uri) }
        _trashedItems.value = _mediaItems.value.filter { _trashedUris.value.contains(it.uri) }
    }

    fun restoreFromTrash(uris: Set<Uri>) {
        val current = _trashedUris.value.toMutableSet()
        current.removeAll(uris)
        _trashedUris.value = current
        updateFilteredMedia()
    }

    fun saveAdjustments(uri: Uri, adj: ImageAdjustments) {
        val current = _adjustments.value.toMutableMap()
        current[uri] = adj
        _adjustments.value = current
        updateFilteredMedia()
    }
}

