package dev.afgk.localsound.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.playlists.PlaylistRepository
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.ui.helpers.StringFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

//data class PlaylistViewModelUiState(
//    val name: String,
//    val totalTracksCount: Int,
//    val totalDuration: Int,
//    val stats: String,
//    val tracks: List<TrackAndArtist>,
//)

sealed class PlaylistViewModelUiState() {
    data object Loading : PlaylistViewModelUiState()

    data class Success(
        val name: String,
        val totalTracksCount: Int,
        val totalDuration: Int,
        val stats: String,
        val tracks: List<TrackAndArtist>,
    ) : PlaylistViewModelUiState()

    data object PlaylistNotFound : PlaylistViewModelUiState()
}

class PlaylistViewModel(
    private val playlistId: Long,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val playlistFlow =
        playlistRepository.getPlaylistTracks(playlistId).map { result ->
            if (result == null) return@map PlaylistViewModelUiState.PlaylistNotFound

            val (playlist, tracks) = result

            val totalTracksCount = tracks.size
            val totalDuration = tracks.sumOf { it.track.duration }
            val stats = "${totalTracksCount} música${if (totalTracksCount > 1) "s" else ""}, ${
                StringFormatter.fromSecondsToHoursAndMinutes(totalDuration)
            }"

            PlaylistViewModelUiState.Success(
                name = playlist.name,
                totalTracksCount = totalTracksCount,
                totalDuration = totalDuration,
                stats = stats,
                tracks = tracks
            )
        }

    val playlistState = playlistFlow.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        PlaylistViewModelUiState.Loading
    )
}