package com.nortlinos.wearos.presentation.screen

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Converts the small HTML subset Audiobookshelf descriptions use (paragraphs, line breaks,
 * bold/italic/underline, lists, headings, links, entities) into an [AnnotatedString]. Plain text
 * passes through untouched; unknown tags are dropped but their text is kept. Scripts and styles
 * are never rendered.
 */
internal object HtmlText {
    private val TAG_OR_ENTITY = Regex("""</?[a-zA-Z][^>]*>|<!--|&(#\d+|#[xX][0-9a-fA-F]+|[a-zA-Z]+);""")
    private val TOKEN = Regex("""<!--.*?-->|<(/?)([a-zA-Z][a-zA-Z0-9]*)[^>]*?(/?)>|[^<]+|<""", RegexOption.DOT_MATCHES_ALL)
    private val BLOCK = setOf("p", "div", "ul", "ol", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "table", "tr")
    private val HEADING = setOf("h1", "h2", "h3", "h4", "h5", "h6")
    private val ENTITIES = mapOf(
        "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'", "nbsp" to " ",
        "ndash" to "\u2013", "mdash" to "\u2014", "hellip" to "\u2026", "lsquo" to "\u2018",
        "rsquo" to "\u2019", "ldquo" to "\u201C", "rdquo" to "\u201D", "copy" to "\u00A9",
        "bull" to "\u2022", "middot" to "\u00B7"
    )

    /** True when [text] contains markup or entities worth decoding. */
    fun looksLikeHtml(text: String): Boolean = TAG_OR_ENTITY.containsMatchIn(text)

    fun toAnnotated(text: String): AnnotatedString =
        if (!looksLikeHtml(text)) AnnotatedString(text) else parse(text)

    fun decodeEntities(text: String): String {
        if ('&' !in text) return text
        return Regex("""&(#\d+|#[xX][0-9a-fA-F]+|[a-zA-Z]+);""").replace(text) { m ->
            val name = m.groupValues[1]
            when {
                name.startsWith("#x") || name.startsWith("#X") ->
                    codePoint(name.drop(2).toIntOrNull(16))
                name.startsWith("#") -> codePoint(name.drop(1).toIntOrNull())
                else -> ENTITIES[name]
            } ?: m.value
        }
    }

    private fun codePoint(value: Int?): String? =
        value?.takeIf { it in 1..0x10FFFF && it !in 0xD800..0xDFFF }
            ?.let { String(Character.toChars(it)) }

    private fun parse(html: String): AnnotatedString {
        val sb = StringBuilder()
        // (start offset, style) for each currently open inline tag, keyed by tag name.
        val open = ArrayList<Pair<String, Pair<Int, SpanStyle>>>()
        val spans = ArrayList<Triple<Int, Int, SpanStyle>>()
        val listStack = ArrayList<Pair<Boolean, IntArray>>() // (ordered, counter)
        var skipDepth = 0
        var skipTag = ""

        fun newline(count: Int) {
            if (sb.isEmpty()) return
            var trailing = 0
            while (trailing < sb.length && sb[sb.length - 1 - trailing] == '\n') trailing++
            repeat((count - trailing).coerceAtLeast(0)) { sb.append('\n') }
        }

        fun styleFor(tag: String): SpanStyle? = when (tag) {
            "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
            "i", "em", "cite", "dfn" -> SpanStyle(fontStyle = FontStyle.Italic)
            "u", "ins", "a" -> SpanStyle(textDecoration = TextDecoration.Underline)
            "s", "strike", "del" -> SpanStyle(textDecoration = TextDecoration.LineThrough)
            in HEADING -> SpanStyle(fontWeight = FontWeight.Bold)
            else -> null
        }

        for (m in TOKEN.findAll(html)) {
            val token = m.value
            if (token.startsWith("<!--")) continue
            val tag = m.groups[2]?.value?.lowercase()
            if (tag == null) {
                if (skipDepth > 0 || token == "<") {
                    if (skipDepth == 0) sb.append(token)
                    continue
                }
                val decoded = decodeEntities(token).replace(Regex("""\s+"""), " ")
                val collapsed = if (sb.isEmpty() || sb.last() == '\n') decoded.trimStart() else decoded
                sb.append(collapsed)
                continue
            }
            val closing = m.groups[1]?.value == "/"
            if (skipDepth > 0) {
                if (tag == skipTag) skipDepth += if (closing) -1 else 1
                continue
            }
            if (!closing && (tag == "script" || tag == "style") && m.groups[3]?.value != "/") {
                skipTag = tag
                skipDepth = 1
                continue
            }
            when {
                tag == "br" -> sb.append('\n')
                tag == "hr" -> newline(1)
                tag == "li" -> if (!closing) {
                    newline(1)
                    val list = listStack.lastOrNull()
                    if (list?.first == true) {
                        list.second[0]++
                        sb.append("${list.second[0]}. ")
                    } else {
                        sb.append("\u2022 ")
                    }
                } else newline(1)
                tag == "ul" || tag == "ol" -> if (!closing) {
                    newline(if (listStack.isEmpty()) 2 else 1)
                    listStack.add((tag == "ol") to intArrayOf(0))
                } else {
                    listStack.removeLastOrNull()
                    newline(if (listStack.isEmpty()) 2 else 1)
                }
                tag in BLOCK -> newline(if (tag == "tr") 1 else 2)
                else -> Unit
            }
            val style = styleFor(tag)
            if (style != null) {
                if (!closing && m.groups[3]?.value != "/") {
                    open.add(tag to (sb.length to style))
                } else if (closing) {
                    val idx = open.indexOfLast { it.first == tag }
                    if (idx >= 0) {
                        val (start, s) = open.removeAt(idx).second
                        if (sb.length > start) spans.add(Triple(start, sb.length, s))
                    }
                }
            }
        }

        val trimmed = sb.toString().trimEnd('\n', ' ')
        val end = trimmed.length
        return buildAnnotatedString {
            append(trimmed)
            spans.forEach { (s, e, style) ->
                val clipped = e.coerceAtMost(end)
                if (s < clipped) addStyle(style, s, clipped)
            }
            open.forEach { (_, p) -> if (p.first < end) addStyle(p.second, p.first, end) }
        }
    }
}

/** Whether descriptions are rendered as formatted HTML; provided from the Settings toggle. */
val LocalFormatDescriptions = compositionLocalOf { true }
