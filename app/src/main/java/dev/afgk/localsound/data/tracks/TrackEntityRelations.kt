package dev.afgk.localsound.data.tracks

import androidx.room.Embedded
import androidx.room.Relation
import dev.afgk.localsound.data.artists.ArtistEntity

data class TrackAndArtist(
    @Embedded val track: TrackEntity,
    @Relation(
        parentColumn = "artistId",
        entityColumn = "id"
    )
    val artist: ArtistEntity?
)