package dev.afgk.localsound.data.eventemitter

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Observes changes in the Android MediaStore for external audio files.
 *
 * This class uses a callbackFlow to expose MediaStore changes as a Flow.
 * This makes it lifecycle-aware and easy to consume from a CoroutineScope,
 * like a ViewModel's viewModelScope, automatically handling registration
 * and un-registration of the ContentObserver.
 */
class MediaWatcher(private val context: Context) {

    val mediaStoreChanges: Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                // A change was detected in the MediaStore, emit a signal.
                trySend(Unit)
            }
        }

        // Register the observer to listen for changes.
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )

        // When the flow is cancelled, unregister the observer.
        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }
}
