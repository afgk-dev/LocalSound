package dev.afgk.localsound.data.tracks

import dev.afgk.localsound.data.audioFiles.AudioFilesRepository

class TracksRepository(
    private val tracksDao: TracksDao,
    private val audioFilesRepository: AudioFilesRepository
) {
    fun getTracksWithArtist() = tracksDao.getTracksWithArtist()

    /**
     * Fetches all audio files from the MediaStore and inserts them into the local database.
     * This is the central point for keeping the app's data in sync with the device's media.
     */
    suspend fun sync() {
        val audioFiles = audioFilesRepository.loadFiles()
        val trackEntities = audioFiles.map { audioFile ->
            TrackEntity(
                // Note: The ID from MediaStore is not used as the primary key to avoid conflicts.
                name = audioFile.name,
                duration = audioFile.duration.toInt(),
                uri = audioFile.path, // Assuming 'path' is the URI
                // artistId and releaseId would require more complex logic to resolve
                // For now, we leave them as null.
            )
        }
        tracksDao.insert(*trackEntities.toTypedArray())
    }
}