package dev.afgk.localsound.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.playlists.PlaylistRepository
import dev.afgk.localsound.data.tracks.TrackAndArtist
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class PlaylistViewModelUiState(
    val name: String,
    val totalTracksCount: Int,
    val totalDuration: Int,
    val tracks: List<TrackAndArtist>,
)

class PlaylistViewModel(
    private val playlistId: Long,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val playlistFlow =
        playlistRepository.getPlaylistTracks(playlistId).map { (playlist, tracks) ->
            PlaylistViewModelUiState(
                name = playlist.name,
                totalTracksCount = tracks.size,
                totalDuration = tracks.sumOf { it.track.duration },
                tracks = tracks
            )
        }

    val playlistState = playlistFlow.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        null
    )
}