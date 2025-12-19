package dev.afgk.localsound.data.tracks

import androidx.room.Embedded
import androidx.room.Relation
import dev.afgk.localsound.data.artists.ArtistEntity
import dev.afgk.localsound.data.releases.ReleaseEntity

data class EnrichedTrack(
    @Embedded val track: TrackEntity,
    @Relation(
        parentColumn = "artistId",
        entityColumn = "id"
    )
    val artist: ArtistEntity?,

    @Relation(
        parentColumn = "releaseId",
        entityColumn = "id",
    )
    val release: ReleaseEntity?
)