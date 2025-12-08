package dev.afgk.localsound.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.FragmentPlaylistBinding
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.tracks.TracksListAdapter
import kotlinx.coroutines.launch

class PlaylistFragment : Fragment() {
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var viewModel: PlaylistViewModel
    private val tracksListAdapter = TracksListAdapter(emptyList())
    private val tracksSearchResultsAdapter = TracksListAdapter(emptyList())

    private val _TAG = "PlaylistFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        val playlistId = arguments?.getLong("playlistId")

        if (playlistId == null) {
            navController.popBackStack()
            return
        }

        viewModel = ViewModelProvider.create(
            this,
            viewModelFactory {
                PlaylistViewModel(
                    playlistId,
                    MyApplication.appModule.playlistRepository
                )
            }
        )[PlaylistViewModel::class]

        binding.playlistTracksList.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistTracksList.adapter = tracksListAdapter

        binding.playlistTracksSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistTracksSearchResults.adapter = tracksSearchResultsAdapter

        binding.sortingButton.setOnClickListener {
            viewModel.toggleSorting()
        }

        binding.searchView.editText.doOnTextChanged { text, _, _, _ ->
            viewModel.search(text.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistState.collect { playlist ->
                when (playlist) {
                    is PlaylistViewModelUiState.Success -> success(playlist)
                    is PlaylistViewModelUiState.PlaylistNotFound -> notFound()
                    is PlaylistViewModelUiState.Loading -> loading()
                }
            }
        }
    }

    fun success(playlist: PlaylistViewModelUiState.Success) {
        binding.playlistLayout.visibility = View.VISIBLE
        binding.playlistNotFoundLayout.visibility = View.GONE
        binding.playlistLoadingLayout.visibility = View.GONE

        binding.playlistName.text = playlist.name
        binding.playlistStats.text = playlist.stats

        tracksListAdapter.updateData(playlist.tracks.map { (_, track) -> track })
        tracksSearchResultsAdapter.updateData(playlist.searchedTracks.map { (_, track) -> track })

        when (playlist.searchedTracks.isEmpty()) {
            true -> {
                binding.noSearchResults.visibility = View.VISIBLE
                binding.playlistTracksSearchResults.visibility = View.GONE
            }

            false -> {
                binding.noSearchResults.visibility = View.GONE
                binding.playlistTracksSearchResults.visibility = View.VISIBLE
            }
        }

        val sortingButton = binding.sortingButton

        when (playlist.sorting) {
            PlaylistSorting.OLDER -> {
                sortingButton.text = "Mais antigas"
                (sortingButton as MaterialButton)
                    .setIconResource(R.drawable.outline_keyboard_arrow_down_24)
            }

            PlaylistSorting.RECENT -> {
                sortingButton.text = "Mais recentes"
                (sortingButton as MaterialButton)
                    .setIconResource(R.drawable.baseline_keyboard_arrow_up_24)
            }
        }
    }

    fun loading() {
        binding.playlistLayout.visibility = View.GONE
        binding.playlistNotFoundLayout.visibility = View.GONE
        binding.playlistLoadingLayout.visibility = View.VISIBLE
    }

    fun notFound() {
        binding.playlistLayout.visibility = View.GONE
        binding.playlistNotFoundLayout.visibility = View.VISIBLE
        binding.playlistLoadingLayout.visibility = View.GONE
    }
}