package dev.afgk.localsound.data.playlists

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.afgk.localsound.data.core.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistsDao : BaseDao<PlaylistEntity> {
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getTotal(): Int

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getPlaylist(id: Long): Flow<PlaylistAndTracks?>
}