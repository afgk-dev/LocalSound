package dev.afgk.localsound.data.tracks

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface TracksDao: BaseDao<TrackEntity> {
    //get a list of all tracks uris on db
    @Query("SELECT uri FROM tracks")
    suspend fun getAllUris(): List<String>

    //Get the tracks ids who are in the db but not in the storage
    @Query("SELECT id FROM tracks WHERE uri NOT IN (:storageUris)")
    suspend fun getIdsOfTracksNotInStorage(storageUris: List<String>): List<Long>

    //@Query("SELECT * FROM tracks WHERE id IN(id: List<Long>)
    //Delete tracks by ids
    @Query("DELETE FROM tracks WHERE id IN (:ids)")
    suspend fun deleteTracksByIds(ids: List<Long>)
}