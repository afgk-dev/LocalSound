package dev.afgk.localsound.data.playlists

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

class PlaylistData(
    val playlistName: String,
    val coverUri: String
)

@Dao
interface PlaylistDao: BaseDao<PlaylistEntity> {

    //Get all playlists
    @Query("SELECT name AS 'playlistName', coverUri FROM playlists")
    suspend fun getAll(): List<PlaylistData>
    //Get all the playlists that have the given prefix
    @Query("SELECT name AS 'playlistName', coverUri FROM playlists WHERE name LIKE :name || '%'")
    suspend fun getByName(name: String): List<PlaylistData>
}