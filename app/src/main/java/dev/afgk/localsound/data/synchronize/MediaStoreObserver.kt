package dev.afgk.localsound.data.synchronize

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import dev.afgk.localsound.data.core.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MediaStoreObserver(private val context: Context) : ContentObserver(Handler(Looper.getMainLooper())) {

    private val observer = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        if (selfChange) {
            return
        }

        Log.d(
            "MediaStoreObserver",
            "Mudança detectada no MediaStore. Uri: $uri. Agendando sincronização."
        )
        observer.launch {
            try {
                val database = AppDatabase.Companion.getDatabase(context)
                val synchronizer = DatabaseSynchronizer(context, database)

                synchronizer.sync()
            } catch (e: Exception) {
                Log.e("MediaStoreObserver", "Falha na sincronização", e)
            }
        }

    }
}