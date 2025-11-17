package dev.afgk.localsound.data.emissordeEventos

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore

class Mudancas {

    class MediaObserver(
        handler: Handler,
        private val onChangeCallback: () -> Unit
    ) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            onChangeCallback()
        }
    }

    class MediaWatcher(private val context: Context) {

        private var observer: MediaObserver? = null

        fun startWatching(onChange: () -> Unit) {
            val handler = Handler(Looper.getMainLooper())

            observer = MediaObserver(handler) {
                onChange()
            }

            context.contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                observer!!
            )
        }

        fun stopWatching() {
            observer?.let {
                context.contentResolver.unregisterContentObserver(it)
            }
        }
    }
}
