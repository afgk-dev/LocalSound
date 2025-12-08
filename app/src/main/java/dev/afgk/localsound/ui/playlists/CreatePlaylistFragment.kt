package dev.afgk.localsound.ui.playlists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.databinding.FragmentCreatePlaylistBinding
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.navigation.NavigationRoutes
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreatePlaylistFragment : Fragment() {
    private val _TAG = "CreatePlaylistFragment"

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CreatePlaylistViewModel
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        viewModel = ViewModelProvider.create(
            this,
            viewModelFactory {
                CreatePlaylistViewModel(MyApplication.appModule.playlistRepository)
            }
        )[CreatePlaylistViewModel::class]

        val playlistNameInputText = binding.playlistNameInput.text.toString()
        val firstTrackId = arguments?.getLong("trackId")!!

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

        Log.i(_TAG, requireContext().filesDir.path)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { value ->
                    if (value.createdPlaylistId != null) {
                        Snackbar.make(
                            view,
                            "Playlist criada com sucesso!",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        navController.navigate("${NavigationRoutes.playlist}/${value.createdPlaylistId}") {
                            popUpTo(route = "${NavigationRoutes.createPlaylist}/${firstTrackId}") {
                                inclusive = true
                            }
                        }
                    }

                    if (
                        playlistNameInputText == "" &&
                        value.playlistName != playlistNameInputText
                    ) binding.playlistNameInput.setText(value.playlistName)

                    binding.playlistNameInputLayout.error = value.playlistNameInputError
                    binding.cover.setCoverUri(value.playlistCoverUri)
                }
            }
        }

        viewModel.loadPlaylistDefaultName()

        viewModel.setupListeners { state ->
            binding.playlistNameInput.addTextChangedListener { text ->
                state.update { it.copy(playlistName = text.toString()) }
                viewModel.validate()
            }
        }

        binding.saveButton.setOnClickListener { _ ->
            viewModel.create(
                firstTrackId = firstTrackId,
                context = requireContext(),
            )
        }
    }
}