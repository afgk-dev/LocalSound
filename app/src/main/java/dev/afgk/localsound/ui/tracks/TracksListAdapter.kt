package dev.afgk.localsound.ui.tracks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.data.audioFiles.AudioFile
import dev.afgk.localsound.databinding.TrackListItemBinding
import dev.afgk.localsound.ui.helpers.StringFormatter

class TracksListAdapter(var dataSet: List<AudioFile>) :
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
        viewHolder.trackName.text = dataSet[position].name
        viewHolder.trackArtistName.text = dataSet[position].artist ?: "Artista desconhecido"
        viewHolder.trackDuration.text = StringFormatter.toMinutesSeconds(dataSet[position].duration)
    }

    override fun getItemCount() = dataSet.size
}