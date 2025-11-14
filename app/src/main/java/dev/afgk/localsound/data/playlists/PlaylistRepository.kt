package dev.afgk.localsound.data.playlists

import dev.afgk.localsound.data.tracks.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao
) {
    suspend fun getTotal() = playlistDao.getTotal()

    suspend fun create(
        name: String,
        firstTrack: TrackEntity? = null
    ) = withContext(Dispatchers.IO) {
        val playlist = PlaylistEntity(name = name)
        val insertedPlaylistId = playlistDao.insert(playlist)

        if (firstTrack != null) {
            val playlistTrack = PlaylistTrackEntity(
                trackId = firstTrack.id,
                playlistId = insertedPlaylistId
            )

            playlistTrackDao.insert(playlistTrack)
        }
    }
}