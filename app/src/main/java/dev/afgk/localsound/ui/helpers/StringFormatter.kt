package dev.afgk.localsound.ui.helpers

class StringFormatter {
    companion object {
        fun toMinutesSeconds(duration: Number): String {
            val durationSeconds = duration.toInt() / 1000
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60

            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}