package dev.afgk.localsound.data.audioFiles

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

class AudioFilesRepository(private val context: Context) {
    fun loadFiles(): List<AudioFile> {
        val audioFiles = mutableListOf<AudioFile>()

        val cols = object {
            val id = MediaStore.Audio.Media._ID
            val title = MediaStore.Audio.Media.TITLE
            val artist = MediaStore.Audio.Media.ARTIST
            val album = MediaStore.Audio.Media.ALBUM
            val albumId = MediaStore.Audio.Media.ALBUM_ID
            val duration = MediaStore.Audio.Media.DURATION
            val isMusic = MediaStore.Audio.Media.IS_MUSIC
        }

        val projection = arrayOf(
            cols.id,
            cols.title,
            cols.artist,
            cols.album,
            cols.albumId,
            cols.duration
        )
        val selection = "${cols.isMusic} = ?"
        val selectionArgs = arrayOf("1")
        val sortOrder = "${cols.title} ASC"

        val audioCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
            else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val cursor = context.contentResolver.query(
            audioCollection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val idIdx = cursor.getColumnIndexOrThrow(cols.id)
                val titleIdx = cursor.getColumnIndexOrThrow(cols.title)
                val artistIdx = cursor.getColumnIndexOrThrow(cols.artist)
                val albumIdx = cursor.getColumnIndexOrThrow(cols.album)
                val albumIdIdx = cursor.getColumnIndexOrThrow(cols.albumId)
                val durationIdx = cursor.getColumnIndexOrThrow(cols.duration)

                val id = cursor.getLong(idIdx)
                val title = cursor.getString(titleIdx)
                val artist = cursor.getString(artistIdx)
                val album = cursor.getString(albumIdx)
                val albumId = cursor.getLong(albumIdIdx)
                val duration = cursor.getInt(durationIdx) / 1000

                val uri = ContentUris.withAppendedId(audioCollection, id)

                val albumArtworkCollectionUri = Uri.parse("content://media/external/audio/albumart")
                val artworkUri =
                    ContentUris.withAppendedId(
                        albumArtworkCollectionUri,
                        albumId
                    )

                audioFiles.add(
                    AudioFile(
                        id,
                        title,
                        artist,
                        album,
                        artworkUri,
                        duration,
                        uri
                    )
                )
            }
        }

        return audioFiles
    }
}