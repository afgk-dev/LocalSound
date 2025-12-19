package dev.afgk.localsound.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.databinding.QueueSheetBinding
import dev.afgk.localsound.ui.PlayerViewModel
import dev.afgk.localsound.ui.helpers.viewModelFactory
import kotlinx.coroutines.launch

class QueueBottomSheetModal() : BottomSheetDialogFragment() {
    private var _binding: QueueSheetBinding? = null
    private val binding get() = _binding!!

    private val tracksRepository = MyApplication.appModule.tracksRepository
    private val playerViewModel: PlayerViewModel by activityViewModels {
        viewModelFactory { PlayerViewModel(tracksRepository) }
    }

    private val queueListAdapter = QueueListAdapter(listOf()) {
        playerViewModel.addNext(it.id)
    }

    private val customQueueListAdapter = CustomQueueListAdapter(listOf()) {
        playerViewModel.removeFromQueue(it.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = QueueSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                playerViewModel.uiState.collect { uiState ->
                    val customQueue = uiState.nextQueue.filter { !it.isCustomQueued }
                    val queue = uiState.nextQueue.filter { it.isCustomQueued }

                    queueListAdapter.updateData(customQueue)
                    customQueueListAdapter.updateData(queue)

                    if (queue.isNotEmpty()) binding.queueDivider.visibility = View.VISIBLE
                    else binding.queueDivider.visibility = View.GONE
                }
            }
        }
    }

    fun setupRecyclerViews() {
        binding.queue.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = queueListAdapter
        }

        binding.customQueue.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = customQueueListAdapter
        }
    }
}