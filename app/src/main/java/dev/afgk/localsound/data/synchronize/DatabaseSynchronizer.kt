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


class DatabaseSynchronizer (
    private val mediaStoreRepo: AudioFilesRepository,
    private val database: AppDatabase
){
    // Synchronize the data
    suspend fun sync() {
        // Ensure it will be made on an optimized thread
        withContext(Dispatchers.IO) {
            Log.d("DatabaseSynchronizer", "Starting sync process")

            val tracksFromDevice = mediaStoreRepo.loadFiles()
            val urisFromDevice = tracksFromDevice.map { it.path }
            val urisInDb = database.tracksDao().getAllUris().toSet()
            //toSet() is much faster to verify if an element is in the list he is big O(1)on average
            val newFiles = tracksFromDevice.filter { it.path !in urisInDb}

            Log.d(
                "DatabaseSynchronizer",
                "Encontrados ${newFiles.size} novos arquivos para inserir"
            )
            insertNewTracks(newFiles)

            //Delete tracks that are not in the device and artists and releases without tracks
            database.withTransaction {
                deleteTracksNotInStorage(urisFromDevice)
                deleteArtistsWithoutTracks()
                deleteReleasesWithoutTracks()
            }


            Log.d("DatabaseSynchronizer", "Processo de sincronização finalizado")

        }

    }
    private suspend fun insertNewTracks(newFiles: List<AudioFile>) {
        if (newFiles.isNotEmpty()) {
            database.withTransaction {
                val tracksToInsert = newFiles.map { newFile ->
                    val artistName = newFile.artist
                    val releaseName = newFile.release
                    var artistId: Long? = null
                    var releaseId: Long? = null

                    if (artistName != null) {
                        artistId = database.artistsDao().getArtistIdByName(artistName)
                        if (artistId == null){
                            val newArtist = ArtistEntity(
                                name = artistName,
                                pictureUri = null
                            )
                            artistId = database.artistsDao().insert(newArtist)
                        }
                    }

                    if (releaseName != null) {
                        releaseId = database.releaseDao().getReleaseIdByName(releaseName)
                        if(releaseId == null){
                            val newRelease = ReleaseEntity(
                                name = releaseName,
                                coverArtUri = null
                            )
                            releaseId = database.releaseDao().insert(newRelease)
                        }
                    }

                    TrackEntity(
                        name = newFile.name,
                        duration = newFile.duration.toInt(),
                        uri = newFile.path,
                        artistId = artistId,
                        releaseId = releaseId
                    )
                }

                database.tracksDao().insert(*tracksToInsert.toTypedArray())
                }
            Log.d("DatabaseSynchronizer", "${newFiles.size} novos arquivos inseridos com sucesso")
        }
        else {
            Log.d("DatabaseSynchronizer", "Nenhum novo arquivo para inserir")
        }
    }
    private suspend fun deleteTracksNotInStorage(urisFromDevice: List<String>){
        val deletedTracks = database.tracksDao().getTracksNotInStorage(urisFromDevice)
        Log.d("DatabaseSynchronizer", "Encontrados ${deletedTracks.size} músicas para excluir")
        deletedTracks.forEach {Log.d("DatabaseSynchronizer", "Excluindo track: ${it.toString()}") }

        database.tracksDao().deleteTracksNotInStorage(urisFromDevice)

    }
    private suspend fun deleteArtistsWithoutTracks(){
        val deletedArtists = database.artistsDao().getArtistsWithoutTracks()
        Log.d("DatabaseSynchronizer", "Encontrados ${deletedArtists.size} artistas para excluir")
        deletedArtists.forEach {Log.d("DatabaseSynchronizer", "Excluindo artista: ${it.toString()}") }

        database.artistsDao().deleteArtistsWithoutTracks()
    }
    private suspend fun deleteReleasesWithoutTracks(){
        val deletedReleases = database.releaseDao().getReleasesWithoutTracks()
        Log.d("DatabaseSynchronizer", "Encontrados ${deletedReleases.size} releases para excluir")
        deletedReleases.forEach {Log.d("DatabaseSynchronizer", "Excluindo release: ${it.toString()}") }

        database.releaseDao().deleteReleasesWithoutTracks()
    }
}