package dev.afgk.localsound

import android.app.Application
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import dev.afgk.localsound.data.synchronize.MediaStoreObserver
import dev.afgk.localsound.di.AppModule
import dev.afgk.localsound.di.AppModuleImpl

class MyApplication : Application() {
    companion object {
        lateinit var appModule: AppModule
    }

    private lateinit var mediaStoreObserver: MediaStoreObserver

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)

        registerMediaStoreObserver()
    }

    private fun registerMediaStoreObserver() {
        mediaStoreObserver = MediaStoreObserver(applicationContext)

        val audioUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        contentResolver.registerContentObserver(
            audioUri,
            true,
            mediaStoreObserver
        )

        Log.d("MyApplication", "MediaStoreObserver ativo")
    }

    override fun onTerminate() {
        contentResolver.unregisterContentObserver(mediaStoreObserver)
        super.onTerminate()
    }
}