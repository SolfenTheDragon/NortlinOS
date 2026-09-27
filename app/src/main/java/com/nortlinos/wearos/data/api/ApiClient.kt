package com.nortlinos.wearos.data.api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor(
    private val httpClient: OkHttpClient
) {
    private var cachedConfig: Pair<String, String?>? = null
    private var cachedApi: AudiobookshelfApi? = null

    @Synchronized
    fun scopedApi(baseUrl: String, token: String?): AudiobookshelfApi {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val config = normalized to token
        if (config == cachedConfig) return checkNotNull(cachedApi)
        return buildApi(normalized, token).also {
            cachedConfig = config
            cachedApi = it
        }
    }

    private fun buildApi(url: String, token: String?): AudiobookshelfApi {
        // newBuilder() shares the underlying connection pool and dispatcher with every other
        // caller, so a server/token change does not discard warm connections.
        val client = httpClient.newBuilder()
            .addInterceptor(authInterceptor(token))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(AudiobookshelfApi::class.java)
    }

    private fun authInterceptor(token: String?): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val request = if (!token.isNullOrEmpty()) {
            val bearerValue = "Bearer" + " " + token
            original.newBuilder()
                .header("Authorization", bearerValue)
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    /**
     * Builds a short-lived, unauthenticated client for one OIDC sign-in attempt.
     *
     * Two properties matter and neither is true of [scopedApi]:
     *  - redirects are **not** followed, because `GET /auth/openid` communicates the identity
     *    provider's authorization URL through the `Location` header of a `302`;
     *  - a private cookie jar is retained for the lifetime of the attempt, because the server
     *    parks the PKCE/redirect state in an Express session and `/auth/openid/callback` rejects
     *    the exchange with "No session" if that cookie is missing.
     *
     * It is intentionally not cached: each attempt needs a clean jar.
     */
    fun newAuthSession(baseUrl: String): AudiobookshelfApi {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val client = httpClient.newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .cookieJar(SessionCookieJar())
            .build()
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AudiobookshelfApi::class.java)
    }

    /** In-memory cookie jar scoped to a single sign-in attempt. */
    private class SessionCookieJar : CookieJar {
        private val cookies = LinkedHashMap<String, Cookie>()

        @Synchronized
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            for (cookie in cookies) this.cookies[cookie.name] = cookie
        }

        @Synchronized
        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookies.values.toList()
    }

    companion object {
        /** Builds the auth header value used for streaming/downloading raw audio & cover URLs. */
        fun authHeader(token: String?): Pair<String, String>? {
            if (token.isNullOrEmpty()) return null
            val bearerValue = "Bearer" + " " + token
            return "Authorization" to bearerValue
        }

        /** Resolves a server-relative path (e.g. cover art) into an absolute URL. */
        fun resolveUrl(baseUrl: String, path: String): String {
            if (path.startsWith("http://") || path.startsWith("https://")) return path
            val base = baseUrl.trimEnd('/')
            val suffix = if (path.startsWith("/")) path else "/$path"
            return "$base$suffix"
        }

        /** The server-relative endpoint that streams an item's cover art. */
        fun coverEndpoint(itemId: String): String = "/api/items/$itemId/cover"

        /**
         * Asks Audiobookshelf's cover endpoint for a server-resized copy. Other URLs, and ones
         * that already carry a query, are returned untouched.
         */
        fun sizedCoverUrl(url: String, widthPx: Int): String =
            if (url.substringBefore('?') == url && url.endsWith("/cover")) "$url?width=$widthPx" else url

        /**
         * Picks the image source for an item's cover.
         *
         * A downloaded cover is an absolute path on the watch and is used as-is. The `coverPath`
         * an item carries from the server is a path inside the *server's* filesystem, not a URL,
         * so it can only be used as a presence flag — the image itself must be requested from
         * [coverEndpoint]. Passing the raw value through renders nothing.
         */
        fun coverSource(localCoverPath: String?, coverPath: String?, itemId: String): String? =
            localCoverPath?.takeIf { it.isNotEmpty() }
                ?: coverPath?.takeIf { it.isNotEmpty() }?.let { coverEndpoint(itemId) }
    }
}
