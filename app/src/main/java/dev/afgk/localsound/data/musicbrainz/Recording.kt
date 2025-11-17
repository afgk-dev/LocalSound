// Recording.kt
package dev.afgk.localsound.data.musicbrainz

import com.google.gson.annotations.SerializedName

data class RecordingSearchResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("recordings") val recordings: List<Recording>
)

data class Recording(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("length") val length: Long?,
    @SerializedName("artist-credit") val artistCredit: List<ArtistCredit>,
    @SerializedName("releases") val releases: List<Release>?
) {
    val artistName: String
        get() = artistCredit.firstOrNull()?.artist?.name ?: "Artista Desconhecido"
}

data class ArtistCredit(
    @SerializedName("name") val name: String,
    @SerializedName("artist") val artist: Artist
)

data class Artist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class Release(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String?
)