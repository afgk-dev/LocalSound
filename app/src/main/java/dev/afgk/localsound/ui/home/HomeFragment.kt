package dev.afgk.localsound.ui.home

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
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
import dev.afgk.localsound.data.tracks.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var viewModel: HomeViewModel

    private val tracksListAdapter = TracksListAdapter(emptyList()) { track ->
        android.widget.Toast.makeText(
            requireContext(),
            "Tocando: ${track.track.name}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private enum class SearchMode { TRACKS, ARTISTS, PLAYLISTS }
    private var currentSearchMode = SearchMode.TRACKS
    private var searchJob: kotlinx.coroutines.Job? = null

    data class MetadataInfo(val nomeMusica: String, val nomeArtista: String)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        if (!PermissionsUiState.can(Ability.READ_AUDIO, requireContext()))
            return navController.navigate(NavigationRoutes.onboarding._route)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dbHelper = MyApplication.appModule.database.openHelper
                val db = dbHelper.writableDatabase
                val audioRepo = MyApplication.appModule.audioFilesRepository

                val arquivosEncontrados = audioRepo.loadFiles()

                if (arquivosEncontrados.isNotEmpty()) {
                    db.execSQL("DELETE FROM tracks")
                    db.execSQL("DELETE FROM artists")

                    arquivosEncontrados.forEach { arquivo ->
                        try {
                            val metadata = buscarMetadados(arquivo.name, arquivo.artist)

                            var artistId: Long = -1

                            val cursorArtist = db.query("SELECT id FROM artists WHERE name = ?", arrayOf(metadata.nomeArtista))

                            if (cursorArtist.moveToFirst()) {
                                artistId = cursorArtist.getLong(0)
                            }
                            cursorArtist.close()

                            if (artistId == -1L) {
                                val cvArtist = ContentValues().apply {
                                    put("name", metadata.nomeArtista)
                                    put("pictureUri", null as String?)
                                    put("createdAt", Date().time)
                                }
                                artistId = db.insert("artists", SQLiteDatabase.CONFLICT_IGNORE, cvArtist)
                            }

                            val cvTrack = ContentValues().apply {
                                put("name", metadata.nomeMusica)
                                put("duration", ((arquivo.duration?.toInt() ?: 0) / 1000))
                                put("uri", arquivo.path)
                                put("artistId", artistId)
                                put("releaseId", null as Long?)
                                put("createdAt", Date().time)
                            }
                            db.insert("tracks", SQLiteDatabase.CONFLICT_IGNORE, cvTrack)

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModel = ViewModelProvider.create(
            this, viewModelFactory { HomeViewModel(MyApplication.appModule.tracksRepository) }
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

    // ========================================================================
    // FUNÇÃO PREPARADA PARA A API DO MUSICBRAINZ
    // ========================================================================
    private suspend fun buscarMetadados(nomeArquivo: String, artistaTag: String?): MetadataInfo {
        var artistaFinal = artistaTag
        var musicaFinal = nomeArquivo

        if (artistaFinal.isNullOrBlank() || artistaFinal == "<unknown>") {
            if (nomeArquivo.contains("-")) {
                val partes = nomeArquivo.split("-")
                artistaFinal = partes[0].trim()
                musicaFinal = partes.getOrElse(1) { nomeArquivo }.replace(".mp3", "", true).trim()
            } else {
                artistaFinal = "Artista Desconhecido"
            }
        }
        return MetadataInfo(musicaFinal, artistaFinal!!)
    }

    private fun setupSearch() {
        val searchAdapter = TracksListAdapter(emptyList()) { item ->
            binding.searchView.hide()
            val tipo = when (currentSearchMode) {
                SearchMode.PLAYLISTS -> "Playlist"
                SearchMode.ARTISTS -> "Artista"
                else -> "Música"
            }
            android.widget.Toast.makeText(requireContext(), "Selecionado $tipo: ${item.track.name}", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.recyclerSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
        binding.searchView.setupWithSearchBar(binding.searchBar)

        binding.chipGroupSearch.setOnCheckedStateChangeListener { group, checkedIds ->
            currentSearchMode = when (checkedIds.firstOrNull()) {
                dev.afgk.localsound.R.id.chip_artists -> SearchMode.ARTISTS
                dev.afgk.localsound.R.id.chip_playlists -> SearchMode.PLAYLISTS
                else -> SearchMode.TRACKS
            }
            performSearch(binding.searchView.text.toString(), searchAdapter)
        }
        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { performSearch(s.toString(), searchAdapter) }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String, adapter: TracksListAdapter) {
        searchJob?.cancel()
        searchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val listaResultado = mutableListOf<dev.afgk.localsound.data.tracks.TrackAndArtist>()
            if (query.isNotBlank()) {
                try {
                    val db = MyApplication.appModule.database.openHelper.readableDatabase
                    val buscaLike = "%$query%"

                    val (sql, args) = when (currentSearchMode) {
                        SearchMode.TRACKS -> {
                            val sqlTracks = "SELECT * FROM tracks WHERE name LIKE ? OR artistId IN (SELECT id FROM artists WHERE name LIKE ?)"
                            Pair(sqlTracks, arrayOf(buscaLike, buscaLike))
                        }
                        SearchMode.ARTISTS -> {
                            Pair("SELECT * FROM artists WHERE name LIKE ?", arrayOf(buscaLike))
                        }
                        SearchMode.PLAYLISTS -> {
                            Pair("SELECT * FROM playlists WHERE name LIKE ?", arrayOf(buscaLike))
                        }
                    }
                    val cursor = db.query(sql, args)
                    cursor.use { c ->
                        val nameIdx = c.getColumnIndex("name")
                        val durationIdx = c.getColumnIndex("duration")
                        val uriIdx = c.getColumnIndex("uri")
                        while (c.moveToNext()) {
                            if (nameIdx != -1) {
                                val nomeEncontrado = c.getString(nameIdx)
                                val duracao = if (durationIdx != -1) c.getInt(durationIdx) else 0
                                val uri = if (uriIdx != -1) c.getString(uriIdx) else ""
                                val trackFake = dev.afgk.localsound.data.tracks.TrackEntity(
                                    name = nomeEncontrado,
                                    duration = duracao,
                                    uri = uri,
                                    artistId = null,
                                    createdAt = Date()
                                )
                                val artistaFake = if (currentSearchMode != SearchMode.TRACKS) {
                                    dev.afgk.localsound.data.artists.ArtistEntity(
                                        id = 0,
                                        name = if (currentSearchMode == SearchMode.ARTISTS) "Artista" else "Playlist",
                                        pictureUri = null,
                                        createdAt = Date()
                                    )
                                } else { null }
                                listaResultado.add(dev.afgk.localsound.data.tracks.TrackAndArtist(trackFake, artistaFake))
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            withContext(Dispatchers.Main) { adapter.updateData(listaResultado) }
        }
    }
}