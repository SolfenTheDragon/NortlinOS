package com.nortlinos.wearos.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioTrackParsingTest {
    @Test
    fun `track byte size is read from server metadata`() {
        val track = Gson().fromJson(
            """
            {
              "index": 1,
              "startOffset": 0,
              "duration": 60,
              "title": "Track",
              "contentUrl": "/api/items/book/file/1",
              "mimeType": "audio/mpeg",
              "metadata": { "size": 123456 }
            }
            """.trimIndent(),
            AudioTrack::class.java
        )

        assertEquals(123_456L, track.metadata?.size)
    }
}
