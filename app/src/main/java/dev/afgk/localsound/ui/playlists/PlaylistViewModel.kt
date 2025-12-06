package dev.afgk.localsound.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.playlists.PlaylistRepository
import dev.afgk.localsound.data.playlists.PlaylistTrackWithDetails
import dev.afgk.localsound.ui.helpers.StringFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed class PlaylistViewModelUiState() {
    data object Loading : PlaylistViewModelUiState()

    data class Success(
        val name: String,
        val totalTracksCount: Int,
        val totalDuration: Int,
        val stats: String,
        val tracks: List<PlaylistTrackWithDetails>,
        val sorting: PlaylistSorting
    ) : PlaylistViewModelUiState()

    data object PlaylistNotFound : PlaylistViewModelUiState()
}

enum class PlaylistSorting {
    RECENT,
    OLDER
}

class PlaylistViewModel(
    private val playlistId: Long,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val sortingDir = MutableStateFlow(PlaylistSorting.RECENT)

    private val playlistFlow =
        combine(
            playlistRepository.getPlaylistTracks(playlistId),
            sortingDir
        ) { playlistResult, sorting ->
            if (playlistResult == null) return@combine PlaylistViewModelUiState.PlaylistNotFound

            val (playlist, tracks) = playlistResult

            val sortedTracks = when (sorting) {
                PlaylistSorting.RECENT -> tracks.sortedByDescending { it.connection.createdAt }
                PlaylistSorting.OLDER -> tracks.sortedBy { it.connection.createdAt }
            }

            val totalTracksCount = tracks.size
            val totalDuration = tracks.sumOf { it.track.track.duration }
            val stats = "${totalTracksCount} música${if (totalTracksCount > 1) "s" else ""}, ${
                StringFormatter.fromSecondsToHoursAndMinutes(totalDuration)
            }"

            PlaylistViewModelUiState.Success(
                name = playlist.name,
                totalTracksCount = totalTracksCount,
                totalDuration = totalDuration,
                stats = stats,
                tracks = sortedTracks,
                sorting = sorting
            )
        }

    val playlistState = playlistFlow.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        PlaylistViewModelUiState.Loading
    )

    fun toggleSorting() {
        sortingDir.value = if (sortingDir.value == PlaylistSorting.RECENT) {
            PlaylistSorting.OLDER
        } else {
            PlaylistSorting.RECENT
        }
    }
}