package dev.afgk.localsound.data.audioFiles

data class AudioFile(
    val id: Long,
    val name: String,
    val artist: String?,
    val release: String?,
    val duration: Number,
    val path: String,
)