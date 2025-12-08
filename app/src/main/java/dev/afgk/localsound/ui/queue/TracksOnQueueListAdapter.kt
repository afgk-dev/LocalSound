package dev.afgk.localsound.ui.queue

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.data.queue.QueueWithTrackAndArtist
import dev.afgk.localsound.databinding.TrackListItemBinding

class TracksOnQueueListAdapter(
    private  val isSuggestionTracks: Boolean = false,
    private val onRemoveClick: (QueueWithTrackAndArtist) -> Unit,
    private val onAddClick: (QueueWithTrackAndArtist) -> Unit
) : RecyclerView.Adapter<TracksOnQueueListAdapter.ViewHolder>() {

    private var queueItems: List<QueueWithTrackAndArtist> = emptyList()
    class ViewHolder(val binding: TrackListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TrackListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = queueItems[position]

        holder.binding.trackName.text = currentItem.trackAndArtist.track.name
        holder.binding.trackArtistName.text = currentItem.trackAndArtist.artist?.name

        holder.binding.trackDuration.visibility = View.GONE
        holder.binding.button.visibility = View.GONE

        if(isSuggestionTracks) {
            holder.binding.addToQueue.visibility = View.VISIBLE

            holder.binding.addToQueue.setOnClickListener {
                onAddClick(currentItem)
            }
        } else{
            holder.binding.removeFromQueue.visibility = View.VISIBLE
            holder.binding.addToPlaylist.visibility = View.VISIBLE

            holder.binding.removeFromQueue.setOnClickListener {
                onRemoveClick(currentItem)
            }
        }
    }

    override fun getItemCount() = queueItems.size

    fun updateData(newQueueItems: List<QueueWithTrackAndArtist>) {
        val diffCallback = QueueDiffCallback(this.queueItems, newQueueItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.queueItems = newQueueItems
        diffResult.dispatchUpdatesTo(this)
    }
}

class QueueDiffCallback(
    private val oldList: List<QueueWithTrackAndArtist>,
    private val newList: List<QueueWithTrackAndArtist>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].queueTrackEntity.id == newList[newItemPosition].queueTrackEntity.id
    }
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
