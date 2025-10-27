package dev.afgk.localsound.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.dynamicfeatures.createGraph
import androidx.navigation.dynamicfeatures.fragment.fragment
import androidx.navigation.dynamicfeatures.navigation
import dev.afgk.localsound.ui.onboarding.RequestReadPermissionFragment
import dev.afgk.localsound.ui.tracks.TracksListFragment

object NavigationRoutes {
    object onboarding {
        const val _route = "onboarding"
        const val requestReadPermission = "requestReadPermission"
    }

    const val tracksList = "tracksList"
}

class NavigationGraph {
    fun setGraph(navController: NavController) {
        navController.graph = navController.createGraph(
            startDestination = NavigationRoutes.onboarding._route
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
        }
    }
}