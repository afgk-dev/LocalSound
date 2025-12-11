package dev.afgk.localsound.data.releases

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface ReleaseDao : BaseDao<ReleaseEntity> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override suspend fun insert(vararg obj: ReleaseEntity): List<Long>

    @Query("SELECT * FROM releases WHERE releases.name IN (:names)")
    suspend fun getWhereNameIn(names: List<String>): List<ReleaseEntity>

    @Query(
        """
        DELETE from releases
        WHERE NOT EXISTS (
            SELECT 1
            FROM tracks
            WHERE tracks.releaseId = releases.id
        )
    """
    )
    suspend fun deleteReleasesWithNoTracks()
}