package dev.afgk.localsound.data.tracks

import android.net.Uri
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.afgk.localsound.data.core.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface TracksDao : BaseDao<TrackEntity> {
    @Transaction
    @Query("SELECT * FROM tracks")
    fun getEnrichedTracks(): Flow<List<EnrichedTrack>>

    //get a list of all tracks uris on db
    @Query("SELECT uri FROM tracks")
    suspend fun getAllUris(): List<String>

    @Query("SELECT * FROM tracks WHERE uri IN (:uris)")
    fun getTracksWithUriIn(uris: List<Uri>): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE uri NOT IN (:uris)")
    fun getTracksWithUriNotIn(uris: List<Uri>): Flow<List<TrackEntity>>

    @Query("DELETE FROM tracks WHERE uri NOT IN (:uris)")
    suspend fun deleteTracksWithUriNotIn(uris: List<Uri>)

    //Delete the tracks that are not in the storage
    @Query("Delete FROM tracks WHERE uri NOT IN (:storageUris)")
    suspend fun deleteTracksNotInStorage(storageUris: List<String>)

    //Get the tracks that will be deleted
    @Query("SELECT * FROM tracks WHERE uri NOT IN (:storageUris)")
    suspend fun getTracksNotInStorage(storageUris: List<String>): List<TrackEntity>

    @Transaction
    @Query(
        """
        SELECT * FROM tracks 
        WHERE name LIKE '%' || :query || '%' """
    )
    fun searchTracks(query: String): Flow<List<EnrichedTrack>>
}