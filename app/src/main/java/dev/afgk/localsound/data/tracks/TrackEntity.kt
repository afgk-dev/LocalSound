package dev.afgk.localsound.data.tracks

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.afgk.localsound.data.artists.ArtistEntity
import dev.afgk.localsound.data.releases.ReleaseEntity
import java.util.Date

@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["artistId"]),
        Index(value = ["releaseId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["id"],
            childColumns = ["artistId"]
        ),
        ForeignKey(
            entity = ReleaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["releaseId"]
        )
    ]
)
data class TrackEntity(
    @PrimaryKey(
        autoGenerate = true
    ) val id: Long = 0,

    val name: String,
    val duration: Int,
    val uri: Uri,

    val artistId: Long? = null,
    val releaseId: Long? = null,

    val createdAt: Date = Date()
)