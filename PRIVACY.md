# NortlinOS Privacy Policy

**Last updated: October 11, 2026**

NortlinOS is a Wear OS audiobook and podcast client for connecting to an Audiobookshelf server
chosen by the user. The app is maintained by SolfenTheDragon. This policy describes information
handled by the NortlinOS app.

## Information the app handles

When you sign in with a username and password, NortlinOS sends those credentials to the
Audiobookshelf server address you entered so that server can authenticate you. The app stores the
server address, username, password for password-based sign-in, and session tokens on the watch in
Android Keystore-backed encrypted preferences. Single sign-on credentials are handled by your
Audiobookshelf server and identity provider; NortlinOS receives and stores the resulting session
tokens, not your identity-provider password.

NortlinOS stores library and podcast metadata, playback positions, bookmarks, settings, and
downloaded audio and cover images on the watch so the app can work offline. Credentials, library
data, playback state, downloads, and WorkManager state are excluded from device backup. Display
preferences may be included in Android backup.

## How information is used and shared

The app uses this information only to sign in to the server you selected, display your library,
stream or download media, and synchronize playback progress and bookmarks with that server.
Credentials, tokens, library content, and playback data are sent to your selected Audiobookshelf
server as needed for those features. During single sign-on, your server and the identity provider
you configured handle the authentication flow.

NortlinOS does not send this information to the app developer. The app contains no advertising,
analytics, or crash-reporting service. The app does not sell personal information.

Server traffic requires HTTPS and the app validates the server's TLS certificate using Android's
normal trust checks. Addresses using HTTP are rejected; private self-hosted servers need a valid
HTTPS configuration to sign in, stream, sync, or download.

## Retention and deletion

Use **Log out** to remove the saved server session and password login from the watch. This does
not delete downloaded books, cached library data, or locally saved playback progress. Delete
downloads from the app to remove downloaded media. Android removes app-private data when NortlinOS
is uninstalled.

NortlinOS does not create Audiobookshelf accounts. To delete an Audiobookshelf account or data
stored by a server, contact that server's administrator. Logging out of NortlinOS does not delete
server-side account data.

## Contact

For privacy questions, contact the maintainer through the
[NortlinOS GitHub repository](https://github.com/SolfenTheDragon/NortlinOS). Do not post passwords,
session tokens, or other private information in a public issue.
