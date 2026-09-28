# -*- coding: utf-8 -*-
"""Fix broken phraseIdentity commas and expand packs using translators (Bing)."""
from __future__ import annotations

import json
import re
import time
from pathlib import Path

import translators as ts

ROOT = Path(__file__).resolve().parents[1]
L10N = ROOT / "app/src/main/java/com/replica/cleaner/l10n/L10nPhrases.kt"
CACHE = ROOT / "tools/translation_cache_bing.json"

NEW_KEYS = [
    "items", "%d space saving tips", "Tip %d", "TRANSFER %d FILES", "PUT %d APPS TO SLEEP",
    "On", "Off", "Wi-fi", "Up-time", "Folders with nothing inside", "Your app diary",
    "How much time you spent in each app?", "Rarely used apps", "Not used",
    "Keep the best, drop the rest", "Added more than a year ago", "Big files",
    "Found a few big items. Take a look.", "%d empty folders", "%d files in Downloads",
    "%s can be reviewed", "Free up to %s", "You have spent least time using %s",
    "%d bad photos found", "%s can be cleaned", "%d screenshots found", "%d similar photos",
    "%d old photos", "%d optimizable images", "Get %s more space", "%d videos using %s",
    "Quick Clean", "Unneeded files", "Files to review", "Hidden caches", "Media", "Photos",
    "Audio", "Video", "Cloud Transfers", "Photo Optimizer", "Video Optimizer", "Sleep Mode",
    "Deep Clean", "Browser Cleaner", "System Info",
    "Shrink your photos and free up storage space while keeping the memories.",
    "Lighter videos, same moments. Free space by reviewing oversized clips.",
    "Large videos use the most space. Select clips to remove local copies after you've backed them up.",
    "Pick apps to force-stop. Android will confirm each one in system settings.",
    "Deep Clean walks hidden caches and browser leftovers. Start Quick Clean to review and remove what Android allows.",
    "Browser data sizes are measured in Quick Clean. Open the locked Browser data row to jump into each browser's storage screen.",
    "Sleep Mode uses Force stop action and puts apps to sleep until you need them again. Force stopped apps can't access system resources and thus have no effect on battery, data, memory, and storage space. You can wake up stopped apps by reopening them any time.",
    "Force stop stops all app processes on the spot. Usually you need to open your settings to Force stop each app one-by-one, but Sleep Mode lets you stop multiple apps at once.",
    "Nope! When you force stop apps, there's no risk of losing data, preferences, or account info.\n\nBut don't forget — force stopped apps can't send you notifications or work in the background, so you shouldn't force stop apps that send important info (like security or messaging apps).",
    "There are a few exception apps that can wake themselves up without being opened (like Facebook Messenger and some system apps), but most apps will stay force stopped until you need them.",
    "Temporary files that are deep in your app settings and more difficult to remove.",
    "Saved data collected by your browsers when you browse or search online.",
    "Temporary files that can be recreated.",
    "Leftover installation files after new apps are installed.",
    "Small preview versions of your photos.",
    "Folders with nothing inside.",
    "Files you downloaded and may no longer need.",
    "You're not signed in", "You're using the free version of Cleanup.",
    "You've freed %s with Cleaner so far.", "%d permissions missing", "OR", "Scanning photos",
    "Apps create temporarily needed files called 'caches', but afterward, when you don't need them anymore, caches just take up space. Hidden caches (sometimes called private caches) take up much more space and are more tricky to delete, while visible caches take up less space and can be removed easily. Usually, you need to open your settings to remove hidden app caches one-by-one, but Deep Clean lets you clean hidden caches for many apps simultaneously.",
    "You can clean individual hidden caches by going to App detail > Clean cache in your system settings. Deep Clean does the same thing, but instead of one-by-one, it will clean all the apps you select at once.",
    "Nope! There's nothing in your cache files that your apps can't recreate if needed.\n\nBut be careful to clean up these files regularly, otherwise your device can collect a lot of junk files over time.",
    "Auto Cleaning schedules Quick Clean and related cleanup so junk is removed without opening the app each time.",
    "Yes. You can set how often Auto Cleaning runs and which categories it includes.",
    "Yes. It only removes junk categories you allow — personal files stay untouched.",
    "When you're ready to clean, Browser Cleaner navigates your device settings and taps on all the right buttons on your behalf. This automated process allows us to find and delete your browser data fast and safely.",
    "For sure! Browsing records are not important to your device or how it works, so you probably won't notice that they're gone.",
    "Yes! However, cleaning Google Search data may have some unintended consequences. Our testing showed you might need to grant some permissions again. Nothing destructive, just a bit inconvenient — that's why this item is never pre-selected for cleaning.",
    "Add custom shortcuts to your dashboard for quick access to your favorite info.",
    "It lets you pin shortcuts to the tools and info you use most on Home.",
    "Yes. Customize which tiles appear and their order.",
    "Yes. Custom dashboard is included with Premium.",
    "Auto Cleaning",
    "If your subscription is linked to this account, it will activate automatically.",
    "Enter the code you received after purchase from another device or AVG app.",
    "If you've purchased a subscription from Google Play, this can restore it.",
    "Get more info about AVG subscriptions and how to redeem them ",
    "Enter the activation code from your purchase receipt or another AVG product.",
    "Redeem subscription", "Privacy Policy", "Settings", "Personal privacy",
    "Share app-usage data with AVG to help us with new product development.",
    "Share app-usage data with 3rd-party analytics tools to improve this app.",
    "Automatically scan for bad, similar or optimizable photos you might want to clean.",
    "Include SD card when scanning storage space. This may slow down the scanning speed.",
    "Choose how often you want to receive notifications.",
    "An up-to-date report of newly installed applications. Delivered once a week.",
    "Enable %s notifications", "Notify me when you clean this much or more.",
    "Let me know if there's unimportant data left behind after uninstalling an app.",
    "Measure how I use my battery so I can see insights about how to save power.",
    "Select how often we should clean automatically.",
    "Photos that show what's visible on your device display at the moment they're taken.",
    "The original photos that were used to create optimized duplicates.",
    "Sent when less than 5% of your storage space is left.",
    "Sent when there's 10 MB or more that you can safely clean.",
    "Sent when apps have not been opened in a month.",
    "Sent when an app's storage grows unusually fast.",
    "Sent when new applications are installed.",
    "Sent when blurry or dark photos are detected.",
    "Sent when near-duplicate shots pile up.",
    "Sent when screenshots take up noticeable space.",
    "Sent when there's at least 4 document files in your Download folder.",
    "Sent when at least 4 files have occupied 50 MB or more space in the past week.",
    "Sent when at least 4 videos have occupied 50 MB or more in the past week.",
    "Best recommendations for you.",
    "Temporary files that make ads work.",
    "Leftover files after you uninstall apps from your device.",
    "Files that you already moved to the Trash folder.",
    "Files that you downloaded from the internet.",
    "Photos from your camera that we detected as blurry, dark, or low quality.",
    "Files that are at least 100 MB and were created at least one month ago.",
    "Includes log files, junk files imported from other systems, and other temporary data.",
    "Your payment information is only visible to Google.",
    "A powerful cleaner that removes hidden junk.",
    "Clear all your browsing records for more space and privacy.",
    "Force stops unused apps to optimize your device.",
    "Get warned when a link is dangerous",
    "Schedule automatic scans to stay safer",
    "Protect personal photos with unlimited coverage",
    "Receive quick replies from support",
    "Go Premium to shrink your photos and free up storage space.",
    "Unlock the Video Optimizer and cut your largest files down to size.",
    "Auto Cleaning only works for items that are safe to delete without review. Since Auto Cleaning runs in the background, you can't double-check before cleaning items in the categories you select.\n\nTo make sure we don't remove something you need, we don't automatically clean similar photos, unused apps, or large videos.",
    "Themes",
    "and ",
    "Your subscription renews unless canceled.",
]

PACK_LANG = {
    "phrasesEs": "es", "phrasesDe": "de", "phrasesFr": "fr", "phrasesIt": "it",
    "phrasesPt": "pt", "phrasesRu": "ru", "phrasesAr": "ar", "phrasesHi": "hi",
    "phrasesZh": "zh-CN", "phrasesJa": "ja", "phrasesKo": "ko", "phrasesTr": "tr",
    "phrasesVi": "vi", "phrasesId": "id", "phrasesNl": "nl", "phrasesPl": "pl",
    "phrasesUk": "uk", "phrasesTh": "th", "phrasesEl": "el",
}


def parse_kotlin_map(body: str) -> dict[str, str]:
    entries: dict[str, str] = {}
    i, n = 0, len(body)

    def read_string(pos: int):
        assert body[pos] == '"'
        pos += 1
        out = []
        while pos < n:
            c = body[pos]
            if c == "\\":
                pos += 1
                esc = body[pos] if pos < n else ""
                out.append({"n": "\n", "t": "\t", "r": "\r", '"': '"', "\\": "\\"}.get(esc, esc))
                pos += 1
                continue
            if c == '"':
                return "".join(out), pos + 1
            out.append(c)
            pos += 1
        return "".join(out), pos

    while i < n:
        q = body.find('"', i)
        if q < 0:
            break
        key, j = read_string(q)
        rest = body[j:].lstrip()
        if not rest.startswith("to "):
            i = j
            continue
        j = body.find('"', j)
        if j < 0:
            break
        val, j = read_string(j)
        entries[key] = val
        i = j
    return entries


def escape_kt(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")


def format_map(entries: dict[str, str], name: str) -> str:
    lines = [f"private val {name}: Map<String, String> = mapOf("]
    for k in sorted(entries.keys(), key=lambda x: x.casefold()):
        lines.append(f'    "{escape_kt(k)}" to "{escape_kt(entries[k])}",')
    lines.append(")")
    return "\n".join(lines)


def protect(text: str):
    markers = []
    def repl(m):
        markers.append(m.group(0))
        return f"@@{len(markers)-1}@@"
    tmp = re.sub(r"%[ds]|AVG|Wi-Fi|Wi-fi|APKs|FAQ|VPN|SD|IP|CPU|SSID", repl, text)
    return tmp, markers


def unprotect(text: str, markers):
    for i, m in enumerate(markers):
        text = text.replace(f"@@{i}@@", m)
        text = text.replace(f"@{i}@", m)
    return text


def translate_one(text: str, lang: str) -> str:
    if not text.strip():
        return text
    # Brand / identical
    if text in {"AVG", "Dropbox", "Google Play", "Google Drive", "Android", "Premium", "OR"}:
        return text
    tmp, markers = protect(text)
    for attempt in range(4):
        try:
            out = ts.translate_text(tmp, translator="bing", from_language="en", to_language=lang)
            if out:
                return unprotect(out, markers)
        except Exception as e:
            wait = 1.5 * (attempt + 1)
            print(f"  retry {attempt+1} {lang}: {e}")
            time.sleep(wait)
    return text


def main():
    text = L10N.read_text(encoding="utf-8")
    # Parse identity even with missing commas (parser ignores commas)
    m = re.search(
        r"internal val phraseIdentity: Map<String, String> = mapOf\((.*?)\)\n\ninternal fun phrasesFor",
        text,
        re.S,
    )
    identity = parse_kotlin_map(m.group(1))
    for k in NEW_KEYS:
        identity.setdefault(k, k)
    print("identity", len(identity))

    pack_re = re.compile(
        r"private val (phrases\w+): Map<String, String> = mapOf\((.*?)\)\n(?=\nprivate val|\Z)",
        re.S,
    )
    packs = {pm.group(1): parse_kotlin_map(pm.group(2)) for pm in pack_re.finditer(text)}

    cache = json.loads(CACHE.read_text(encoding="utf-8")) if CACHE.exists() else {}

    expanded = {}
    for pack, lang in PACK_LANG.items():
        existing = packs.get(pack, {})
        pack_cache = cache.setdefault(pack, {})
        final = {}
        missing = []
        for k in identity:
            if k in existing and existing[k] != k:
                final[k] = existing[k]
            elif k in pack_cache and pack_cache[k] != k:
                final[k] = pack_cache[k]
            else:
                missing.append(k)
        print(f"\n{pack} translate {len(missing)} (have {len(final)})")
        for i, k in enumerate(missing):
            tr = translate_one(k, lang)
            final[k] = tr
            pack_cache[k] = tr
            if (i + 1) % 25 == 0:
                CACHE.write_text(json.dumps(cache, ensure_ascii=False, indent=0), encoding="utf-8")
                print(f"  {i+1}/{len(missing)}")
            time.sleep(0.35)
        expanded[pack] = final
        CACHE.write_text(json.dumps(cache, ensure_ascii=False, indent=0), encoding="utf-8")
        print(f"{pack} done {len(final)}")

    id_lines = ["internal val phraseIdentity: Map<String, String> = mapOf("]
    for k in sorted(identity.keys(), key=lambda x: x.casefold()):
        id_lines.append(f'    "{escape_kt(k)}" to "{escape_kt(identity[k])}",')
    id_lines.append(")")

    phrases_for_m = re.search(
        r"internal fun phrasesFor\(tag: String\): Map<String, String> = when \(tag\) \{.*?\n\}\n\n",
        text,
        re.S,
    )
    before = text[: m.start()]
    pack_blocks = [format_map(expanded[p], p) + "\n" for p in PACK_LANG]
    new_text = before + "\n".join(id_lines) + "\n\n" + phrases_for_m.group(0) + "\n".join(pack_blocks).rstrip() + "\n"
    L10N.write_text(new_text, encoding="utf-8")
    print("WROTE", L10N)
    for p, d in expanded.items():
        print(p, len(d))


if __name__ == "__main__":
    main()
