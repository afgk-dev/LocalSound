package dev.afgk.localsound.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

enum class Ability {
    READ_AUDIO
}

class PermissionUiStateInvalidAbilityError : Exception("Invalid ability")

class PermissionsUiState() {
    private lateinit var onPermissionCallback: ((granted: Boolean) -> Unit)

    fun request(
        ability: Ability,
        launcher: ActivityResultLauncher<String>,
        cb: ((granted: Boolean) -> Unit)
    ) {
        val permission = mapAbilityToPermission(ability)

        onPermissionCallback = cb
        launcher.launch(permission)
    }

    fun onPermission(granted: Boolean) {
        onPermissionCallback(granted)
    }

    companion object {
        @JvmStatic
        private fun mapAbilityToPermission(ability: Ability): String {
            val sdkVersion = Build.VERSION.SDK_INT

            return when {
                ability == Ability.READ_AUDIO && sdkVersion >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_MEDIA_AUDIO
                ability == Ability.READ_AUDIO && sdkVersion < Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_EXTERNAL_STORAGE
                else -> throw PermissionUiStateInvalidAbilityError()
            }
        }

        fun can(ability: Ability, context: Context): Boolean {
            fun checkPermission(permission: String, context: Context): Boolean {
                return ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }

            return checkPermission(mapAbilityToPermission(ability), context)
        }
    }
}