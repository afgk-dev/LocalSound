package dev.afgk.localsound.data.playlists

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface PlaylistTrackDao: BaseDao<PlaylistTrackEntity> {

    //Get all tracks on a specified playlist
    @Query("SELECT * FROM playlists_tracks pt JOIN tracks t ON(pt.trackId == t.id) JOIN playlists p  ON(pt.playlistId = p.id) JOIN artists a ON (t.artistId = a.id) WHERE p.name LIKE :playlistName ORDER BY  t.name")
    suspend fun getAll(playlistName: String): List<PlaylistTrackEntity>

    //Get all tracks that have the given prefix on a specified playlist
    @Query("SELECT * FROM playlists_tracks pt JOIN tracks t ON(pt.trackId == t.id) JOIN playlists p  ON(pt.playlistId = p.id) JOIN artists a ON (t.artistId = a.id) WHERE p.name LIKE :playlistName AND t.name LIKE  :trackName || '%'  ORDER BY  t.name")
    suspend fun getByName(playlistName: String, trackName: String): List<PlaylistTrackEntity>

    @Query("DELETE FROM playlists_tracks WHERE id = :id")
    suspend fun deletePlaylistTracksById(id: List<Long>)
}