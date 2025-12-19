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

        viewModel.uiState.collect { state ->
            Log.i(_TAG, state.toString())

            if (currentRoute !in allowededRoutes)
                return@collect hide()

            if (state.hidden) hide()
            else if (state.buffering) buffering()
            else if (state.error != null) error()
            else if (state.track != null) {
                if (state.playing) playing() else paused()

                track = state.track.name
                artist = state.track.artistName

                binding.addToPlaylist.setOnClickListener(null)
                binding.addToPlaylist.setOnClickListener {
                    PlaylistQuickActionsBottomSheetModal(state.track.trackId)
                        .show(
                            activity.supportFragmentManager,
                            _TAG
                        )
                }

                binding.progress.setProgress(
                    state.track.progress.toInt(),
                    true
                )
            }
        }
    }

    fun show() {
        binding.miniPlayerCard.visibility = VISIBLE
    }

    fun hide() {
        binding.miniPlayerCard.visibility = GONE
    }

    fun buffering() {
        show()

        playPauseButton.isEnabled = false
    }

    fun error() {
        hide()
    }

    fun playing() {
        show()

        playPauseButton.isEnabled = true
        playPauseButton.setIconResource(R.drawable.rounded_pause_24dp)
    }

    fun paused() {
        show()

        playPauseButton.isEnabled = true
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