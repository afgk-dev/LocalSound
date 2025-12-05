package dev.afgk.localsound.ui.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.queue.QueueRepository
import dev.afgk.localsound.data.queue.QueueTrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QueueViewModel(
    private val queueRepository: QueueRepository
): ViewModel(){

    val personalizedQueue = queueRepository.getQueue()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun removeFromQueue(queueItem: QueueTrackEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            queueRepository.removeFromQueue(queueItem)
        }
    }
}