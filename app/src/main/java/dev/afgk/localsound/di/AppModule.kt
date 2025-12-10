package dev.afgk.localsound.di

import android.content.Context
import androidx.room.Room
import dev.afgk.localsound.data.audioFiles.AudioFilesRepository
import dev.afgk.localsound.data.core.AppDatabase
import dev.afgk.localsound.data.eventemitter.MediaWatcher
import dev.afgk.localsound.data.tracks.TracksRepository

interface AppModule {
    val database: AppDatabase
    val audioFilesRepository: AudioFilesRepository
    val tracksRepository: TracksRepository
    val mediaWatcher: MediaWatcher
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
        TracksRepository(database.tracksDao(), audioFilesRepository)
    }

    override val mediaWatcher: MediaWatcher by lazy {
        MediaWatcher(context)
    }
}