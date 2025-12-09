package dev.afgk.localsound.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.databinding.FragmentUpsertPlaylistBinding
import dev.afgk.localsound.ui.helpers.viewModelFactory
import kotlinx.coroutines.launch
import java.util.Date

class UpsertPlaylistFragment : Fragment() {
    private val _TAG = "CreatePlaylistFragment"

    private var _binding: FragmentUpsertPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: UpsertPlaylistViewModel
    private lateinit var navController: NavController

    private var coverCacheSignature = Date().time.toString()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpsertPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firstTrackId = arguments?.getLong("trackId").let { if (it == -1L) null else it }
        val playlistId = arguments?.getLong("playlistId").let { if (it == -1L) null else it }

        navController = findNavController()

        viewModel = ViewModelProvider.create(
            this,
            viewModelFactory {
                UpsertPlaylistViewModel(
                    playlistId = playlistId,
                    playlistRepository = MyApplication.appModule.playlistRepository
                )
            }
        )[UpsertPlaylistViewModel::class]

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) viewModel.setCoverUri(uri)
            }

        binding.cover.setOnPickerClick {
            pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }

        binding.saveButton.setOnClickListener { _ ->
            viewModel.setName(binding.playlistNameInput.text.toString())

            if (playlistId != null) viewModel.update(requireContext())
            else if (firstTrackId != null) viewModel.create(
                firstTrackId = firstTrackId,
                context = requireContext()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UpsertPlaylistUiState.Create -> create(state)
                        is UpsertPlaylistUiState.Update -> update(state)
                        UpsertPlaylistUiState.Loading -> loading()
                    }
                }
            }
        }
    }

    fun loading() {
        binding.loaded.visibility = View.GONE
        binding.loading.visibility = View.VISIBLE
    }

    fun create(state: UpsertPlaylistUiState.Create) {
        binding.title.text = "Crie sua playlist!"

        if (state.created) {
            Snackbar.make(
                view!!,
                "Playlist criada com sucesso",
                Snackbar.LENGTH_SHORT
            ).show()

            navController.popBackStack()
        }

        binding.cover.setCoverUri(state.coverUri)

        upsert(state.name, state.errors)
    }

    fun update(state: UpsertPlaylistUiState.Update) {
        binding.title.text = "Atualize sua playlist!"

        if (state.updated) {
            Snackbar.make(
                view!!,
                "Playlist atualizada com sucesso",
                Snackbar.LENGTH_SHORT
            ).show()

            navController.popBackStack()
        }

        binding.cover.setCoverUri(
            state.coverUri,
            if (!state.didCoverChange) coverCacheSignature else state.coverUri.toString()
        )

        upsert(state.name, state.errors)
    }

    fun upsert(
        name: String?,
        errors: List<String>,
    ) {
        binding.loaded.visibility = View.VISIBLE
        binding.loading.visibility = View.GONE

        val currentText = binding.playlistNameInput.text.toString()
        if (currentText != (name ?: "")) {
            binding.playlistNameInput.setText(name ?: "")
        }

        binding.playlistNameInput.setText(name ?: "")

        binding.playlistNameInputLayout.error = errors.firstOrNull()
    }
}