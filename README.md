<img src="resources/NortlinOS.png" alt="NortlinOS logo" width="52" align="left" />

# NortlinOS

An audiobook and podcast player for Wear OS that connects to your own Audiobookshelf server.

[![Latest release](https://img.shields.io/github/v/release/SolfenTheDragon/NortlinOS?label=latest%20release)](https://github.com/SolfenTheDragon/NortlinOS/releases/latest)
[![License: GPL v3](https://img.shields.io/github/license/SolfenTheDragon/NortlinOS)](LICENSE)

Download books and podcasts to your watch, leave your phone at home, and pick up where you left
off on any device. NortlinOS works with [Audiobookshelf](https://www.audiobookshelf.org/), the
self-hosted audiobook and podcast server.

- **Listen without your phone.** Downloads play with no phone or internet connection.
- **Stay in sync.** Your place, bookmarks and listening time sync with Audiobookshelf, so the
  watch, the phone app and the web player agree.
- **Built for the wrist.** Big controls, crown volume, a Now playing tile, and a check that stops
  audio from playing out loud when headphones aren't connected.
- **Light on battery.** Designed for long listening sessions, especially with downloaded books.

<table>
  <tr>
    <td align="center"><img src="resources/Signed%20In%20Menu.png" alt="Screenshot of the NortlinOS main menu with connection status" width="190" /><br />Main menu</td>
    <td align="center"><img src="resources/Book%20Menu.png" alt="Screenshot of a book page with play and download options" width="190" /><br />Book page</td>
    <td align="center"><img src="resources/Playback%20Menu.png" alt="Screenshot of the player with skip controls and a progress ring" width="190" /><br />Player</td>
  </tr>
</table>

## Contents

- [Requirements](#requirements)
- [Install](#install)
- [Get started](#get-started)
- [How to](#how-to)
- [Controls and gestures](#controls-and-gestures)
- [Settings](#settings)
- [How syncing works](#how-syncing-works)
- [Battery](#battery)
- [Troubleshooting](#troubleshooting)
- [Get help](#get-help)
- [Privacy](#privacy)
- [For developers](#for-developers)
- [Roadmap](#roadmap)
- [License](#license)

## Requirements

- A watch running **Wear OS 4 or newer**, for example a Pixel Watch 2 or Galaxy Watch 6 or later.
- An **Audiobookshelf server** the watch can reach, on your home network or the internet.
  Version 2.26 or newer is recommended; NortlinOS is tested with 2.38.
- **Bluetooth headphones**, recommended. The watch speaker works too, if your watch has one.

## Install

NortlinOS isn't on the Play Store yet, so you install it from a file with a computer.

1. Download an APK from the [latest NortlinOS release](https://github.com/SolfenTheDragon/NortlinOS/releases/latest).
   Choose one:
   - **`NortlinOS-x.y.z.apk`**: for servers with an `https://` address. Pick this one if you're
     not sure.
   - **`NortlinOS-x.y.z-insecure.apk`**: also connects to `http://` addresses and servers with
     self-signed certificates. Your password and listening are not protected from anyone else
     on the network, so only use it on a network you trust.
2. On the watch, turn on **Developer options**: go to **Settings → System → About** and tap
   **Build number** seven times.
3. In **Developer options**, turn on **ADB debugging** and **Debug over Wi-Fi** (on some watches
   this is called **Wireless debugging**). The watch shows an address such as `192.168.1.20:5555`.
4. On a computer on the same Wi-Fi network, install the
   [Android SDK Platform-Tools](https://developer.android.com/tools/releases/platform-tools), then
   run:

   ```bash
   adb connect 192.168.1.20:5555
   adb install -r NortlinOS-x.y.z.apk
   ```

   Use the address your watch shows. On newer watches, run `adb pair` first, as the watch
   explains when you open **Wireless debugging**.

**To update**, download the new APK and repeat step 4. Your sign-in, downloads and progress are
kept. You can switch between the two APK types the same way.

## Get started

<img src="resources/Main%20Menu.png" alt="Screenshot of the NortlinOS sign-in screen" width="200" align="right" />

1. Open NortlinOS on the watch.
2. Enter your server address, for example `https://abs.example.com`. If you leave out
   `https://`, NortlinOS adds it.
3. Enter your Audiobookshelf **username and password**, and tap **Sign in**.
4. Tap **Server library**, pick a book, and tap **Play or resume** to stream it, or
   **Download for offline** to save it to the watch.

The watch signs in separately from the Audiobookshelf app on your phone; signing in on one
doesn't sign in the other. Once signed in, you stay signed in.

**Sign in with single sign-on (SSO).** If your server uses OpenID Connect, a sign-in button for
it appears after you enter the address. The sign-in page opens on your paired phone. For this to
work, your server administrator must add this address in Audiobookshelf under **Settings →
Authentication → Allowed Mobile Redirect URIs**:

```
https://wear.googleapis.com/3p_auth/com.nortlinos.wearos
```

If your watch has its own browser, SSO also works there without that setting.

**No account?** Tap **Listen offline** on the sign-in screen to play the downloads already on
the watch.

<br clear="right" />

## How to

### Find something to listen to

- **Recent books** on the main menu lists your 10 most recently played unfinished books.
- **Server library** lists your books as cards with their covers. Scroll with the crown or a
  swipe. Turn on **Show series** in Settings to group books into series; from a series you can
  download all its books at once.
- **Search** finds books by title, author, narrator or series.
- **Podcasts** lists your podcast libraries and shows. Open a show to see its episodes.

### Download for offline listening

- **One book or episode:** open it and tap **Download for offline**. Or, in any list, press and
  hold its tile to start the download without leaving the list. Hold again to pause it.
- **A whole podcast show:** open the show and tap **Download all episodes**. NortlinOS checks
  the watch's free space first and warns you if the episodes might not fit.
- **Manage downloads:** **Downloaded** on the main menu lists everything stored on the watch. Open
  an item to pause, resume, cancel or delete its download. Settings shows how much space
  downloads use.

Downloads continue in the background and resume where they stopped if interrupted. Downloaded
items show a check mark in lists.

### Play and control a book

- Tap **Play or resume**. If no headphones are connected, NortlinOS asks first: tap **Connect headphones**
  to open the watch's audio output screen (playback starts once they connect), or **Use watch
  speaker**.
- Skip back or forward 30 seconds, and turn the crown to change the volume. On round watches, the
  ring around the edge shows your progress through the book, with a small gap between chapters.
- Swipe left on the player for **Playback tools**: previous or next chapter, **All chapters**,
  the sleep timer (up to 12 hours), playback speed, and bookmarks. Swipe right to go back.
- While a book is loaded, **Now playing** appears at the top of the main menu.

### Save your place with a bookmark

On the player, swipe left and tap **Bookmark this spot**. **View bookmarks** lists them: tap one
to jump to it, or press and hold to remove it. Bookmarks sync with Audiobookshelf. Bookmarks are
available for books, not podcast episodes.

### Use the Now playing tile

Swipe left from the watch face to the end of your tiles, tap **+**, and choose **NortlinOS Now
playing**. The tile shows the current book and how much is left. Tap **Pause** to stop playback
from the tile, or **Resume** to open the player and carry on.

### Check your listening stats

Tap **Listening stats** on the main menu. It opens on today's listening time and your current
streak, with a chart of the last 7 days. Scroll down for all-time listening, days listened,
finished books and episodes, your most-listened titles, and recent sessions. The figures come from
your server and include every device you listen on.

### Set up your watch's extra buttons

If your watch has buttons besides the power button, go to **Settings → Hardware buttons** and
choose an action for each: play or pause, back or forward 30 seconds, previous or next chapter,
bookmark this spot, or open the player. The buttons do this while NortlinOS is open; elsewhere
they keep their usual function. The watch buzzes to confirm each press, with a different buzz if
no book is loaded.

## Controls and gestures

| Where | Action | What it does |
|---|---|---|
| Any screen | Swipe right | Go back |
| Lists | Turn the crown or swipe | Scroll |
| Book, series or episode lists | Press and hold a tile | Start or pause its download |
| Bookmarks list | Press and hold a bookmark | Remove it |
| Player | Turn the crown | Change the volume |
| Player | Swipe left | Open playback tools |
| Playback tools | Swipe right | Return to the player |
| Sleep timer | Turn the crown | Choose the timer length |
| Now playing tile | Tap **Pause** or **Resume** | Pause, or open the player and resume |
| Extra hardware buttons | Press | The action you chose in Settings |

## Settings

| Setting | What it does | Default |
|---|---|---|
| Theme | Accent color, from presets or any HTML color such as `#FF8800` | Amber |
| Hardware buttons | Actions for your watch's extra buttons (only on watches that have them) | Watch default |
| Show series | Open libraries grouped into series | Off |
| Pre-cache libraries | Load library listings in the background after connecting. Uses more battery and data | Off |
| Headphone check | Ask before playing through the watch speaker | On |
| Format descriptions | Show book and podcast descriptions as formatted text | On |
| Search on main menu | Show **Search** on the main menu | On |
| Podcasts on main menu | Show **Podcasts** on the main menu | On |
| Stats on main menu | Show **Listening stats** on the main menu. When off, stats are never loaded | On |
| Show playback progress as | Progress for the **Whole book** or the **Current chapter** | Whole book |
| Log out | Sign out of your server. Downloads and progress stay on the watch | — |

## How syncing works

- **Your place** in each book and episode syncs with your server whenever the watch is connected,
  over Wi-Fi or through your phone. Listening you do offline is sent the next time it connects.
- **If two devices disagree**, for example you listened on the watch offline and on your phone
  meanwhile, NortlinOS shows **Progress differs** and asks which position to keep.
- **Listening time** on the watch, including offline listening, is sent to your server as
  listening sessions, so it counts in Audiobookshelf's stats. The watch appears there as
  **NortlinOS** with its model name.
- **Your sign-in** renews itself in the background, so you aren't asked to sign in again. On
  Audiobookshelf 2.36 and newer, the watch is listed under your account's active sessions, and
  **Log out** ends that session on the server too.

## Battery

NortlinOS is designed to be light on battery during long listening sessions:

- It doesn't try to reach the server when the watch has no connection.
- Streaming fetches audio a few minutes ahead in bursts, so the radio can switch off in between.
- Where the watch supports it, audio decoding runs on dedicated low-power hardware.
- Nothing updates on screen while the screen is off, and in always-on mode the player turns almost
  entirely black.
- Covers are downloaded once at watch size and reused.
- The tile only updates when you play or pause.

For the longest battery life, download books and episodes before you listen instead of streaming
them.

## Troubleshooting

| Problem | Try this |
|---|---|
| **Offline**, **No network connection** or **Unable to connect** | Check the watch is on Wi-Fi or connected to your phone, and that the server address works from your phone on the same network. |
| The server address is rejected | Use an `https://` address with a valid certificate, or install the insecure APK (see [Install](#install)). |
| The SSO button doesn't appear | Your server doesn't have OpenID Connect turned on. Sign in with your username and password. |
| SSO opens but never finishes | Ask your server administrator to add the redirect address from [Get started](#get-started). |
| A book or episode won't play | If it isn't downloaded, the watch needs a connection to stream it. |
| A podcast show has no episodes offline | Open the show once while connected so its episode list is saved, and download the episodes you want. |
| No sound | Connect Bluetooth headphones, or turn the crown on the player to raise the volume. If you chose **Use watch speaker**, that lasts until NortlinOS is closed. |
| **No headphones** appears even though they're paired | Make sure they're on and connected to the watch, not only to your phone, then tap **Connect headphones**. |
| **Progress differs** appears | Another device saved newer progress while the watch had unsynced listening. Pick the position to keep. |
| A cover changed on the server but not on the watch | Covers are saved on the watch. Clear NortlinOS's cache in the watch's settings. |
| The tile says **Start a book to resume it from here** | Play any book once. The tile remembers it from then on. |
| **Hardware buttons** is missing from Settings | Your watch has no buttons that apps can use. |
| Listening stats show the wrong day | Stats are grouped by date on your server. If your server's time zone differs from yours, late-night listening can appear on the next or previous day. |

## Get help

- **Found a bug or have an idea?** [Open an issue on GitHub](https://github.com/SolfenTheDragon/NortlinOS/issues).
  Include your watch model, your Wear OS version, the NortlinOS version (shown on the
  [releases page](https://github.com/SolfenTheDragon/NortlinOS/releases)), and your
  Audiobookshelf version.
- **Questions about your server**, such as accounts, libraries or SSO setup, are best answered by
  your server administrator or the [Audiobookshelf documentation](https://www.audiobookshelf.org/docs).

Never post passwords, server addresses you want to keep private, or session tokens in an issue.

## Privacy

NortlinOS sends your sign-in, library and listening data only to the Audiobookshelf server you
choose. It has no advertising, analytics or crash reporting. Read the full
[NortlinOS privacy policy](PRIVACY.md), which you can also open from the sign-in screen and
Settings.

## For developers

NortlinOS is a Kotlin and Jetpack Compose for Wear OS app built with Gradle:

```bash
./gradlew test             # unit tests
./gradlew assembleDebug    # debug APK in app/build/outputs/apk/debug
```

**HTTP and self-signed servers.** The `nortlinos.allowInsecureServers` switch in
`gradle.properties` controls whether the app accepts `http://` addresses and untrusted
certificates. It is `true` in this repository for development. Set it to `false` for builds you
distribute; release builds of both APK types are produced by the release workflow in
`.github/workflows/release.yml`.

Pull requests are welcome. Please open an issue first for larger changes, and run the unit tests
before submitting.

## Roadmap

Planned improvements. These aren't in the app yet.

### Listening

- **Sleep timer until the end of the chapter**, and a shake of the wrist to add more time.
- **Skip silence and voice boost** for clearer, shorter listening.
- **Choose the skip length**: 10, 15, 30 or 60 seconds. Press and hold to skip a whole chapter.
- **Mark a book or episode as finished**, or not finished, from the watch.

### Library and podcasts

- **Collections and playlists** from your Audiobookshelf server.
- **Continue listening from your server**, so a book you started on another device shows up on
  the watch straight away.
- **Automatic podcast downloads**: keep the newest episodes of a show on the watch and remove
  ones you've finished.
- **Remove finished downloads automatically** to free up storage.
- **Download only on Wi-Fi**, a setting you can turn on or off. When it's on, downloads wait for
  Wi-Fi instead of using your phone's connection.

### On your watch

- **A watch face complication** showing the time left in your book or today's listening.
- **Resume from the watch's media controls** when your headphones connect, without opening the
  app.
- **Support for Wear OS 7**, including Live Updates and Wear Widgets.
- **Swipe a list item to reveal actions**, such as deleting a download or bookmark, and a
  one-time hint that pressing and holding a tile downloads it.

### Sync and accounts

- **Instant progress updates**: moving forward on your phone updates the watch right away.
- **Multiple servers or accounts**, with quick switching.
- **Listening stats in your server's time zone**, so late-night listening lands on the right day.
- **Listening stats available offline**, even after the app restarts.
- **Support for Audiobookshelf version 3** when it's released.

### Availability and quality

- **NortlinOS on the Google Play Store.**
- **Translations** into other languages.
- **Automated screen tests** on small and large watches, which also keep the screenshots here up
  to date.

## License

NortlinOS is free software, released under the
[GNU General Public License v3.0](LICENSE).
