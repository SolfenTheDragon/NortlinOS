package com.nortlinos.wearos.presentation.screen

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryPagingTest {
    @Test
    fun `prefetch starts six books before end`() {
        assertFalse(
            LibraryPaging.shouldLoadNext(33, 40, hasMore = true, loading = false, searching = false)
        )
        assertTrue(
            LibraryPaging.shouldLoadNext(34, 40, hasMore = true, loading = false, searching = false)
        )
    }

    @Test
    fun `prefetch is disabled while loading or searching`() {
        assertFalse(
            LibraryPaging.shouldLoadNext(39, 40, hasMore = true, loading = true, searching = false)
        )
        assertFalse(
            LibraryPaging.shouldLoadNext(39, 40, hasMore = true, loading = false, searching = true)
        )
    }
}
