package dev.afgk.localsound.ui.player

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.databinding.QueueTrackItemBinding
import dev.afgk.localsound.ui.PlayerTrack

class QueueListAdapter(
    var tracks: List<PlayerTrack>,
    var onAddToCustomQueue: (PlayerTrack) -> Unit
) : RecyclerView.Adapter<QueueListAdapter.ViewHolder>() {
    private val _TAG = "QueueListAdapter"

    class ViewHolder(binding: QueueTrackItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val trackName = binding.trackName
        val trackArtistName = binding.trackArtistName
        val addToCustomQueueBtn = binding.addToCustomQueueBtn
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = QueueTrackItemBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.trackName.text = tracks[position].name
        viewHolder.trackArtistName.text = tracks[position].artistName

        viewHolder.addToCustomQueueBtn.setOnClickListener {
            Log.d(_TAG, "Add to custom queue clicked!")
            onAddToCustomQueue(tracks[position])
        }
    }

    override fun getItemCount() = tracks.size

    fun updateData(newTracks: List<PlayerTrack>) {
        val diffResult = DiffUtil.calculateDiff(
            object : DiffUtil.Callback() {
                override fun getNewListSize() = newTracks.size
                override fun getOldListSize() = tracks.size

                override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                    return tracks[oldPos].id == newTracks[newPos].id
                }

                override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                    return tracks[oldPos] == newTracks[newPos]
                }
            }
        )

        tracks = newTracks
        diffResult.dispatchUpdatesTo(this)
    }
}