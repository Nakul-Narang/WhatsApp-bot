# M0 spike: can your Poco read and reply to WhatsApp?

This is NOT the real app. It is a throwaway test of one question: does the notification
route work on a Poco C71 (4 GB, Android 15)? If it fails, nothing else in the spec is worth building.

This code has never been compiled. It was written without an Android SDK, so the first real
build happens on GitHub (see below). If the build fails, open the failed run, copy the red error
text, and bring it back. A first-build failure is normal, not a disaster.

## What is in the box

| File | What it does |
| --- | --- |
| `WaListenerService.kt` | Android hands it every notification. It drops everything except WhatsApp, then pulls out the chat name, the text, and the reply box. |
| `MessageHub.kt` | A small in-memory list the service writes to and the screen reads from. Nothing is saved to disk. |
| `MainActivity.kt` | One screen: permission status, captured messages, and a button that sends a fixed test reply after an approval dialog. |
| `AndroidManifest.xml` | Declares the screen and the listener service. Has no internet permission on purpose. |

## Get the APK (GitHub builds it for you, no Android Studio needed)

1. Push this project to a GitHub repo. Every push starts the **Build APK** workflow.
2. On GitHub open the repo, go to the **Actions** tab, and open the latest **Build APK** run.
3. Wait for the green tick (the first run takes a few minutes).
4. Scroll to **Artifacts** at the bottom and download `wa-agent-m0-debug-apk`. It is a zip with `app-debug.apk` inside.
5. Move `app-debug.apk` to the Poco, open it, and allow "Install unknown apps" for your file manager or browser when asked. It is a debug build, so Play Protect may warn you. That is expected for an app you built yourself.

If the run is red, the APK was not built. Copy the error and send it back.

To run from Android Studio instead: File > Open, choose this folder, then press Run with the phone plugged in and USB debugging on.

Files besides the four above: `build.gradle.kts`, `app/build.gradle.kts`, `settings.gradle.kts` and
`gradle.properties` are the build setup (pinned to AGP 8.12.0, Gradle 8.13, Kotlin 2.1.20). The
`res/` folder holds a placeholder icon. `.github/workflows/build-apk.yml` is the GitHub build.

## Phone settings (Poco kills background apps, so do all of these)

Menu names vary a little by version.

1. Open the app, tap **Open notification access settings**, and allow **WA Agent M0**.
2. Settings > Apps > WA Agent M0 > Battery: choose **No restrictions**.
3. Settings > Apps > Permissions > **Autostart**: turn it on for WA Agent M0.
4. Open Recent apps and lock WA Agent M0 (long-press its card, choose Lock).

## The M0 test

1. From a second phone or number, message your Poco on WhatsApp. The text appears in the app. Pass 1.
2. Lock the screen, wait a few minutes, message again, then unlock. The message is still captured. Pass 2.
3. Tap **Send test reply to latest chat**, then **Send** in the dialog. "M0 test reply" arrives on the other phone. Pass 3.
4. Leave the phone idle overnight, then message it in the morning. Pass 4.

Write down which passes failed and what you saw. A failure is useful information, not a disaster.

## Known limits (all on purpose for M0)

- Messages vanish when the app closes. Storage and the 3-day delete come in M1.
- WhatsApp may repost a notification when it updates, so you may see a message twice. De-duplication is M1.
- The reply only works while that chat's notification is still on screen. If you clear it, you will see "reply box has expired".
- Chats you locked in WhatsApp should not show their text. Check that during testing instead of trusting me.
- The reply text is fixed. Voice input, summaries and the cloud model are later milestones.
