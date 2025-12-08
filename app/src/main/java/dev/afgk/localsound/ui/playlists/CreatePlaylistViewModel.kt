package dev.afgk.localsound.ui.playlists

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.helpers.SaveToInternalStorageFailure
import dev.afgk.localsound.data.helpers.saveToInternalStorage
import dev.afgk.localsound.data.playlists.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreatePlaylistState(
    val playlistCoverUri: Uri? = null,
    val playlistName: String = "",

    val createdPlaylistId: Long? = null,
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

    fun create(
        context: Context? = null,
        firstTrackId: Long? = null
    ) {
        if (!validate()) return

        viewModelScope.launch {
            val createdPlaylist =
                playlistRepository.create(_uiState.value.playlistName, firstTrackId)

            val coverUri = uiState.value.playlistCoverUri


            if (
                coverUri != null
                && context != null
            ) {
                val internalPath =
                    Pair(
                        context.filesDir.path,
                        "playlist_cover_${createdPlaylist.id}"
                    )

                saveToInternalStorage(
                    internalPath,
                    coverUri,
                    context.contentResolver,
                ).onSuccess { file ->
                    playlistRepository.update(createdPlaylist.copy(coverUri = file.toUri()))
                }.onFailure { failure ->
                    when (failure) {
                        is SaveToInternalStorageFailure -> Log.e(_TAG, failure.toString())
                    }
                }
            }

            _uiState.update { it.copy(createdPlaylistId = createdPlaylist.id) }
        }
    }

    fun setCoverUri(uri: Uri?) {
        _uiState.update { it.copy(playlistCoverUri = uri) }
    }
}