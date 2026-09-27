package com.nortlinos.wearos.data.api

import com.nortlinos.wearos.data.model.Library
import com.nortlinos.wearos.data.model.LibraryItem
import com.nortlinos.wearos.data.model.LoginResponse
import com.nortlinos.wearos.data.model.PlaybackSession
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface AudiobookshelfApi {
    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

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

    @POST("api/session/{id}/sync")
    suspend fun syncSession(
        @Path("id") sessionId: String,
        @Body body: SessionSyncRequest
    ): Response<Unit>

    @GET("api/me/progress/{id}")
    suspend fun getProgress(@Path("id") itemId: String): Response<MediaProgressDto>

    @PATCH("api/me/progress/{id}")
    suspend fun updateProgress(
        @Path("id") itemId: String,
        @Body body: ProgressUpdateRequest
    ): Response<Unit>
}

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
    val forceDirectPlay: Boolean = true,
    val mediaPlayer: String = "wearos-app",
    val supportedMimeTypes: List<String> = listOf(
        "audio/mpeg",
        "audio/mp4",
        "audio/aac",
        "audio/ogg",
        "audio/flac",
        "audio/wav"
    )
)
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
    val mediaProgress: List<MediaProgressDto> = emptyList()
)
