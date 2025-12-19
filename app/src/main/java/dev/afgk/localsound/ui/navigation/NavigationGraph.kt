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

object NavigationRoutes {
    object onboarding {
        const val _route = "onboarding"
        const val requestReadPermission = "requestReadPermission"
        const val syncTracks = "syncTracks"
    }

    const val home = "home"
    const val player = "player"

    const val createPlaylist = "createPlaylist"
    const val updatePlaylist = "updatePlaylist"

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

                fragment<SyncTracksFragment>(
                    route = NavigationRoutes.onboarding.syncTracks,
                )
            }

            fragment<HomeFragment>(
                route = NavigationRoutes.home
            )

            fragment<PlayerFragment>(
                route = NavigationRoutes.player
            ) {}

            fragment<UpsertPlaylistFragment>(
                route = "${NavigationRoutes.createPlaylist}/{trackId}"
            ) {
                argument("trackId") {
                    type = NavType.LongType
                    nullable = false
                }

                argument("playlistId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            }

            fragment<UpsertPlaylistFragment>(
                route = "${NavigationRoutes.updatePlaylist}/{playlistId}"
            ) {
                argument("playlistId") {
                    type = NavType.LongType
                    nullable = false
                }

                argument("trackId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            }

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