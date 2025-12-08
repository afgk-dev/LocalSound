package dev.afgk.localsound.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.data.tracks.TracksRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
@OptIn(FlowPreview::class)
class HomeViewModel(
    private val tracksRepository: TracksRepository
) : ViewModel() {
    private val tracksFlow = tracksRepository.getTracksWithArtist()

    val tracksState = tracksFlow.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )
    private val _searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<TrackAndArtist>> = _searchQuery
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