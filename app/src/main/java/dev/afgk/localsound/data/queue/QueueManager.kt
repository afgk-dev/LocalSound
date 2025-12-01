
package dev.afgk.localsound.data.queue

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dev.afgk.localsound.data.audioFiles.AudioFile

class QueueManager(private val context: Context) {

    private var player: ExoPlayer? = null
    private var isShuffle = false

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()
        }
    }

    fun setTracks(tracks: List<AudioFile>) {
        val mediaItems = tracks.map { MediaItem.fromUri(it.path) }
        player?.setMediaItems(mediaItems)
        player?.prepare()
    }

    fun playTrack(index: Int) {
        player?.seekToDefaultPosition(index)
        player?.playWhenReady = true
    }

    fun playPause() {
        if (player?.isPlaying == true) {
            player?.pause()
        } else {
            player?.play()
        }
    }

    fun skipToNext() {
        player?.seekToNext()
    }

    fun skipToPrevious() {
        player?.seekToPrevious()
    }

    fun setShuffleMode(shuffle: Boolean) {
        isShuffle = shuffle
        player?.shuffleModeEnabled = isShuffle
    }

    fun toggleShuffleMode() {
        setShuffleMode(!isShuffle)
    }

    fun release() {
        player?.release()
        player = null
    }

    fun getPlayer(): ExoPlayer? {
        return player
    }
}
