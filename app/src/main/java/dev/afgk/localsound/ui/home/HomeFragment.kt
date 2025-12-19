package dev.afgk.localsound.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.databinding.FragmentHomeBinding
import dev.afgk.localsound.ui.Ability
import dev.afgk.localsound.ui.HomeViewModel
import dev.afgk.localsound.ui.PermissionsUiState
import dev.afgk.localsound.ui.PlayerViewModel
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.navigation.NavigationRoutes
import dev.afgk.localsound.ui.playlists.PlaylistCardItemAdapter
import dev.afgk.localsound.ui.tracks.TracksListAdapter
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.util.query

class HomeFragment : Fragment() {
    private val _TAG = "HomeFragment"

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var viewModel: HomeViewModel

    private val playerViewModel: PlayerViewModel by activityViewModels {
        viewModelFactory {
            PlayerViewModel(MyApplication.appModule.tracksRepository)
        }
    }

    private val tracksListAdapter = TracksListAdapter(emptyList()) {
        playerViewModel.playTrack(it)
    }
    private val playlistCardAdapter = PlaylistCardItemAdapter(emptyList())

    private val searchAdapter = TracksListAdapter(emptyList()) { track ->
        playerViewModel.playTrack(track.track)
        binding.searchView.hide()
    }

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

        if (!PermissionsUiState.can(Ability.READ_AUDIO, requireContext())) {
            return navController.navigate(NavigationRoutes.onboarding._route)
        }

        binding.navigateToCreatePlaylist.setOnClickListener { _ ->
            navController.navigate(
                "${NavigationRoutes.createPlaylist}/${1L}"
            )
        }

        viewModel = ViewModelProvider.create(
            this,
            viewModelFactory {
                HomeViewModel(
                    MyApplication.appModule.tracksRepository,
                    MyApplication.appModule.playlistRepository)
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

        binding.playlistsCarousel.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.playlistsCarousel.adapter = playlistCardAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.playlistsState.collect{ playlists ->
                    if (playlists.isEmpty()) {
                        binding.navigateToCreatePlaylist.visibility = View.VISIBLE
                        binding.playlistsCarousel.visibility = View.GONE
                        binding.playlistListTitle.text = "Não há playlists"
                    } else {
                        binding.navigateToCreatePlaylist.visibility = View.GONE
                        binding.playlistsCarousel.visibility = View.VISIBLE
                        binding.playlistListTitle.text = "Minhas Playlists"
                    }
                    playlistCardAdapter.updateData(playlists)
                }
            }
        }

        //Open the FragmentPlaylist of the selected playlist
        playlistCardAdapter.onItemClick = {playlistItem ->
            navController.navigate("${NavigationRoutes.playlist}/${playlistItem.playlist.id}")
        }

        setupRecyclers()
        setupSearchListeners()
        observeViewModel()
    }

    private fun setupRecyclers() {
        binding.tracksList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksListAdapter
        }

        binding.tracksSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
    }

    private fun setupSearchListeners() {
        binding.searchView.editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateSearchQuery(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.searchResults.collect { filteredTracks ->
                        searchAdapter.updateData(filteredTracks)

                        if(filteredTracks.isNotEmpty()){
                            binding.tracksSearchResults.visibility = View.VISIBLE
                            binding.textNoResults.visibility = View.GONE
                        } else {
                            binding.tracksSearchResults.visibility = View.GONE
                            binding.textNoResults.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}