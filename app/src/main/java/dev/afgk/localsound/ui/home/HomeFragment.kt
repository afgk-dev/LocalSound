package dev.afgk.localsound.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.databinding.FragmentHomeBinding
import dev.afgk.localsound.ui.Ability
import dev.afgk.localsound.ui.HomeViewModel
import dev.afgk.localsound.ui.PermissionsUiState
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.navigation.NavigationRoutes
import dev.afgk.localsound.ui.playlists.PlaylistQuickActionsBottomSheetModal
import dev.afgk.localsound.ui.tracks.TracksListAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val _TAG = "HomeFragment"

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var viewModel: HomeViewModel

    private val playlistQuickActions = PlaylistQuickActionsBottomSheetModal(1L)
    private val tracksListAdapter = TracksListAdapter(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        if (!PermissionsUiState.can(
                Ability.READ_AUDIO,
                requireContext()
            )
        ) return navController.navigate(
            NavigationRoutes.onboarding._route
        )

        binding.navigateToCreatePlaylist.setOnClickListener { _ ->
            navController.navigate(
                "${NavigationRoutes.createPlaylist}/${1L}"
            )
        }

        binding.navigateToPlaylist.setOnClickListener { _ ->
            navController.navigate("${NavigationRoutes.playlist}/${1L}")
        }

        binding.openBottomSheetModal.setOnClickListener { _ ->
            playlistQuickActions.show(
                requireActivity().supportFragmentManager,
                _TAG
            )
        }

        binding.navigateToPlaylist.setOnClickListener { _ ->
            navController.navigate("${NavigationRoutes.playlist}/${1}")
        }

        viewModel = ViewModelProvider.create(
            this,
            viewModelFactory {
                HomeViewModel(MyApplication.appModule.tracksRepository)
            }
        )[HomeViewModel::class]

        binding.tracksList.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksList.adapter = tracksListAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tracksState.collect { tracks ->
                    if (tracks.isEmpty()) {
                        binding.textNoMusics.visibility = View.VISIBLE
                        binding.tracksListGroup.visibility = View.GONE
                    } else {
                        binding.textNoMusics.visibility = View.GONE
                        binding.tracksListGroup.visibility = View.VISIBLE
                    }

                    tracksListAdapter.updateData(tracks)
                }
            }
        }
    }
}