# AVG Cleaner+ (1.0.48)

Android phone cleaner inspired by AVG Cleaner: junk scan, media & apps cleanup, tips, settings, paywall, ads, and local account flows.

**Version:** `1.0.48` (versionCode `49`)  
**Package:** `com.mob.ccleaner.storage.avg.cleaner.phone.androidcleaner`  
**Repo:** https://github.com/shaharyar395/avg-cleaner-replica  

Privacy policy: https://sites.google.com/view/mob-apps-inc/privacy-policy  

---

## Stack

| | |
|---|---|
| Language / UI | Kotlin, Jetpack Compose (Material 3) |
| Navigation | Navigation Compose |
| Persistence | DataStore Preferences |
| Background | WorkManager |
| Ads | Google Mobile Ads (AdMob Mediation) |
| Images | Coil |
| JDK | 17 |
| Gradle / AGP | 8.9 / 8.7.3 |
| Kotlin | 2.2.0 |
| SDK | compile / target **35**, min **26** |

---

## Features

### Cleaning & storage
- **Quick Clean** — scan junk (caches, residual files, leftover APKs, ad caches, thumbnails, empty folders) and delete selected items
- **Free space / used %** — live storage via `StatFs`
- **Hidden caches / browser data** — measured where Android allows (deletion of other apps’ private caches is a platform limit)
- **Downloads / files to review** — listed for review before delete
- **Media Overview** — photos, video, audio, other files (MediaStore)
- **Photo analysis** — similar, bad quality, sensitive (screenshots), old, optimizable (on-device)
- **Apps Overview** — installed apps, usage, unused apps, App Manager uninstall / info
- **Space-saving tips** — generated from live scans
- **System Info** — device, network, RAM, storage
- **Automatic Cleaning** — WorkManager schedule + notifications

### Product UI
- First-run: splash → get started → consent → permissions → scan → ready
- Bottom tabs: Home, Tools, Storage, Account
- Themes (dark / light / system + accent)
- Multi-language strings (in-app language picker)
- Premium paywall (first-run YEARLY plans vs upgrade “Ignore this offer” + exclusive 10% retention)
- Sign-in / account picker (local session; Google UI flows without full OAuth backend)
- Feature transition ads (interstitial + banner fallback overlay)

### Ads (production — DATA CLEANER sheet)

| Placement | Ad unit |
|---|---|
| App ID | `ca-app-pub-9297250663056879~1961673643` |
| Banner | `…/3270885884` |
| Interstitial | `…/7217121324` |
| Rewarded | `…/1770101953` |
| App Open | `…/9162515973` |
| Advanced Native | `…/9244518862` |

IDs live in `app/src/main/res/values/strings.xml`. Banner + interstitial are wired in the UI; rewarded / app open / native IDs are reserved for later placements.

---

## Build & run

### Requirements
- Android Studio (Ladybug+) or command-line JDK 17 + Android SDK
- Physical device recommended (real junk / photos / usage)

### Open project
Open the `avg-cleaner-replica` folder (not its parent) so Gradle finds `settings.gradle.kts`.

Create `local.properties` if needed:

```properties
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
```

### Build debug APK

```bat
gradlew.bat assembleDebug
```

Output:

```
app\build\outputs\apk\debug\cleaner1-debug.apk
```

Rename/copy for distribution as e.g. `AVG-Cleaner-plus-1.0.48.apk`.

### Install via ADB

```bat
adb install -r app\build\outputs\apk\debug\cleaner1-debug.apk
```

Or push to device Download:

```bat
adb push app\build\outputs\apk\debug\cleaner1-debug.apk /sdcard/Download/AVG-Cleaner-plus-1.0.48.apk
```

---

## Permissions

| Permission | Why |
|---|---|
| All files access (`MANAGE_EXTERNAL_STORAGE`) | Junk scan & clean shared storage |
| Usage access (`PACKAGE_USAGE_STATS`) | App sizes, screen time, unused apps |
| Notifications | Cleaning tips & auto-clean alerts |
| Media (images / video / audio) | Media grids & photo analysis |
| Internet | AdMob + opening privacy / links |

Special access (All files / Usage) has no runtime dialog — use in-app **GO TO SETTINGS**.

---

## Project layout

```
app/src/main/java/com/replica/cleaner/
├── MainActivity.kt / CleanerApp.kt
├── ads/                 AdMob init + interstitial manager
├── core/                Prefs, Permissions, formatting
├── data/                Repository + scanners (junk, media, apps, device, photos)
├── work/                Auto-clean, notifications, boot receiver
├── l10n/                Translations
└── ui/
    ├── nav/             Routes + AppNavHost + feature transition ads
    ├── components/      Banner, charts, scaffolds, overlays
    ├── screens/         Home, Tools, Storage, Account, Premium, Auth, …
    ├── theme/
    └── util/            Privacy policy helper
```

---

## Stubbed / not production-complete

- **Play Billing** — CONTINUE on paywall sets a local `premium` flag (no real Google Play products yet)
- **Google / social auth** — UI + local email session; no full OAuth backend
- **Cloud providers** (Dropbox / Drive / OneDrive) — UI only
- **Firebase / Crashlytics** — package reserved; needs `app/google-services.json` from the MOBAPPS Drive folder
- **AppLovin MAX** — not used (sheet selects AdMob Mediation only)
- **Sleep Mode / deep force-stop** — not possible with public APIs without Accessibility Service

---

## Platform limits (Android)

- Cannot clear another app’s private cache via public APIs
- Cannot silently uninstall or force-stop other apps
- Per-app battery drain is not exposed; screen-time is used as a proxy

---

## Privacy

- Junk / media / photo analysis run on device
- Privacy Policy link opens the MOBAPPS URL above from Settings and legal footers
- AdMob serves ads (requires network); Firebase not integrated until `google-services.json` is added

---

## APK artifact (1.0.48)

| | |
|---|---|
| Display name | AVG Cleaner+ |
| applicationId | `com.mob.ccleaner.storage.avg.cleaner.phone.androidcleaner` |
| versionName / versionCode | `1.0.48` / `49` |
| Typical debug output | `cleaner1-debug.apk` → distribute as `AVG-Cleaner-plus-1.0.48.apk` |
