package com.nortlinos.wearos.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryPageMathTest {
    @Test
    fun `full page requests another page`() {
        assertTrue(LibraryPageMath.hasMore(resultCount = 40, limit = 40))
    }

    @Test
    fun `partial page is the end`() {
        assertFalse(LibraryPageMath.hasMore(resultCount = 38, limit = 40))
    }

    @Test
    fun `empty page is the end`() {
        assertFalse(LibraryPageMath.hasMore(resultCount = 0, limit = 40))
    }
}
