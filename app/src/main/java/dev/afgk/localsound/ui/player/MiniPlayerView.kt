package dev.afgk.localsound.ui.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.button.MaterialButton
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.ViewMiniPlayerBinding
import dev.afgk.localsound.ui.PlayerViewModel
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

    private var currentRoute: String? = null
    private val visibleRoutes = listOf(
        NavigationRoutes.home,
        "${NavigationRoutes.playlist}/{playlistId}"
    )

    private var playerViewModel: PlayerViewModel? = null

    // XML child views
    private val miniPlayerCard = binding.miniPlayerCard
    private val addToPlaylistBtn = binding.addToPlaylist
    private val prevBtn = binding.prevButton
    private val nextBtn = binding.nextButton
    private val playPauseBtn = (binding.playPauseButton as MaterialButton)
    private val trackNameTxt = binding.track
    private val artistNameTxt = binding.artist
    private val progressIndicator = binding.progress

    // Setup listeners for buttons
    init {
        prevBtn.setOnClickListener { playerViewModel?.previous() }
        playPauseBtn.setOnClickListener { playerViewModel?.playPause() }
        nextBtn.setOnClickListener { playerViewModel?.next() }
    }

    // Public methods
    fun setCurrentRoute(route: String?) {
        currentRoute = route
        if (currentRoute !in visibleRoutes) hide()
    }

    fun setOnMiniPlayerCardClick(fn: () -> Unit) {
        miniPlayerCard.setOnClickListener { fn() }
    }

    suspend fun bind(viewModel: PlayerViewModel, fragmentManager: FragmentManager) {
        playerViewModel = viewModel

        viewModel.uiState.collect { uiState ->
            if (currentRoute !in visibleRoutes) return@collect hide()

            if (uiState.hidden) hide()
            else if (uiState.buffering) buffering()
            else if (uiState.error != null) error()
            else if (uiState.track != null) {
                if (uiState.playing) playing() else paused()

                val track = uiState.track

                trackNameTxt.text = track.name
                artistNameTxt.text = track.artistName

                setAddPlaylistBtnListener(track.id, fragmentManager)

                progressIndicator.setProgress(
                    track.progress.toInt(),
                    true
                )
            }
        }
    }

    // Private util methods
    private fun setAddPlaylistBtnListener(trackId: Long, fragmentManager: FragmentManager) {
        addToPlaylistBtn.setOnClickListener(null)
        addToPlaylistBtn.setOnClickListener {
            PlaylistQuickActionsBottomSheetModal(trackId)
                .show(
                    fragmentManager,
                    _TAG
                )
        }
    }

    // Private UI state methods
    private fun show() {
        miniPlayerCard.visibility = VISIBLE
    }

    private fun hide() {
        miniPlayerCard.visibility = GONE
    }

    private fun buffering() {
        show()
        playPauseBtn.isEnabled = false
    }

    private fun error() {
        hide()
    }

    private fun playing() {
        show()

        playPauseBtn.isEnabled = true
        playPauseBtn.setIconResource(R.drawable.rounded_pause_24dp)
    }

    private fun paused() {
        show()

        playPauseBtn.isEnabled = true
        playPauseBtn.setIconResource(R.drawable.rounded_play_arrow_24dp)
    }
}