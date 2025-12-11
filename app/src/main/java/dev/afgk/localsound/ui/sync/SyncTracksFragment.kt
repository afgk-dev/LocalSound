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
import kotlinx.coroutines.launch

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

        setupListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                syncTracksViewModel.uiState.collect {
                    when (it) {
                        is SyncTracksUiState.Init -> toBeSynced()
                        is SyncTracksUiState.Syncing -> syncing()
                        is SyncTracksUiState.Failed -> failed()
                        is SyncTracksUiState.Synced -> synced()
                    }
                }
            }
        }
    }

    fun setupListeners() {
        binding.syncButton.setOnClickListener {
            syncTracksViewModel.sync(requireContext())
        }
    }

    fun toBeSynced() {
        binding.loading.visibility = View.GONE
        binding.syncButton.visibility = View.VISIBLE
    }

    fun syncing() {
        binding.loading.visibility = View.VISIBLE
        binding.syncButton.visibility = View.GONE
    }

    fun synced() {
        navController.navigate(NavigationRoutes.home) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
        }
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