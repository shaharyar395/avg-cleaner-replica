from pathlib import Path
import re
p = Path(r"app/src/main/java/com/replica/cleaner/l10n/L10nPhrases.kt")
t = p.read_text(encoding="utf-8")

def count(name: str) -> int:
    m = re.search(rf"{name}: Map<String, String> = mapOf\((.*?)\)\n", t, re.S)
    if not m:
        return -1
    return len(re.findall(r'"((?:\\.|[^"\\])*)"\s+to\s+', m.group(1)))

print("identity", count("phraseIdentity"))
print("es", count("phrasesEs"))
print("el", count("phrasesEl"))
print("has space tips", "%d space saving tips" in t)
print("has tip pct", "Tip %d" in t)
print("size", p.stat().st_size)
