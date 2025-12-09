package dev.afgk.localsound.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
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

    private lateinit var viewModel: PlaylistQuickActionsViewModel
    private lateinit var navController: NavController
    private lateinit var playlistListAdapter: PlaylistListAdapter

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

        viewModel = ViewModelProvider.create(
            this,
            viewModelFactory {
                PlaylistQuickActionsViewModel(
                    trackId,
                    MyApplication.appModule.playlistRepository
                )
            }
        )[PlaylistQuickActionsViewModel::class]

        playlistListAdapter = PlaylistListAdapter(
            playlists = emptyList(),
            onAddClick = fun(item) {
                viewModel.addToPlaylist(item.id)
            },
            onRemoveClick = fun(item) {
                viewModel.removeFromPlaylist(item.id)
            }
        )

        val playlistsList = binding.playlistsList

        playlistsList.layoutManager = LinearLayoutManager(requireContext())
        playlistsList.adapter = playlistListAdapter

        binding.createNewPlaylist.setOnClickListener { _ ->
            dismiss()
            navController.navigate("${NavigationRoutes.createPlaylist}/${trackId}")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PlaylistQuickActionsUiState.Success -> playlistListAdapter.updateData(
                            state.playlists
                        )

                        is PlaylistQuickActionsUiState.Loading -> {}
                    }
                }
            }
        }
    }
}