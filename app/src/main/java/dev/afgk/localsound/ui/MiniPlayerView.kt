package dev.afgk.localsound.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.material.button.MaterialButton
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.ViewMiniPlayerBinding
import dev.afgk.localsound.ui.playlists.PlaylistQuickActionsBottomSheetModal

class MiniPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val _TAG = "MiniPlayerView"

    private val binding: ViewMiniPlayerBinding =
        ViewMiniPlayerBinding.inflate(LayoutInflater.from(context), this, true)

    private val playPauseButton = (binding.playPauseButton as MaterialButton)

    var track: String
        get() = binding.track.text.toString()
        set(value) {
            binding.track.text = value
        }

    var artist: String
        get() = binding.artist.text.toString()
        set(value) {
            binding.artist.text = value
        }

//    var currentRoute: String?
//        get() = null
//        set(route) {
//            when (route) {
//                NavigationRoutes.home,
//                "${NavigationRoutes.playlist}/{playlistId}"
//                    -> show()
//
//                else -> hide()
//            }
//        }

    var currentRoute: String? = null

    private var playerViewModel: PlayerViewModel? = null

    suspend fun bindViewModel(viewModel: PlayerViewModel) {
        playerViewModel = viewModel

        viewModel.state.collect {
            Log.i(_TAG, it.status.name)

            when (it.status) {
                PlayerStatus.PLAYING -> playing()
                PlayerStatus.PAUSED -> paused()
                PlayerStatus.BUFFERING -> Unit
                else -> hide()
            }

            if (it.mediaMetadata != null) {
                track = it.mediaMetadata.title.toString()
                artist = it.mediaMetadata.artist.toString()
            }
        }
    }

    fun show() {
        binding.miniPlayerCard.visibility = VISIBLE
    }

    fun hide() {
        binding.miniPlayerCard.visibility = GONE
    }

    fun playing() {
        show()
        playPauseButton.setIconResource(R.drawable.rounded_pause_24dp)
    }

    fun paused() {
        show()
        playPauseButton.setIconResource(R.drawable.rounded_play_arrow_24dp)
    }

    init {
        with(binding) {
            addToPlaylist.setOnClickListener {
                PlaylistQuickActionsBottomSheetModal()
            }
            prevButton.setOnClickListener { playerViewModel?.previous() }
            playPauseButton.setOnClickListener { playerViewModel?.playPause() }
            nextButton.setOnClickListener { playerViewModel?.next() }
        }
    }
}