package dev.afgk.localsound.data.synchronize

import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import dev.afgk.localsound.data.artists.ArtistEntity
import dev.afgk.localsound.data.artists.ArtistsRepository
import dev.afgk.localsound.data.audioFiles.AudioFilesRepository
import dev.afgk.localsound.data.core.AppDatabase
import dev.afgk.localsound.data.releases.ReleaseEntity
import dev.afgk.localsound.data.releases.ReleasesRepository
import dev.afgk.localsound.data.tracks.TrackEntity
import dev.afgk.localsound.data.tracks.TracksRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class DatabaseSynchronizer(
    private val mediaStoreRepo: AudioFilesRepository,
    private val database: AppDatabase,
    private val tracksRepository: TracksRepository,
    private val artistsRepository: ArtistsRepository,
    private val releasesRepository: ReleasesRepository
) {
    private val _TAG = "DatabaseSynchronizer"

    suspend fun sync() {
        Log.d(_TAG, "Starting sync process")

        val tracksFromDevice = mediaStoreRepo.loadFiles()
        val tracksFromDeviceUris = tracksFromDevice.map { it.uri }

        val alreadySyncedTracks =
            tracksRepository.getTracksWithUriIn(tracksFromDeviceUris).map {
                it.map { t -> t.uri }
            }.first()

        val registersToCreate = tracksFromDevice.filter { it.uri !in alreadySyncedTracks }
            .map {
                val artist = if (it.artist != null) ArtistEntity(
                    name = it.artist
                ) else null

                val release = if (it.release != null) ReleaseEntity(
                    name = it.release
                ) else null

                data class Track(
                    val name: String,
                    val duration: Int,
                    val uri: Uri,
                    val artistName: String?,
                    val releaseName: String?
                )

                val track = Track(
                    name = it.name,
                    duration = it.duration,
                    uri = it.uri,
                    artistName = it.artist,
                    releaseName = it.release
                )

                Triple(track, artist, release)
            }

        val artistsToCreate = registersToCreate.mapNotNull { it.second }.toTypedArray()
        val releasesToCreate = registersToCreate.mapNotNull { it.third }.toTypedArray()

        Log.i(_TAG, "Found ${tracksFromDevice} tracks in device")
        Log.i(_TAG, "Found ${artistsToCreate.size} artists to create")
        Log.i(_TAG, "Found ${releasesToCreate.size} releases to create")
        Log.i(_TAG, "Found ${registersToCreate.size} tracks to create")

        database.withTransaction {
            val createdArtists = artistsRepository.insert(*artistsToCreate)
            val createdReleases = releasesRepository.insert(*releasesToCreate)

            val tracksToCreate = registersToCreate.map { (track) ->
                val artistId = createdArtists.firstOrNull {
                    it.name == track.artistName
                }?.id

                val releaseId = createdReleases.firstOrNull {
                    it.name == track.releaseName
                }?.id

                TrackEntity(
                    name = track.name,
                    uri = track.uri,
                    duration = track.duration,
                    artistId = artistId,
                    releaseId = releaseId
                )
            }.toTypedArray()

            val createdTracks = tracksRepository.insert(*tracksToCreate)

            Log.i(_TAG, "Created ${createdTracks.size} tracks")
            Log.i(_TAG, createdTracks.joinToString("\n") { it.toString() })
        }

        Log.i(
            _TAG,
            "Deleting tracks not present in storage and clearing artists and releases without any tracks"
        )

        tracksRepository.deleteTracksWithUriNotIn(tracksFromDeviceUris)
        artistsRepository.deleteWithNoTracks()
        releasesRepository.deleteWithNoTracks()

        Log.d(_TAG, "Sync process finished!")
    }
}