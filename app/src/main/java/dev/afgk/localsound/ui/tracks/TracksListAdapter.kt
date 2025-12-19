package dev.afgk.localsound.ui.tracks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.data.tracks.EnrichedTrack
import dev.afgk.localsound.databinding.TrackListItemBinding
import dev.afgk.localsound.ui.helpers.StringFormatter

class TracksListAdapter(
    var tracks: List<EnrichedTrack>,
    val popupMenu: TrackPopupMenu,
    var onTrackPlay: (EnrichedTrack) -> Unit
) :
    RecyclerView.Adapter<TracksListAdapter.ViewHolder>() {

    class ViewHolder(binding: TrackListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val trackName = binding.trackName
        val trackArtistName = binding.trackArtistName
        val trackDuration = binding.trackDuration
        val trackClickableArea = binding.trackClickableArea
        val popupMenuBtn = binding.popupMenuBtn
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
        val enrichedTrack = tracks[position]
        val (track, artist) = enrichedTrack

        viewHolder.trackName.text = track.name
        viewHolder.trackArtistName.text = artist?.name ?: "Artista desconhecido"
        viewHolder.trackDuration.text =
            StringFormatter.fromSecondsToMinutesAndSeconds(track.duration)

        viewHolder.trackClickableArea.setOnClickListener {
            onTrackPlay(enrichedTrack)
        }

        viewHolder.popupMenuBtn.setOnClickListener {
            popupMenu.setTrackId(track.id)
            popupMenu.show(viewHolder.popupMenuBtn)
        }
    }

    override fun getItemCount() = tracks.size

    fun updateData(newTracks: List<EnrichedTrack>) {
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