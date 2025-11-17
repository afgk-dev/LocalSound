package dev.afgk.localsound.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.dynamicfeatures.createGraph
import androidx.navigation.dynamicfeatures.fragment.fragment
import androidx.navigation.dynamicfeatures.navigation
import dev.afgk.localsound.ui.home.HomeFragment
import dev.afgk.localsound.ui.onboarding.RequestReadPermissionFragment
import dev.afgk.localsound.ui.playlists.CreatePlaylistFragment
import dev.afgk.localsound.ui.playlists.PlaylistFragment

object NavigationRoutes {
    object onboarding {
        const val _route = "onboarding"
        const val requestReadPermission = "requestReadPermission"
    }

    const val home = "home"
    const val createPlaylist = "createPlaylist"
    const val playlist = "playlist"
}

class NavigationGraph {
    fun setGraph(navController: NavController) {
        navController.graph = navController.createGraph(
            startDestination = NavigationRoutes.home
        ) {
            navigation(
                startDestination = NavigationRoutes.onboarding.requestReadPermission,
                route = NavigationRoutes.onboarding._route
            ) {
                fragment<RequestReadPermissionFragment>(
                    route = NavigationRoutes.onboarding.requestReadPermission,
                )
            }

            fragment<HomeFragment>(
                route = NavigationRoutes.home
            )

            fragment<CreatePlaylistFragment>(
                route = NavigationRoutes.createPlaylist
            )

            fragment<PlaylistFragment>(
                route = "${NavigationRoutes.playlist}/{playlistId}"
            ) {
                argument("playlistId") {
                    type = NavType.LongType
                    nullable = false
                }
            }
        }
    }
}