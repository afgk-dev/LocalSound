package dev.afgk.localsound.data.tracks

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.afgk.localsound.data.core.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface TracksDao : BaseDao<TrackEntity> {
    @Transaction
    @Query("SELECT * FROM tracks")
    fun getTracksWithArtist(): Flow<List<TrackAndArtist>>
}