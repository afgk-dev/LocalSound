package dev.afgk.localsound.di

import android.content.Context
import dev.afgk.localsound.data.audioFiles.AudioFilesRepository

interface AppModule {
    val audioFilesRepository: AudioFilesRepository
}

class AppModuleImpl(
    private val context: Context
) : AppModule {
    override val audioFilesRepository: AudioFilesRepository by lazy {
        AudioFilesRepository(context)
    }
}