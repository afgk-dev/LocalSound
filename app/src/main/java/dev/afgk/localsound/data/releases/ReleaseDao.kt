package dev.afgk.localsound.data.releases

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface ReleaseDao: BaseDao<ReleaseEntity> {
    //Get all the releases by prefix
    @Query("SELECT * FROM releases WHERE name LIKE :name || '%'")
    suspend fun getReleaseByName(name: String): List<ReleaseEntity>

    //Get the id of the Release by the name
    @Query("SELECT id FROM releases WHERE name = :name")
    suspend fun getReleaseIdByName(name: String?): Long?

    //Get all colluns by Ids
    @Query("SELECT * FROM artists WHERE id = (:ids)")
    suspend fun getReleasesById(ids: List<Long>): List<ReleaseEntity>

    //Return the ids of releases who have no tracks
    @Query("SELECT r.id FROM releases AS r JOIN tracks AS t ON (r.id = t.releaseId) GROUP BY r.id HAVING COUNT(t.releaseId) = 0")
    suspend fun getIdsOfReleasesWithoutTracks(): List<Long>
    //Delete by ids
    @Query("DELETE FROM releases WHERE id IN (:ids)")
    suspend fun deleteReleasesByIds(ids: List<Long>)
}