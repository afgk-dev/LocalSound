package dev.afgk.localsound.ui.playlists

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.playlists.PlaylistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlaylistListItem(
    val id: Long,
    val name: String,
    val coverUri: Uri?,
    val totalTracks: Int,
    val isTrackAdded: Boolean
)

sealed class PlaylistQuickActionsUiState {
    data object Loading : PlaylistQuickActionsUiState()
    data class Success(
        val playlists: List<PlaylistListItem>
    ) : PlaylistQuickActionsUiState()
}

class PlaylistQuickActionsViewModel(
    private val trackId: Long,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val playlistsFlow = playlistRepository.getPlaylists().map { playlists ->
        PlaylistQuickActionsUiState.Success(
            playlists = playlists.map { (playlist, tracks) ->
                PlaylistListItem(
                    id = playlist.id,
                    name = playlist.name,
                    coverUri = playlist.coverUri,
                    totalTracks = tracks.size,
                    isTrackAdded = tracks.any { it.id == trackId }
                )
            }
        )
    }

    val uiState = playlistsFlow.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        PlaylistQuickActionsUiState.Loading
    )

    fun addToPlaylist(
        playlistId: Long
    ) {
        viewModelScope.launch {
            playlistRepository.addToPlaylist(playlistId, trackId)
        }
    }

    fun removeFromPlaylist(
        playlistId: Long
    ) {
        viewModelScope.launch {
            playlistRepository.removeFromPlaylist(playlistId, trackId)
        }
    }
}