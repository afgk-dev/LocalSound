package dev.afgk.localsound.ui.queue

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.data.queue.QueueAndTrack
import dev.afgk.localsound.databinding.TrackOnQueueItemBinding
import dev.afgk.localsound.ui.helpers.StringFormatter

class QueueListAdapter(
    private val onRemoveClick: (QueueAndTrack) -> Unit
) : RecyclerView.Adapter<QueueListAdapter.ViewHolder>() {

    private var queueItems: List<QueueAndTrack> = emptyList()
    class ViewHolder(val binding: TrackOnQueueItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TrackOnQueueItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = queueItems[position]
        holder.binding.trackName.text = currentItem.track.name
        holder.binding.trackArtistName.text = "Artista Desconhecido"

        holder.binding.removeFromQueue.setOnClickListener {
            onRemoveClick(currentItem)
        }
    }

    override fun getItemCount() = queueItems.size

    fun updateData(newQueueItems: List<QueueAndTrack>) {
        val diffCallback = QueueDiffCallback(this.queueItems, newQueueItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.queueItems = newQueueItems
        diffResult.dispatchUpdatesTo(this)
    }
}

class QueueDiffCallback(
    private val oldList: List<QueueAndTrack>,
    private val newList: List<QueueAndTrack>
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
