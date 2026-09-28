<img src="resources/NortlinOS.png" alt="NortlinOS logo" width="52" align="left" />

# NortlinOS

**Your Audiobookshelf library, on your wrist.**

NortlinOS is an audiobook player for Wear OS watches that connects to your own
[Audiobookshelf](https://www.audiobookshelf.org/) server. Download books to the watch, leave your
phone at home, and pick up exactly where you left off on any device.

## What you can do

- **Browse and search your library** with the crown or a swipe. Books appear as cards with their
  cover art. Switch to **Series** to group books together, search series by name, title, author, or
  narrator, open a series, and queue all its books
  for download at once. Books already downloaded or downloading are left alone.
- **Download books to the watch** and listen with no phone or internet connection. Downloads can
  be paused, resumed, cancelled, or deleted, and an interrupted download continues where it stopped.
- **Stream** books you haven't downloaded when the watch is online.
- **Recent books.** The main menu lists your 10 most recently played unfinished books. Tap one to
  open its book page, where you can play it or download it first.
  A **Now playing** shortcut sits at the top of the main menu while a book is loaded.
- **Add the Now playing tile** (swipe left from the watch face to the end of your tiles, tap **+**,
  and pick NortlinOS *Now playing*).
  It shows your current book over its cover and how much is left. While a book plays, **Pause**
  stops it right from the tile; when paused, **Resume** opens the player and carries on (so
  NortlinOS can check for headphones first).
- **No accidental speaker playback.** If you press play without headphones connected, NortlinOS
  asks first: tap **Connect headphones** to open the watch's audio output screen (playback starts
  by itself once they connect), or **Use watch speaker** if your watch has one.
- **Control playback** from a simple player: play/pause and 30-second skip back/forward. Turn the
  **crown to change the volume**. On round watches a ring around the edge shows how far through
  the book you are, with a small gap between chapters.
- **Swipe left on the player** for extra tools (the dots at the bottom show which page you're on):
  - previous/next chapter, or All chapters to jump straight to any chapter;
  - a sleep timer (any length up to 12 hours);
  - playback speed;
- **Keep your place everywhere.** Progress syncs with your server automatically, so the phone
  app, the web player and the watch agree. If the watch and another device both moved on
  while out of touch, NortlinOS asks which position to keep instead of guessing.
- **See the cover in the watch's media controls**, so you can pause from anywhere.
- **Show progress for the whole book or the current chapter** (Settings).
- **Tailor the app to how you listen** (Settings): **Show series** opens libraries in the Series
  view every time, and **Search on main menu** hides the Search entry if you never use it.
- **Keep listening after signing out.** *Listen offline* on the sign-in screen opens your
  downloads without an account.

## Requirements

- A watch running **Wear OS 4 or newer** (for example Pixel Watch 2+, Galaxy Watch 6+).
- An **Audiobookshelf server** you can reach from the watch, on your home network or the internet.
- **Bluetooth headphones** are recommended. The watch speaker works if your watch has one.

## Installing

NortlinOS isn't on the Play Store yet. It's installed from a file:

1. Download the latest `NortlinOS-x.y.z.apk` from this repository's **Releases** page.
2. On the watch, enable **Developer options** (Settings → System → About → tap *Build number*
   seven times). Then turn on **ADB debugging** and **Debug over Wi-Fi** (or **Wireless
   debugging**).
3. On a computer with [Android platform tools](https://developer.android.com/tools/releases/platform-tools)
   on the same Wi-Fi network, connect to the address the watch shows, then install:

   ```bash
   adb connect <watch-ip>:<port>      # newer watches: run `adb pair` first
   adb install -r NortlinOS-x.y.z.apk
   ```

To update, repeat step 3 with the new file. `-r` keeps your sign-in, downloads and progress.

## Signing in

**You must sign in seperately from the app on your phone, they do not share login information**

1. Open NortlinOS and enter your **server address**, e.g. `https://abs.example.com` or
   `192.168.1.20:13378`. If you leave out `https://`, NortlinOS tries a secure connection first.
2. Enter your **username and password** and tap **Sign in**.

**Single sign-on (SSO):** if your server has OpenID Connect enabled, a sign-in button for it
appears as soon as you enter the address. The sign-in page opens on your **paired phone**.
For that to work, your server administrator needs to add this address under **Settings →
Authentication → Allowed Mobile Redirect URIs** in Audiobookshelf:

```
https://wear.googleapis.com/3p_auth/com.nortlinos.wearos
```

If the watch itself has a browser, SSO also works there without that setting.

You stay signed in. On Audiobookshelf 2.26 and newer, NortlinOS quietly renews your session in the
background.

## Listening offline

Open a book and tap **Download for offline**. Covers, chapters and details are saved along with the audio.
If you stay on the book's page, a full-screen check mark tells you when the download finishes
(or fails, or is deleted). **Downloaded** on the main menu lists everything stored on the watch, and Settings shows how much
space downloads use and how much is free. Downloads use Wi-Fi or your phone's connection and
continue in the background.

Progress you make offline is saved on the watch and sent to your server the next time the watch
is connected, either via Wifi or through your phones connection.

## Battery

NortlinOS is built to be light on battery during long listening sessions:

- When the watch has no connection, it doesn't try to reach the server.
- Streaming fetches audio a few minutes ahead in large bursts so the radio can switch off in between.
- Where the watch supports it, audio decoding is handed to dedicated low-power hardware.
- Nothing updates on screen while the screen is off. In always-on mode the player turns
  almost entirely black.
- Covers are downloaded once at watch size and then reused.
- The tile only updates when you play or pause; it never wakes the watch on a schedule.

For the longest battery life, download books ahead of time instead of streaming.

## Troubleshooting

| Problem | Try |
|---|---|
| "Offline", "No network connection" or "Unable to connect" | Check the watch is on Wi-Fi or connected to your phone, and that the address works from a phone on the same network. |
| SSO button doesn't appear | OpenID Connect isn't enabled on the server. Use your username and password. |
| SSO opens but never returns | Ask your admin to add the redirect address above to the server's allow list. |
| A book won't play | If it isn't downloaded, the watch needs a connection to stream it. |
| A cover changed on the server but not on the watch | Covers are cached. Clear the app's cache in the watch's settings. |
| "Progress differs" appears | Another device saved newer progress while the watch had unsynced listening. Pick the position you want to keep. |
| No sound | Connect Bluetooth headphones, or turn the crown on the player to raise the volume. If you chose *Use watch speaker*, that choice lasts until NortlinOS is closed. |
| "No headphones" appears even though they're paired | Make sure they are turned on and connected to the watch (not only to your phone), then tap **Connect headphones**. |
| The tile says "Start a book to resume it from here" | Play any book once; the tile remembers it from then on. |

## Roadmap

Improvements being worked on:

- Power efficiency improvements
- UI smoothness updates
- Updates to the harness to help handle future Audiobookshelf API updates gracefully

## Privacy

NortlinOS talks only to the Audiobookshelf server you sign in to. Your sign-in is stored
encrypted on the watch and is never included in backups. There are no ads, analytics, or
third-party services.

## License

NortlinOS is released under the terms of the [license](LICENSE) included in this repository.
