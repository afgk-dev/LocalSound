package dev.afgk.localsound.data.playlists

import androidx.room.Entity
import androidx.room.ForeignKey
import dev.afgk.localsound.data.tracks.TrackEntity
import java.util.Date

@Entity(
    tableName = "playlists_tracks",
    primaryKeys = ["trackId", "playlistId"],
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaylistTrackEntity(
    val trackId: Long,
    val playlistId: Long,
    val createdAt: Date = Date()
)