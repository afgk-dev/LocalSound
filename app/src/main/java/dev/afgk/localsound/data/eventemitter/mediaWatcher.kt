package dev.afgk.localsound.data.eventemitter

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore

class MediaWatcher(private val context: Context) {

    private var observer: ContentObserver? = null

    fun startWatching(onChange: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())

        observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                onChange()
            }
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
