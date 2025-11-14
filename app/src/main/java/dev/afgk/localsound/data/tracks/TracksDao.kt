package dev.afgk.localsound.data.tracks

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

class TrackData(
    val trackName: String,
    val duration: String,
    val uri: String,
    val artistName: String
)

@Dao
interface TracksDao: BaseDao<TrackEntity> {

    @Query("SELECT t.name AS 'trackName', t.duration, t.uri, a.name AS 'artistName' FROM tracks as t JOIN artists as a ON (t.artistId == a.id) ORDER BY t.name")
    suspend fun getAll(): List<TrackData>

    @Query("SELECT t.name AS 'trackName', t.duration, t.uri, a.name AS 'artistName' FROM tracks as t JOIN artists as a ON (t.artistId == a.id) WHERE t.name LIKE  :track || '%' ORDER BY t.name")
    suspend fun getTrackByName(track: String): List<TrackData>

}