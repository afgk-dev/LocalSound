package dev.afgk.localsound.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.playlists.PlaylistRepository
import dev.afgk.localsound.data.tracks.TracksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val tracksRepository: TracksRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val tracksFlow = tracksRepository.getTracksWithArtist()
    private val playlistsFlow = playlistRepository.getPlaylists()

    val tracksState = tracksFlow.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    val playlistsState = playlistsFlow.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )
}