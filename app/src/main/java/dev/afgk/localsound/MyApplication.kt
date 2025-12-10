package dev.afgk.localsound

import android.app.Application
import dev.afgk.localsound.di.AppModule
import dev.afgk.localsound.di.AppModuleImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MyApplication : Application() {

    // Create a CoroutineScope that will live as long as the application itself
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)

        // Start listening for media store changes at the application level
        observeMediaStoreChanges()
    }

    private fun observeMediaStoreChanges() {
        appModule.mediaWatcher.mediaStoreChanges
            .onEach {
                // When a change is detected, re-sync the tracks repository.
                // This will fetch new songs and update the database, and the UI
                // will be automatically updated thanks to the Flow in the ViewModel.
                appModule.tracksRepository.sync()
            }
            .launchIn(applicationScope)
    }
}