package dev.afgk.localsound.data.queue

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface QueueDao : BaseDao<QueueTrackEntity> {
    @Query("DELETE FROM queue_tracks")
    suspend fun clearQueue()

    @Query("UPDATE queue_tracks SET isCurrent = 0")
    suspend fun clearCurrentStatus()

    @Query("UPDATE queue_tracks SET isCurrent = 1 WHERE trackId = :trackId")
    suspend fun setCurrentTrack(trackId: Long)

    @Query("SELECT trackId FROM queue_tracks ORDER BY position ASC")
    suspend fun getAllQueueTrackIds(): List<Long>

    @Transaction
    suspend fun updateQueue(tracks: List<QueueTrackEntity>) {
        clearQueue()
        insert(tracks)
    }

    @Transaction
    suspend fun updateCurrentTrack(trackId: Long) {
        clearCurrentStatus()
        setCurrentTrack(trackId)
    }
}
