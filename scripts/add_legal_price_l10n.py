#!/usr/bin/env python3
"""Add a few leftover legal/price phrase keys."""
from pathlib import Path
import re

PATH = Path("app/src/main/java/com/replica/cleaner/l10n/L10nPhrases.kt")

NEW = {
    " and ": {
        "Es": " y ", "De": " und ", "Fr": " et ", "It": " e ", "Pt": " e ",
        "Ru": " и ", "Ar": " و ", "Hi": " और ", "Zh": "和", "Ja": "と",
        "Ko": " 및 ", "Tr": " ve ", "Vi": " và ", "Id": " dan ", "Nl": " en ",
        "Pl": " oraz ", "Uk": " і ", "Th": " และ ", "El": " και ",
    },
    ". Your subscription renews unless canceled.": {
        "Es": ". Tu suscripción se renueva salvo cancelación.",
        "De": ". Dein Abo verlängert sich, sofern nicht gekündigt.",
        "Fr": ". Votre abonnement se renouvelle sauf résiliation.",
        "It": ". L'abbonamento si rinnova se non annullato.",
        "Pt": ". Sua assinatura é renovada salvo cancelamento.",
        "Ru": ". Подписка продлевается, пока её не отменят.",
        "Ar": ". يتجدد اشتراكك ما لم يُلغَ.",
        "Hi": ". सदस्यता रद्द होने तक नवीनीकृत होती रहती है।",
        "Zh": "。除非取消，否则订阅会自动续订。",
        "Ja": "。解約しない限りサブスクリプションは更新されます。",
        "Ko": ". 취소하지 않으면 구독이 갱신됩니다.",
        "Tr": ". İptal edilmediği sürece aboneliğin yenilenir.",
        "Vi": ". Thuê bao sẽ gia hạn trừ khi hủy.",
        "Id": ". Langganan diperpanjang kecuali dibatalkan.",
        "Nl": ". Je abonnement wordt verlengd tenzij je opzegt.",
        "Pl": ". Subskrypcja odnawia się, dopóki jej nie anulujesz.",
        "Uk": ". Підписка поновлюється, доки її не скасують.",
        "Th": ". การสมัครจะต่ออายุจนกว่าจะยกเลิก",
        "El": ". Η συνδρομή ανανεώνεται εκτός αν ακυρωθεί.",
    },
    "%s / year": {
        "Es": "%s / año", "De": "%s / Jahr", "Fr": "%s / an", "It": "%s / anno",
        "Pt": "%s / ano", "Ru": "%s / год", "Ar": "%s / سنة", "Hi": "%s / वर्ष",
        "Zh": "%s / 年", "Ja": "%s / 年", "Ko": "%s / 년", "Tr": "%s / yıl",
        "Vi": "%s / năm", "Id": "%s / tahun", "Nl": "%s / jaar",
        "Pl": "%s / rok", "Uk": "%s / рік", "Th": "%s / ปี", "El": "%s / έτος",
    },
}

PACKS = [
    ("phrasesEs", "Es"), ("phrasesDe", "De"), ("phrasesFr", "Fr"), ("phrasesIt", "It"),
    ("phrasesPt", "Pt"), ("phrasesRu", "Ru"), ("phrasesAr", "Ar"), ("phrasesHi", "Hi"),
    ("phrasesZh", "Zh"), ("phrasesJa", "Ja"), ("phrasesKo", "Ko"), ("phrasesTr", "Tr"),
    ("phrasesVi", "Vi"), ("phrasesId", "Id"), ("phrasesNl", "Nl"), ("phrasesPl", "Pl"),
    ("phrasesUk", "Uk"), ("phrasesTh", "Th"), ("phrasesEl", "El"),
]


def escape_kt(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def insert_entries(block: str, entries: dict[str, str]) -> str:
    existing = set(re.findall(r'"((?:\\.|[^"\\])*)"\s+to\s+', block))
    to_add = [f'    "{escape_kt(k)}" to "{escape_kt(v)}"' for k, v in entries.items() if k not in existing]
    if not to_add:
        return block
    lines = block.rstrip().split("\n")
    assert lines[-1].strip() == ")"
    body = lines[:-1]
    for i in range(len(body) - 1, -1, -1):
        s = body[i].rstrip()
        if s and not s.lstrip().startswith("//"):
            if not s.endswith(","):
                body[i] = s + ","
            break
    for i, line in enumerate(to_add):
        body.append(line + ("," if i < len(to_add) - 1 else ""))
    body.append(")")
    return "\n".join(body)


def main():
    text = PATH.read_text(encoding="utf-8")
    # identity
    m = re.search(
        r"(internal val phraseIdentity: Map<String, String> = mapOf\()(.*?)(\n\))",
        text,
        re.S,
    )
    full = m.group(0)
    text = text[: m.start()] + insert_entries(full, {k: k for k in NEW}) + text[m.end() :]

    for pack, lang in PACKS:
        m = re.search(rf"(private val {pack}: Map<String, String> = mapOf\()(.*?)(\n\))", text, re.S)
        if not m:
            continue
        entries = {k: NEW[k][lang] for k in NEW}
        text = text[: m.start()] + insert_entries(m.group(0), entries) + text[m.end() :]
        print("ok", pack)

    PATH.write_text(text, encoding="utf-8")
    # verify ar
    i = text.find("private val phrasesAr")
    j = text.find("private val phrasesHi")
    ar = text[i:j]
    for k in [
        "You're not signed in",
        "Already have a subscription?",
        "Set up preferences for all your tools",
        "Premium Plus",
        "Cleanup Premium",
        "Get rid of clutter and unused files",
    ]:
        print(k, "OK" if k in ar else "MISSING")


if __name__ == "__main__":
    main()
