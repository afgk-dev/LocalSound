package dev.afgk.localsound.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.afgk.localsound.data.audioFiles.AudioFilesRepository
import dev.afgk.localsound.databinding.FragmentLibraryBinding
import dev.afgk.localsound.databinding.FragmentTracksListBinding
import dev.afgk.localsound.ui.Ability
import dev.afgk.localsound.ui.PermissionsUiState
import dev.afgk.localsound.ui.TracksListViewModel
import dev.afgk.localsound.ui.navigation.NavigationRoutes
import dev.afgk.localsound.ui.playlists.PlaylistCardAdapter
import dev.afgk.localsound.ui.tracks.TracksListAdapter
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    private lateinit var viewModel: TracksListViewModel

    private var tracksListAdapter = TracksListAdapter(listOf())
    private lateinit var playlistCardAdapter: PlaylistCardAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
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

        viewModel = ViewModelProvider.create(
            this,
            TracksListViewModel.Factory,
            extras = MutableCreationExtras().apply {
                set(
                    TracksListViewModel.AUDIO_FILES_REPOSITORY_KEY,
                    AudioFilesRepository(requireContext())
                )
            }
        )[TracksListViewModel::class]

        binding.tracksList.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksList.adapter = tracksListAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state.tracks) {
                        null -> viewModel.loadData()
                        else -> {
                            binding.textLoading.visibility = View.GONE
                            binding.tracksList.visibility = View.VISIBLE
                            tracksListAdapter.dataSet = state.tracks
                        }
                    }
                }
            }
        }

        setupRecyclerView()
        setupSearchBar()
        setupPlaylistsCarousel()
    }


    private fun setupRecyclerView() {
        tracksListAdapter = TracksListAdapter(emptyList())
        binding.tracksList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksListAdapter
        }
    }
    private fun setupSearchBar(){
        binding.searchBar.setOnClickListener {
            // Navigate to the search screen
            navController.navigate(NavigationRoutes.SearchFragment)
        }
            /*Abrir o navigation drawer
            binding.searchBar.setNavigationOnClickListener {
                Drawer.open(
            }*/
    }
    private fun setupPlaylistsCarousel() {
        playlistCardAdapter = PlaylistCardAdapter()

        binding.playlistsCarouselRv.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = playlistCardAdapter
        }


    }
/*The one who knows that writed this
    private fun observeViewModel() {
        // Observa o estado da UI a partir do ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Se a lista de músicas ainda não foi carregada, pede ao ViewModel para carregar
                    if (state.tracks == null) {
                        viewModel.loadData()
                    } else {
                        // Quando os dados chegam, atualiza a lista
                        tracksListAdapter.dataSet = state.tracks
                        tracksListAdapter.notifyDataSetChanged() // Notifica o adapter para redesenhar a lista
                    }
                }
            }
        }
    }
  */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
