package dev.afgk.localsound.data.synchronize

import android.content.Context
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
    private val context: Context,
    private val database: AppDatabase){

    private val mediaStoreRepo = AudioFilesRepository(context)

    // Get the DAOs
    private val tracksDao: TracksDao = database.tracksDao()
    private val artistDao: ArtistDao = database.artistsDao()
    private val releaseDao: ReleaseDao = database.releaseDao()

    // Synchronize the data

        suspend fun sync() {

        // Ensure it will be made on an optimized thread
        withContext(Dispatchers.IO) {
            Log.d("DatabaseSynchronizer", "Starting sync process.")

            val filesFromDevice = mediaStoreRepo.loadFiles()
            val urisFromDevice = filesFromDevice.map { it.path }
            val tracksFromDb = tracksDao.getAll()
            val urisInDb = tracksFromDb.map { it.uri }



            val newFiles = filesFromDevice.filter { it.path !in urisInDb }

            if (newFiles.isNotEmpty()) {
                Log.d(
                    "DatabaseSynchronizer",
                    "Encontrados ${newFiles.size} novos arquivos para inserir."
                )
                insertNewTracks(newFiles)
            } else {
                Log.d("DatabaseSynchronizer", "Nenhum arquivo novo para inserir.")
            }


            val trackIdsToDelete = tracksFromDb
                .filter { it.uri !in urisFromDevice } // Filtra as ENTIDADES que não estão mais no dispositivo
                .map { it.id }                           // Extrai APENAS os IDs dessas entidades

            if (trackIdsToDelete.isNotEmpty()) {
                Log.d("DatabaseSynchronizer", "Encontrados ${trackIdsToDelete.size} IDs de músicas para excluir.")
                deleteOldTracks(trackIdsToDelete) // Chama a nova função de deleção por IDs
            } else {
                Log.d("DatabaseSynchronizer", "Nenhuma música para excluir.")
            }

            Log.d("DatabaseSynchronizer", "Processo de sincronização finalizado.")

        }

    }
    private suspend fun insertNewTracks(newFiles: List<AudioFile>) {
        database.withTransaction {
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

    private suspend fun deleteOldTracks(tracksIdsToDelete: List<Long>) {
        database.withTransaction {
            tracksDao.deleteTrackById(tracksIdsToDelete)
        }
        Log.d("DatabaseSynchronizer", "Músicas obsoletas excluídas com sucesso.")
    }
}