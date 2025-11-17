package dev.afgk.localsound.data.releases

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface ReleaseDao: BaseDao<ReleaseEntity> {

    //Get all of the releases
    @Query("SELECT * FROM releases")
    suspend fun getAll(): List<ReleaseEntity>
}