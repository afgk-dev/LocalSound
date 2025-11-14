package dev.afgk.localsound.data.artists

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

class ArtistData(
    val name: String,
    val pictureUri: String
)

@Dao
interface ArtistDao: BaseDao<ArtistEntity> {

    //Get all artists
    @Query("SELECT name, pictureUri FROM artists")
    suspend fun getAllArtists(): List<ArtistData>

    //Get all the artists by prefix
    @Query("SELECT name, pictureUri  FROM artists WHERE name LIKE :name || '%'")
    suspend fun getArtistByName(name: String): List<ArtistData>
}