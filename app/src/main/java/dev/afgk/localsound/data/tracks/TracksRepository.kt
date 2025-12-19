package dev.afgk.localsound.data.tracks

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TracksRepository(
    private val tracksDao: TracksDao
) {
    fun getTracksWithArtist() = tracksDao.getEnrichedTracks()

    suspend fun getTracksWithUriIn(uris: List<Uri>) = withContext(Dispatchers.IO) {
        tracksDao.getTracksWithUriIn(uris)
    }

    suspend fun insert(vararg tracks: TrackEntity) = withContext(Dispatchers.IO) {
        tracksDao.insert(*tracks)
    }

    suspend fun deleteTracksWithUriNotIn(uris: List<Uri>) = withContext(Dispatchers.IO) {
        tracksDao.deleteTracksWithUriNotIn(uris)
    }
}