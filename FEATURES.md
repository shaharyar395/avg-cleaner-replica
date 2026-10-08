# AVG Cleaner+ — Complete Feature List

**App name:** AVG Cleaner+  
**Package:** `com.mob.ccleaner.storage.avg.cleaner.phone.androidcleaner`  
**Latest build documented:** 1.0.51  
**Privacy policy:** https://sites.google.com/view/mob-apps-inc/privacy-policy  

This document lists **every feature, screen, and setting** in the app.

---

## 1. First-run / onboarding

1. **Splash** — brand flash, then routes by saved flags (clearing app data restarts this).
2. **Get started** — legal copy, Privacy Policy link, GET STARTED, ALREADY PURCHASED.
3. **Updating cleaning database** — short progress screen.
4. **Intro carousel** — product intro pages.
5. **First-run Premium gate** — YEARLY / MONTHLY plan cards (first-run UI only).
6. **Onboarding steps** — access + scan explanation, SEE RESULTS.
7. **Permissions** — All files access, Usage access, Notifications (GO TO SETTINGS / ALLOW / NOT NOW).
8. **Permission needed** — if a special grant is still missing.
9. **First junk scan** — scanning animation.
10. **Ready** — first Quick Clean or continue-with-ads.
11. **Continue with ads** — free path after declining Premium.
12. **Consent** — personalized ads vs upgrade.

---

## 2. Bottom navigation

| Tab | What it is |
|---|---|
| **Home** | Storage dashboard + Quick Clean + shortcuts |
| **Tools** | All cleaner tools grouped by job |
| **Storage** | Media donut, photos/video/audio/files, photo analysis |
| **Account** | Premium, sign-in, settings, themes, about |

Home (non-premium) also shows an **AdMob banner** above the tab bar.

---

## 3. Home

- Live **free / used space** and segmented bar (unneeded, hidden caches, files to review).
- **QUICK CLEAN** button with estimated reclaimable size.
- **Customize** — which Quick Clean categories are ticked.
- **Sleep Mode** shortcut (premium-locked).
- **Tips** shortcut.
- **Media** shortcut (photos/video overview).
- **Apps** shortcut (Apps Overview via ads gate).
- **Auto Cleaning** shortcut.
- **UPGRADE** pill when not premium.
- **AVG Antivirus / VPN** promo rows (opens Play Store if tapped from Tools; Home may show related cards).
- Refreshes storage and permissions when the app resumes.

---

## 4. Quick Clean (core junk cleaner)

### Scan categories (real on-device scan)

**Always available (not premium-locked):**
1. Visible caches  
2. Residual files (leftovers after uninstall)  
3. Installed APKs  
4. Ad caches  
5. Thumbnails  
6. Empty folders  

**Also scanned / listed:**
7. Hidden caches (size is real; deleting other apps’ private cache is an Android limit)  
8. Browser data (size is real; same platform limit)  
9. Downloads / files to review (listed unticked so nothing deletes by accident)  
10. Trash  
11. Large / old files  
12. App data leftovers (where found)

### Flow
- Select / deselect categories and items.
- Run **FINISH CLEANING** — deletes selected junk files that Android allows.
- Cleaning animation → **Advanced issues** → optional **Resolve all** (premium) → **Space cleaned** results.
- Detailed cleaning results list.
- Settings gear → Quick Clean category toggles.

---

## 5. Tools tab

### Remove junk
| Tool | Free / Premium | What it does |
|---|---|---|
| Quick Clean | Free | Same as Home Quick Clean |
| Deep Clean | Premium | Hidden junk / hidden memory upsell + flow |
| Browser Cleaner | Premium | Browser clutter upsell + flow |
| Auto Cleaning | Premium | Scheduled automatic junk cleaning |

### Make more room
| Tool | Free / Premium | What it does |
|---|---|---|
| Cloud Transfers | Free UI | Pick files and hand them to Drive / Dropbox / OneDrive apps |
| Photo Optimizer | Premium | Re-encodes photos (JPEG, smaller resolution) and replaces originals |
| Video Optimizer | Premium | Transcodes large videos (~720p / ~1.5 Mbps) and replaces originals when smaller |

### Reduce load
| Tool | Free / Premium | What it does |
|---|---|---|
| System Info | Free | Device, uptime, network, RAM, storage stats |
| Sleep Mode | Premium | Pick apps and open Android force-stop / app settings |

### More by AVG (Play Store links)
- AVG Antivirus (`com.antivirus`)
- AVG Secure VPN (`com.avg.android.vpn`)
- Alarm Clock Xtreme (`com.apalon.alarmclock.weather`)  
Shows Installed / Not installed.

---

## 6. Photo Optimizer (premium)

- Lists large / “optimizable” photos from the gallery.
- Select all / deselect, before vs estimated-after sizes.
- **OPTIMIZE PHOTOS** — compresses and overwrites (or writes `Pictures/CleanerOptimized` if in-place write fails).
- Progress % and result: how many optimized, bytes freed, skipped.
- Needs **All files access** (and media permission) to actually shrink files.

---

## 7. Video Optimizer (premium)

- Lists large videos (about 8 MB+).
- Select clips, then **OPTIMIZE VIDEOS**.
- Re-encodes to smaller MP4 and replaces the original when the new file is smaller.
- Progress % and bytes-freed summary.

---

## 8. Storage tab

- Donut / breakdown: **Photos, Video, Audio, Other files, Apps**.
- Open each category through a short **ads gate** (non-premium), then a grid.
- **Photos** grid — sort by date or size, multi-select, delete via MediaStore.
- **Video** grid — same, with video thumbnails.
- **Audio** grid.
- **Other files** grid.
- **Photo analysis** (on-device, no upload):
  - Similar photos (perceptual hash)
  - Bad quality (blur / dark / blown-out)
  - Sensitive / screenshots
  - Old photos (older than ~1 year)
  - Optimizable (large dimensions / large file)
- **Apps** row → Apps Overview.
- **Tips** entry from storage.
- Requests media permissions if missing.

---

## 9. Apps

- **Apps gate** (commercial / ads, then content).
- **Apps Overview** — installed vs system counts, storage used, drainers.
- **Screen time** — last 7 days bar chart (Usage access).
- **Times opened / unused apps**.
- **App lists:** all, unused, drainers, etc.
- **App Manager** — sort, multi-select, uninstall (system confirmation), open app info.
- Battery-drainer ranking uses **foreground screen time** (Android does not expose true per-app battery).

---

## 10. Tips

- Numbered space-saving tips generated from the latest junk / media / apps scan.
- Ordered by **Analysis preferences → tip priority**.
- Actions jump to Quick Clean, photo grids (bad / similar / old), or app lists.
- Premium-locked tips show lock + upgrade.

---

## 11. System Info

Live device facts:
- Model / Android version  
- Uptime  
- RAM  
- Internal storage  
- Wi-Fi / IP  
- Bluetooth  
- Mobile data  

---

## 12. Cloud Transfers

- Lists recent media to send.
- Connects conceptually to **Dropbox, Google Drive, OneDrive** (opens those apps; no OAuth SDK).
- Share sheet with selected URIs.
- Settings:
  - Delete files after transfer
  - Upload only on Wi-Fi
- Cloud provider rows in Settings → Cloud services.

---

## 13. Sleep Mode (premium)

- Paywall / FAQ if not premium.
- If premium: list of installed apps, multi-select, force-stop via system app screens.
- Android does **not** allow silent force-stop of other apps without Accessibility Service.

---

## 14. Deep Clean & Browser Cleaner (premium)

- Feature landing / upsell with FAQs.
- Tied to hidden caches and browser-data sizes from the junk scan.
- Actual deletion of *other apps’ private caches / browser profiles* is blocked by Android (same as real AVG without Accessibility).

---

## 15. Auto Cleaning (premium)

- Master switch.
- Categories: junk files, downloads, screenshots, optimized originals (with keep-N-days).
- Frequency (e.g. Daily).
- Notification when a run would exceed a size threshold.
- WorkManager job + BootReceiver to re-arm after reboot.
- Per-category auto-clean screens.

---

## 16. Account tab

- Total space freed lifetime.
- Signed-in email (or Sign in).
- **Upgrade to Premium**.
- **Explore Premium Features** (pager tour + Learn More / Upgrade).
- **Redeem subscription** / activation code screens.
- **Settings**.
- **Themes**.
- **Help & Feedback** (opens support site).
- **About this app** — version, licenses, Simulate Premium (debug/local).

---

## 17. Sign-in & account

- Email + password (stored locally, not AVG cloud).
- Sign up + create password.
- Forgot password flow (local reset UI).
- **Sign in with Google** — system Google account picker; saves chosen email locally.
- Shows signed-in account on Account tab.
- Sign out.
- Apple sign-in message: not available on Android.
- Privacy Policy + terms links on auth screens.

---

## 18. Premium / paywall

- **First-run plans:** YEARLY white cards.
- **Upgrade paywall:** Ignore this offer, YEARLY/MONTHLY, Plus cards.
- **Exclusive 10%** sheet if user backs out of upgrade.
- Google Play–style payment sheet (local confirm; **Play Billing not wired**).
- CONTINUE sets a **local premium flag** (unlocks tools in-app).
- **Explore Premium Features** pages:
  1. Deep Clean  
  2. Auto Cleaning  
  3. Browser Cleaner  
  4. Sleep Mode  
  5. Custom dashboard  
  6. Photo Optimizer  
  7. Video Optimizer  
- Learn More opens feature-specific upsell; Optimizer pages go to Upgrade.
- Redeem subscription / already purchased.
- Privacy Policy in legal footers.

**Premium feature list on plans includes:** no ads, Deep Clean, Auto Cleaning, Browser Cleaner, Sleep Mode, Custom dashboard, Photo Optimizer, Video Optimizer, extra cleaner/antivirus marketing rows.

---

## 19. Settings (full tree)

### Quick Clean
- Toggle each junk category that Home/Tools Quick Clean will scan.

### Analysis preferences
- Find unwanted photos (similar / bad / optimizable scan).
- Scan SD card.
- Tip priority order (Junk, Device memory, Apps, Photos and video, Other files).

### Notifications
- Master notifications + reports.
- Channels: Junk cleaning, Applications, Photos, Other files, Cleaning progress.
- Frequency / report day.
- New installs alerts.
- Per-category notification threshold (MB).

### Realtime detection
- App leftovers after uninstall.
- Battery monitoring insights.

### Cloud services
- Provider list (Dropbox / Drive / OneDrive).
- Delete after transfer.
- Wi-Fi only upload.

### Personal privacy
- Privacy Policy (MOBAPPS URL).
- Product Policy text.
- Share usage with AVG (toggle).
- Share usage with 3rd-party analytics (toggle).

### Language
31 languages, including RTL Arabic:

Català, Dansk, Deutsch, English, Español, Français, Indonesia, Italiano, Magyar, Nederlands, Norsk bokmål, Polski, Português, Português (Portugal), Română, Slovenčina, Suomi, Svenska, Türkçe, Čeština, Ελληνικά, Български, Русский, Українська, العربية, हिन्दी, ไทย, 한국어, 中文 (简体), 日本語, Tiếng Việt.

### Themes
- Dark / Light / System.
- Accent: Green, Blue, Purple (Purple may be premium).
- Live preview + apply.

---

## 20. Ads (non-premium)

| Format | Wired |
|---|---|
| Banner (Home) | Yes |
| Interstitial (feature enter/exit, tab change) | Yes |
| Rewarded / App Open / Native unit IDs | Stored for later |
| Transition overlay | Full-screen interstitial, fallback large banner |

Production AdMob App ID + units from MOBAPPS DATA CLEANER sheet.  
Premium hides ads.

---

## 21. Permissions the app uses

- All files access (`MANAGE_EXTERNAL_STORAGE`) — junk + optimizer writes  
- Usage access — app sizes, screen time, unused apps  
- Notifications  
- Photos / video / audio  
- Internet (ads + opening links)  
- Query all packages (app list)  
- Get accounts (Google picker)  
- Bluetooth / Wi-Fi (System Info)  
- Boot completed (auto-clean schedule)

---

## 22. What is UI-complete but not a real backend

| Area | Status |
|---|---|
| Google Play Billing | Local premium flag only |
| Google / email account | Local session, no AVG cloud |
| Cloud SDKs | Opens installed apps / share sheet |
| Firebase / Crashlytics | Not included until `google-services.json` |
| AppLovin MAX | Not used (AdMob mediation only) |
| Silent force-stop / clear other app cache | Android platform limit |

---

## 23. Feature map (every route)

Splash → Get started → Updating DB → Intro → Premium gate → Onboarding → Permissions → Scanning → Ready → Quick Clean / Continue with ads → **Home / Tools / Storage / Account** plus:

Quick Clean, Cleaning, Advanced issues, Cleaning results, Space cleaned, Continue ads, Media overview, Photo/video/audio/file grids, Storage ads gate, Apps gate, Apps overview, App lists, Tips, System info, Cloud transfers, Settings (all subpages), Auto cleaning (+ category), Themes, Premium, Premium resolve, Premium features, Feature detail, Feature upsell, Sign in, Redeem, Redeem activation, About, Licenses.

---

*AVG Cleaner+ is a functional replica of the AVG Cleaner product UI. It is not an official AVG product.*
