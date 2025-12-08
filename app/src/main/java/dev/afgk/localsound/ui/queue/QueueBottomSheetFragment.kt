package dev.afgk.localsound.ui.queue

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.databinding.FragmentQueueBottomSheetBinding
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.queue.TracksOnQueueListAdapter
import kotlinx.coroutines.launch

class QueueBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentQueueBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: QueueViewModel
    private lateinit var personalizedQueueAdapter: TracksOnQueueListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQueueBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )

            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.isDraggable = true
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                behavior.skipCollapsed = false
                val screenHeight = resources.displayMetrics.heightPixels
                behavior.peekHeight = (screenHeight * 0.5).toInt()
                behavior.maxHeight = (screenHeight * 0.5).toInt()
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory {
            QueueViewModel(
                MyApplication.appModule.queueRepository
            )
        })[QueueViewModel::class.java]

        setupPersonalizedQueueAdapter()

        observeData()
    }

    private fun setupPersonalizedQueueAdapter() {
        personalizedQueueAdapter = TracksOnQueueListAdapter(
            isSuggestionTracks = false,
            onRemoveClick = { queueAndTrack ->
            viewModel.removeFromQueue(queueAndTrack.queueTrackEntity)
        }, onAddClick = {   }
        )
        binding.personalizedQueue.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = personalizedQueueAdapter
        }
    }


    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.personalizedQueue.collect { queueItems ->
                personalizedQueueAdapter.updateData(queueItems)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
