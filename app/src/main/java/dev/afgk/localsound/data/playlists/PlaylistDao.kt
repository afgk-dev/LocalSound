package dev.afgk.localsound.data.playlists

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface PlaylistDao: BaseDao<PlaylistEntity> {

    //Get all playlists
    @Query("SELECT * FROM playlists")
    suspend fun getAll(): List<PlaylistEntity>
    //Get all the playlists that have the given prefix
    @Query("SELECT * FROM playlists WHERE name LIKE :name || '%'")
    suspend fun getByName(name: String): List<PlaylistEntity>
}