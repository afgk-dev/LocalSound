package dev.afgk.localsound.data.audioFiles

import android.content.ContentUris
import android.content.Context
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
            val duration = MediaStore.Audio.Media.DURATION
            val isMusic = MediaStore.Audio.Media.IS_MUSIC
        }

        val projection = arrayOf(
            cols.id,
            cols.title,
            cols.artist,
            cols.album,
            cols.duration
        )
        val selection = "${cols.isMusic} = ?"
        val selectionArgs = arrayOf("1")
        val sortOrder = "${cols.title} ASC"

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
            else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val cursor = context.contentResolver.query(
            collection,
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
                val durationIdx = cursor.getColumnIndexOrThrow(cols.duration)

                val id = cursor.getLong(idIdx)
                val title = cursor.getString(titleIdx)
                val artist = cursor.getString(artistIdx)
                val album = cursor.getString(albumIdx)
                val duration = cursor.getInt(durationIdx) / 1000

                val uri = ContentUris.withAppendedId(collection, id)

                audioFiles.add(
                    AudioFile(
                        id = id,
                        name = title,
                        artist = artist,
                        release = album,
                        duration = duration,
                        uri = uri
                    )
                )
            }
        }

        return audioFiles
    }
}