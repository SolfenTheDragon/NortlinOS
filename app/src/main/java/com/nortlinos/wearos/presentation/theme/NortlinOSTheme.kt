package com.nortlinos.wearos.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import com.nortlinos.wearos.data.local.AccentTheme

/**
 * Amber-and-teal scheme on a pure black background. Wear OS screens are OLED, so black is free
 * and every lit container costs power; containers stay dark and saturated colors are reserved for
 * the primary action and state accents.
 */
private val BaseColorScheme = ColorScheme(
    primary = Color(0xFFF4A340),
    primaryDim = Color(0xFFC87616),
    primaryContainer = Color(0xFF5A3A0E),
    onPrimary = Color.Black,
    onPrimaryContainer = Color(0xFFFFDDB5),
    secondary = Color(0xFF65D6C4),
    secondaryDim = Color(0xFF279889),
    secondaryContainer = Color(0xFF1F3B37),
    onSecondary = Color.Black,
    onSecondaryContainer = Color(0xFFCDEFE9),
    tertiary = Color(0xFFB9C8F5),
    tertiaryDim = Color(0xFF8D9BC6),
    tertiaryContainer = Color(0xFF2E3650),
    onTertiary = Color.Black,
    onTertiaryContainer = Color(0xFFDDE4FF),
    surfaceContainerLow = Color(0xFF161412),
    surfaceContainer = Color(0xFF211F1C),
    surfaceContainerHigh = Color(0xFF2E2B27),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFD4C8BA),
    outline = Color(0xFF9C8F80),
    outlineVariant = Color(0xFF4F463C),
    background = Color.Black,
    onBackground = Color.White,
    error = Color(0xFFFF8A80),
    errorDim = Color(0xFFE06C62),
    errorContainer = Color(0xFF5C1A15),
    onError = Color.Black,
    onErrorContainer = Color(0xFFFFDAD5)
)

/** Accent colors as opaque ARGB values. [primary] is also the color of primary buttons. */
class AccentPalette(
    val primary: Long, val dim: Long, val container: Long, val onContainer: Long
)

private val PresetPalettes = mapOf(
    AccentTheme.AMBER to AccentPalette(0xFFF4A340, 0xFFC87616, 0xFF5A3A0E, 0xFFFFDDB5),
    AccentTheme.TEAL to AccentPalette(0xFF65D6C4, 0xFF279889, 0xFF1F3B37, 0xFFCDEFE9),
    AccentTheme.BLUE to AccentPalette(0xFF8AB4F8, 0xFF5B86CC, 0xFF1F3354, 0xFFD6E4FF),
    AccentTheme.GREEN to AccentPalette(0xFF7FD67F, 0xFF4BA04B, 0xFF1F3F1F, 0xFFD2F2D2),
    AccentTheme.PURPLE to AccentPalette(0xFFC9A0F5, 0xFF9A6CC8, 0xFF3D2B54, 0xFFEBDAFF),
    AccentTheme.ROSE to AccentPalette(0xFFF58FB0, 0xFFC85F82, 0xFF54243A, 0xFFFFD8E6)
)

object AccentColors {
    private const val MIN_LUMINANCE = 0.35
    private val named = mapOf(
        "black" to 0x000000L, "white" to 0xFFFFFFL, "red" to 0xFF0000L, "lime" to 0x00FF00L,
        "blue" to 0x0000FFL, "yellow" to 0xFFFF00L, "cyan" to 0x00FFFFL, "aqua" to 0x00FFFFL,
        "magenta" to 0xFF00FFL, "fuchsia" to 0xFF00FFL, "gray" to 0x808080L, "grey" to 0x808080L,
        "silver" to 0xC0C0C0L, "maroon" to 0x800000L, "olive" to 0x808000L, "green" to 0x008000L,
        "purple" to 0x800080L, "teal" to 0x008080L, "navy" to 0x000080L, "orange" to 0xFFA500L,
        "pink" to 0xFFC0CBL, "gold" to 0xFFD700L, "coral" to 0xFF7F50L, "crimson" to 0xDC143CL,
        "violet" to 0xEE82EEL, "indigo" to 0x4B0082L, "turquoise" to 0x40E0D0L,
        "salmon" to 0xFA8072L, "tomato" to 0xFF6347L, "skyblue" to 0x87CEEBL
    )

    /**
     * Parses an HTML color (`#RGB`, `#RRGGBB`, the same without `#`, or a common CSS color name)
     * into opaque ARGB, or null when it is not a valid color.
     */
    fun parse(input: String): Long? {
        val text = input.trim().lowercase()
        named[text]?.let { return 0xFF000000L or it }
        val hex = text.removePrefix("#")
        if (hex.any { it !in '0'..'9' && it !in 'a'..'f' }) return null
        val rgb = when (hex.length) {
            3 -> hex.map { "$it$it" }.joinToString("")
            6 -> hex
            else -> return null
        }
        return 0xFF000000L or rgb.toLong(16)
    }

    fun format(argb: Long): String = "#%06X".format(argb and 0xFFFFFF)

    /** Builds a full palette from one color, lifting colors too dark to read on black. */
    fun fromColor(argb: Long): AccentPalette {
        var primary = argb or 0xFF000000L
        if (luminance(primary) < MIN_LUMINANCE) {
            var amount = 0.1f
            while (amount < 1f && luminance(mix(argb, 0xFFFFFFFFL, amount)) < MIN_LUMINANCE) amount += 0.1f
            primary = mix(argb, 0xFFFFFFFFL, amount.coerceAtMost(1f))
        }
        return AccentPalette(
            primary = primary,
            dim = mix(primary, 0xFF000000L, 0.22f),
            container = mix(primary, 0xFF000000L, 0.7f),
            onContainer = mix(primary, 0xFFFFFFFFL, 0.8f)
        )
    }

    fun palette(theme: AccentTheme, custom: Long?): AccentPalette = when {
        theme == AccentTheme.CUSTOM && custom != null -> fromColor(custom)
        else -> PresetPalettes[theme] ?: PresetPalettes.getValue(AccentTheme.AMBER)
    }

    private fun channel(argb: Long, shift: Int) = ((argb shr shift) and 0xFF).toInt()

    private fun mix(from: Long, to: Long, amount: Float): Long {
        fun blend(shift: Int): Long =
            (channel(from, shift) + (channel(to, shift) - channel(from, shift)) * amount)
                .toInt().coerceIn(0, 255).toLong() shl shift
        return 0xFF000000L or blend(16) or blend(8) or blend(0)
    }

    private fun luminance(argb: Long): Double {
        fun lin(shift: Int): Double {
            val c = channel(argb, shift) / 255.0
            return if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * lin(16) + 0.7152 * lin(8) + 0.0722 * lin(0)
    }
}

/**
 * App-wide Wear Material 3 theme wrapper. Wrap the app's root composable in this so every
 * screen shares consistent colors, typography, and shapes.
 */
@Composable
fun NortlinOSTheme(
    accent: AccentTheme = AccentTheme.AMBER,
    customColor: Long? = null,
    content: @Composable () -> Unit
) {
    val scheme = remember(accent, customColor) {
        val a = AccentColors.palette(accent, customColor)
        BaseColorScheme.copy(
            primary = Color(a.primary),
            primaryDim = Color(a.dim),
            primaryContainer = Color(a.container),
            onPrimaryContainer = Color(a.onContainer)
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
