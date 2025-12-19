package dev.afgk.localsound.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.FragmentPlayerBinding
import dev.afgk.localsound.ui.PlayerUiState
import dev.afgk.localsound.ui.PlayerViewModel
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.playlists.PlaylistQuickActionsBottomSheetModal
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {
    private val _TAG = "PlayerFragment"

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val tracksRepository = MyApplication.appModule.tracksRepository
    private val playerViewModel: PlayerViewModel by activityViewModels {
        viewModelFactory { PlayerViewModel(tracksRepository) }
    }

    private var isTouchingProgressSlider = false
    private var lastProgressSliderMaxValue = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                playerViewModel.uiState.collect { uiState -> show(uiState) }
            }
        }
    }

    private fun show(uiState: PlayerUiState) {
        if (uiState.track != null) {
            val track = uiState.track

            binding.trackName.text = track.name
            binding.artistName.text = track.artistName
            binding.artwork.setArtworkUri(track.coverUri)

            val playBtn = binding.playBtn as MaterialButton

            if (uiState.playing) playBtn.setIconResource(R.drawable.rounded_pause_24dp)
            else playBtn.setIconResource(R.drawable.rounded_play_arrow_24dp)

            val fragmentManager = requireActivity().supportFragmentManager
            setAddToPlaylistListener(track.id, fragmentManager)

            if (lastProgressSliderMaxValue != (track.duration.toFloat())) {
                lastProgressSliderMaxValue = track.duration.toFloat()
                binding.progress.valueTo = track.duration.toFloat()
            }

            if (!isTouchingProgressSlider) binding.progress.value = track.position.toFloat()
        }

        binding.playBtn.isEnabled = !uiState.buffering
    }

    private fun setAddToPlaylistListener(trackId: Long, fragmentManager: FragmentManager) {
        binding.addToPlaylistBtn.setOnClickListener(null)
        binding.addToPlaylistBtn.setOnClickListener {
            PlaylistQuickActionsBottomSheetModal(trackId)
                .show(
                    fragmentManager,
                    _TAG
                )
        }
    }

    private fun setupListeners() {
        binding.playBtn.setOnClickListener {
            playerViewModel.playPause()
        }

        binding.nextBtn.setOnClickListener { playerViewModel.next() }
        binding.prevBtn.setOnClickListener { playerViewModel.previous() }

        binding.progress.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(p0: Slider) {
                isTouchingProgressSlider = true
            }

            override fun onStopTrackingTouch(p0: Slider) {
                isTouchingProgressSlider = false
            }
        })

        binding.progress.addOnChangeListener { _, value, fromUser ->
            if (fromUser) playerViewModel.seekTo(value.toLong())
        }

        binding.queueBtn.setOnClickListener {
            QueueBottomSheetModal().show(requireActivity().supportFragmentManager, _TAG)
        }
    }
}