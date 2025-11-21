package dev.afgk.localsound.data.artists

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface ArtistDao: BaseDao<ArtistEntity> {
    //Get all the artists by prefix
    @Query("SELECT * FROM artists WHERE name LIKE :name || '%'")
    suspend fun getArtistByName(name: String): List<ArtistEntity>

    //Get the id of the artist by the name
    @Query("SELECT id FROM artists WHERE name = :name")
    suspend fun getArtistIdByName(name: String?): Long?

    //Get all colluns by Ids
    @Query("SELECT * FROM artists WHERE id = (:ids)")
    suspend fun getArtistsById(ids: List<Long>): List<ArtistEntity>

    //Return the ids of artists who have no tracks
    @Query("SELECT a.id FROM artists AS a JOIN tracks AS t ON (a.id = t.artistId) GROUP BY a.id HAVING COUNT(t.artistId) = 0")
    suspend fun getIdsOfArtistsWithoutTracks(): List<Long>

    //Delete by ids
    @Query("DELETE FROM artists WHERE id IN (:ids)")
    suspend fun deleteArtitsByIds(ids: List<Long>)
}