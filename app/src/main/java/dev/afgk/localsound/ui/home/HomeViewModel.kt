package dev.afgk.localsound.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.playlists.PlaylistRepository
import dev.afgk.localsound.data.tracks.EnrichedTrack
import dev.afgk.localsound.data.tracks.TracksRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class)

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

    private val _searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<EnrichedTrack>> = _searchQuery
        .debounce(250L)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                tracksRepository.searchTracks(query)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}