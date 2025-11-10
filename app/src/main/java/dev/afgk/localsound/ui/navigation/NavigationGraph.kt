package dev.afgk.localsound.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.dynamicfeatures.createGraph
import androidx.navigation.dynamicfeatures.fragment.fragment
import androidx.navigation.dynamicfeatures.navigation
import dev.afgk.localsound.ui.onboarding.RequestReadPermissionFragment
import dev.afgk.localsound.ui.screens.LibraryFragment
import dev.afgk.localsound.ui.screens.SearchFragment
import dev.afgk.localsound.ui.tracks.TracksListFragment

object NavigationRoutes {
    object onboarding {
        const val _route = "onboarding"
        const val requestReadPermission = "requestReadPermission"
    }

    const val tracksList = "tracksList"
    const val LibraryFragment = "LibraryFragment"
    const val SearchFragment = "SearchFragment"
}

class NavigationGraph {
    fun setGraph(navController: NavController) {
        navController.graph = navController.createGraph(
            startDestination = NavigationRoutes.LibraryFragment
        ) {
            navigation(
                startDestination = NavigationRoutes.onboarding.requestReadPermission,
                route = NavigationRoutes.onboarding._route
            ) {
                fragment<RequestReadPermissionFragment>(
                    route = NavigationRoutes.onboarding.requestReadPermission
                )
            }

            fragment<TracksListFragment>(
                route = NavigationRoutes.tracksList
            )
            fragment<LibraryFragment>(
                route = NavigationRoutes.LibraryFragment
            )
            fragment<SearchFragment>(
                route = NavigationRoutes.SearchFragment
            )
        }
    }
}