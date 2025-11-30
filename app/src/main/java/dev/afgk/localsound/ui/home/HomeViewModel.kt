package dev.afgk.localsound.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.data.tracksfun.TracksRepository
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

    fun filterTracks(query: String): List<TrackAndArtist> {
        val currentList = tracksState.value

        if (query.isBlank()) {
            return currentList
        }

        return currentList.filter { item ->
            val matchName = item.track.name.contains(query, ignoreCase = true)

            val matchArtist = item.artist?.name?.contains(query, ignoreCase = true) == true

            matchName || matchArtist
        }
    }

}