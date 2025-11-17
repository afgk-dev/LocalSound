package dev.afgk.localsound.data.queue

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao
import dev.afgk.localsound.data.playlists.PlaylistEntity

@Dao
interface QueueTrackDao: BaseDao<QueueTrackEntity> {


    /**Update*/
    //Update tracks position on Queue
    @Query("UPDATE queue_tracks SET position = (position - 1)")
    suspend fun updateQueue()

    /**Delete*/
    //Delete all tracks from queue when the position is lower than -5
    @Query("DELETE FROM queue_tracks WHERE position = -5")
    suspend fun deleteQueue()


    /**Select*/
    //Get the current track on the Queue
    @Query("SELECT * FROM queue_tracks as q JOIN tracks as t ON (q.trackId = t.id) JOIN artists as a ON(t.artistId == a.id) WHERE q.position == 0")
    suspend fun getCurrentTrack():QueueTrackEntity?

    //Get all tracks on the Queue with the position bigger than 0
    @Query("SELECT * FROM queue_tracks as q JOIN tracks as t ON (q.trackId = t.id) JOIN artists as a ON(t.artistId == a.id) WHERE q.position > 0")
    suspend fun getNextTracks(): List<QueueTrackEntity>

    //Get all tracks on the Queue with the position bellow 0
    @Query("SELECT * FROM queue_tracks as q JOIN tracks as t ON (q.trackId = t.id) JOIN artists as a ON(t.artistId == a.id) WHERE q.position < 0")
    suspend fun getPastTracks(): List<QueueTrackEntity>


    @Query("DELETE FROM queue_tracks WHERE id = :id")
    suspend fun deleteQueueTrackById(id: List<Long>)
}