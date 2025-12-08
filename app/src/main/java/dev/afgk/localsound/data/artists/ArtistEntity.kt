package dev.afgk.localsound.data.artists

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "artists",
    indices = [
        Index(value = ["pictureUri"], unique = true)
    ]
)
data class ArtistEntity(
    @PrimaryKey(
        autoGenerate = true
    ) val id: Long = 0,

    val name: String,
    val pictureUri: String? = null,

    val createdAt: Date = Date()
)