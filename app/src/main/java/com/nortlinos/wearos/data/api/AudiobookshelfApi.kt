package com.nortlinos.wearos.data.api

import com.nortlinos.wearos.data.model.Library
import com.nortlinos.wearos.data.model.LibraryItem
import com.nortlinos.wearos.data.model.LoginResponse
import com.nortlinos.wearos.data.model.PlaybackSession
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface AudiobookshelfApi {
    /**
     * Password sign-in. Audiobookshelf 2.26+ only puts the refresh token in the response body when
     * the client sends `x-return-tokens: true`; without it the token goes into a browser cookie
     * this client never keeps, and the session cannot be renewed once the 1-hour access token
     * expires.
     */
    @POST("login")
    suspend fun login(
        @Body body: LoginRequest,
        @Header("x-return-tokens") returnTokens: String = "true"
    ): Response<LoginResponse>

    /**
     * Ends this device's server-side auth session (Audiobookshelf 2.26+), so it no longer appears
     * under the account's active sessions and its refresh token stops working.
     */
    @POST("logout")
    suspend fun logout(@Header("x-refresh-token") refreshToken: String): Response<ResponseBody>

    /** Unauthenticated capability probe; reports which auth methods the server has enabled. */
    @GET("status")
    suspend fun serverStatus(): Response<ServerStatusDto>

    /**
     * Starts the OIDC mobile flow. Answers `302` with the identity provider's authorization URL in
     * the `Location` header, so it must be issued on a client with redirect following disabled.
     */
    @GET("auth/openid")
    suspend fun openIdAuthorize(@QueryMap params: Map<String, String>): Response<ResponseBody>

    /** Exchanges the authorization code for a session, using the cookie set by [openIdAuthorize]. */
    @GET("auth/openid/callback")
    suspend fun openIdCallback(
        @Query("state") state: String,
        @Query("code") code: String,
        @Query("code_verifier") codeVerifier: String
    ): Response<LoginResponse>

    /**
     * Mints a fresh access token. Cookie-less clients must pass the refresh token in the header —
     * otherwise the server only rotates it into a cookie and never returns the new value.
     */
    @POST("auth/refresh")
    suspend fun refreshSession(@Header("x-refresh-token") refreshToken: String): Response<LoginResponse>

    @GET("api/me")
    suspend fun getCurrentUser(): Response<CurrentUserDto>

    @GET("api/libraries")
    suspend fun getLibraries(): Response<LibrariesResponse>

    @GET("api/libraries/{id}/items")
    suspend fun getLibraryItems(
        @Path("id") libraryId: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int = 0,
        @Query("sort") sort: String = "media.metadata.title",
        @Query("include") include: String = "progress"
    ): Response<LibraryItemsResponse>

    @GET("api/libraries/{id}/search")
    suspend fun searchLibrary(
        @Path("id") libraryId: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 50
    ): Response<SearchResponse>

    @GET("api/items/{id}")
    suspend fun getLibraryItem(
        @Path("id") itemId: String,
        @Query("expanded") expanded: Int = 1,
        @Query("include") include: String = "progress"
    ): Response<LibraryItem>

    @POST("api/items/{id}/play")
    suspend fun startPlaybackSession(
        @Path("id") itemId: String,
        @Body request: PlaybackRequest = PlaybackRequest()
    ): Response<PlaybackSession>

    @POST("api/items/{id}/play/{episodeId}")
    suspend fun startPodcastPlaybackSession(
        @Path("id") itemId: String,
        @Path("episodeId") episodeId: String,
        @Body request: PlaybackRequest = PlaybackRequest()
    ): Response<PlaybackSession>

    @POST("api/session/{id}/sync")
    suspend fun syncSession(
        @Path("id") sessionId: String,
        @Body body: SessionSyncRequest
    ): Response<Unit>

    /** Closes a playback session that will not be synced (Audiobookshelf discards it unsaved). */
    @POST("api/session/{id}/close")
    suspend fun closeSession(@Path("id") sessionId: String): Response<ResponseBody>

    /**
     * Uploads listening sessions recorded on the watch. The server creates each one by id, or
     * updates it if that id was uploaded before, and counts its `timeListening` toward listening
     * stats. It also applies the session's position to media progress, but only when the
     * session is newer than the server's progress.
     */
    @POST("api/session/local-all")
    suspend fun syncLocalSessions(@Body body: LocalSessionsRequest): Response<LocalSessionsResponse>

    @GET("api/me/progress/{id}")
    suspend fun getProgress(@Path("id") itemId: String): Response<MediaProgressDto>

    /** All of the user's media progress (Audiobookshelf 2.36+); `404` on older servers. */
    @GET("api/me/progress")
    suspend fun getAllProgress(): Response<AllProgressResponse>

    /** The user's bookmarks for one item (Audiobookshelf 2.36+); `404` on older servers. */
    @GET("api/me/bookmarks/{id}")
    suspend fun getBookmarks(@Path("id") itemId: String): Response<BookmarksResponse>

    @PATCH("api/me/progress/{id}")
    suspend fun updateProgress(
        @Path("id") itemId: String,
        @Body body: ProgressUpdateRequest
    ): Response<Unit>

    @PATCH("api/me/progress/{id}/{episodeId}")
    suspend fun updatePodcastEpisodeProgress(
        @Path("id") itemId: String,
        @Path("episodeId") episodeId: String,
        @Body body: ProgressUpdateRequest
    ): Response<Unit>

    @POST("api/me/item/{id}/bookmark")
    suspend fun createBookmark(
        @Path("id") itemId: String,
        @Body body: BookmarkRequest
    ): Response<AudioBookmarkDto>

    @DELETE("api/me/item/{id}/bookmark/{time}")
    suspend fun deleteBookmark(
        @Path("id") itemId: String,
        @Path("time") time: Int
    ): Response<Unit>
}

/** How Audiobookshelf labels the player in this app's sessions. */
const val MEDIA_PLAYER = "wearos-app"

data class LoginRequest(val username: String, val password: String)
data class ServerStatusDto(
    val app: String? = null,
    val serverVersion: String? = null,
    val isInit: Boolean = true,
    val authMethods: List<String>? = null,
    val authFormData: AuthFormDataDto? = null
)
data class AuthFormDataDto(
    val authOpenIDButtonText: String? = null,
    val authOpenIDAutoLaunch: Boolean = false
)
data class LibrariesResponse(val libraries: List<Library> = emptyList())
data class LibraryItemsResponse(
    val results: List<LibraryItem> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val page: Int = 0
)
data class SearchResponse(
    val book: List<SearchBookResult> = emptyList(),
    val tags: List<LibraryItem> = emptyList()
)
data class SearchBookResult(val libraryItem: LibraryItem)
data class PlaybackRequest(
    /**
     * Identifies the watch to the server. Without it Audiobookshelf derives a device from the
     * user agent and IP, so listening sessions show up as an unnamed "okhttp" client.
     */
    val deviceInfo: DeviceInfoDto? = null,
    val forceDirectPlay: Boolean = true,
    val mediaPlayer: String = MEDIA_PLAYER,
    val supportedMimeTypes: List<String> = listOf(
        "audio/mpeg",
        "audio/mp4",
        "audio/aac",
        "audio/ogg",
        "audio/flac",
        "audio/wav"
    )
)
/** The client-supplied half of Audiobookshelf's `DeviceInfo`; the server fills in the rest. */
data class DeviceInfoDto(
    val deviceId: String,
    val clientName: String,
    val clientVersion: String,
    val manufacturer: String,
    val model: String,
    val sdkVersion: Int
)
data class LocalSessionsRequest(
    val deviceInfo: DeviceInfoDto,
    val sessions: List<LocalSessionDto>
)
/** Times are seconds, timestamps epoch milliseconds, as Audiobookshelf's `PlaybackSession`. */
data class LocalSessionDto(
    val id: String,
    val libraryItemId: String,
    val episodeId: String?,
    val mediaType: String,
    val displayTitle: String?,
    val displayAuthor: String?,
    val duration: Double,
    val playMethod: Int,
    val mediaPlayer: String,
    val startTime: Double,
    val currentTime: Double,
    val timeListening: Double,
    val startedAt: Long,
    val updatedAt: Long
) {
    companion object {
        /** Audiobookshelf's `PlayMethod` values. */
        const val PLAY_METHOD_DIRECT_PLAY = 0
        const val PLAY_METHOD_LOCAL = 3
    }
}
data class LocalSessionsResponse(val results: List<LocalSessionResult> = emptyList())
data class LocalSessionResult(
    val id: String? = null,
    val success: Boolean = false,
    val error: String? = null
)
data class AllProgressResponse(val mediaProgress: List<MediaProgressDto> = emptyList())
data class BookmarksResponse(val bookmarks: List<AudioBookmarkDto> = emptyList())
data class SessionSyncRequest(
    val currentTime: Double,
    val timeListened: Double,
    val duration: Double
)
data class ProgressUpdateRequest(
    val currentTime: Double,
    val duration: Double,
    val isFinished: Boolean = false
)
data class MediaProgressDto(
    val libraryItemId: String? = null,
    val episodeId: String? = null,
    val currentTime: Double = 0.0,
    val duration: Double = 0.0,
    val lastUpdate: Long = 0L,
    val updatedAt: Long = 0L
) {
    val effectiveUpdatedAt: Long get() = maxOf(lastUpdate, updatedAt)
}
data class CurrentUserDto(
    val id: String,
    val username: String,
    val mediaProgress: List<MediaProgressDto> = emptyList(),
    val bookmarks: List<AudioBookmarkDto> = emptyList()
)
data class BookmarkRequest(val time: Int, val title: String)
data class AudioBookmarkDto(
    val libraryItemId: String? = null,
    val title: String = "",
    val time: Int = 0,
    val createdAt: Long = 0L
)
