package dev.afgk.localsound.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.audioFiles.AudioFile
import dev.afgk.localsound.data.audioFiles.AudioFilesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TracksListState(
    val tracks: List<AudioFile>? = null
)

class TracksListViewModel(
    private val audioFilesRepository: AudioFilesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TracksListState())
    val uiState: StateFlow<TracksListState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            val audioFiles = audioFilesRepository.loadFiles()

            _uiState.value = _uiState.value.copy(tracks = audioFiles)
        }
    }
}