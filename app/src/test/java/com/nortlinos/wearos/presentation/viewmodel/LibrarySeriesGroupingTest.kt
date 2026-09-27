package com.nortlinos.wearos.presentation.viewmodel

import com.nortlinos.wearos.data.local.LibraryItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySeriesGroupingTest {
    @Test
    fun groupsBySeriesNameAndOrdersBySequence() {
        val groups = LibrarySeriesGrouping.group(
            listOf(
                book("dune-2", "Dune Messiah", "Dune #2"),
                book("standalone", "Standalone", null),
                book("dune-1", "Dune", "Dune #1"),
                book("other", "Other", "Another series")
            )
        )

        assertEquals(listOf("Another series", "Dune"), groups.map { it.name })
        assertEquals(listOf("dune-1", "dune-2"), groups.last().books.map { it.id })
        assertTrue(groups.none { group -> group.books.any { it.id == "standalone" } })
    }

    @Test
    fun groupingIgnoresCaseAndSupportsDecimalSequences() {
        val groups = LibrarySeriesGrouping.group(
            listOf(
                book("second", "Second", "Saga #2"),
                book("one-half", "Novella", "saga #1.5"),
                book("first", "First", "Saga #1")
            )
        )

        assertEquals(1, groups.size)
        assertEquals(listOf("first", "one-half", "second"), groups.single().books.map { it.id })
    }

    private fun book(id: String, title: String, series: String?) = LibraryItemEntity(
        id = id,
        originServerUrl = "https://abs.example",
        libraryId = "library",
        mediaType = "book",
        title = title,
        author = null,
        series = series,
        narrator = null,
        description = null,
        coverPath = null,
        localCoverPath = null,
        durationMs = 0,
        updatedAt = 0
    )
}
