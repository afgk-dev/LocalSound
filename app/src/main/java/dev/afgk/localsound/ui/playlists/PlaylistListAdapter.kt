package dev.afgk.localsound.ui.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.PlaylistQuickActionsSheetListItemBinding

class PlaylistListAdapter(
    var playlists: List<PlaylistListItem>,
    val onAddClick: (item: PlaylistListItem) -> Unit,
    val onRemoveClick: (item: PlaylistListItem) -> Unit,
) :
    RecyclerView.Adapter<PlaylistListAdapter.ViewHolder>() {

    class ViewHolder(binding: PlaylistQuickActionsSheetListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val playlistName = binding.playlistDetails.playlistName
        val playlistCover = binding.playlistDetails.playlistCover
        val playlistTracksCount = binding.playlistDetails.playlistTracksCount
        val addToPlaylistButton = binding.addToPlaylist
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): PlaylistListAdapter.ViewHolder {
        val binding = PlaylistQuickActionsSheetListItemBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: PlaylistListAdapter.ViewHolder, position: Int) {
        val playlistItem = playlists[position]
        val isTrackAdded = playlistItem.isTrackAdded

        viewHolder.playlistName.text = playlistItem.name
        viewHolder.playlistTracksCount.text =
            "${playlistItem.totalTracks} música${if (playlistItem.totalTracks !== 1) "s" else ""}"

        viewHolder.addToPlaylistButton.setOnClickListener { _ ->
            if (isTrackAdded) onRemoveClick(playlistItem)
            else onAddClick(playlistItem)
        }

        viewHolder.addToPlaylistButton.setIconResource(
            if (isTrackAdded) R.drawable.baseline_check_circle_24
            else R.drawable.outline_add_circle_outline_24
        )
    }

    override fun getItemCount() = playlists.size

    fun updateData(newPlaylists: List<PlaylistListItem>) {
        val diffResult = DiffUtil.calculateDiff(
            object : DiffUtil.Callback() {
                override fun getNewListSize() = newPlaylists.size
                override fun getOldListSize() = playlists.size

                override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                    return playlists[oldPos].id == newPlaylists[newPos].id
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