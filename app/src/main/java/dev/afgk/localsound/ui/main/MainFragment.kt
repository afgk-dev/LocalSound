package dev.afgk.localsound.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dev.afgk.localsound.databinding.FragmentExampleBinding
import dev.afgk.localsound.ui.navigation.NavGraphRoutes

class ExampleFragment : Fragment() {
    private var _binding: FragmentExampleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("ExampleFragment", "Container id ${container?.accessibilityClassName}")

        _binding = FragmentExampleBinding.inflate(inflater, container, false)
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("ExampleFragment", "View created")

        binding.button.setOnClickListener { view ->
            Log.i("ExampleFragment", "Button clicked")
            view.findNavController().navigate(NavGraphRoutes.TRACK_LIST_FRAGMENT)
        }
    }
}