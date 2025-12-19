package dev.afgk.localsound.data.playlists

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import dev.afgk.localsound.data.tracks.EnrichedTrack
import dev.afgk.localsound.data.tracks.TrackEntity

data class PlaylistTrackWithDetails(
    @Embedded
    val connection: PlaylistTrackEntity,

    @Relation(
        entity = TrackEntity::class,
        parentColumn = "trackId",
        entityColumn = "id"
    )
    val track: EnrichedTrack
)

data class PlaylistAndTracksWithArtists(
    @Embedded val playlist: PlaylistEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId",
        entity = PlaylistTrackEntity::class
    )
    val tracks: List<PlaylistTrackWithDetails>
)

data class PlaylistAndTracks(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistTrackEntity::class,
            parentColumn = "playlistId",
            entityColumn = "trackId"
        )
    )
    val tracks: List<TrackEntity>
)