package dev.afgk.localsound.ui.playlists

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.playlists.PlaylistRepository
import dev.afgk.localsound.data.playlists.PlaylistTrackWithDetails
import dev.afgk.localsound.ui.helpers.StringFormatter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Date

sealed class PlaylistViewModelUiState() {
    data object Loading : PlaylistViewModelUiState()

    data class Success(
        val name: String,
        val totalTracksCount: Int,
        val totalDuration: Int,
        val stats: String,
        val coverUri: Uri?,
        val tracks: List<PlaylistTrackWithDetails>,
        val searchedTracks: List<PlaylistTrackWithDetails>,
        val sorting: PlaylistSorting,
        val updatedAt: Date? = null,
        val shuffle: Boolean = false
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
    private val search = MutableStateFlow("")
    private val shuffle = MutableStateFlow(false)

    @OptIn(FlowPreview::class)
    private val playlistFlow =
        combine(
            playlistRepository.getPlaylistTracks(playlistId),
            sortingDir,
            search.debounce(300L),
            shuffle
        ) { playlistResult, sorting, query, shuffle ->
            if (playlistResult == null) return@combine PlaylistViewModelUiState.PlaylistNotFound

            val (playlist, tracks) = playlistResult

            val sortedTracks = when (sorting) {
                PlaylistSorting.RECENT -> tracks.sortedByDescending { it.connection.createdAt }
                PlaylistSorting.OLDER -> tracks.sortedBy { it.connection.createdAt }
            }

            val searchedTracks = when (query) {
                "" -> listOf()
                else -> sortedTracks.filter { (_, track) ->
                    track.track.name.contains(query, true) || track.artist?.name?.contains(
                        query,
                        true
                    ) ?: false
                }
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
                coverUri = playlist.coverUri,
                tracks = sortedTracks,
                searchedTracks = searchedTracks,
                sorting = sorting,
                updatedAt = playlist.updatedAt,
                shuffle = shuffle
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

    fun toggleShuffle() {
        shuffle.update { !it }
    }

    fun search(query: String) {
        search.value = query
    }
}