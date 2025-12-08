package dev.afgk.localsound.data.synchronize

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.afgk.localsound.MyApplication

class DatabaseSyncWorker(
    private val appContext: Context,
    private val params: WorkerParameters
): CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result{
        Log.d("DatabaseSyncWorker", "Worker iniciado para sincronizar o bd")

        val synchronizer = MyApplication.appModule.databaseSynchronizer

        return try {
            synchronizer.sync()
            Log.d("DatabaseSyncWorker", "Sincronização feita com sucesso")
            Result.success()
        } catch (e: Exception) {
            Log.e("DatabaseSyncWorker", "Erro durante a sincronização", e)
            Result.failure()
        }
    }
}