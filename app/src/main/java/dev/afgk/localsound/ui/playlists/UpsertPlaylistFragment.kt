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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.databinding.FragmentUpsertPlaylistBinding
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.navigation.NavigationRoutes
import kotlinx.coroutines.launch
import java.util.Date

class UpsertPlaylistFragment : Fragment() {
    private val _TAG = "CreatePlaylistFragment"

    private var _binding: FragmentUpsertPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: UpsertPlaylistViewModel
    private lateinit var navController: NavController

    private var coverCacheSignature = Date().time.toString()

    private lateinit var confirmDeletionModal: MaterialAlertDialogBuilder

    private var firstTrackId: Long? = null
    private var playlistId: Long? = null

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

        firstTrackId = arguments?.getLong("trackId").let { if (it == -1L) null else it }
        playlistId = arguments?.getLong("playlistId").let { if (it == -1L) null else it }

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

        confirmDeletionModal = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tem certeza?")
            .setMessage("Ao continuar, você irá remover essa playlist e todas as músicas que estão nela.")
            .setNegativeButton("Cancelar") { dialog, which -> dialog.dismiss() }
            .setPositiveButton("Continuar") { dialog, which ->
                if (playlistId == null) return@setPositiveButton

                viewModel.delete(playlistId!!)

                dialog.dismiss()
            }

        binding.deleteButton.setOnClickListener {
            confirmDeletionModal.show()
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
        binding.deleteButton.visibility = View.GONE

        if (state.createdId != null) {
            Snackbar.make(
                requireView(),
                "Playlist criada com sucesso",
                Snackbar.LENGTH_SHORT
            ).show()

            navController.navigate("${NavigationRoutes.playlist}/${state.createdId}") {
                popUpTo(
                    if (firstTrackId != null) "${NavigationRoutes.createPlaylist}/{trackId}"
                    else "${NavigationRoutes.updatePlaylist}/{playlistId}"
                ) { inclusive = true }
            }
        }

        binding.cover.setCoverUri(state.coverUri)

        upsert(state.name, state.errors)
    }

    fun update(state: UpsertPlaylistUiState.Update) {
        binding.title.text = "Atualize sua playlist!"
        binding.deleteButton.visibility = View.VISIBLE

        if (state.updated) {
            Snackbar.make(
                requireView(),
                "Playlist atualizada com sucesso",
                Snackbar.LENGTH_SHORT
            ).show()

            navController.popBackStack()
        }

        if (state.deleted) {
            Snackbar.make(
                requireView(),
                "Playlist removida com sucesso",
                Snackbar.LENGTH_SHORT
            ).show()

            navController.navigate(NavigationRoutes.home)
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