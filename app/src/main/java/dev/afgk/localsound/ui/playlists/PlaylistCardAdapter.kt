package dev.afgk.localsound.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.FragmentPlaylistCardBinding
import kotlin.random.Random


class PlaylistCardAdapter(
    //Need the SQLite to be made to work or data class for Playlist
    //private var playlists: List<Playlist>
) : RecyclerView.Adapter<PlaylistCardAdapter.PlaylistCardViewHolder>() {

    private val numberOfCards = Random.nextInt(3,10)
    class PlaylistCardViewHolder(val binding: FragmentPlaylistCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

        //Need to receive a param who will give the playlist data
        fun bind() {
            binding.playlistName.text = "músicas"
            val trackCountText = "${Random.nextInt(1,300)} tracks"
            binding.playlistNumberOfTracks.text = trackCountText

            binding.image1.setImageResource(R.drawable.baseline_audiotrack_24)
            binding.image2.setImageResource(R.drawable.baseline_audiotrack_24)
            binding.image3.setImageResource(R.drawable.baseline_audiotrack_24)
            binding.image4.setImageResource(R.drawable.baseline_audiotrack_24)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistCardViewHolder {
        val binding = FragmentPlaylistCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistCardViewHolder(binding)
    }

    //Called by RecyclerView to connect the playlist data with the view holder
    override fun onBindViewHolder(holder: PlaylistCardViewHolder, position: Int) {
        // Pega os dados da posição correta e chama o bind
        holder.bind()
    }

    /**Need the SQLite to work*/
     override fun getItemCount(): Int {
         //Inform the view the number of playlists
        return numberOfCards
     }


}