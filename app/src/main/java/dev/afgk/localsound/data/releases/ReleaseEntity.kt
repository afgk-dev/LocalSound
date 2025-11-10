package dev.afgk.localsound.data.releases

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "releases",
    indices = [
        Index(value = ["coverArtUri"], unique = true)
    ]
)
data class ReleaseEntity(
    @PrimaryKey(
        autoGenerate = true
    ) val id: Long,

    val name: String,
    val coverArtUri: String?,

    val createdAt: Date
)