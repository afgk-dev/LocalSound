package dev.afgk.localsound.data.core

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.afgk.localsound.data.artists.ArtistDao
import dev.afgk.localsound.data.artists.ArtistEntity
import dev.afgk.localsound.data.playlists.PlaylistDao
import dev.afgk.localsound.data.playlists.PlaylistEntity
import dev.afgk.localsound.data.playlists.PlaylistTrackDao
import dev.afgk.localsound.data.playlists.PlaylistTrackEntity
import dev.afgk.localsound.data.queue.QueueTrackDao
import dev.afgk.localsound.data.queue.QueueTrackEntity
import dev.afgk.localsound.data.releases.ReleaseDao
import dev.afgk.localsound.data.releases.ReleaseEntity
import dev.afgk.localsound.data.tracks.TrackEntity
import dev.afgk.localsound.data.tracks.TracksDao

@Database(
    entities = [
        ArtistEntity::class,
        ReleaseEntity::class,
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        QueueTrackEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)

abstract class AppDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun releasesDao(): ReleaseDao
    abstract fun tracksDao(): TracksDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackDao(): PlaylistTrackDao
    abstract fun queueDao(): QueueTrackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "localsound_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}
