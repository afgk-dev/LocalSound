package dev.afgk.localsound.data.queue

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao
import dev.afgk.localsound.data.playlists.PlaylistEntity

class QueueData(
    val position: Int,
    val trackName: String,
    val artistName: String,
    val uri: String,
)

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
    @Query("SELECT q.position, t.name AS 'trackName', t.uri, a.name AS 'artistName' FROM queue_tracks as q JOIN tracks as t ON (q.trackId = t.id) JOIN artists as a ON(t.artistId == a.id) WHERE q.position == 0")
    suspend fun getCurrentTrack():QueueData?

    //Get all tracks on the Queue with the position bigger than 0
    @Query("SELECT q.position, t.name AS 'trackName', t.uri, a.name AS 'artistName' FROM queue_tracks as q JOIN tracks as t ON (q.trackId = t.id) JOIN artists as a ON(t.artistId == a.id) WHERE q.position > 0")
    suspend fun getNextTracks(): List<QueueData>
    //Get all tracks on the Queue with the position bellow 0
    @Query("SELECT q.position, t.name AS 'trackName', t.uri, a.name AS 'artistName' FROM queue_tracks as q JOIN tracks as t ON (q.trackId = t.id) JOIN artists as a ON(t.artistId == a.id) WHERE q.position < 0")
    suspend fun getPastTracks(): List<QueueData>
}