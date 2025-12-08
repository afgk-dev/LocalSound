package dev.afgk.localsound.data.playlists

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistRepository(
    private val playlistsDao: PlaylistsDao,
    private val playlistTrackDao: PlaylistTrackDao
) {
    suspend fun getTotal() = playlistsDao.getTotal()

    fun getPlaylistTracks(id: Long) = playlistsDao.getPlaylist(id)

    fun getPlaylists() = playlistsDao.getPlaylists()

    suspend fun update(obj: PlaylistEntity) = playlistsDao.update(obj)

    suspend fun create(
        name: String,
        firstTrackId: Long? = null
    ): PlaylistEntity = withContext(Dispatchers.IO) {
        val playlist = PlaylistEntity(name = name)
        val insertedPlaylistId = playlistsDao.insert(playlist)

        if (firstTrackId != null) {
            val playlistTrack = PlaylistTrackEntity(
                trackId = firstTrackId,
                playlistId = insertedPlaylistId
            )

            playlistTrackDao.insert(playlistTrack)
        }

        return@withContext playlist.copy(id = insertedPlaylistId)
    }

    suspend fun addToPlaylist(
        playlistId: Long,
        trackId: Long
    ) = withContext(Dispatchers.IO) {
        val playlistTrack = PlaylistTrackEntity(
            playlistId = playlistId,
            trackId = trackId
        )

        playlistTrackDao.insert(playlistTrack)
    }

    suspend fun removeFromPlaylist(
        playlistId: Long,
        trackId: Long
    ) = withContext(Dispatchers.IO) {
        playlistTrackDao.delete(PlaylistTrackEntity(trackId, playlistId))
    }
}