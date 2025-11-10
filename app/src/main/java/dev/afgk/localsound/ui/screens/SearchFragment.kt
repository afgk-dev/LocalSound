package dev.afgk.localsound.ui.screens

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.FragmentSearchBinding
import dev.afgk.localsound.ui.TracksListViewModel
import dev.afgk.localsound.ui.tracks.TracksListAdapter

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: TracksListViewModel by activityViewModels() // Acessa o ViewModel compartilhado
    private lateinit var tracksListAdapter: TracksListAdapter
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)
    //TODO Implement the filter logic
    //and remake the screen to be like the prototype
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}