package dev.afgk.localsound.data.tracks
import kotlinx.coroutines.flow.Flow
import dev.afgk.localsound.data.tracks.TracksDao

class TracksRepository(
    private val tracksDao: TracksDao
) {
    fun getTracksWithArtist() = tracksDao.getTracksWithArtist()

    fun searchTracks(query: String): Flow<List<TrackAndArtist>> {
        return tracksDao.searchTracks(query)
    }
}