package dev.afgk.localsound.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.databinding.PlaylistQuickActionsSheetBinding
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.navigation.NavigationRoutes
import kotlinx.coroutines.launch

class PlaylistQuickActionsBottomSheetModal(
    private val trackId: Long
) : BottomSheetDialogFragment() {
    private var _binding: PlaylistQuickActionsSheetBinding? = null
    private val binding get() = _binding!!

    private val playlistRepository = MyApplication.appModule.playlistRepository
    private val playlistQuickActionsViewModel: PlaylistQuickActionsViewModel by viewModels {
        viewModelFactory { PlaylistQuickActionsViewModel(trackId, playlistRepository) }
    }
    private val playlistListAdapter = PlaylistListAdapter(
        playlists = emptyList(),
        onAddClick = fun(item) {
            playlistQuickActionsViewModel.addToPlaylist(item.id)
        },
        onRemoveClick = fun(item) {
            playlistQuickActionsViewModel.removeFromPlaylist(item.id)
        }
    )

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlaylistQuickActionsSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        setupRecycleViews()
        setupListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                playlistQuickActionsViewModel.uiState.collect { state ->
                    when (state) {
                        is PlaylistQuickActionsUiState.Success -> success(state)
                        is PlaylistQuickActionsUiState.Loading -> loading()
                    }
                }
            }
        }
    }

    private fun success(uiState: PlaylistQuickActionsUiState.Success) {
        binding.loading.visibility = View.GONE

        if (uiState.playlists.isNotEmpty()) {
            binding.hasPlaylists.visibility = View.VISIBLE
            binding.noPlaylists.visibility = View.GONE
        } else {
            binding.hasPlaylists.visibility = View.GONE
            binding.noPlaylists.visibility = View.VISIBLE
        }

        playlistListAdapter.updateData(uiState.playlists)
    }

    private fun loading() {
        binding.loading.visibility = View.VISIBLE
        binding.noPlaylists.visibility = View.GONE
        binding.hasPlaylists.visibility = View.GONE
    }

    private fun setupRecycleViews() {
        binding.playlistsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistListAdapter
        }
    }

    private fun setupListeners() {
        with(binding) {
            createNewPlaylist.setOnClickListener { _ ->
                dismiss()
                navController.navigate("${NavigationRoutes.createPlaylist}/${trackId}")
            }

            createNewPlaylist2.setOnClickListener { _ ->
                dismiss()
                navController.navigate("${NavigationRoutes.createPlaylist}/${trackId}")
            }
        }
    }
}