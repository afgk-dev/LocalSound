package dev.afgk.localsound.data.queue

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.afgk.localsound.data.core.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao : BaseDao<QueueTrackEntity> {
    @Query("SELECT MAX(position) FROM queue_tracks")
    suspend fun getLastPosition(): Int?

    @Transaction
    @Query("SELECT * FROM queue_tracks ORDER BY position ASC")
    fun getQueueWithTracksAndArtists(): Flow<List<QueueWithTrackAndArtist>>

}