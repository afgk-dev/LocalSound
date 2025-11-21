package dev.afgk.localsound.data.synchronize

import android.util.Log
import androidx.room.withTransaction
import dev.afgk.localsound.data.artists.ArtistDao
import dev.afgk.localsound.data.artists.ArtistEntity
import dev.afgk.localsound.data.audioFiles.AudioFile
import dev.afgk.localsound.data.audioFiles.AudioFilesRepository
import dev.afgk.localsound.data.core.AppDatabase
import dev.afgk.localsound.data.releases.ReleaseDao
import dev.afgk.localsound.data.releases.ReleaseEntity
import dev.afgk.localsound.data.tracks.TrackEntity
import dev.afgk.localsound.data.tracks.TracksDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date


class DatabaseSynchronizer (
    private val mediaStoreRepo: AudioFilesRepository,
    private val database: AppDatabase,
    private val tracksDao: TracksDao,
    private val artistDao: ArtistDao,
    private val releaseDao: ReleaseDao
){
    // Synchronize the data
    suspend fun sync() {
        // Ensure it will be made on an optimized thread
        withContext(Dispatchers.IO) {
            Log.d("DatabaseSynchronizer", "Starting sync process.")

            val tracksFromDevice = mediaStoreRepo.loadFiles()
            val urisFromDevice = tracksFromDevice.map { it.path }
            val urisInDb = tracksDao.getAllUris()
            //toSet() is much faster to verify if an element is in the list he is big O(1)on average
            val newFiles = tracksFromDevice.filter { it.path !in urisInDb.toSet()}

            Log.d(
                "DatabaseSynchronizer",
                "Encontrados ${newFiles.size} novos arquivos para inserir."
            )
            insertNewTracks(newFiles)

            //Delete tracks that are not in the device and artists and releases without tracks
            val trackIdsToDelete = tracksDao.getIdsOfTracksNotInStorage(urisFromDevice)
            val artistsIdsToDelete = artistDao.getIdsOfArtistsWithoutTracks()
            val releasesToDeleate = releaseDao.getIdsOfReleasesWithoutTracks()
            deleteTracksNotInStorage(trackIdsToDelete)
            deleteArtitsWithoutTracks(artistsIdsToDelete)
            deleteReleasesWithoutTracks(releasesToDeleate)


            Log.d("DatabaseSynchronizer", "Processo de sincronização finalizado.")

        }

    }
    private suspend fun insertNewTracks(newFiles: List<AudioFile>) {
        database.withTransaction {
            //If newFiles i empty will not insert anything
            newFiles.forEach { newFile ->
                val artistName = newFile.artist
                val releaseName = newFile.release
                var artistId: Long? = null
                var releaseId: Long? = null

                //If artistName is null, artistId on tracks will be null
                if (artistName != null) {
                    artistId = artistDao.getArtistIdByName(artistName)
                    // Search for the artistId. If it doesn't find, it will be null
                    if (artistId == null){
                        // If id is null will add on database and get the new id
                        val newArtist = ArtistEntity(id = 0, name = artistName, pictureUri = null, Date())
                        artistId = artistDao.insert(newArtist)
                    }
                }
                //If releaseName is null, releaseId on tracks will be null
                if (releaseName != null) {
                    // If id is null will add on database and get the new id
                    releaseId = releaseDao.getReleaseIdByName(releaseName)
                    if(releaseId == null){
                        val newRelease = ReleaseEntity(id = 0, name = releaseName, coverArtUri = null, Date())
                        releaseId = releaseDao.insert(newRelease)
                    }
                }
                

                val trackEntity = TrackEntity(
                    id = 0,
                    name = newFile.name,
                    duration = newFile.duration.toInt(),
                    uri = newFile.path,
                    artistId = artistId,
                    releaseId = releaseId
                )
                tracksDao.insert(trackEntity)
            }
        }
        Log.d("DatabaseSynchronizer", "${newFiles.size} novos arquivos inseridos com sucesso.")
    }
    private suspend fun deleteTracksNotInStorage(trackIds: List<Long>){
        Log.d("DatabaseSynchronizer", "Encontrados ${trackIds.size} IDs de músicas para excluir.")
        database.withTransaction {
            val deletedTracks = tracksDao.getTracksByIds(trackIds)
            deletedTracks.forEach {Log.d("DatabaseSynchronizer", "Excluindo track: ${it.toString()}") }

            tracksDao.deleteTracksByIds(trackIds)
        }
    }
    private suspend fun deleteArtitsWithoutTracks(artistsIds: List<Long>){
        Log.d("DatabaseSynchronizer", "Encontrados ${artistsIds.size} IDs de artistas para excluir.")
        database.withTransaction {
            val deletedArtists = artistDao.getArtistsById(artistsIds)
            deletedArtists.forEach {Log.d("DatabaseSynchronizer", "Excluindo artista: ${it.toString()}") }

            artistDao.deleteArtitsByIds(artistsIds)
        }
    }
    private suspend fun deleteReleasesWithoutTracks(releasesIds: List<Long>){
        Log.d("DatabaseSynchronizer", "Encontrados ${releasesIds.size} IDs de releases para excluir.")
        database.withTransaction {
            val deletedReleases = releaseDao.getReleasesById(releasesIds)
            deletedReleases.forEach {Log.d("DatabaseSynchronizer", "Excluindo release: ${it.toString()}") }

            releaseDao.deleteReleasesByIds(releasesIds)
        }
    }
}