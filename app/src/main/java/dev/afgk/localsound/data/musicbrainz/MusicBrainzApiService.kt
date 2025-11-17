package dev.afgk.localsound.data.musicbrainz

import retrofit2.http.GET
import retrofit2.http.Query

interface MusicBrainzApiService {

    @GET("recording")
    suspend fun searchRecordings(
        @Query("query") query: String,
        @Query("fmt") format: String = "json",
        @Query("inc") include: String = "releases+artist-credits"
    ): RecordingSearchResponse
}