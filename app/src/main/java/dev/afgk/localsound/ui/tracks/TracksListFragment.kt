package dev.afgk.localsound.ui.tracks

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.FragmentRequestReadPermissionBinding
import dev.afgk.localsound.databinding.FragmentTracksListBinding
import dev.afgk.localsound.ui.Ability
import dev.afgk.localsound.ui.PermissionsUiState
import dev.afgk.localsound.ui.navigation.NavigationRoutes

class TracksListFragment : Fragment() {
    private var _binding: FragmentTracksListBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTracksListBinding.inflate(inflater, container, false)
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
    }
}