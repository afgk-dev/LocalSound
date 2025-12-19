package dev.afgk.localsound.ui

import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import dev.afgk.localsound.data.tracks.EnrichedTrack
import dev.afgk.localsound.data.tracks.TracksRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class PlayerTrack(
    val id: Long,

    val name: String,
    val artistName: String,
    val coverUri: Uri? = null,

    val duration: Long,
    val position: Long = 0L,
    val progress: Float = 0f
)

data class PlayerUiState(
    val track: PlayerTrack? = null,
    val trackQueuePosition: Int? = null,

    val playing: Boolean = false,
    val buffering: Boolean = false,
    val hidden: Boolean = true,
    val error: String? = null,

    val shuffle: Boolean = false
)

class PlayerViewModel(
    private val tracksRepository: TracksRepository
) : ViewModel() {
    private val _TAG = "PlayerViewModel"

    private var player: Player? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = update()
        override fun onMediaItemTransition(media: MediaItem?, reason: Int) = update()
        override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) = update()
        override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) = update()

        override fun onPlayerError(error: PlaybackException) =
            _uiState.update { it.copy(error = error.toString()) }
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.collect {
                if (uiState.value.playing) {
                    while (true) {
                        delay(500.milliseconds)
                        update()
                    }
                }
            }
        }
    }

    // Public methods
    fun setPlayer(newPlayer: Player) {
        newPlayer.addListener(playerListener)
        player = newPlayer
    }

    fun playPause() = player?.let { if (it.isPlaying) it.pause() else it.play() }

    fun playTrack(track: EnrichedTrack, initialQueue: List<EnrichedTrack>? = null) {
        player?.let { player ->
            viewModelScope.launch {
                val queue = (initialQueue ?: tracksRepository.getTracksWithArtist().first())
                    .filter { it.track.id != track.track.id }
                    .map { it.toMediaItem() }

                player.setMediaItems(queue)

                if (player.playbackState == Player.STATE_IDLE) player.prepare()

                player.play()
            }
        }
    }

    fun next() = player?.seekToNext()
    fun previous() = player?.seekToPrevious()
    fun seekTo(position: Long) = player?.seekTo(position)

    // Private util methods
    private fun update() {
        player?.let { p ->
            var playingTrack = if (_uiState.value.track != p.currentMediaItem)
                p.currentMediaItem?.toPlayerTrack()
            else _uiState.value.track

            val duration = p.duration
            val currentPosition = p.currentPosition
            val progress = (currentPosition.toFloat() / duration.toFloat()) * 100

            playingTrack = playingTrack?.copy(
                duration = duration,
                position = currentPosition,
                progress = progress
            )

            _uiState.update {
                it.copy(
                    playing = p.isPlaying,
                    buffering = p.playbackState == Player.STATE_BUFFERING,
                    hidden = p.playbackState == Player.STATE_IDLE,
                    track = playingTrack,
                )
            }
        }
    }

    // Extension functions
    @OptIn(UnstableApi::class)
    fun Player.getPlaybackQueue(): List<PlayerTrack> {
        val timeline = currentTimeline
        if (timeline.isEmpty) return emptyList()

        val window = Timeline.Window()
        val queue = mutableListOf<PlayerTrack>()

        val windowCount = timeline.windowCount
        var index = if (shuffleModeEnabled) {
            currentWindowIndex
        } else {
            0
        }

        repeat(windowCount) {
            timeline.getWindow(index, window)
            queue.add(window.mediaItem.toPlayerTrack())

            index = timeline.getNextWindowIndex(
                index,
                repeatMode,
                shuffleModeEnabled
            )
        }

        return queue
    }

    fun MediaItem.toPlayerTrack(): PlayerTrack {
        val trackId = mediaId.toLong()
        val name = mediaMetadata.title?.toString() ?: "Sem título"
        val artistName = mediaMetadata.artist?.toString() ?: "Artista desconhecido"
        val coverUri = mediaMetadata.artworkUri
        val duration = mediaMetadata.durationMs ?: 0L

        return PlayerTrack(
            trackId,
            name,
            artistName,
            coverUri,
            duration,
        )
    }

    fun EnrichedTrack.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(track.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.name)
                    .setArtist(artist?.name)
                    .setArtworkUri(release?.artworkUri)
                    .build()
            )
            .build()
    }
}