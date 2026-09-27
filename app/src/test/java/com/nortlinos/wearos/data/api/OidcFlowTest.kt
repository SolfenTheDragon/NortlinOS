package com.nortlinos.wearos.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom

class OidcFlowTest {

    @Test
    fun `openid is advertised only when listed in auth methods`() {
        assertTrue(OidcFlow.supportsOpenId(ServerStatusDto(authMethods = listOf("local", "openid"))))
        assertTrue(OidcFlow.supportsOpenId(ServerStatusDto(authMethods = listOf("OpenID"))))
        assertFalse(OidcFlow.supportsOpenId(ServerStatusDto(authMethods = listOf("local"))))
        assertFalse(OidcFlow.supportsOpenId(ServerStatusDto(authMethods = null)))
        assertFalse(OidcFlow.supportsOpenId(null))
    }

    @Test
    fun `button text falls back when the server supplies none`() {
        assertEquals("Sign in with SSO", OidcFlow.buttonText(null))
        assertEquals(
            "Sign in with SSO",
            OidcFlow.buttonText(ServerStatusDto(authFormData = AuthFormDataDto(authOpenIDButtonText = "  ")))
        )
        assertEquals(
            "Login with Authentik",
            OidcFlow.buttonText(
                ServerStatusDto(authFormData = AuthFormDataDto(authOpenIDButtonText = "Login with Authentik"))
            )
        )
    }

    @Test
    fun `code challenge matches the RFC 7636 S256 example`() {
        // RFC 7636 appendix B.
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM", OidcFlow.codeChallenge(verifier))
    }

    @Test
    fun `generated verifiers are unpadded url safe and unique`() {
        val random = SecureRandom()
        val first = OidcFlow.newCodeVerifier(random)
        val second = OidcFlow.newCodeVerifier(random)
        assertNotEquals(first, second)
        // RFC 7636 allows 43..128 characters from the unreserved set.
        assertTrue(first.length in 43..128)
        assertTrue(first.all { it.isLetterOrDigit() || it == '-' || it == '_' })
    }

    @Test
    fun `authorize query requests the mobile pkce flow`() {
        val query = OidcFlow.authorizeQuery("nortlinos://oauth", "challenge", "state-1")
        assertEquals("code", query["response_type"])
        assertEquals("nortlinos://oauth", query["redirect_uri"])
        assertEquals("challenge", query["code_challenge"])
        // The server rejects anything other than S256 for this flow.
        assertEquals("S256", query["code_challenge_method"])
        assertEquals("state-1", query["state"])
    }

    @Test
    fun `wear redirect uri carries the package name`() {
        assertEquals(
            "https://wear.googleapis.com/3p_auth/com.nortlinos.wearos",
            OidcFlow.wearRedirectUri("com.nortlinos.wearos")
        )
    }

    @Test
    fun `redirect yields the authorization code`() {
        val result = OidcFlow.parseRedirect("nortlinos://oauth?code=abc123&state=xyz", "xyz")
        assertEquals(OidcFlow.RedirectResult.Success("abc123"), result)
    }

    @Test
    fun `redirect from the phone companion is accepted`() {
        val result = OidcFlow.parseRedirect(
            "https://wear.googleapis.com/3p_auth/com.nortlinos.wearos?state=xyz&code=abc123",
            "xyz"
        )
        assertEquals(OidcFlow.RedirectResult.Success("abc123"), result)
    }

    @Test
    fun `mismatched state is rejected so a stale response cannot be exchanged`() {
        val result = OidcFlow.parseRedirect("nortlinos://oauth?code=abc123&state=other", "xyz")
        assertTrue(result is OidcFlow.RedirectResult.Failure)
    }

    @Test
    fun `missing state is rejected`() {
        val result = OidcFlow.parseRedirect("nortlinos://oauth?code=abc123", "xyz")
        assertTrue(result is OidcFlow.RedirectResult.Failure)
    }

    @Test
    fun `provider error is surfaced with its description`() {
        val result = OidcFlow.parseRedirect(
            "nortlinos://oauth?error=access_denied&error_description=User%20said%20no&state=xyz",
            "xyz"
        )
        assertEquals(OidcFlow.RedirectResult.Failure("User said no"), result)
    }

    @Test
    fun `provider error without a description still names the error`() {
        val result = OidcFlow.parseRedirect("nortlinos://oauth?error=access_denied", "xyz")
        result as OidcFlow.RedirectResult.Failure
        assertTrue(result.message.contains("access_denied"))
    }

    @Test
    fun `cancelled sign in reports a failure rather than crashing`() {
        assertTrue(OidcFlow.parseRedirect(null, "xyz") is OidcFlow.RedirectResult.Failure)
        assertTrue(OidcFlow.parseRedirect("", "xyz") is OidcFlow.RedirectResult.Failure)
        assertTrue(OidcFlow.parseRedirect("nortlinos://oauth", "xyz") is OidcFlow.RedirectResult.Failure)
    }

    @Test
    fun `query values are percent decoded and fragments ignored`() {
        val params = OidcFlow.parseQuery("nortlinos://oauth?code=a%2Bb%2Fc&state=s#fragment=ignored")
        assertEquals("a+b/c", params["code"])
        assertEquals("s", params["state"])
    }
}
