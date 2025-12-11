package dev.afgk.localsound.data.audioFiles

import android.net.Uri

data class AudioFile(
    val id: Long,
    val name: String,
    val artist: String?,
    val release: String?,
    val duration: Int,
    val uri: Uri,
)