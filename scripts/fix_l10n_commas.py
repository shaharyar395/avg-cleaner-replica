"""Add missing commas between mapOf entries in L10nPhrases.kt."""
from pathlib import Path
import re

path = Path(r"app/src/main/java/com/replica/cleaner/l10n/L10nPhrases.kt")
text = path.read_text(encoding="utf-8")

# Lines that look like map entries without trailing commas:
#    "key" to "value"
# or  "key" to "value"  (last before ) is ok without comma in Kotlin but our broken ones
# Fix: any line matching entry pattern that does NOT end with , and whose next non-empty line
# is another entry (starts with quote after indent) should get a comma.

lines = text.splitlines(keepends=True)
entry = re.compile(r'^(\s*"(?:\\.|[^"\\])*"\s+to\s+"(?:\\.|[^"\\])*")(\s*)$')

fixed = 0
out = []
for i, line in enumerate(lines):
    m = entry.match(line.rstrip("\r\n"))
    if m:
        # peek next non-empty
        j = i + 1
        while j < len(lines) and lines[j].strip() == "":
            j += 1
        nxt = lines[j].strip() if j < len(lines) else ""
        if nxt.startswith('"') and " to " in nxt:
            # needs comma
            newline = m.group(1) + "," + ("\r\n" if line.endswith("\r\n") else "\n")
            out.append(newline)
            fixed += 1
            continue
    out.append(line)

path.write_text("".join(out), encoding="utf-8")
print(f"fixed {fixed} commas")
