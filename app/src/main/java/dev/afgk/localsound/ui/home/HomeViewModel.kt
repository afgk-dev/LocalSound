package dev.afgk.localsound.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.queue.QueueRepository
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.data.tracksfun.TracksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val tracksRepository: TracksRepository,
    private val queueRepository: QueueRepository
) : ViewModel() {
    private val tracksFlow = tracksRepository.getTracksWithArtist()

    val tracksState = tracksFlow.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    fun addToQueue(trackAndArtist: TrackAndArtist){
        val trackId = trackAndArtist.track.id

        viewModelScope.launch(Dispatchers.IO) {
            queueRepository.addToQueue(trackId)
        }
    }
}