package com.nortlinos.wearos.presentation.screen

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlTextTest {
    @Test
    fun `plain text is untouched`() {
        assertFalse(HtmlText.looksLikeHtml("Just a plain description."))
        assertEquals("a < b\nline two", HtmlText.toAnnotated("a < b\nline two").text)
    }

    @Test
    fun `paragraphs and breaks become newlines`() {
        assertEquals("One\n\nTwo\nThree", HtmlText.toAnnotated("<p>One</p><p>Two<br/>Three</p>").text)
    }

    @Test
    fun `bold and italic spans are applied`() {
        val result = HtmlText.toAnnotated("Hi <b>there</b> <i>you</i>")
        assertEquals("Hi there you", result.text)
        val bold = result.spanStyles.single { it.item.fontWeight == FontWeight.Bold }
        assertEquals("there", result.text.substring(bold.start, bold.end))
        assertEquals(2, result.spanStyles.size)
    }

    @Test
    fun `lists render bullets and numbers`() {
        assertEquals("\u2022 a\n\u2022 b", HtmlText.toAnnotated("<ul><li>a</li><li>b</li></ul>").text)
        assertEquals("1. a\n2. b", HtmlText.toAnnotated("<ol><li>a</li><li>b</li></ol>").text)
    }

    @Test
    fun `entities decode and scripts are dropped`() {
        assertEquals("Tom & Jerry \u2014 \u00e9 <3", HtmlText.toAnnotated("Tom &amp; Jerry &mdash; &#233; &lt;3").text)
        assertEquals("ok", HtmlText.toAnnotated("<script>alert(1)</script><p>ok</p>").text)
        assertTrue(HtmlText.looksLikeHtml("fish &amp; chips"))
    }

    @Test
    fun `unclosed and unknown tags do not crash`() {
        assertEquals("text", HtmlText.toAnnotated("<custom>text<b>").text)
    }
}
