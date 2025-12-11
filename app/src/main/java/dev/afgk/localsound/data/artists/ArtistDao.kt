package dev.afgk.localsound.data.artists

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.afgk.localsound.data.core.BaseDao

@Dao
interface ArtistDao : BaseDao<ArtistEntity> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override suspend fun insert(vararg obj: ArtistEntity): List<Long>

    @Query("SELECT * FROM artists WHERE artists.name IN (:names)")
    suspend fun getWhereNameIn(names: List<String>): List<ArtistEntity>

    @Query(
        """
        DELETE from artists
        WHERE NOT EXISTS (
            SELECT 1
            FROM tracks
            WHERE tracks.artistId = artists.id
        )
    """
    )
    suspend fun deleteArtistsWithNoTracks()
}