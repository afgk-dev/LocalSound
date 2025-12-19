package dev.afgk.localsound.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import dev.afgk.localsound.data.tracks.TrackEntity
import dev.afgk.localsound.data.tracks.TracksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PlayerStatus {
    PAUSED,
    PLAYING,
    FINISHED,
    BUFFERING,
    NOT_PREPARED
}

data class PlayerUiState(
    val status: PlayerStatus = PlayerStatus.NOT_PREPARED,
    val mediaMetadata: MediaMetadata? = null,
    val media: MediaItem? = null
)

class PlayerViewModel(
    private val tracksRepository: TracksRepository
) : ViewModel() {
    private val _TAG = "PlayerViewModel"

    private var player: Player? = null

    private var availableTracks = tracksRepository.getTracksWithArtist()
    private val _state = MutableStateFlow(PlayerUiState())

    val state = _state.asStateFlow()

    fun setPlayer(newPlayer: Player) {
        newPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                var status: PlayerStatus

                if (isPlaying) status = PlayerStatus.PLAYING
                else status = when (newPlayer.playbackState) {
                    Player.STATE_BUFFERING -> PlayerStatus.BUFFERING
                    Player.STATE_ENDED -> PlayerStatus.FINISHED
                    else -> PlayerStatus.PAUSED
                }

                Log.i(_TAG, "status = $status")

                _state.update { it.copy(status = status) }
            }

            override fun onMediaItemTransition(media: MediaItem?, reason: Int) {
                if (media == null) return

                _state.update { it.copy(media = media) }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                Log.i(_TAG, "artist = ${mediaMetadata.artist}")
                Log.i(_TAG, "title = ${mediaMetadata.title}")

                if (mediaMetadata.title == null)
                    return

                _state.update { it.copy(mediaMetadata = mediaMetadata) }
            }
        })

        player = newPlayer
    }

    fun playPause() {
        if (player == null) return

        if (_state.value.status == PlayerStatus.PLAYING) player?.pause()
        else if (_state.value.status == PlayerStatus.PAUSED) player?.play()
    }

    fun playTrack(
        track: TrackEntity,
        tracksQueue: List<TrackEntity>? = null,
        shuffle: Boolean = false
    ) {
        if (player == null) return

        val mediaItemBuilder = MediaItem.Builder()

        player?.setMediaItems(listOf())

        if (_state.value.status == PlayerStatus.NOT_PREPARED)
            player?.prepare()

        fun mapNextTracks(tracks: List<TrackEntity>): List<MediaItem> {
            val index = tracks.indexOfFirst { it.id == track.id }
            if (index == -1) {
                // Se não achou na lista, coloca a música selecionada primeiro e o resto depois
                return listOf(track, *tracks.filter { it.id != track.id }.toTypedArray()).map {
                    mediaItemBuilder.setMediaId(it.id.toString()).setUri(it.uri).build()
                }
            }

            // Cria a sequência circular: do index até o fim, depois do início até o index
            val orderedTracks = tracks.subList(index, tracks.size) + tracks.subList(0, index)
            
            return orderedTracks.map {
                mediaItemBuilder.setMediaId(it.id.toString())
                    .setUri(it.uri).build()
            }
        }

        viewModelScope.launch {
            val sourceList = tracksQueue ?: availableTracks.first().map { it.track }
            player?.setMediaItems(mapNextTracks(sourceList))

            player?.shuffleModeEnabled = shuffle
            player?.play()
        }
    }

    fun next() {
        if (player == null) return
        player?.seekToNext()
    }

    fun previous() {
        if (player == null) return
        player?.seekToPrevious()
    }
}
