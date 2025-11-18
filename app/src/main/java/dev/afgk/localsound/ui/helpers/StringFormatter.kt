package dev.afgk.localsound.ui.helpers

class StringFormatter {
    companion object {
        fun fromSecondsToMinutesAndSeconds(duration: Number): String {
            val durationSeconds = duration.toInt()
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60

            return String.format("%02d:%02d", minutes, seconds)
        }

        fun fromSecondsToHoursAndMinutes(duration: Number): String {
            val durationSeconds = duration.toInt()
            val hours = durationSeconds / 3600
            val minutes =
                if (durationSeconds < 3600) durationSeconds / 60 else durationSeconds % 3600

            if (hours < 1) return String.format("%2d min", minutes)
            else return String.format("%2d hr %2d min", hours, minutes)
        }
    }
}