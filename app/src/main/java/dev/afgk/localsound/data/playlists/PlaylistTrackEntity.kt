package dev.afgk.localsound.data.playlists

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.afgk.localsound.data.tracks.TrackEntity

@Entity(
    tableName = "playlists_tracks",
    indices = [
        Index(value = ["trackId"]),
        Index(value = ["playlistId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"]
        ),
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"]
        )
    ]
)
data class PlaylistTrackEntity(
    @PrimaryKey(
        autoGenerate = true
    ) val id: Long,

    val trackId: Long,
    val playlistId: Long,
)