package dev.afgk.localsound.data.releases

import androidx.room.Dao
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao



class ReleaseData(
    val name: String,
    val coverArtUri: String
)

@Dao
interface ReleaseDao: BaseDao<ReleaseEntity> {

    //Get all of the releases
    @Query("SELECT name, coverArtUri FROM releases")
    suspend fun getAll(): List<ReleaseData>
}