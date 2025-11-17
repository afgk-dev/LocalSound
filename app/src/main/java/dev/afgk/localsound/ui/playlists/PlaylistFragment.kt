package dev.afgk.localsound.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.afgk.localsound.MyApplication
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

        val playlistTracksList = binding.playlistTracksList

        playlistTracksList.layoutManager = LinearLayoutManager(requireContext())
        playlistTracksList.adapter = tracksListAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistState.collect { playlist ->
                if (playlist == null) return@collect

                binding.playlistName.text = playlist.name
                binding.playlistStats.text =
                    "${playlist.totalTracksCount}, ${playlist.totalDuration}"

                tracksListAdapter.updateData(playlist.tracks)
            }
        }
    }
}