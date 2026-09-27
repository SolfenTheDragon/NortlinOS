package com.nortlinos.wearos.tile

import android.graphics.Bitmap
import java.io.File
import kotlin.math.min

/**
 * The tile's copy of the book cover: a centre-cropped square, small enough that sending it inline
 * with every tile layout is cheap (a few KB), well under the renderer's inline image limit.
 */
object TileCover {
    const val SIZE_PX = 128
    private const val JPEG_QUALITY = 80

    /** Writes [source] as a [SIZE_PX] square JPEG, replacing [file] atomically. */
    fun write(file: File, source: Bitmap) {
        val side = min(source.width, source.height)
        val cropped = Bitmap.createBitmap(
            source,
            (source.width - side) / 2,
            (source.height - side) / 2,
            side,
            side
        )
        val scaled = Bitmap.createScaledBitmap(cropped, SIZE_PX, SIZE_PX, true)
        val temp = File(file.parentFile, file.name + ".tmp")
        temp.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        if (!temp.renameTo(file)) {
            temp.delete()
            error("Could not save tile cover")
        }
        if (scaled !== cropped) scaled.recycle()
        if (cropped !== source) cropped.recycle()
    }
}
