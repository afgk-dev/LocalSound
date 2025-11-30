package dev.afgk.localsound.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import dev.afgk.localsound.ui.tracks.TracksListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dev.afgk.localsound.data.tracks.TrackEntity
import java.util.Date

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var viewModel: HomeViewModel

    private val tracksListAdapter = TracksListAdapter(emptyList()) { track ->
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

        if (!PermissionsUiState.can(
                Ability.READ_AUDIO,
                requireContext()
            )
        ) return navController.navigate(
            NavigationRoutes.onboarding._route
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = MyApplication.appModule.database
                val audioRepo = MyApplication.appModule.audioFilesRepository
                val tracksDao = database.tracksDao()

                val arquivosEncontrados = audioRepo.loadFiles()

                if (arquivosEncontrados.isNotEmpty()) {
                    arquivosEncontrados.forEach { arquivo ->
                        try {
                            val novaMusica = TrackEntity(
                                name = arquivo.name,
                                duration = (arquivo.duration.toInt() / 1000),
                                uri = arquivo.path,
                                artistId = null,
                                createdAt = Date()
                            )
                            tracksDao.insert(novaMusica)
                        } catch (e: Exception) {
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // ========================================================================

        viewModel = ViewModelProvider.create(
            this,
            viewModelFactory {
                HomeViewModel(MyApplication.appModule.tracksRepository)
            }
        )[HomeViewModel::class]

        binding.tracksList.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksList.adapter = tracksListAdapter

        setupSearch()

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

    private fun setupSearch() {
        val searchAdapter = TracksListAdapter(emptyList()) { track ->
            binding.searchView.hide()
        }

        binding.recyclerSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }

        binding.searchView.setupWithSearchBar(binding.searchBar)

        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                val filteredList = viewModel.filterTracks(query)
                searchAdapter.updateData(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}