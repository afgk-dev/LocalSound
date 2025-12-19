package dev.afgk.localsound.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dev.afgk.localsound.databinding.FragmentRequestReadPermissionBinding
import dev.afgk.localsound.ui.Ability
import dev.afgk.localsound.ui.PermissionsUiState
import dev.afgk.localsound.ui.navigation.NavigationRoutes

class RequestReadPermissionFragment : Fragment() {
    private val TAG = "RequestReadPermissionFragment";

    private var _binding: FragmentRequestReadPermissionBinding? = null
    private val binding get() = _binding!!

    private val permissionsUiState = PermissionsUiState()

    private lateinit var navController: NavController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        { g -> permissionsUiState.onPermission(g) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestReadPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        binding.requestPermissionBtn.setOnClickListener {
            permissionsUiState.request(Ability.READ_AUDIO, requestPermissionLauncher) { granted ->
                if (granted) navController.navigate(NavigationRoutes.onboarding.syncTracks)
                findNavController().navigate(NavigationRoutes.onboarding.loadingMusic)
            }
        }
    }
}