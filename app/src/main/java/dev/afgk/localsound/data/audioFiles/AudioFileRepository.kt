package dev.afgk.localsound.data.audioFiles

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File

class AudioFilesRepository(private val context: Context) {
    fun loadFiles(): List<AudioFile> {
        val audioFiles = mutableListOf<AudioFile>()

        val musicFolder = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            .path

        val cols = object {
            val id = MediaStore.Audio.Media._ID
            val title = MediaStore.Audio.Media.TITLE
            val artist = MediaStore.Audio.Media.ARTIST
            val duration = MediaStore.Audio.Media.DURATION

            /** TODO: Use ContentResolver instead of this. Doesn't work in Android 10+ due to Scoped Storage */
            val path = MediaStore.Audio.Media.DATA
        }

        val projection = arrayOf(
            cols.id,
            cols.title,
            cols.artist,
            cols.duration,
            cols.path
        )
        val selection = "${cols.path} LIKE ?"
        val selectionArgs = arrayOf("${musicFolder}${File.separator}%")
        val sortOrder = "${cols.title} ASC"

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
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
                val durationIdx = cursor.getColumnIndexOrThrow(cols.duration)
                val pathIdx = cursor.getColumnIndexOrThrow(cols.path)

                val id = cursor.getLong(idIdx)
                val title = cursor.getString(titleIdx)
                val artist = cursor.getString(artistIdx)
                val duration = cursor.getInt(durationIdx)
                val path = cursor.getString(pathIdx)

                audioFiles.add(AudioFile(id, title, artist, duration, path))
            }
        }

        Log.d("AudioFilesRepository", "Found ${audioFiles.size} files in ${musicFolder}")
        audioFiles.forEach { Log.d("AudioFilesRepository", it.toString()) }

        return audioFiles
    }
}