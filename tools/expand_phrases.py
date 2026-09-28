# -*- coding: utf-8 -*-
"""Expand L10nPhrases.kt packs to cover all phraseIdentity keys (+ new tip/UI keys)."""
from __future__ import annotations

import json
import re
import time
from pathlib import Path

from deep_translator import GoogleTranslator

ROOT = Path(__file__).resolve().parents[1]
L10N = ROOT / "app/src/main/java/com/replica/cleaner/l10n/L10nPhrases.kt"
CACHE = ROOT / "tools/translation_cache.json"

NEW_KEYS = [
    "items",
    "%d space saving tips",
    "Tip %d",
    "TRANSFER %d FILES",
    "PUT %d APPS TO SLEEP",
    "On",
    "Off",
    "Wi-fi",
    "Up-time",
    "Folders with nothing inside",
    "Your app diary",
    "How much time you spent in each app?",
    "Rarely used apps",
    "Not used",
    "Keep the best, drop the rest",
    "Added more than a year ago",
    "Big files",
    "Found a few big items. Take a look.",
    "%d empty folders",
    "%d files in Downloads",
    "%s can be reviewed",
    "Free up to %s",
    "You have spent least time using %s",
    "%d bad photos found",
    "%s can be cleaned",
    "%d screenshots found",
    "%d similar photos",
    "%d old photos",
    "%d optimizable images",
    "Get %s more space",
    "%d videos using %s",
    "Quick Clean",
    "Unneeded files",
    "Files to review",
    "Hidden caches",
    "Media",
    "Photos",
    "Audio",
    "Video",
    "Cloud Transfers",
    "Photo Optimizer",
    "Video Optimizer",
    "Sleep Mode",
    "Deep Clean",
    "Browser Cleaner",
    "System Info",
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
    "You're not signed in",
    "You're using the free version of Cleanup.",
    "You've freed %s with Cleaner so far.",
    "%d permissions missing",
    "OR",
    "Scanning photos",
    # FeatureUpsell FAQ bodies / titles often still English
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
    "Redeem subscription",
    "Privacy Policy",
    "Settings",
    "Personal privacy",
    "Share app-usage data with AVG to help us with new product development.",
    "Share app-usage data with 3rd-party analytics tools to improve this app.",
    "Automatically scan for bad, similar or optimizable photos you might want to clean.",
    "Include SD card when scanning storage space. This may slow down the scanning speed.",
    "Choose how often you want to receive notifications.",
    "An up-to-date report of newly installed applications. Delivered once a week.",
    "Enable %s notifications",
    "Notify me when you clean this much or more.",
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
]

# deep-translator target codes
LANG_TARGETS = {
    "phrasesEs": "es",
    "phrasesDe": "de",
    "phrasesFr": "fr",
    "phrasesIt": "it",
    "phrasesPt": "pt",
    "phrasesRu": "ru",
    "phrasesAr": "ar",
    "phrasesHi": "hi",
    "phrasesZh": "zh-CN",
    "phrasesJa": "ja",
    "phrasesKo": "ko",
    "phrasesTr": "tr",
    "phrasesVi": "vi",
    "phrasesId": "id",
    "phrasesNl": "nl",
    "phrasesPl": "pl",
    "phrasesUk": "uk",
    "phrasesTh": "th",
    "phrasesEl": "el",
}


def parse_kotlin_map(body: str) -> dict[str, str]:
    entries: dict[str, str] = {}
    i = 0
    n = len(body)

    def read_string(pos: int) -> tuple[str, int]:
        assert body[pos] == '"'
        pos += 1
        out = []
        while pos < n:
            c = body[pos]
            if c == "\\":
                pos += 1
                if pos >= n:
                    break
                esc = body[pos]
                mapping = {"n": "\n", "t": "\t", "r": "\r", '"': '"', "\\": "\\"}
                out.append(mapping.get(esc, esc))
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
    return (
        s.replace("\\", "\\\\")
        .replace('"', '\\"')
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
    )


def format_map(entries: dict[str, str], name: str) -> str:
    lines = [f"private val {name}: Map<String, String> = mapOf("]
    for k in sorted(entries.keys(), key=lambda x: x.casefold()):
        lines.append(f'    "{escape_kt(k)}" to "{escape_kt(entries[k])}",')
    # drop trailing comma on last entry for style consistency with Kotlin trailing commas (allowed)
    lines.append(")")
    return "\n".join(lines)


def translate_batch(texts: list[str], target: str, cache: dict, pack: str) -> dict[str, str]:
    out: dict[str, str] = {}
    pending: list[str] = []
    pack_cache = cache.setdefault(pack, {})
    for t in texts:
        if t in pack_cache:
            out[t] = pack_cache[t]
        else:
            pending.append(t)

    if not pending:
        return out

    translator = GoogleTranslator(source="en", target=target)
    # Translate in chunks of ~25 to avoid length limits
    chunk_size = 20
    for i in range(0, len(pending), chunk_size):
        chunk = pending[i : i + chunk_size]
        for text in chunk:
            try:
                # Preserve placeholders roughly by protecting them
                protected = text
                placeholders = []
                for ph in ("%d", "%s", "%s\\n", "\\n"):
                    pass
                # Protect %d and %s
                tmp = text
                markers = []

                def protect(m):
                    markers.append(m.group(0))
                    return f"⟦{len(markers)-1}⟧"

                tmp = re.sub(r"%[ds]|AVG|Wi-Fi|Wi-fi|APKs|FAQ|VPN|SD|IP|CPU|SSID|MB|GB", protect, tmp)
                translated = translator.translate(tmp)
                if translated is None:
                    translated = text
                for idx, marker in enumerate(markers):
                    translated = translated.replace(f"⟦{idx}⟧", marker)
                    translated = translated.replace(f"[{idx}]", marker)
                # Fix common placeholder damage
                translated = translated.replace("% д", "%d").replace("% с", "%s")
                translated = translated.replace("%د", "%d").replace("%س", "%s")
            except Exception as e:
                print(f"  translate fail [{target}]: {e} :: {text[:60]}")
                translated = text
            out[text] = translated
            pack_cache[text] = translated
            time.sleep(0.12)
        print(f"  {pack}/{target}: {min(i+chunk_size, len(pending))}/{len(pending)}")
        CACHE.write_text(json.dumps(cache, ensure_ascii=False, indent=2), encoding="utf-8")
    return out


def main():
    text = L10N.read_text(encoding="utf-8")
    m = re.search(
        r"(internal val phraseIdentity: Map<String, String> = mapOf\()(.*?)(\)\n\ninternal fun phrasesFor)",
        text,
        re.S,
    )
    if not m:
        raise SystemExit("phraseIdentity not found")

    identity = parse_kotlin_map(m.group(2))
    print(f"identity before: {len(identity)}")
    for k in NEW_KEYS:
        identity.setdefault(k, k)
    # Android already in identity usually
    identity.setdefault("Android", "Android")
    identity.setdefault("Unnecessary data", "Unnecessary data")
    identity.setdefault("Large videos", "Large videos")
    print(f"identity after: {len(identity)}")

    # Rebuild identity block (sorted for stability)
    id_lines = ["internal val phraseIdentity: Map<String, String> = mapOf("]
    for k in sorted(identity.keys(), key=lambda x: x.casefold()):
        id_lines.append(f'    "{escape_kt(k)}" to "{escape_kt(identity[k])}",')
    id_lines.append(")")
    new_identity = "\n".join(id_lines)

    # Parse existing packs
    pack_re = re.compile(
        r"private val (phrases\w+): Map<String, String> = mapOf\((.*?)\)\n(?=\nprivate val|\Z)",
        re.S,
    )
    packs: dict[str, dict[str, str]] = {}
    for pm in pack_re.finditer(text):
        packs[pm.group(1)] = parse_kotlin_map(pm.group(2))
        print(f"existing {pm.group(1)}: {len(packs[pm.group(1)])}")

    cache = {}
    if CACHE.exists():
        cache = json.loads(CACHE.read_text(encoding="utf-8"))

    all_keys = list(identity.keys())
    expanded: dict[str, dict[str, str]] = {}
    for pack, target in LANG_TARGETS.items():
        existing = packs.get(pack, {})
        missing = [k for k in all_keys if k not in existing]
        print(f"\n=== {pack} missing {len(missing)} ===")
        translated = translate_batch(missing, target, cache, pack) if missing else {}
        merged = dict(existing)
        for k in all_keys:
            if k in merged:
                continue
            merged[k] = translated.get(k, k)
        # Ensure every identity key present
        assert len(merged) >= len(all_keys)
        # Only keep identity keys (+ any extras already there that are fine)
        final = {k: merged[k] for k in all_keys}
        # Prefer existing translation if present
        for k, v in existing.items():
            if k in final:
                final[k] = v
        expanded[pack] = final
        print(f"{pack} final: {len(final)}")

    CACHE.write_text(json.dumps(cache, ensure_ascii=False, indent=2), encoding="utf-8")

    # Build new file: keep header through phrasesFor when, then packs
    phrases_for_m = re.search(r"internal fun phrasesFor\(tag: String\): Map<String, String> = when \(tag\) \{.*?\n\}\n\n", text, re.S)
    if not phrases_for_m:
        raise SystemExit("phrasesFor not found")

    header_end = m.start()
    # Everything before phraseIdentity
    before = text[:header_end]
    phrases_for = phrases_for_m.group(0)

    pack_blocks = []
    for pack in LANG_TARGETS:
        pack_blocks.append(format_map(expanded[pack], pack))
        pack_blocks.append("")

    new_text = before + new_identity + "\n\n" + phrases_for + "\n".join(pack_blocks).rstrip() + "\n"
    L10N.write_text(new_text, encoding="utf-8")
    print(f"\nWrote {L10N}")
    for pack, d in expanded.items():
        print(f"  {pack}: {len(d)}")


if __name__ == "__main__":
    main()
