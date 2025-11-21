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
}