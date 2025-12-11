package dev.afgk.localsound.data.artists

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArtistsRepository(
    private val artistsDao: ArtistDao
) {
    suspend fun insert(vararg artists: ArtistEntity): List<ArtistEntity> =
        withContext(Dispatchers.IO) {
            artistsDao.insert(*artists)
            artistsDao.getWhereNameIn(artists.map { it.name })
        }

    suspend fun deleteWithNoTracks() =
        withContext(Dispatchers.IO) { artistsDao.deleteArtistsWithNoTracks() }
}