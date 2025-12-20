package dev.afgk.localsound.ui.tracks

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.forEach
import com.google.android.material.color.MaterialColors
import dev.afgk.localsound.R

enum class TrackPopupMenuAction {
    AddToQueue,
    AddToPlaylist
}

class TrackPopupMenu(
    private val context: Context,
    private val onAction: (action: TrackPopupMenuAction, trackId: Long) -> Unit
) {
    private var trackId = 0L

    fun show(view: View) {
        val popupMenu = PopupMenu(context, view)

        popupMenu.menuInflater.inflate(R.menu.track_popup_menu, popupMenu.menu)

        popupMenu.setForceShowIcon(true)

        popupMenu.menu.forEach { item ->
            item.icon?.setTint(
                MaterialColors.getColor(
                    view,
                    com.google.android.material.R.attr.colorOnSurface,
                    Color.BLACK
                )
            )
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add_to_queue -> {
                    onAction(TrackPopupMenuAction.AddToQueue, trackId)
                    true
                }

                R.id.action_add_to_playlist -> {
                    onAction(TrackPopupMenuAction.AddToPlaylist, trackId)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    fun setTrackId(newTrackId: Long) {
        trackId = newTrackId
    }
}