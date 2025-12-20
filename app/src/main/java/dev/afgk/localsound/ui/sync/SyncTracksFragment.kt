package dev.afgk.localsound.ui.sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dev.afgk.localsound.databinding.FragmentSyncTracksBinding
import dev.afgk.localsound.ui.Ability
import dev.afgk.localsound.ui.PermissionsUiState
import dev.afgk.localsound.ui.navigation.NavigationRoutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SyncTracksFragment : Fragment() {
    private var _binding: FragmentSyncTracksBinding? = null
    private val binding get() = _binding!!

    private val syncTracksViewModel: SyncTracksViewModel by viewModels()

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncTracksBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        if (!PermissionsUiState.can(
                Ability.READ_AUDIO,
                requireContext()
            )
        ) return navController.navigate(NavigationRoutes.onboarding._route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                syncTracksViewModel.uiState.collect {
                    when (it) {
                        is SyncTracksUiState.Init -> toBeSynced()
                        is SyncTracksUiState.Syncing -> Unit
                        is SyncTracksUiState.Failed -> failed()
                        is SyncTracksUiState.Synced -> synced()
                    }
                }
            }
        }
    }

    fun toBeSynced() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.loadingIndicator.setProgress(50, true)

            delay(1.seconds)

            syncTracksViewModel.sync(requireContext())

            binding.loadingIndicator.setProgress(75, true)

            delay(1.seconds)
        }
    }

    suspend fun synced() {
        binding.loadingIndicator.setProgress(100, true)

        delay(500.milliseconds)

        binding.loadingIndicator.animate()
            .alpha(0f)
            .setDuration(300L)
            .withEndAction {
                binding.loadingIndicator.visibility = View.GONE
                binding.loadingText.text = "Tudo pronto!"

                viewLifecycleOwner.lifecycleScope.launch {
                    delay(500.milliseconds)

                    navController.navigate(NavigationRoutes.home) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            }
            .start()

        binding.check.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300L)
            .withStartAction {
                binding.check.visibility = View.VISIBLE
            }
            .start()
    }

    fun failed() {
        Snackbar.make(
            requireView(),
            "Erro ao sincronizar músicas",
            Snackbar.LENGTH_SHORT
        ).show()

        toBeSynced()
    }
}