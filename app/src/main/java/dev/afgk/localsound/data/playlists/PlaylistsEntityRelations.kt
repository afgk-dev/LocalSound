package dev.afgk.localsound.data.playlists

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.data.tracks.TrackEntity

data class PlaylistAndTracks(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entity = TrackEntity::class,
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistTrackEntity::class,
            parentColumn = "playlistId",
            entityColumn = "trackId"
        )
    )
    val tracks: List<TrackAndArtist>
)