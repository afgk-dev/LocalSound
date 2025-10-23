package dev.afgk.localsound.ui.tracks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.FragmentTrackListBinding
import dev.afgk.localsound.ui.navigation.NavGraphRoutes

class TrackListFragment : Fragment() {
    private var _binding: FragmentTrackListBinding? = null;
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener { view -> view.findNavController().navigate(NavGraphRoutes.EXAMPLE_FRAGMENT) }
    }
}