package dev.afgk.localsound.ui.playlists

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.afgk.localsound.data.helpers.saveToInternalStorage
import dev.afgk.localsound.data.playlists.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

//data class UpsertPlaylistUiState(
//    val playlistCoverUri: Uri? = null,
//    val playlistName: String = "",
//
//    val upsertedPlaylistId: Long? = null,
//    val playlistNameInputError: String? = null
//)

sealed class UpsertPlaylistUiState() {
    object Loading : UpsertPlaylistUiState()

    data class Create(
        val coverUri: Uri? = null,
        val name: String? = null,

        val errors: List<String> = listOf(),
        val created: Boolean = false
    ) : UpsertPlaylistUiState()

    data class Update(
        val name: String,
        val coverUri: Uri?,
        val didCoverChange: Boolean = false,

        val errors: List<String> = listOf(),
        val updated: Boolean = false
    ) : UpsertPlaylistUiState()
}

class UpsertPlaylistViewModel(
    private val playlistId: Long? = null,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val _TAG = "UpsertPlaylistViewModel"

    private var _name = MutableStateFlow<String?>(null)
    private var _coverUri = MutableStateFlow<Uri?>(null)
    private var _upserted = MutableStateFlow(false)

    val uiState: Flow<UpsertPlaylistUiState> =
        combine(
            (flow { emit(UpsertPlaylistUiState.Loading) }) as Flow<UpsertPlaylistUiState>,
            _name,
            _coverUri,
            _upserted
        ) { _, name, coverUri, upserted ->
            val errors = validate()

            if (playlistId != null)
                return@combine playlistRepository.getMinimal(playlistId)
                    .map { playlist ->
                        val notNullCoverUri = coverUri ?: playlist.coverUri

                        UpsertPlaylistUiState.Update(
                            name = name ?: playlist.name,
                            coverUri = notNullCoverUri,
                            didCoverChange = notNullCoverUri != playlist.coverUri,
                            errors = errors,
                            updated = upserted
                        )
                    }
                    .first()

            UpsertPlaylistUiState.Create(
                name = name,
                coverUri = coverUri,
                errors = errors,
                created = upserted
            )
        }


    init {
        if (playlistId == null)
            viewModelScope.launch {
                val total = playlistRepository.getTotal()
                _name.value = "Minha playlist #${total + 1}"
            }
    }

    fun setName(name: String) {
        _name.update { name }
    }

    fun setCoverUri(uri: Uri?) {
        _coverUri.update { uri }
    }

    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (_name.value?.isEmpty() == true) errors.add("Obrigatório")

        return errors
    }

    fun create(
        firstTrackId: Long?,
        context: Context
    ) {
        viewModelScope.launch {
            val state = uiState.first() as UpsertPlaylistUiState.Create

            if (state.errors.isNotEmpty()) return@launch

            val playlist =
                playlistRepository.create(name = state.name!!, firstTrackId = firstTrackId)

            if (state.coverUri != null) {
                saveCoverFile(
                    playlistId = playlist.id,
                    coverUri = state.coverUri,
                    context = context
                ).onSuccess { file ->
                    playlistRepository.update(playlist.copy(coverUri = file.toUri()))
                }
            }

            _upserted.update { true }
        }
    }

    fun update(
        context: Context
    ) {
        if (playlistId == null) return

        viewModelScope.launch {
            val state = uiState.first() as UpsertPlaylistUiState.Update

            if (state.errors.isNotEmpty()) return@launch

            val playlist = playlistRepository.getMinimal(playlistId).first()
            var savedCoverUri: Uri? = playlist.coverUri

            if (
                state.coverUri != null &&
                state.didCoverChange
            )
                savedCoverUri = saveCoverFile(
                    playlistId = playlist.id,
                    coverUri = state.coverUri,
                    context = context
                ).getOrNull()?.toUri()

            playlistRepository.update(
                playlist.copy(
                    name = state.name,
                    coverUri = savedCoverUri,
                    updatedAt = Date()
                )
            )

            _upserted.update { true }
        }
    }

    suspend fun saveCoverFile(
        playlistId: Long,
        coverUri: Uri,
        context: Context
    ): Result<File> {
        val internalPath =
            Pair(
                context.filesDir.path,
                "playlist_cover_${playlistId}"
            )

        return saveToInternalStorage(
            internalPath,
            coverUri,
            context.contentResolver,
        )
    }
}