package dev.afgk.localsound.data.queue

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.afgk.localsound.data.core.BaseDao
import dev.afgk.localsound.data.tracks.TrackEntity

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

    @Query("SELECT * FROM queue_tracks ORDER BY position ASC")
    suspend fun getQueueTracks(): List<QueueTrackEntity>

    @Query("SELECT trackId FROM queue_tracks WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentTrackId(): Long?

    @Transaction
    @Query("""
        SELECT tracks.* FROM tracks 
        INNER JOIN queue_tracks ON tracks.id = queue_tracks.trackId 
        ORDER BY queue_tracks.position ASC
    """)
    suspend fun getTracksInQueueOrdered(): List<TrackEntity>

    @Transaction
    suspend fun updateQueue(tracks: List<QueueTrackEntity>) {
        clearQueue()
        if (tracks.isNotEmpty()) {
            insert(tracks)
        }
    }

    @Transaction
    suspend fun updateCurrentTrack(trackId: Long) {
        clearCurrentStatus()
        setCurrentTrack(trackId)
    }
}
