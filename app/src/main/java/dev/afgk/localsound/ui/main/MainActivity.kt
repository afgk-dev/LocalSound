package dev.afgk.localsound.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import dev.afgk.localsound.R
import dev.afgk.localsound.databinding.ActivityMainBinding
import dev.afgk.localsound.ui.navigation.NavGraph

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Uses view binding generated class for activity_main.xml, inflates it and then get the
         * .root view to set as content view.
         *
         * [More about view binding](https://developer.android.com/topic/libraries/view-binding?hl=en)
         */

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        /**
         * Setting up NavGraph for navigation
         *
         * [More about navigation](https://developer.android.com/guide/navigation/design)
         */

        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController

        NavGraph().setGraph(navController)

        /**
         * Enable edge-to-edge and ensure that the app content won't be behind
         * system bars (i.e. status bar, navigation bar and captions bar), applying padding
         * to the root view (R.id.main).
         *
         * [More about edge-to-edge](https://developer.android.com/develop/ui/views/layout/edge-to-edge)
         */

        WindowCompat.enableEdgeToEdge(window)

        val rootView: View = findViewById(R.id.main)

        /**
         * Used for backwards compatibility for SDK < 30.
         */
        ViewGroupCompat.installCompatInsetsDispatch(rootView)

        /**
         * This method sets a callback that will be called when window insets changes. It returns
         * WindowInsetsCompat.CONSUMED, to avoid padding in child views.
         */
        ViewCompat.setOnApplyWindowInsetsListener(rootView) insetsCb@{ v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            return@insetsCb WindowInsetsCompat.CONSUMED
        }
    }
}