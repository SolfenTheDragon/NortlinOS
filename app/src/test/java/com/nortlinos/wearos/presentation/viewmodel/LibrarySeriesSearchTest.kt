package com.nortlinos.wearos.presentation.viewmodel

import com.nortlinos.wearos.data.local.LibraryItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySeriesSearchTest {
    private val series = listOf(
        LibrarySeries("Dune", listOf(book("dune-1", "Dune", "Frank Herbert", "Scott Brick"))),
        LibrarySeries("Discworld", listOf(book("gg", "Going Postal", "Terry Pratchett", "Stephen Briggs")))
    )

    @Test
    fun blankQueryReturnsEverythingUnchanged() {
        assertSame(series, LibrarySeriesSearch.filter(series, "   "))
    }

    @Test
    fun matchesSeriesNameIgnoringCase() {
        assertEquals(listOf("Discworld"), LibrarySeriesSearch.filter(series, "disc").map { it.name })
    }

    @Test
    fun matchesBookTitleAuthorAndNarrator() {
        assertEquals(listOf("Discworld"), LibrarySeriesSearch.filter(series, "going postal").map { it.name })
        assertEquals(listOf("Dune"), LibrarySeriesSearch.filter(series, "herbert").map { it.name })
        assertEquals(listOf("Dune"), LibrarySeriesSearch.filter(series, "scott").map { it.name })
        assertTrue(LibrarySeriesSearch.filter(series, "nonexistent").isEmpty())
    }

    private fun book(id: String, title: String, author: String?, narrator: String?) = LibraryItemEntity(
        id = id,
        originServerUrl = "https://abs.example",
        libraryId = "library",
        mediaType = "book",
        title = title,
        author = author,
        series = null,
        narrator = narrator,
        description = null,
        coverPath = null,
        localCoverPath = null,
        durationMs = 0,
        updatedAt = 0
    )
}
