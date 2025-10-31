package dev.afgk.localsound.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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

    companion object {
        val AUDIO_FILES_REPOSITORY_KEY = object : CreationExtras.Key<AudioFilesRepository> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val audioFilesRepository = this[AUDIO_FILES_REPOSITORY_KEY] as AudioFilesRepository

                TracksListViewModel(audioFilesRepository)
            }
        }
    }
}