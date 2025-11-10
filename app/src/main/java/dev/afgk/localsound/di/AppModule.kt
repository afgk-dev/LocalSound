package dev.afgk.localsound.di

import android.content.Context
import androidx.room.Room
import dev.afgk.localsound.data.audioFiles.AudioFilesRepository
import dev.afgk.localsound.data.core.AppDatabase

interface AppModule {
    val database: AppDatabase
    val audioFilesRepository: AudioFilesRepository
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
}