package com.nortlinos.wearos.presentation.theme

import com.nortlinos.wearos.data.local.AccentTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccentColorsTest {
    @Test
    fun parsesHexNamesAndShorthand() {
        assertEquals(0xFFFF8800L, AccentColors.parse("#FF8800"))
        assertEquals(0xFFFF8800L, AccentColors.parse(" ff8800 "))
        assertEquals(0xFFFF8800L, AccentColors.parse("#f80"))
        assertEquals(0xFFFFA500L, AccentColors.parse("Orange"))
    }

    @Test
    fun rejectsInvalidColors() {
        assertNull(AccentColors.parse(""))
        assertNull(AccentColors.parse("#12"))
        assertNull(AccentColors.parse("#GG0000"))
        assertNull(AccentColors.parse("notacolor"))
    }

    @Test
    fun formatsUppercaseHash() {
        assertEquals("#00FF7F", AccentColors.format(0xFF00FF7FL))
    }

    @Test
    fun liftsColorsTooDarkForBlackBackground() {
        val palette = AccentColors.fromColor(0xFF000033L)
        assertNotEquals(0xFF000033L, palette.primary)
        assertEquals(0xFFF4A340L, AccentColors.palette(AccentTheme.AMBER, null).primary)
    }

    @Test
    fun brightColorsKeepTheirPrimary() {
        assertEquals(0xFFFF8800L, AccentColors.fromColor(0xFFFF8800L).primary)
    }

    @Test
    fun customWithoutColorFallsBackToAmber() {
        assertEquals(
            AccentColors.palette(AccentTheme.AMBER, null).primary,
            AccentColors.palette(AccentTheme.CUSTOM, null).primary
        )
    }
}
