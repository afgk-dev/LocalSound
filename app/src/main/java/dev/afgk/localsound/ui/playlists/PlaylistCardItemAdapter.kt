package dev.afgk.localsound.ui.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.data.playlists.PlaylistAndTracks
import dev.afgk.localsound.databinding.PlaylistCardItemBinding

class PlaylistCardItemAdapter(var playlists: List<PlaylistAndTracks>) :
    RecyclerView.Adapter<PlaylistCardItemAdapter.ViewHolder>() {

    class ViewHolder(binding: PlaylistCardItemBinding): RecyclerView.ViewHolder(binding.root){
        val coverArt = binding.playlistCover
        val playlistName = binding.playlistName
        val playlistTotalTracks = binding.playlistTotalTracks
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlaylistCardItemBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.coverArt.setCoverUri(playlists[position].playlist.coverUri)
        viewHolder.playlistName.text = playlists[position].playlist.name
        viewHolder.playlistTotalTracks.text = "${playlists[position].tracks.size} música${if (playlists[position].tracks.size !== 1) "s" else ""}"
    }

    override fun getItemCount() = playlists.size

    fun updateData(newPlaylists: List<PlaylistAndTracks>){
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            object: DiffUtil.Callback(){
                override fun getNewListSize() = newPlaylists.size
                override fun getOldListSize() = playlists.size

                override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                    return playlists[oldPos].playlist.id == newPlaylists[newPos].playlist.id

                }

                override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                    return playlists[oldPos] == newPlaylists[newPos]
                }
            }
        )
        playlists = newPlaylists
        diffResult.dispatchUpdatesTo(this)
    }
}