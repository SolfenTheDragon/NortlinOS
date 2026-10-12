package com.nortlinos.wearos.data.repository

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object SecureServerUrl {
    const val HTTPS_REQUIRED_MESSAGE =
        "Use an HTTPS server address. NortlinOS does not send credentials or session data over HTTP."

    fun normalize(input: String): String {
        val trimmed = input.trim().trimEnd('/')
        require(trimmed.isNotBlank()) { "Server URL is required" }
        require(!trimmed.startsWith("http://", ignoreCase = true)) {
            HTTPS_REQUIRED_MESSAGE
        }
        val absoluteUrl = when {
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            trimmed.contains("://") -> throw IllegalArgumentException("Server address must use HTTPS")
            else -> "https://$trimmed"
        }

        val url = absoluteUrl.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Enter a valid HTTPS server address")
        require(url.isHttps) { HTTPS_REQUIRED_MESSAGE }
        require(url.username.isEmpty() && url.password.isEmpty()) {
            "Do not include credentials in the server address"
        }
        require(url.encodedQuery == null && url.encodedFragment == null) {
            "Server address must not contain a query or fragment"
        }
        return url.toString().trimEnd('/')
    }

    fun isHttps(input: String): Boolean =
        runCatching { normalize(input) }.isSuccess
}
