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
}