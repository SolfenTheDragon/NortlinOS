package com.nortlinos.wearos.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecureServerUrlTest {
    @Test
    fun `adds HTTPS when scheme is omitted`() {
        assertEquals(
            "https://books.example:13378",
            SecureServerUrl.normalize(" books.example:13378/ ")
        )
    }

    @Test
    fun `preserves HTTPS server paths`() {
        assertEquals(
            "https://books.example/audiobookshelf",
            SecureServerUrl.normalize("https://books.example/audiobookshelf/")
        )
    }

    @Test
    fun `rejects HTTP and other schemes`() {
        listOf("http://books.example", "ftp://books.example").forEach { address ->
            try {
                SecureServerUrl.normalize(address, allowHttp = false)
                throw AssertionError("Expected $address to be rejected")
            } catch (expected: IllegalArgumentException) {
                assertTrue(expected.message.orEmpty().contains("HTTPS"))
            }
        }
    }

    @Test
    fun `rejects embedded credentials query and fragment`() {
        listOf(
            "https://user:password@books.example",
            "https://books.example?token=secret",
            "https://books.example#private"
        ).forEach { address ->
            try {
                SecureServerUrl.normalize(address)
                throw AssertionError("Expected address to be rejected")
            } catch (_: IllegalArgumentException) {
                // Invalid URL components are not accepted.
            }
        }
    }

    @Test
    fun `identifies insecure legacy sessions`() {
        assertTrue(SecureServerUrl.isHttps("https://books.example"))
        assertFalse(SecureServerUrl.isHttps("http://books.example", allowHttp = false))
    }

    @Test
    fun `accepts HTTP when insecure servers are allowed`() {
        assertEquals(
            "http://192.168.1.5:13378",
            SecureServerUrl.normalize("http://192.168.1.5:13378/", allowHttp = true)
        )
        assertTrue(SecureServerUrl.isHttps("http://books.example", allowHttp = true))
    }
}
