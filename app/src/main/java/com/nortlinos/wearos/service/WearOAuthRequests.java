package com.nortlinos.wearos.service;

import android.net.Uri;

import androidx.wear.phone.interactions.authentication.OAuthRequest;

/**
 * Creates an {@link OAuthRequest} from an authorization URL that is already complete.
 *
 * <p>{@code OAuthRequest.Builder} always rewrites {@code redirect_uri} to its own
 * {@code wear.googleapis.com} value and throws if the supplied URL disagrees. Audiobookshelf's
 * mobile flow needs the opposite: the identity provider must redirect to the Audiobookshelf
 * server, which then forwards the result to the companion URL. The URL therefore has to reach the
 * companion untouched.
 *
 * <p>The constructor used here is {@code internal} in Kotlin, which is a compile-time restriction
 * only — it is a public constructor in the compiled class, so Java can call it. This is an
 * implementation detail of a version-pinned dependency, so callers must treat failure as expected
 * and fall back to another sign-in transport.
 */
final class WearOAuthRequests {

    private WearOAuthRequests() {
    }

    static OAuthRequest create(String packageName, Uri authorizationUrl) {
        return new OAuthRequest(packageName, authorizationUrl);
    }
}
