package dev.afgk.localsound.data.tracks

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface TracksDao: BaseDao<TrackEntity> {

    @Query("SELECT * FROM tracks as t JOIN artists as a ON (t.artistId == a.id) ORDER BY t.name")
    suspend fun getAll(): List<TrackEntity>

    @Query("SELECT * FROM tracks as t JOIN artists as a ON (t.artistId == a.id) WHERE t.name LIKE  :track || '%' ORDER BY t.name")
    suspend fun getTrackByName(track: String): List<TrackEntity>

    @Query("SELECT uri FROM tracks")
    suspend fun getAllUris(): List<String>

    //Get the id of the Track by the name
    @Query("SELECT id FROM tracks WHERE name = :name")
    suspend fun getTrackIdByName(name: String): Long

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrackById(id: List<Long>)
}