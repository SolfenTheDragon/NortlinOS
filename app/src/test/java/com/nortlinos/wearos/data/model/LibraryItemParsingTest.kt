package com.nortlinos.wearos.data.model

import com.google.gson.Gson
import com.nortlinos.wearos.data.api.MediaProgressDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parses payloads shaped like the two Audiobookshelf responses the app actually consumes.
 *
 * Library pages return *minified* items (`toOldJSONMinified`): flattened `authorName` /
 * `narratorName` / `seriesName` strings, and no `tracks` or `chapters` keys at all. Item detail
 * and search return *expanded* items, which additionally carry `narrators` as an array of plain
 * strings and `series` as an array of objects. Gson instantiates Kotlin classes without running
 * their constructors, so any absent key becomes null regardless of the declared default — these
 * tests pin both shapes so that stays visible.
 */
class LibraryItemParsingTest {
    private val gson = Gson()

    private val minifiedItem = """
        {
          "id": "li_hobbit",
          "libraryId": "lib_main",
          "folderId": "fol_1",
          "mediaType": "book",
          "media": {
            "metadata": {
              "title": "The Hobbit",
              "titleIgnorePrefix": "Hobbit, The",
              "subtitle": null,
              "authorName": "J. R. R. Tolkien",
              "narratorName": "Rob Inglis",
              "seriesName": "Middle-earth #1",
              "genres": ["Fantasy"],
              "publishedYear": "1937",
              "description": "A hobbit goes there and back again.",
              "explicit": false
            },
            "coverPath": "/metadata/items/li_hobbit/cover.jpg",
            "tags": [],
            "numTracks": 2,
            "numChapters": 3,
            "duration": 39600.5,
            "size": 123456789
          },
          "updatedAt": 1700000000000
        }
    """.trimIndent()

    private val expandedItem = """
        {
          "id": "li_hobbit",
          "libraryId": "lib_main",
          "mediaType": "book",
          "media": {
            "metadata": {
              "title": "The Hobbit",
              "authors": [{"id": "aut_1", "name": "J. R. R. Tolkien"}],
              "narrators": ["Rob Inglis", "Andy Serkis"],
              "series": [{"id": "ser_1", "name": "Middle-earth", "sequence": "1"}],
              "description": "A hobbit goes there and back again.",
              "authorName": "J. R. R. Tolkien",
              "narratorName": "Rob Inglis, Andy Serkis",
              "seriesName": "Middle-earth #1"
            },
            "coverPath": "/metadata/items/li_hobbit/cover.jpg",
            "duration": 39600.5,
            "tracks": [
              {
                "index": 1,
                "startOffset": 0,
                "duration": 19800.25,
                "title": "part1.m4b",
                "contentUrl": "/s/item/li_hobbit/part1.m4b",
                "mimeType": "audio/mp4",
                "metadata": {"size": 61728394}
              }
            ],
            "chapters": [
              {"id": 0, "start": 0, "end": 1200.5, "title": "An Unexpected Party"},
              {"id": 1, "start": 1200.5, "end": 2400, "title": "Roast Mutton"}
            ]
          },
          "updatedAt": 1700000000000
        }
    """.trimIndent()

    @Test
    fun `minified library item parses`() {
        val item = gson.fromJson(minifiedItem, LibraryItem::class.java)

        assertEquals("li_hobbit", item.id)
        assertEquals("lib_main", item.libraryId)
        assertEquals("The Hobbit", item.media.metadata.title)
        assertEquals("J. R. R. Tolkien", item.media.metadata.authorName)
        assertEquals("Rob Inglis", item.media.metadata.narratorName)
        assertEquals("Middle-earth #1", item.media.metadata.seriesName)
        assertEquals(39600.5, item.media.duration!!, 0.001)
    }

    @Test
    fun `minified library item reports no chapters instead of throwing`() {
        val item = gson.fromJson(minifiedItem, LibraryItem::class.java)

        // The key is absent, so Gson leaves the field null despite the Kotlin default. Callers
        // must go through the accessor rather than touching the raw field.
        assertTrue(item.media.chapterList.isEmpty())
        assertTrue(item.media.trackList.isEmpty())
        assertTrue(item.media.episodeList.isEmpty())
    }

    @Test
    fun `expanded library item parses narrators as plain strings`() {
        val item = gson.fromJson(expandedItem, LibraryItem::class.java)

        assertEquals(listOf("Rob Inglis", "Andy Serkis"), item.media.metadata.narrators)
    }

    @Test
    fun `expanded library item parses series as objects`() {
        val item = gson.fromJson(expandedItem, LibraryItem::class.java)

        val series = item.media.metadata.series
        assertNotNull(series)
        assertEquals(1, series!!.size)
        assertEquals("Middle-earth", series[0].name)
        assertEquals("1", series[0].sequence)
    }

    @Test
    fun `expanded library item exposes chapters and tracks`() {
        val item = gson.fromJson(expandedItem, LibraryItem::class.java)

        assertEquals(2, item.media.chapterList.size)
        assertEquals("An Unexpected Party", item.media.chapterList[0].title)
        assertEquals(1200.5, item.media.chapterList[0].end, 0.001)
        assertEquals(1, item.media.trackList.size)
        assertEquals("/s/item/li_hobbit/part1.m4b", item.media.trackList[0].contentUrl)
    }

    @Test
    fun `expanded podcast item parses episodes and podcast author`() {
        val podcast = gson.fromJson(
            """
                {
                  "id": "li_podcast",
                  "libraryId": "lib_podcasts",
                  "mediaType": "podcast",
                  "media": {
                    "metadata": {
                      "title": "Example Show",
                      "author": "Example Publisher",
                      "description": "A show description."
                    },
                    "coverPath": "/metadata/items/li_podcast/cover.jpg",
                    "episodes": [
                      {
                        "id": "ep_1",
                        "title": "Episode One",
                        "subtitle": "An introduction",
                        "publishedAt": 1700000000000,
                        "audioFile": {
                          "duration": 123.5,
                          "metadata": {"size": 987654}
                        }
                      }
                    ]
                  },
                  "updatedAt": 1700000000000
                }
            """.trimIndent(),
            LibraryItem::class.java
        )

        assertEquals("Example Publisher", podcast.media.metadata.displayAuthor)
        assertEquals(1, podcast.media.episodeList.size)
        assertEquals("ep_1", podcast.media.episodeList.single().id)
        assertEquals("Episode One", podcast.media.episodeList.single().title)
        assertEquals(123.5, podcast.media.episodeList.single().audioFile?.duration ?: 0.0, 0.001)
        assertEquals(987654L, podcast.media.episodeList.single().audioFile?.metadata?.size)
    }

    @Test
    fun `podcast enclosure length parses as an estimated size`() {
        val enclosure = gson.fromJson("""{"length":"12345"}""", PodcastEnclosure::class.java)

        assertEquals(12345L, enclosure.sizeBytes)
    }

    @Test
    fun `podcast progress preserves its episode identity`() {
        val progress = gson.fromJson(
            """{"libraryItemId":"li_podcast","episodeId":"ep_1","currentTime":42.5,"duration":123.5,"lastUpdate":1700000000000}""",
            MediaProgressDto::class.java
        )

        assertEquals("li_podcast", progress.libraryItemId)
        assertEquals("ep_1", progress.episodeId)
        assertEquals(42.5, progress.currentTime, 0.001)
    }

    @Test
    fun `empty series array does not throw`() {
        val json = minifiedItem.replace(
            "\"seriesName\": \"Middle-earth #1\"",
            "\"series\": [], \"narrators\": []"
        )

        val item = gson.fromJson(json, LibraryItem::class.java)

        assertNull(item.media.metadata.displaySeries)
        assertEquals("Rob Inglis", item.media.metadata.displayNarrator)
    }

    @Test
    fun `display series prefers the flattened name and falls back to the array`() {
        val minified = gson.fromJson(minifiedItem, LibraryItem::class.java)
        assertEquals("Middle-earth #1", minified.media.metadata.displaySeries)

        val arrayOnly = gson.fromJson(
            expandedItem.replace("\"seriesName\": \"Middle-earth #1\"", "\"seriesName\": null"),
            LibraryItem::class.java
        )
        assertEquals("Middle-earth #1", arrayOnly.media.metadata.displaySeries)
    }

    @Test
    fun `display narrator joins the expanded string array`() {
        val arrayOnly = gson.fromJson(
            expandedItem.replace(
                "\"narratorName\": \"Rob Inglis, Andy Serkis\"",
                "\"narratorName\": null"
            ),
            LibraryItem::class.java
        )

        assertEquals("Rob Inglis, Andy Serkis", arrayOnly.media.metadata.displayNarrator)
    }

    @Test
    fun `item without metadata title still parses`() {
        val json = minifiedItem.replace("\"title\": \"The Hobbit\"", "\"title\": null")

        val item = gson.fromJson(json, LibraryItem::class.java)

        assertNull(item.media.metadata.title)
    }
}
