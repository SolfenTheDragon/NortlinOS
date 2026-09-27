package com.nortlinos.wearos.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class ServerIdentityTest {
    @Test
    fun `normalization removes whitespace and trailing slashes`() {
        assertEquals(
            "https://books.example/base",
            ServerIdentity.normalize("  https://books.example/base///  ")
        )
    }

    @Test
    fun `different servers retain different identities`() {
        val first = ServerIdentity.normalize("https://one.example")
        val second = ServerIdentity.normalize("https://two.example")

        assert(first != second)
    }

    @Test
    fun `matching ignores only trailing slash differences`() {
        assert(ServerIdentity.matches("https://books.example/", "https://books.example"))
        assert(!ServerIdentity.matches("https://one.example", "https://two.example"))
    }
}
