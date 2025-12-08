package dev.afgk.localsound.data.releases

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.artists.ArtistEntity
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface ReleaseDao: BaseDao<ReleaseEntity> {
    //Get all the releases by prefix
    @Query("SELECT * FROM releases WHERE name LIKE :name || '%'")
    suspend fun getReleaseByName(name: String): List<ReleaseEntity>

    //Get the id of the Release by the name
    @Query("SELECT id FROM releases WHERE name LIKE :name")
    suspend fun getReleaseIdByName(name: String?): Long?

    //Get all colluns by Ids
    @Query("SELECT * FROM artists WHERE id IN (:ids)")
    suspend fun getReleasesById(ids: List<Long>): List<ReleaseEntity>

    //Delete the releases who have no tracks
    @Query("DELETE FROM releases WHERE id NOT IN (SELECT DISTINCT releaseId FROM tracks WHERE releaseId IS NOT NULL)")
    suspend fun deleteReleasesWithoutTracks()

    //Get releases who have no tracks
    @Query("SELECT * FROM artists WHERE id NOT IN (SELECT DISTINCT releaseId FROM tracks WHERE releaseId IS NOT NULL)")
    suspend fun getReleasesWithoutTracks(): List<ArtistEntity>
    //Delete by ids
    @Query("DELETE FROM releases WHERE id IN (:ids)")
    suspend fun deleteReleasesByIds(ids: List<Long>)

    //Get all releases name on db
    @Query("SELECT * FROM releases WHERE name IN (:names)")
    suspend fun getReleasesByNames(names: Set<String>): List<ReleaseEntity>
}