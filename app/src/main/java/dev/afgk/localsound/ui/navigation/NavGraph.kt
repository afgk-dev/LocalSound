package dev.afgk.localsound.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.dynamicfeatures.createGraph
import androidx.navigation.dynamicfeatures.fragment.fragment
import dev.afgk.localsound.ui.tracks.TrackListFragment
import dev.afgk.localsound.ui.main.ExampleFragment

object NavGraphRoutes {
    const val EXAMPLE_FRAGMENT = "exampleFragment"
    const val TRACK_LIST_FRAGMENT = "trackListFragment"
}

class NavGraph {
    fun setGraph(navController: NavController) {
        navController.graph = navController.createGraph(startDestination = NavGraphRoutes.EXAMPLE_FRAGMENT) {
            fragment<ExampleFragment>(
                route = NavGraphRoutes.EXAMPLE_FRAGMENT
            )

            fragment<TrackListFragment>(
                route = NavGraphRoutes.TRACK_LIST_FRAGMENT
            )
        }
    }
}