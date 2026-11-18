package com.nortlinos.wearos.data.api

import com.google.gson.Gson
import com.nortlinos.wearos.data.model.LoginResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Payloads shaped like the Audiobookshelf 2.26–2.38 auth and `/api/me/*` responses. */
class ModernEndpointParsingTest {
    private val gson = Gson()

    @Test
    fun loginWithReturnTokensCarriesRefreshToken() {
        val json = """
            {"user":{"id":"u1","username":"me","token":"legacy","accessToken":"acc",
            "refreshToken":"ref"},"userDefaultLibraryId":"lib1","serverSettings":{}}
        """
        val user = gson.fromJson(json, LoginResponse::class.java).user
        assertEquals("acc", user.bearerToken)
        assertEquals("ref", user.refreshToken)
    }

    @Test
    fun parsesAllProgress() {
        val json = """
            {"mediaProgress":[
              {"libraryItemId":"li1","episodeId":null,"currentTime":12.5,"duration":100.0,
               "lastUpdate":1700000000000},
              {"libraryItemId":"pod1","episodeId":"ep1","currentTime":3.0,"duration":60.0,
               "lastUpdate":1700000001000}
            ]}
        """
        val progress = gson.fromJson(json, AllProgressResponse::class.java).mediaProgress
        assertEquals(2, progress.size)
        assertEquals("ep1", progress[1].episodeId)
        assertEquals(1_700_000_001_000L, progress[1].effectiveUpdatedAt)
    }

    @Test
    fun parsesItemBookmarks() {
        val json = """
            {"bookmarks":[{"libraryItemId":"li1","title":"Chapter 3","time":615,
             "createdAt":1700000000000}]}
        """
        val bookmarks = gson.fromJson(json, BookmarksResponse::class.java).bookmarks
        assertEquals(615, bookmarks.single().time)
    }

    @Test
    fun playbackRequestSendsDeviceInfo() {
        val request = PlaybackRequest(
            deviceInfo = DeviceInfoDto("dev-1", "NortlinOS", "1.0", "Google", "Pixel Watch 4", 36)
        )
        val json = gson.toJson(request)
        assertTrue(json.contains("\"deviceId\":\"dev-1\""))
        assertTrue(json.contains("\"clientName\":\"NortlinOS\""))
        assertTrue(json.contains("\"mediaPlayer\":\"wearos-app\""))
    }
}
