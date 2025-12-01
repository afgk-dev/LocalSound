package dev.afgk.localsound.ui.tracks

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.R
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.databinding.TrackListItemBinding
import dev.afgk.localsound.ui.helpers.StringFormatter

class TracksListAdapter(
    var tracks: List<TrackAndArtist>,
    private val onAddToQueueSelected: (TrackAndArtist) -> Unit
) : RecyclerView.Adapter<TracksListAdapter.ViewHolder>() {

    class ViewHolder(binding: TrackListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val trackName = binding.trackName
        val trackArtistName = binding.trackArtistName
        val trackDuration = binding.trackDuration
        val button = binding.button
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = TrackListItemBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.trackName.text = tracks[position].track.name
        viewHolder.trackArtistName.text = tracks[position].artist?.name ?: "Artista desconhecido"
        viewHolder.trackDuration.text =
            StringFormatter.fromSecondsToMinutesAndSeconds(tracks[position].track.duration)
        viewHolder.button.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view, Gravity.END, 0, R.style.PopupMenuOverlap)
            popup.menuInflater.inflate(R.menu.track_item_menu, popup.menu)
            popup.setForceShowIcon(true)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.add_to_queue -> {
                        android.util.Log.d("TracksListAdapter", "Tentando adicionar ID: ${tracks[position].track.id} na queue")
                        onAddToQueueSelected(tracks[position])
                        true
                    }
                    R.id.add_to_playlist -> {
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = tracks.size

    fun updateData(newTracks: List<TrackAndArtist>) {
        val diffResult = DiffUtil.calculateDiff(
            object : DiffUtil.Callback() {
                override fun getNewListSize() = newTracks.size
                override fun getOldListSize() = tracks.size

                override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                    return tracks[oldPos].track.id == newTracks[newPos].track.id
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