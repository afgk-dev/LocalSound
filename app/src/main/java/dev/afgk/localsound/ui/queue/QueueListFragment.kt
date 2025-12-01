package dev.afgk.localsound.ui.queue

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.R
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.tracks.TracksListAdapter
import kotlinx.coroutines.launch

class QueueListFragment : Fragment(R.layout.fragment_list_recycler) {

    private lateinit var viewModel: QueueViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireParentFragment(), viewModelFactory {
            QueueViewModel(
                MyApplication.appModule.queueRepository,
                MyApplication.appModule.tracksRepository
            )
        })[QueueViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.isNestedScrollingEnabled = false

        val adapter = TracksListAdapter(emptyList()) { trackAndArtist ->
            val itemToRemove = viewModel.currentQueue.value.find { it.track.id == trackAndArtist.track.id }
            if (itemToRemove != null) {
                viewModel.removeFromQueue(itemToRemove.queueTrackEntity)
            }
        }
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentQueue.collect { queueItems ->
                val tracks = queueItems.map { TrackAndArtist(it.track, null) }
                adapter.updateData(tracks)
            }
        }
    }
}