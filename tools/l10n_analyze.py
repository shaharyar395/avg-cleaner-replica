# -*- coding: utf-8 -*-
import re
from pathlib import Path

p = Path("app/src/main/java/com/replica/cleaner/l10n/L10nPhrases.kt")
t = p.read_text(encoding="utf-8")

m = re.search(
    r"internal val phraseIdentity: Map<String, String> = mapOf\((.*?)\)\n\ninternal fun phrasesFor",
    t,
    re.S,
)
body = m.group(1)
keys = re.findall(r'"((?:\\.|[^"\\])*)"\s+to\s+', body)
# de-escape
keys = [k.encode("utf-8").decode("unicode_escape") if "\\" in k else k for k in keys]
# simpler: keep raw kotlin string contents
keys_raw = re.findall(r'"((?:\\.|[^"\\])*)"\s+to\s+"', body)
print("phraseIdentity keys:", len(keys_raw))

packs = list(re.finditer(r"private val (phrases\w+): Map<String, String> = mapOf\((.*?)\)\n(?=\nprivate val|\Z)", t, re.S))
for m in packs:
    name = m.group(1)
    pk = re.findall(r'"((?:\\.|[^"\\])*)"\s+to\s+"', m.group(2))
    missing = [k for k in keys_raw if k not in set(pk)]
    print(f"{name}: {len(pk)} keys, missing {len(missing)}")

# write identity keys for generation
out = Path("tools/identity_keys.txt")
out.write_text("\n".join(keys_raw), encoding="utf-8")
print("wrote", out)
