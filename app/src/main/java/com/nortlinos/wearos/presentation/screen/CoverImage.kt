package com.nortlinos.wearos.presentation.screen

import android.content.Context
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nortlinos.wearos.data.api.ApiClient
import okhttp3.Headers
import java.io.File

/**
 * Renders a book's cover art via Coil, attaching the Audiobookshelf auth header so the
 * server-protected image endpoint can be fetched directly.
 *
 * [contentDescription] defaults to null because covers sit next to the book's title almost
 * everywhere; announcing "Book cover" on every row only slows TalkBack users down.
 */
@Composable
fun CoverImage(
    baseUrl: String?,
    coverPath: String?,
    authToken: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val request = remember(context, baseUrl, coverPath, authToken) {
        coverRequest(context, baseUrl, coverPath, authToken)
    } ?: return

    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(6.dp))
    )
}

/**
 * Darkened, blurred backdrop for the player. Callers omit it entirely in ambient mode.
 *
 * The [ImageRequest] is remembered so the surrounding per-second position updates cannot rebuild
 * it (and re-enter Coil) on every recomposition. The bitmap is decoded at a small fixed size:
 * it is blurred to mush anyway, so a full-screen decode would only cost memory and CPU.
 */
@Composable
fun BlurredCoverBackground(
    baseUrl: String?,
    coverPath: String?,
    authToken: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val request = remember(context, baseUrl, coverPath, authToken) {
        coverRequest(context, baseUrl, coverPath, authToken, decodeSizePx = BACKDROP_DECODE_PX)
    } ?: return

    AsyncImage(
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .graphicsLayer(scaleX = 1.18f, scaleY = 1.18f)
            .blur(24.dp)
    )
}

internal fun coverRequest(
    context: Context,
    baseUrl: String?,
    coverPath: String?,
    authToken: String?,
    decodeSizePx: Int? = null
): ImageRequest? {
    if (coverPath.isNullOrEmpty()) return null
    val localFile = File(coverPath)
    val data: Any = if (localFile.isAbsolute && localFile.exists()) {
        localFile
    } else {
        if (baseUrl.isNullOrEmpty()) return null
        ApiClient.sizedCoverUrl(ApiClient.resolveUrl(baseUrl, coverPath), COVER_REQUEST_WIDTH_PX)
    }
    val headers = ApiClient.authHeader(authToken)?.let { (name, value) ->
        Headers.Builder().add(name, value).build()
    } ?: Headers.Builder().build()
    return ImageRequest.Builder(context)
        .data(data)
        .headers(headers)
        .crossfade(false)
        .apply { decodeSizePx?.let { size(it) } }
        .build()
}

/**
 * Width asked of the server's cover resizer. Audiobookshelf otherwise returns a 400 px WebP,
 * several times larger than any cover this app shows (the biggest is 72 dp); one shared size
 * keeps a single cached copy per book for rows, the detail page, and the player backdrop.
 */
private const val COVER_REQUEST_WIDTH_PX = 160
private const val BACKDROP_DECODE_PX = 96
