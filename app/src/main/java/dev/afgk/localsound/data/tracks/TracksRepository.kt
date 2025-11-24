package dev.afgk.localsound.data.tracksfun

import dev.afgk.localsound.data.tracks.TracksDao

class TracksRepository(
    private val tracksDao: TracksDao
) {
    fun getTracksWithArtist() = tracksDao.getTracksWithArtist()
}