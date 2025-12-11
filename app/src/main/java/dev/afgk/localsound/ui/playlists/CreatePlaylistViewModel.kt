package dev.afgk.localsound.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.playlists.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreatePlaylistState(
    val playlistName: String = "",

    val isPlaylistCreated: Boolean = false,
    val playlistNameInputError: String? = null
)

class CreatePlaylistViewModel(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val _TAG = "CreatePlaylistViewModel"

    private val _uiState = MutableStateFlow(CreatePlaylistState())
    val uiState = _uiState.asStateFlow()

    fun setupListeners(
        setup: (state: MutableStateFlow<CreatePlaylistState>) -> Unit
    ) {
        setup(_uiState)
    }

    fun validate(): Boolean {
        if (_uiState.value.playlistName.isEmpty()) {
            _uiState.update { it.copy(playlistNameInputError = "Obrigatório") }
            return false
        } else {
            _uiState.update { it.copy(playlistNameInputError = null) }
            return true
        }
    }

    fun loadPlaylistDefaultName() {
        viewModelScope.launch {
            val total = playlistRepository.getTotal()
            val newPlaylistNumber = total + 1

            _uiState.update { it.copy(playlistName = "Minha playlist #$newPlaylistNumber") }
        }
    }

    fun create() {
        if (!validate()) return

        viewModelScope.launch {
            playlistRepository.create(_uiState.value.playlistName)
            _uiState.update { it.copy(isPlaylistCreated = true) }
        }
    }
}