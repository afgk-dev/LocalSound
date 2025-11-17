package dev.afgk.localsound.data.core

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.afgk.localsound.data.artists.ArtistEntity
import dev.afgk.localsound.data.playlists.PlaylistEntity
import dev.afgk.localsound.data.playlists.PlaylistTrackDao
import dev.afgk.localsound.data.playlists.PlaylistTrackEntity
import dev.afgk.localsound.data.playlists.PlaylistsDao
import dev.afgk.localsound.data.queue.QueueTrackEntity
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
    abstract fun playlistsDao(): PlaylistsDao
    abstract fun playlistTrackDao(): PlaylistTrackDao
    abstract fun tracksDao(): TracksDao
}
