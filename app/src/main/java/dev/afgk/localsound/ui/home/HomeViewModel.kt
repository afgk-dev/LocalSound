package dev.afgk.localsound.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.tracks.TracksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val tracksRepository: TracksRepository
) : ViewModel() {
    private val tracksFlow = tracksRepository.getTracksWithArtist()

    val tracksState = tracksFlow.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )
}