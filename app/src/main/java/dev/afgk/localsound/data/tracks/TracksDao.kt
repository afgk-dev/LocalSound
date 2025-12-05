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

    //Use that before deletion
    @Query("SELECT * FROM tracks WHERE id IN (:ids)")
    suspend fun getTracksByIds(ids: List<Long>): List<TrackEntity>

    //get a list of all tracks uris on db
    @Query("SELECT uri FROM tracks")
    suspend fun getAllUris(): List<String>

    //Delete the tracks that are not in the storage
    @Query("Delete FROM tracks WHERE uri NOT IN (:storageUris)")
    suspend fun deleteTracksNotInStorage(storageUris: List<String>)

    //Get the tracks that will be deleted
    @Query("SELECT * FROM tracks WHERE uri NOT IN (:storageUris)")
    suspend fun getTracksNotInStorage(storageUris: List<String>): List<TrackEntity>

    //Delete by ids
    @Query("DELETE FROM tracks WHERE id IN (:ids)")
    suspend fun deleteTracksByIds(ids: List<Long>)
}