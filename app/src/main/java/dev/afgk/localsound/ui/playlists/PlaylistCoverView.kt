package dev.afgk.localsound.ui.playlists

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import dev.afgk.localsound.databinding.PlaylistCoverBinding

class PlaylistCoverView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: PlaylistCoverBinding =
        PlaylistCoverBinding.inflate(LayoutInflater.from(context), this)

    fun setCoverUri(uri: Uri?) {
        if (uri != null) {
            Glide.with(this)
                .load(uri)
                .into(binding.image)

            showImageState()
        } else {
            showEmptyState()
        }
    }

    private fun showImageState() {
        binding.image.visibility = View.VISIBLE
        binding.empty.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.image.visibility = View.GONE
        binding.empty.visibility = View.VISIBLE
    }
}