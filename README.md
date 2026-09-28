# Cleaner — an AVG Cleaner replica

A working Android app that re-creates the AVG Cleaner interface and behaviour from
the screen recording: the dark navy shell, the Home dashboard, Quick Clean, Media
Overview with photo analysis, Apps Overview, the space-saving tips feed, the full
Settings tree, Themes, the paywall and the sign-in screen.

Built with Kotlin + Jetpack Compose (Material 3), Navigation Compose, DataStore
and WorkManager. Everything runs on device — no network calls, no analytics.

---

## Build status

**Compiles clean.** `./gradlew clean assembleDebug` → `BUILD SUCCESSFUL`, zero
errors, zero warnings, producing an 18.25 MB debug APK.

Verified against:

| | |
|---|---|
| JDK | Microsoft OpenJDK 17.0.20.1 |
| Gradle | 8.9 (wrapper included) |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 26 |

## Opening it

1. Open the `avg-cleaner-replica` folder itself in Android Studio (Ladybug or
   newer) or in Cursor — not its parent, or Gradle won't find
   `settings.gradle.kts`.
2. The Gradle wrapper **is** included (`gradlew`, `gradlew.bat`,
   `gradle/wrapper/gradle-wrapper.jar`), so no separate Gradle install is needed.
3. Create `local.properties` pointing at your SDK — Android Studio writes this
   for you on first open:
   ```properties
   sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
   ```
4. Build from a terminal if you prefer:
   ```bash
   gradlew.bat assembleDebug
   ```
   Gradle downloads `platforms;android-35` and `build-tools;35.0.0`
   automatically if they are missing.
5. Run on a **physical device**. An emulator renders the UI fine, but it has no
   real junk, no photo library and no app history, so most numbers read 0.

### Grant the two special permissions

Two of the three permissions are *special access* grants — Android will not show
a runtime dialog for them, so the in-app "GO TO SETTINGS" buttons are the only
route:

- **All files access** → Settings › Apps › Special app access › All files access
- **Usage access** → Settings › Apps › Special app access › Usage access

Without All files access the junk scan finds almost nothing. Without Usage access
every app size, screen-time figure and cache total reads 0.

---

## What actually works

| Area | Status |
|---|---|
| Junk scan (visible caches, residual files, leftover APKs, ad caches, thumbnails, empty folders) | Real. Walks shared storage, reports real sizes, deletes real files. |
| Hidden caches | Size is **real** (StorageStatsManager). Deletion is not possible — see *Known limits*. |
| Browser data | Size is **real**. Deletion is not possible — see *Known limits*. |
| Downloads / "files to review" | Real files, listed unticked so nothing goes by accident. |
| Free space, used %, segmented bar | Real (`StatFs`). |
| Media Overview counts and donut | Real (MediaStore). |
| Photo analysis — similar / bad quality / sensitive / old / optimizable | Real, computed on device. See *How photo analysis works*. |
| Photo, video, audio, other-file grids + delete | Real. Deletes through MediaStore. |
| Apps Overview — installed/system counts, % used, drainers | Real (PackageManager + StorageStatsManager). |
| Screen time, times opened, unused apps, weekly bar chart | Real (UsageStatsManager). |
| App Manager — sort, multi-select, uninstall, app info | Real. Uninstall fires Android's own confirmation per package. |
| Space-saving tips | Real — generated from the live scans, ordered by your Settings priority list. |
| System Info | Real (Build, uptime, Wi-Fi, IP, Bluetooth, mobile data, RAM, storage). |
| Settings tree, every toggle | Real, persisted in DataStore. |
| Automatic Cleaning | Real. WorkManager job on the chosen cadence; cleans only ticked categories; posts a notification past the threshold. |
| Notifications + channels | Real, one system channel per category. |
| Themes (Dark / Light / System + accent) | Real, applied live and persisted. |
| Onboarding, consent, permission screens | Real, driven by actual permission state. |

## What is deliberately stubbed

These are marked in the code and reachable in the UI, but they do not do the
real thing yet:

- **Billing.** Tapping CONTINUE on the paywall flips a local `premium` flag
  (`Prefs.setPremium`). There is no Google Play Billing integration — that needs
  a Play Console account, a signed build and real product IDs. Everything behind
  the paywall unlocks, so you can exercise all of it. There is also a
  *Simulate Premium* switch in Account › About this app.
- **Sign in.** Stores a validated email locally so the Account tab shows a
  signed-in state. No backend, and Google sign-in needs an OAuth client ID.
- **Cloud services** (Dropbox / Drive / OneDrive). Rows and settings are there;
  connecting needs each provider's OAuth client ID and SDK.
- **Photo Optimizer / Video Optimizer / Sleep Mode.** Landing pages exist. Sleep
  Mode in particular cannot be built with public APIs — see below.
- **Illustrations.** The line-art drawings are represented by `IllustrationTile`,
  a single composable in `Onboarding.kt`. Drop real vector assets in there and
  every screen picks them up.

## Known limits — things Android will not let any app do

These are platform limits, not gaps in the code. The reference app hits exactly
the same walls, which is why it locks these behind premium or an Accessibility
Service:

- **Clearing another app's cache.** There is no public API. `CLEAR_APP_CACHE` is
  a system permission. This is why *Hidden caches* is measured but not deletable,
  and why the row deep-links to that app's storage screen instead. AVG's "Deep
  Clean" drives the system UI through an Accessibility Service.
- **Clearing another app's browser data.** Same reason.
- **Force-stopping apps** (Sleep Mode). No public API; same Accessibility Service
  trick applies.
- **Silent uninstall.** Every uninstall shows Android's own dialog.
- **Per-app battery drain.** Not exposed since Android 8; the Battery drainer
  tile uses foreground screen time as the closest legitimate proxy.

If you want the Accessibility Service route, that is the single biggest piece of
work left, and it needs a clear disclosure to users plus a Play Store
justification — Google rejects Accessibility use that is not declared properly.

---

## How photo analysis works

`PhotoAnalyzer` decodes each photo down to a 32×32 grayscale array, so a few
hundred photos analyse in seconds and no pixels leave the device:

- **Similar** — an 8×8 difference hash, grouped by Hamming distance ≤ 6.
- **Bad quality** — variance of a 3×3 Laplacian (the standard cheap blur score),
  plus a mean-luminance check for too-dark and blown-out shots.
- **Sensitive** — screenshots, by bucket name and filename.
- **Old** — added more than a year ago.
- **Optimizable** — large pixel dimensions on a large file, worth re-encoding.

Thresholds are constants at the top of the class. If it flags too much or too
little on your library, `BLUR_THRESHOLD` and `SIMILAR_DISTANCE` are the two dials
worth turning.

---

## Project layout

```
app/src/main/java/com/replica/cleaner/
├── MainActivity.kt            entry point, theme + nav host
├── CleanerApp.kt              notification channels
├── core/
│   ├── Formatting.kt          byte/duration/date formatting
│   ├── Permissions.kt         the three permissions + settings intents
│   └── Prefs.kt               every setting, DataStore-backed
├── data/
│   ├── model/Models.kt        all domain types
│   ├── scan/
│   │   ├── JunkScanner.kt     filesystem walk, sizes, deletion
│   │   ├── MediaScanner.kt    MediaStore queries + deletion
│   │   ├── PhotoAnalyzer.kt   blur / duplicate / age heuristics
│   │   ├── AppScanner.kt      PackageManager + UsageStats + StorageStats
│   │   └── DeviceScanner.kt   storage totals, System Info
│   ├── TipsEngine.kt          builds the numbered tip cards
│   └── CleanerRepository.kt   single entry point, caches the last scan
├── work/
│   ├── AutoCleanWorker.kt     scheduled cleaning
│   ├── Notifications.kt       channels + posting
│   └── BootReceiver.kt        re-arms the schedule after reboot
└── ui/
    ├── CleanerViewModel.kt    all screen state
    ├── theme/                 colours, typography, light/dark + accents
    ├── components/            cards, rows, buttons, charts, scaffolds
    ├── nav/                   routes + nav graph
    └── screens/               one file per area
```

`CleanerViewModel` holds every piece of screen state, and `CleanerRepository`
caches the last scan so moving between Home, Quick Clean, Storage and Tips does
not re-walk the filesystem. A clean invalidates that cache.

---

## Things worth doing next

In rough order of payoff:

1. Run it on a device and check the junk scan numbers against a known folder.
   The build is verified but the *runtime behaviour* is not — nothing here has
   been exercised on real hardware yet. `JunkScanner.MAX_DEPTH` (12) and the
   `AD_CACHE_HINTS` list are the two things most likely to need tuning for your
   storage layout.
2. Real vector illustrations in `IllustrationTile`.
3. Google Play Billing, replacing the `vm.setPremium(true)` call in
   `PremiumScreen`.
4. Per-item file icons in Quick Clean (currently a coloured placeholder square).
5. `Prefs.language` is stored but not applied — wiring it means
   `AppCompatDelegate.setApplicationLocales` plus translated `strings.xml`.
6. Tests. There are none. `PhotoAnalyzer.dHash` / `laplacianVariance` and
   `Formatting.kt` are pure functions and the obvious first targets.

## Privacy

Every scan is local. File paths, photo pixels and app usage never leave the
device. There is no analytics SDK, no crash reporter and no network permission
use beyond opening a Play Store link.
