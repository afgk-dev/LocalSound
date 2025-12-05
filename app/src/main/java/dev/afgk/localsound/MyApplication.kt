package dev.afgk.localsound

import android.app.Application
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dev.afgk.localsound.data.synchronize.DatabaseSyncWorker
import dev.afgk.localsound.di.AppModule
import dev.afgk.localsound.di.AppModuleImpl

class MyApplication : Application() {
    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)

        syncDatabaseOnChange()
    }

    private fun syncDatabaseOnChange(){

        val tracksUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
            else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        /*If unconmented it will sync automatically
        val request = OneTimeWorkRequest.Builder(DatabaseSyncWorker::class.java)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "database-sync",
            ExistingWorkPolicy.REPLACE,
            request
        )*/
        Log.d("MyApplication", "DatabaseSyncWorker - Sincronização agendada")
    }
}