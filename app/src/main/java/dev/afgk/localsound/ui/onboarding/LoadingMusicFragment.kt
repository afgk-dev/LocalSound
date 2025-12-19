package dev.afgk.localsound.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dev.afgk.localsound.R
import dev.afgk.localsound.ui.navigation.NavigationRoutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingMusicFragment : Fragment(R.layout.fragment_loading_music) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingLayout = view.findViewById<View>(R.id.loading_state_layout)
        val successLayout = view.findViewById<View>(R.id.success_state_layout)

        viewLifecycleOwner.lifecycleScope.launch {
            delay(3000)

            loadingLayout.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_out))
            loadingLayout.visibility = View.GONE

            successLayout.visibility = View.VISIBLE
            successLayout.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_in))

            delay(1500)

            findNavController().navigate(NavigationRoutes.home) {
                popUpTo(NavigationRoutes.onboarding._route) { inclusive = true }
            }
        }
    }
}