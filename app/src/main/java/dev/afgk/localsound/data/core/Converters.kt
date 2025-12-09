package dev.afgk.localsound.data.core

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    fun fromUriString(value: String?): Uri? {
        return value?.toUri()
    }

    @TypeConverter
    fun uriToUriString(value: Uri?): String? {
        return value?.toString()
    }
}