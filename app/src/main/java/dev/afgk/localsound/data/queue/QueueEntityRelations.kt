package dev.afgk.localsound.data.queue

import androidx.room.Embedded
import androidx.room.Relation
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.data.tracks.TrackEntity

data class QueueAndTrack(
    @Embedded val queueTrackEntity: QueueTrackEntity,
    @Relation(
        parentColumn = "trackId",
        entityColumn = "id"
    )
    val track: TrackEntity
)

data class QueueWithTrackAndArtist(
    @Embedded val queueTrackEntity: QueueTrackEntity,
    @Relation(
        entity = TrackEntity::class,
        parentColumn = "trackId",
        entityColumn = "id"
    )
    val trackAndArtist: TrackAndArtist
)