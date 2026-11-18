package com.nortlinos.wearos.data.repository

import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessTokenExpiryTest {
    private fun jwt(payload: String): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val header = encoder.encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray())
        val body = encoder.encodeToString(payload.toByteArray())
        return "$header.$body.signature"
    }

    @Test
    fun readsExpClaimAsMilliseconds() {
        val token = jwt("""{"userId":"u1","type":"access","exp":1700000000}""")
        assertEquals(1_700_000_000_000L, AccessTokenExpiry.expiresAtMs(token))
    }

    @Test
    fun legacyOpaqueTokenNeverNeedsRefresh() {
        assertNull(AccessTokenExpiry.expiresAtMs("not-a-jwt"))
        assertFalse(AccessTokenExpiry.needsRefresh("not-a-jwt", Long.MAX_VALUE))
    }

    @Test
    fun tokenWithoutExpNeverNeedsRefresh() {
        // Pre-2.26 servers and API keys sign JWTs with no expiry.
        val token = jwt("""{"userId":"u1","username":"me"}""")
        assertNull(AccessTokenExpiry.expiresAtMs(token))
        assertFalse(AccessTokenExpiry.needsRefresh(token, Long.MAX_VALUE))
    }

    @Test
    fun malformedPayloadIsIgnored() {
        assertNull(AccessTokenExpiry.expiresAtMs("a.%%%.c"))
    }

    @Test
    fun refreshesWithinMarginBeforeExpiry() {
        val expSeconds = 1_700_000_000L
        val token = jwt("""{"exp":$expSeconds}""")
        val expiresAt = expSeconds * 1000
        val margin = AccessTokenExpiry.REFRESH_MARGIN_MS
        assertFalse(AccessTokenExpiry.needsRefresh(token, expiresAt - margin - 1))
        assertTrue(AccessTokenExpiry.needsRefresh(token, expiresAt - margin))
        assertTrue(AccessTokenExpiry.needsRefresh(token, expiresAt + 1))
    }
}
