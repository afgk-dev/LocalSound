package dev.afgk.localsound.ui.queue

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.R
import dev.afgk.localsound.ui.tracks.TracksListAdapter
import kotlinx.coroutines.launch

class SuggestionsToQueueFragment : Fragment(R.layout.fragment_list_recycler) {

    private lateinit var viewModel: QueueViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireParentFragment())[QueueViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = TracksListAdapter(emptyList()) { trackAndArtist ->
            viewModel.addToQueue(trackAndArtist)
        }
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.suggestions.collect { tracks ->
                adapter.updateData(tracks)
            }
        }
    }
}
