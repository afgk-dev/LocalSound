package dev.afgk.localsound.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
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

    val isCustomQueued: Boolean = false,
    val queueIndex: Int = 0,

    val duration: Long,
    val position: Long = 0L,
    val progress: Float = 0f
)

data class PlayerUiState(
    val track: PlayerTrack? = null,
    val trackQueuePosition: Int? = null,

    val nextQueue: List<PlayerTrack> = listOf(),

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
        override fun onTimelineChanged(timeline: Timeline, reason: Int) = update()

        override fun onPlayerError(error: PlaybackException) =
            _uiState.update { it.copy(error = error.toString()) }
    }

    private val mediaItemExtrasKeys = object {
        val isCustomQueue = "isCustomQueue"
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
                val queue = mutableListOf(track.toMediaItem())

                (initialQueue ?: tracksRepository.getTracksWithArtist().first())
                    .filter { it.track.id != track.track.id }
                    .map { it.toMediaItem() }
                    .forEach { queue.add(it) }

                player.setMediaItems(queue)

                if (player.playbackState == Player.STATE_IDLE) player.prepare()

                player.play()
            }
        }
    }

    fun addNext(trackId: Long) {
        player?.let { player ->
            val (fromIndex, mediaItem) = player.findTimelineIndexByMediaId(trackId.toString())
            val toIndex = player.currentMediaItemIndex + 1

            if (fromIndex != -1 && mediaItem != null) {
                player.moveMediaItem(fromIndex, toIndex)
                player.replaceMediaItem(toIndex, mediaItem.withCustomQueueFlag(true))
                update()
            }
        }
    }

    fun addNext(track: EnrichedTrack) {
        player?.let { player ->
            val toIndex = player.currentMediaItemIndex + 1

            player.addMediaItem(toIndex, track.toMediaItem().withCustomQueueFlag(true))
            update()
        }
    }

    fun removeFromQueue(trackId: Long) {
        player?.let { player ->
            val (fromIndex) = player.findTimelineIndexByMediaId(trackId.toString())

            if (fromIndex != 1) {
                player.removeMediaItem(fromIndex)
            }
        }
    }

    fun next() = player?.seekToNext()
    fun previous() = player?.seekToPrevious()
    fun seekTo(position: Long) = player?.seekTo(position)

    // Private util methods
    private fun update() {
        player?.let { player ->
            var playingTrack = if (_uiState.value.track != player.currentMediaItem)
                player.currentMediaItem?.toPlayerTrack()
            else _uiState.value.track

            /**
             * This check is done to avoid sending negative numbers when transitioning
             * from one track to another using seekNext and seekPrevious
             */
            val duration = if (player.duration > 0L) player.duration else 1L
            val currentPosition = player.currentPosition
            val progress = (currentPosition.toFloat() / duration.toFloat()) * 100

            playingTrack = playingTrack?.copy(
                duration = duration,
                position = currentPosition,
                progress = progress
            )

            Log.d(
                _TAG,
                "${player.getPlaybackQueue().map { "(${it.name},${it.queueIndex})" }}"
            )

            val queue = player.getPlaybackQueue()
            val nextQueue = player.getUpcomingQueue()

            queue
                .filter { track -> track.id !in nextQueue.map { it.id } && track.isCustomQueued }
                .forEach { player.removeMediaItem(it.queueIndex) }

            _uiState.update {
                it.copy(
                    playing = player.isPlaying,
                    buffering = player.playbackState == Player.STATE_BUFFERING,
                    hidden = player.playbackState == Player.STATE_IDLE,
                    track = playingTrack,
                    nextQueue = player.getUpcomingQueue()
                )
            }
        }
    }

    // Extension functions

    // TODO: Entender o que essa função faz exatamente, gerado por IA
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
            queue.add(window.mediaItem.toPlayerTrack(index))

            index = timeline.getNextWindowIndex(
                index,
                repeatMode,
                shuffleModeEnabled
            )
        }

        return queue
    }

    // TODO: Entender o que essa função faz exatamente, gerado por IA
    @OptIn(UnstableApi::class)
    fun Player.getUpcomingQueue(): List<PlayerTrack> {
        val timeline = currentTimeline
        if (timeline.isEmpty) return emptyList()

        val window = Timeline.Window()
        val queue = mutableListOf<PlayerTrack>()

        val windowCount = timeline.windowCount

        for (index in currentMediaItemIndex until windowCount) {
            timeline.getWindow(index, window)
            queue.add(
                window.mediaItem.toPlayerTrack(queueIndex = index)
            )
        }

        return queue
    }

    // TODO: Entender o que essa função faz exatamente, gerado por IA
    fun Player.findTimelineIndexByMediaId(mediaId: String): Pair<Int, MediaItem?> {
        val timeline = currentTimeline
        val window = Timeline.Window()

        for (i in 0 until timeline.windowCount) {
            timeline.getWindow(i, window)
            if (window.mediaItem.mediaId == mediaId) {
                return Pair(i, window.mediaItem)
            }
        }
        return Pair(-1, null)
    }

    fun MediaItem.toPlayerTrack(queueIndex: Int = 0): PlayerTrack {
        val trackId = mediaId.toLong()
        val name = mediaMetadata.title?.toString() ?: "Sem título"
        val artistName = mediaMetadata.artist?.toString() ?: "Artista desconhecido"
        val coverUri = mediaMetadata.artworkUri
        val duration = mediaMetadata.durationMs ?: 0L
        val isCustomQueued =
            mediaMetadata.extras?.getBoolean(mediaItemExtrasKeys.isCustomQueue) ?: false

        return PlayerTrack(
            trackId,
            name,
            artistName,
            coverUri,
            isCustomQueued,
            queueIndex,
            duration,
        )
    }

    fun MediaItem.withCustomQueueFlag(flag: Boolean): MediaItem {
        val oldExtras = mediaMetadata.extras ?: Bundle()

        val newExtras = Bundle(oldExtras).apply {
            putBoolean(mediaItemExtrasKeys.isCustomQueue, flag)
        }

        return buildUpon()
            .setMediaMetadata(
                mediaMetadata.buildUpon()
                    .setExtras(newExtras)
                    .build()
            )
            .build()
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