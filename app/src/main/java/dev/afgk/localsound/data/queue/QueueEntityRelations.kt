package dev.afgk.localsound.data.queue

import androidx.room.Embedded
import androidx.room.Relation
import dev.afgk.localsound.data.tracks.TrackEntity

data class QueueAndTrack(
    @Embedded val queueTrackEntity: QueueTrackEntity,
    @Relation(
        parentColumn = "trackId",
        entityColumn = "id"
    )
    val track: TrackEntity
)