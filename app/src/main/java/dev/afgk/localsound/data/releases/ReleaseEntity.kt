package dev.afgk.localsound.data.releases

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "releases",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["artworkUri"], unique = true)
    ]
)
data class ReleaseEntity(
    @PrimaryKey(
        autoGenerate = true
    ) val id: Long = 0,

    val name: String,
    val artworkUri: Uri,

    val createdAt: Date = Date()
)