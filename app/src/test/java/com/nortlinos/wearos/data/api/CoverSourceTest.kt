package com.nortlinos.wearos.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoverSourceTest {
    @Test
    fun `remote cover path maps to the item cover endpoint`() {
        // The value the server sends is a path inside its own filesystem, never a fetchable URL.
        assertEquals(
            "/api/items/li_1/cover",
            ApiClient.coverSource(null, "/metadata/items/li_1/cover.jpg", "li_1")
        )
    }

    @Test
    fun `downloaded cover is used as-is`() {
        assertEquals(
            "/data/user/0/app/files/covers/li_1.jpg",
            ApiClient.coverSource("/data/user/0/app/files/covers/li_1.jpg", null, "li_1")
        )
    }

    @Test
    fun `local cover wins over the server copy`() {
        assertEquals(
            "/local/li_1.jpg",
            ApiClient.coverSource("/local/li_1.jpg", "/metadata/items/li_1/cover.jpg", "li_1")
        )
    }

    @Test
    fun `no cover yields null so nothing is rendered`() {
        assertNull(ApiClient.coverSource(null, null, "li_1"))
        assertNull(ApiClient.coverSource("", "", "li_1"))
    }

    @Test
    fun `endpoint is resolved against the server base url`() {
        assertEquals(
            "https://abs.example.com/api/items/li_1/cover",
            ApiClient.resolveUrl("https://abs.example.com/", ApiClient.coverEndpoint("li_1"))
        )
    }

    @Test
    fun `cover endpoint gets a resize width`() {
        assertEquals(
            "https://abs.example/api/items/li_1/cover?width=160",
            ApiClient.sizedCoverUrl("https://abs.example/api/items/li_1/cover", 160)
        )
    }

    @Test
    fun `non-cover and already-queried urls are untouched`() {
        assertEquals(
            "https://abs.example/api/items/li_1/cover?width=80",
            ApiClient.sizedCoverUrl("https://abs.example/api/items/li_1/cover?width=80", 160)
        )
        assertEquals(
            "https://abs.example/img/other.png",
            ApiClient.sizedCoverUrl("https://abs.example/img/other.png", 160)
        )
    }
}
