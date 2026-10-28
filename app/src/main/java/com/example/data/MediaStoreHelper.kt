package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateAdded: Long,
    val bucketId: Long,
    val bucketName: String,
    val isVideo: Boolean,
    val videoUrl: String? = null,
    val durationStr: String = "0:15"
)

class MediaStoreHelper(private val context: Context) {
    suspend fun getMedia(): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<MediaItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: ""
                val dateAdded = cursor.getLong(dateColumn) * 1000L // Convert to milliseconds
                val bucketId = cursor.getLong(bucketIdColumn)
                val bucketName = cursor.getString(bucketNameColumn) ?: "Unknown"
                val mediaType = cursor.getInt(mediaTypeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) 
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI 
                    else 
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                mediaList.add(MediaItem(id, contentUri, name, dateAdded, bucketId, bucketName, mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO))
            }
        }
        if (mediaList.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 3600 * 1000L
            val yearMs = 365 * dayMs

            val mockItems = listOf(
                MediaItem(
                    10001L,
                    Uri.parse("https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200"),
                    "Mountain Sunset.jpg",
                    now - 5000,
                    1L,
                    "Nature",
                    false
                ),
                MediaItem(
                    10009L,
                    Uri.parse("https://images.unsplash.com/photo-1545239351-ef35f43d514b?w=1200"),
                    "Cozy Fireplace.mp4",
                    now - 10 * 1000L,
                    4L,
                    "Videos",
                    true,
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    durationStr = "09:56"
                ),
                MediaItem(
                    10003L,
                    Uri.parse("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=1200"),
                    "Hot Pizza.jpg",
                    now - 12 * 3600 * 1000L,
                    2L,
                    "Food",
                    false
                ),
                MediaItem(
                    10004L,
                    Uri.parse("https://images.unsplash.com/photo-1488477181946-6428a0291777?w=1200"),
                    "Fresh Berries.jpg",
                    now - dayMs - 1000,
                    2L,
                    "Food",
                    false
                ),
                MediaItem(
                    10010L,
                    Uri.parse("https://images.unsplash.com/photo-1428908728789-d2de25dbd4e2?w=1200"),
                    "Raindrops.mp4",
                    now - dayMs - 12 * 3600 * 1000L,
                    4L,
                    "Videos",
                    true,
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    durationStr = "00:15"
                ),
                MediaItem(
                    10005L,
                    Uri.parse("https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=1200"),
                    "Paris Eiffel.jpg",
                    now - 3 * dayMs,
                    3L,
                    "Travel",
                    false
                ),
                MediaItem(
                    10011L,
                    Uri.parse("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200"),
                    "Ocean Breeze.mp4",
                    now - 5 * dayMs,
                    4L,
                    "Videos",
                    true,
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    durationStr = "10:53"
                ),
                MediaItem(
                    10006L,
                    Uri.parse("https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=1200"),
                    "Tokyo Neon.jpg",
                    now - 120 * dayMs, // 2024 item
                    3L,
                    "Travel",
                    false
                ),
                MediaItem(
                    10007L,
                    Uri.parse("https://images.unsplash.com/photo-1541167760496-1628856ab772?w=1200"),
                    "Latte Art.jpg",
                    now - 2 * yearMs, // 2022 item
                    2L,
                    "Food",
                    false
                ),
                MediaItem(
                    10012L,
                    Uri.parse("https://images.unsplash.com/photo-1494548162494-384bba4ab999?w=1200"),
                    "City Sunset Drive.mp4",
                    now - 3 * yearMs, // 2021 item
                    4L,
                    "Videos",
                    true,
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    durationStr = "12:14"
                ),
                MediaItem(
                    10008L,
                    Uri.parse("https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=1200"),
                    "Ocean Waves.jpg",
                    now - 5 * yearMs, // 2019 item
                    1L,
                    "Nature",
                    false
                )
            )
            mediaList.addAll(mockItems)
        }
        mediaList
    }
}
