package dev.afgk.localsound.data.playlists

import dev.afgk.localsound.data.tracks.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistRepository(
    private val playlistsDao: PlaylistsDao,
    private val playlistTrackDao: PlaylistTrackDao
) {
    suspend fun getTotal() = playlistsDao.getTotal()

    fun getPlaylistTracks(id: Long) = playlistsDao.getPlaylist(id)

    suspend fun create(
        name: String,
        firstTrack: TrackEntity? = null
    ) = withContext(Dispatchers.IO) {
        val playlist = PlaylistEntity(name = name)
        val insertedPlaylistId = playlistsDao.insert(playlist)

        if (firstTrack != null) {
            val playlistTrack = PlaylistTrackEntity(
                trackId = firstTrack.id,
                playlistId = insertedPlaylistId
            )

            playlistTrackDao.insert(playlistTrack)
        }
    }
}