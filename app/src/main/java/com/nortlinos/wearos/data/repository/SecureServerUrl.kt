package com.nortlinos.wearos.data.repository

import com.nortlinos.wearos.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object SecureServerUrl {
    const val HTTPS_REQUIRED_MESSAGE =
        "Use an HTTPS server address. NortlinOS does not send credentials or session data over HTTP."

    /** Mirrors the `nortlinos.allowInsecureServers` Gradle property. */
    val allowInsecure: Boolean = BuildConfig.ALLOW_INSECURE_SERVERS

    fun normalize(input: String, allowHttp: Boolean = allowInsecure): String {
        val trimmed = input.trim().trimEnd('/')
        require(trimmed.isNotBlank()) { "Server URL is required" }
        require(allowHttp || !trimmed.startsWith("http://", ignoreCase = true)) {
            HTTPS_REQUIRED_MESSAGE
        }
        val absoluteUrl = when {
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            allowHttp && trimmed.startsWith("http://", ignoreCase = true) -> trimmed
            trimmed.contains("://") -> throw IllegalArgumentException("Server address must use HTTPS")
            else -> "https://$trimmed"
        }

        val url = absoluteUrl.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Enter a valid HTTPS server address")
        require(allowHttp || url.isHttps) { HTTPS_REQUIRED_MESSAGE }
        require(url.username.isEmpty() && url.password.isEmpty()) {
            "Do not include credentials in the server address"
        }
        require(url.encodedQuery == null && url.encodedFragment == null) {
            "Server address must not contain a query or fragment"
        }
        return url.toString().trimEnd('/')
    }

    /** True when [input] is an acceptable server address under the current build's policy. */
    fun isHttps(input: String, allowHttp: Boolean = allowInsecure): Boolean =
        runCatching { normalize(input, allowHttp) }.isSuccess
}
