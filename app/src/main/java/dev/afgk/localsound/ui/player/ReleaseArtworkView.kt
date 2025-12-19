package dev.afgk.localsound.ui.player

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import dev.afgk.localsound.databinding.ReleaseArtworkBinding

class ReleaseArtworkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding: ReleaseArtworkBinding =
        ReleaseArtworkBinding.inflate(LayoutInflater.from(context), this)

    // XML child views
    private val artwork = binding.artwork
    private val empty = binding.empty

    fun setArtworkUri(uri: Uri?, signature: String = uri.toString()) {
        if (uri != null) {
            Glide.with(this)
                .load(uri)
                .signature(ObjectKey(signature))
                .into(artwork)

            showArtwork()
        } else {
            showPlaceholder()
        }
    }

    private fun showArtwork() {
        artwork.visibility = VISIBLE
        empty.visibility = GONE
    }

    private fun showPlaceholder() {
        artwork.visibility = GONE
        empty.visibility = VISIBLE
    }
}