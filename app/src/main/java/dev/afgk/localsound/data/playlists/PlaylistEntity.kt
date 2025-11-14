package dev.afgk.localsound.data.playlists

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(
    tableName = "playlists"
)
data class PlaylistEntity(
    @PrimaryKey(
        autoGenerate = true
    ) val id: Long? = null,

    val name: String,
    val coverUri: String? = null,

    val createdAt: Date = Date()
)