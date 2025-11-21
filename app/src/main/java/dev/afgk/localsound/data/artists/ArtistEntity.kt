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
    ) val id: Long,

    val name: String,
    val pictureUri: String?,

    val createdAt: Date
)