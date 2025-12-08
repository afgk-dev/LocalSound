package dev.afgk.localsound.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.databinding.FragmentHomeBinding
import dev.afgk.localsound.ui.Ability
import dev.afgk.localsound.ui.HomeViewModel
import dev.afgk.localsound.ui.PermissionsUiState
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.navigation.NavigationRoutes
import dev.afgk.localsound.ui.tracks.TracksListAdapter
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.lifecycleScope

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var viewModel: HomeViewModel

    private val mainAdapter = TracksListAdapter(emptyList()) { track ->
        onTrackClicked(track)
    }

    private val searchAdapter = TracksListAdapter(emptyList()) { track ->
        onTrackClicked(track)
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

        viewModel = ViewModelProvider.create(
            this,
            viewModelFactory {
                HomeViewModel(MyApplication.appModule.tracksRepository)
            }
        )[HomeViewModel::class]


        setupRecyclers()
        setupSearchListeners()
        observeViewModel()
    }

    private fun setupRecyclers() {
        binding.tracksList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mainAdapter
        }

        binding.searchContent.recyclerSearchResults.apply {
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
                    viewModel.tracksState.collect { tracks ->
                        if (tracks.isEmpty()) {
                            binding.textNoMusics.visibility = View.VISIBLE
                            binding.tracksListGroup.visibility = View.GONE
                        } else {
                            binding.textNoMusics.visibility = View.GONE
                            binding.tracksListGroup.visibility = View.VISIBLE
                        }

                        mainAdapter.updateData(tracks)
                    }
                }

                launch {
                    viewModel.searchResults.collect { filteredTracks ->
                        searchAdapter.updateData(filteredTracks)
                    }
                }
            }
        }
    }


    private fun onTrackClicked(track: TrackAndArtist) {
        // TODO: PLAYER
        println("Iniciando reprodução da música: ${track.track.name}")
    }
}