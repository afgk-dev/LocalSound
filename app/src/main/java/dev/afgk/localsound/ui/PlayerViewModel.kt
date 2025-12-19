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
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.data.tracks.TracksRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class PlayerTrack(
    val trackId: Long,

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
    val queue: List<PlayerTrack> = listOf(),

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

    private val _state = MutableStateFlow(PlayerUiState())

    val state = _state.asStateFlow()

    fun setPlayer(newPlayer: Player) {
        newPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = update()
            override fun onMediaItemTransition(media: MediaItem?, reason: Int) = update()
            override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) = update()
            override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) = update()

            override fun onPlayerError(error: PlaybackException) =
                _state.update { it.copy(error = error.toString()) }
        })

        player = newPlayer
    }

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

    private fun update() {
        player?.let { p ->
            var currentTrack = _state.value.track
            val currentMedia = p.currentMediaItem

            if (currentTrack != currentMedia && currentMedia != null) {
                val trackId = currentMedia.mediaId.toLong()
                val track = _state.value.queue.find { it.trackId == trackId }

                currentTrack = track
            }

            val duration = p.duration / 1000
            val currentPosition = p.currentPosition / 1000
            val progress = (currentPosition.toFloat() / duration.toFloat()) * 100

            currentTrack = currentTrack?.copy(
                duration = duration,
                position = currentPosition,
                progress = progress
            )

            _state.update {
                it.copy(
                    playing = p.isPlaying,
                    buffering = p.playbackState == Player.STATE_BUFFERING,
                    hidden = p.playbackState == Player.STATE_IDLE,
                    track = currentTrack,
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            _state.collect {
                if (state.value.playing) {
                    while (true) {
                        delay(500.milliseconds)
                        update()
                    }
                }
            }
        }
    }

    fun playPause() = player?.let { if (it.isPlaying) it.pause() else it.play() }

    fun TrackAndArtist.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(track.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.name)
                    .setArtist(artist?.name)
                    .build()
            )
            .build()
    }

    fun TrackAndArtist.toPlayerTrack(): PlayerTrack {
        return PlayerTrack(
            trackId = track.id,
            name = track.name,
            artistName = artist?.name ?: "Artista desconhecido",
            coverUri = null,
            duration = track.duration.toLong()
        )
    }

    fun playTrack(track: TrackAndArtist, initialQueue: List<TrackAndArtist>? = null) {
        player?.let { player ->
            viewModelScope.launch {
                val queue = mutableListOf(track)

                if (initialQueue == null) {
                    tracksRepository
                        .getTracksWithArtist()
                        .first()
                        .filter { it.track.id != track.track.id }
                        .forEach { queue.add(it) }
                } else {
                    initialQueue
                        .filter { it.track.id != track.track.id }
                        .forEach { queue.add(it) }
                }

                _state.update { it.copy(queue = queue.map { t -> t.toPlayerTrack() }) }

                val mediaItems = queue.map { it.toMediaItem() }

                player.setMediaItems(mediaItems)

                if (player.playbackState == Player.STATE_IDLE) player.prepare()

                player.play()
            }
        }
    }

    fun next() = player?.seekToNext()

    fun previous() = player?.seekToPrevious()

    fun shuffle() {
        _state.value.trackQueuePosition?.let { trackPos ->
//            val queueAfterTrack = _state.value.queue.slice(trackPos..-1)
//            val shuffledQueue = queueAfterTrack
//
//            player.currentTimeline
//
//            val window = Timeline.Window()
//            val queue = mutableListOf()
//
//            ShuffleOrder.DefaultShuffleOrder()
        }
    }
}