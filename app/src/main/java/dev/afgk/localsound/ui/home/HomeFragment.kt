package dev.afgk.localsound.ui.home

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.afgk.localsound.MyApplication
import dev.afgk.localsound.databinding.FragmentHomeBinding
import dev.afgk.localsound.ui.Ability
import dev.afgk.localsound.ui.HomeViewModel
import dev.afgk.localsound.ui.PermissionsUiState
import dev.afgk.localsound.ui.helpers.viewModelFactory
import dev.afgk.localsound.ui.navigation.NavigationRoutes

// Necessary for Bottom Sheet
import dev.afgk.localsound.ui.queue.QueueBottomSheetFragment
import dev.afgk.localsound.ui.tracks.TracksListAdapter
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var viewModel: HomeViewModel

    private lateinit var tracksListAdapter: TracksListAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        if (!PermissionsUiState.can(
                Ability.READ_AUDIO,
                requireContext()
            )
        ) return navController.navigate(
            NavigationRoutes.onboarding._route
        )

        viewModel = ViewModelProvider.create(
            this,
            viewModelFactory {
                HomeViewModel(
                    MyApplication.appModule.tracksRepository,
                    MyApplication.appModule.queueRepository)
            }
        )[HomeViewModel::class]

        tracksListAdapter = TracksListAdapter(emptyList()) { selectedTrack ->
            viewModel.addToQueue(selectedTrack)
        }
        binding.tracksList.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksList.adapter = tracksListAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tracksState.collect { tracks ->
                    if (tracks.isEmpty()) {
                        binding.textNoMusics.visibility = View.VISIBLE
                        binding.tracksListGroup.visibility = View.GONE
                    } else {
                        binding.textNoMusics.visibility = View.GONE
                        binding.tracksListGroup.visibility = View.VISIBLE
                    }

                    tracksListAdapter.updateData(tracks)
                }
            }
        }

        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            private val swipeThreshold = 100

            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val distanceY = e1.y - e2.y
                val distanceX = e1.x - e2.x

                if (distanceY > swipeThreshold && abs(distanceY) > abs(distanceX)) {
                    val bottomSheet = QueueBottomSheetFragment()
                    bottomSheet.show(parentFragmentManager, "QueueSheet")
                    return true
                }
                return false
            }
        })

        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}