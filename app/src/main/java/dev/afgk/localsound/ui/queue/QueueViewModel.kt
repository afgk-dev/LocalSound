package dev.afgk.localsound.ui.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.queue.QueueRepository
import dev.afgk.localsound.data.queue.QueueTrackEntity
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.data.tracksfun.TracksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QueueViewModel(
    private val queueRepository: QueueRepository,
    private val tracksRepository: TracksRepository
): ViewModel(){

    val currentQueue = queueRepository.getQueue()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val suggestions = tracksRepository.getTracksWithArtist()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addToQueue(track: TrackAndArtist) {
        viewModelScope.launch(Dispatchers.IO) {
            queueRepository.addToQueue(track.track.id)
        }
    }

    fun removeFromQueue(queueItem: QueueTrackEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            queueRepository.removeFromQueue(queueItem)
        }
    }
}