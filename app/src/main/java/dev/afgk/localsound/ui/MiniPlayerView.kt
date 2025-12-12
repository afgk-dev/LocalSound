package dev.afgk.localsound.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.material.button.MaterialButton
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.ViewMiniPlayerBinding
import dev.afgk.localsound.ui.navigation.NavigationRoutes
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

    var allowededRoutes = listOf(
        NavigationRoutes.home,
        "${NavigationRoutes.playlist}/{playlistId}"
    )

    var currentRoute: String? = null

    private var playerViewModel: PlayerViewModel? = null

    fun changeRoute(route: String?) {
        currentRoute = route

        if (currentRoute !in allowededRoutes) hide()
    }

    suspend fun bindViewModel(viewModel: PlayerViewModel, activity: MainActivity) {
        playerViewModel = viewModel

        viewModel.state.collect { (status, mediaMetadata, media) ->
            Log.i(_TAG, status.name)

            if (currentRoute !in allowededRoutes)
                return@collect hide()

            when (status) {
                PlayerStatus.PLAYING -> playing()
                PlayerStatus.PAUSED -> paused()
                PlayerStatus.BUFFERING -> Unit
                else -> hide()
            }

            if (media != null) {
                binding.addToPlaylist.setOnClickListener(null)
                binding.addToPlaylist.setOnClickListener {
                    Log.i(_TAG, "binding.addToPlaylist")

                    PlaylistQuickActionsBottomSheetModal(media.mediaId.toLong())
                        .show(
                            activity.supportFragmentManager,
                            _TAG
                        )
                }
            }

            if (mediaMetadata != null) {
                track = mediaMetadata.title.toString()
                artist = mediaMetadata.artist.toString() ?: "Artista desconhecido"
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
//                PlaylistQuickActionsBottomSheetModal()
            }
            prevButton.setOnClickListener { playerViewModel?.previous() }
            playPauseButton.setOnClickListener { playerViewModel?.playPause() }
            nextButton.setOnClickListener { playerViewModel?.next() }
        }
    }
}