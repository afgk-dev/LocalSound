package dev.afgk.localsound.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.createGraph
import androidx.navigation.fragment.fragment
import androidx.navigation.navigation
import dev.afgk.localsound.ui.home.HomeFragment
import dev.afgk.localsound.ui.onboarding.RequestReadPermissionFragment
import dev.afgk.localsound.ui.player.PlayerFragment
import dev.afgk.localsound.ui.playlists.PlaylistFragment
import dev.afgk.localsound.ui.playlists.UpsertPlaylistFragment
import dev.afgk.localsound.ui.sync.SyncTracksFragment
import dev.afgk.localsound.ui.onboarding.LoadingMusicFragment

class NavigationGraph {

    fun setGraph(navController: NavController) {
        navController.graph = navController.createGraph(
            startDestination = NavigationRoutes.onboarding._route
        ) {
            navigation(
                startDestination = NavigationRoutes.onboarding.requestReadPermission,
                route = NavigationRoutes.onboarding._route
            ) {
                fragment<RequestReadPermissionFragment>(NavigationRoutes.onboarding.requestReadPermission)
                fragment<SyncTracksFragment>(NavigationRoutes.onboarding.syncTracks)

                fragment<LoadingMusicFragment>(NavigationRoutes.onboarding.loadingMusic)
            }

            fragment<HomeFragment>(NavigationRoutes.home)
            fragment<PlayerFragment>(NavigationRoutes.player)
        }
    }
}
object NavigationRoutes {
    object onboarding {
        const val _route = "onboarding"
        const val requestReadPermission = "requestReadPermission"
        const val syncTracks = "syncTracks"
        const val loadingMusic = "loading_music"
    }

    const val home = "home"
    const val player = "player"

    const val createPlaylist = "createPlaylist"
    const val updatePlaylist = "updatePlaylist"

    const val playlist = "playlist"
}

const val loadingMusic = "loading_music"