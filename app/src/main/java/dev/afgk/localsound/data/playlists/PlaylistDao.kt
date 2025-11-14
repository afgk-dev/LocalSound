package dev.afgk.localsound.data.playlists

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface PlaylistDao : BaseDao<PlaylistEntity> {
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getTotal(): Int
}