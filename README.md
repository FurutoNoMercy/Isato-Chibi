# Isato Chibi Floating Assistant — Android Prototype

Native Android Kotlin project, **not a prebuilt APK**. Import the folder in Android Studio
and run/build on a physical Android phone (Android 8+).

## Features
- Floating animated chibi above other apps using `TYPE_APPLICATION_OVERLAY`
- Drag to reposition; tap to cycle expressions; long press to hide
- Drawn with Android Canvas, no remote assets or network permissions
- Foreground service and persistent notification with Hide action
- In-app enable/disable and overlay permission settings

## Build
1. Open Android Studio → Open → select this folder.
2. Wait for Gradle sync (internet needed for Android Gradle Plugin/Kotlin).
3. Connect phone with USB debugging, click Run; or Build → Build APK(s).
4. Install, launch, grant "Display over other apps", press Show.
5. If prompted, allow notifications for service status.

## Limits
- This is a **2D chibi proof of concept**, not 3D/VRM or a chat AI.
- Android may suppress overlays on permission dialogs, secure screens, or some apps.
- Overlay and foreground service behavior varies by Android/HyperOS battery policy.
- No background auto-start after reboot; user must launch and enable it.
- The service is started from the visible activity; foreground service uses `specialUse`.
- If preparing a Play Store release, review current special-use foreground-service policies.
- The artwork is a simplified original chibi inspired by the requested blue-hair/pink-clip aesthetic, not a detailed anime asset.

## Build APK without Android Studio (GitHub Actions)
1. Create a new GitHub repository and upload **contents** of this project, including `.github/workflows/build-apk.yml` (hidden folder).
2. Open Actions → Build Android APK → Run workflow.
3. After a successful run, download artifact `Isato-Chibi-Debug-APK` and extract `app-debug.apk`.
4. Install APK on your Android device, then grant overlay permission.

The GitHub workflow is included but has **not been run or verified** here.
