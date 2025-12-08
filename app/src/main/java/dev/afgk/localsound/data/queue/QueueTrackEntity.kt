package dev.afgk.localsound.data.queue

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.afgk.localsound.data.tracks.TrackEntity

@Entity(
    tableName = "queue_tracks",
    indices = [
        Index(value = ["trackId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"]
        )
    ]
)
data class QueueTrackEntity(
    @PrimaryKey(
        autoGenerate = true
    ) val id: Long = 0,

    val position: Int,
    val isCustomQueue: Boolean = false,
    val isCurrent: Boolean = false,

    val trackId: Long
)