package dev.afgk.localsound.ui.tracks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.data.tracks.TrackAndArtist
import dev.afgk.localsound.databinding.TrackListItemBinding
import dev.afgk.localsound.ui.helpers.StringFormatter

class TracksListAdapter(var tracks: List<TrackAndArtist>) :
    RecyclerView.Adapter<TracksListAdapter.ViewHolder>() {

    class ViewHolder(binding: TrackListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val trackName = binding.trackName
        val trackArtistName = binding.trackArtistName
        val trackDuration = binding.trackDuration
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