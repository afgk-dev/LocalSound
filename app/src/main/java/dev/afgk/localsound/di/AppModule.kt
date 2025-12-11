package dev.afgk.localsound.di

import android.content.Context
import androidx.room.Room
import dev.afgk.localsound.data.artists.ArtistsRepository
import dev.afgk.localsound.data.audioFiles.AudioFilesRepository
import dev.afgk.localsound.data.core.AppDatabase
import dev.afgk.localsound.data.playlists.PlaylistRepository
import dev.afgk.localsound.data.releases.ReleasesRepository
import dev.afgk.localsound.data.synchronize.DatabaseSynchronizer
import dev.afgk.localsound.data.tracks.TracksRepository

interface AppModule {
    val database: AppDatabase

    val audioFilesRepository: AudioFilesRepository
    val playlistRepository: PlaylistRepository
    val tracksRepository: TracksRepository
    val artistsRepository: ArtistsRepository
    val releasesRepository: ReleasesRepository

    val databaseSynchronizer: DatabaseSynchronizer
}

class AppModuleImpl(
    private val context: Context
) : AppModule {
    override val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java, "localsound-db"
        ).build()
    }

    override val audioFilesRepository: AudioFilesRepository by lazy {
        AudioFilesRepository(context)
    }

    override val tracksRepository: TracksRepository by lazy {
        TracksRepository(database.tracksDao())
    }


    override val artistsRepository: ArtistsRepository by lazy {
        ArtistsRepository(database.artistsDao())
    }

    override val releasesRepository: ReleasesRepository by lazy {
        ReleasesRepository(database.releaseDao())
    }

    override val playlistRepository: PlaylistRepository
        get() = PlaylistRepository(
            database.playlistsDao(),
            database.playlistTrackDao()
        )

    override val databaseSynchronizer: DatabaseSynchronizer by lazy {
        DatabaseSynchronizer(
            mediaStoreRepo = audioFilesRepository,
            artistsRepository = artistsRepository,
            releasesRepository = releasesRepository,
            tracksRepository = tracksRepository,
            database = database
        )
    }
}