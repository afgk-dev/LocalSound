package dev.afgk.localsound.data.releases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReleasesRepository(
    private val releaseDao: ReleaseDao
) {
    suspend fun insert(vararg releases: ReleaseEntity): List<ReleaseEntity> =
        withContext(Dispatchers.IO) {
            releaseDao.insert(*releases)
            releaseDao.getWhereNameIn(releases.map { it.name })
        }

    suspend fun deleteWithNoTracks() =
        withContext(Dispatchers.IO) { releaseDao.deleteReleasesWithNoTracks() }
}