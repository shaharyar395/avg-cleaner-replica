# Generates L10nPhrases.kt — phraseIdentity + per-language packs for tr().
$ErrorActionPreference = "Stop"
$root = (Get-Location).Path
$out = Join-Path $root "app\src\main\java\com\replica\cleaner\l10n\L10nPhrases.kt"

function Esc([string]$s) {
  if ($null -eq $s) { return "" }
  return $s.Replace('\', '\\').Replace('"', '\"').Replace("`r", "").Replace("`n", "\n")
}

function NewPack {
  return New-Object 'System.Collections.Generic.Dictionary[string,string]'
}

$phrases = [System.IO.File]::ReadAllLines((Join-Path $root "tools\phrases_en.txt")) |
  Where-Object { $_.Trim() -ne "" } |
  Select-Object -Unique

$langOrder = @("es","de","fr","it","pt","ru","ar","hi","zh","ja","ko","tr","vi","id","nl","pl","uk","th","el")
$pack = @{}
foreach ($l in $langOrder) { $pack[$l] = NewPack }

# Each row: EN + 19 translations matching $langOrder
$rowsFile = Join-Path $root "tools\phrases_rows.tsv"
$lines = [System.IO.File]::ReadAllLines($rowsFile)
foreach ($line in $lines) {
  if ($line.Trim() -eq "" -or $line.StartsWith("#")) { continue }
  $cols = $line.Split("`t")
  if ($cols.Count -lt 20) {
    Write-Warning ("Skip short row: " + $cols[0])
    continue
  }
  $en = $cols[0]
  for ($i = 0; $i -lt $langOrder.Count; $i++) {
    $val = $cols[$i + 1]
    if (-not [string]::IsNullOrWhiteSpace($val)) {
      $pack[$langOrder[$i]][$en] = $val
    }
  }
}

function EmitMap([string]$name, $map) {
  $sb = New-Object System.Text.StringBuilder
  [void]$sb.AppendLine("private val ${name}: Map<String, String> = mapOf(")
  $keys = @($map.Keys)
  [Array]::Sort($keys)
  for ($i = 0; $i -lt $keys.Count; $i++) {
    $k = Esc $keys[$i]
    $v = Esc $map[$keys[$i]]
    $comma = if ($i -lt $keys.Count - 1) { "," } else { "" }
    [void]$sb.AppendLine("    `"$k`" to `"$v`"$comma")
  }
  [void]$sb.AppendLine(")")
  [void]$sb.AppendLine("")
  return $sb.ToString()
}

$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine("package com.replica.cleaner.l10n")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("/** English UI phrases used as dictionary keys for [tr]. Merged into every [L10n] catalog. */")
[void]$sb.AppendLine("internal val phraseIdentity: Map<String, String> = mapOf(")
for ($i = 0; $i -lt $phrases.Count; $i++) {
  $p = Esc $phrases[$i]
  $comma = if ($i -lt $phrases.Count - 1) { "," } else { "" }
  [void]$sb.AppendLine("    `"$p`" to `"$p`"$comma")
}
[void]$sb.AppendLine(")")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("internal fun phrasesFor(tag: String): Map<String, String> = when (tag) {")
[void]$sb.AppendLine("    `"es`", `"ca`", `"ro`" -> phrasesEs")
[void]$sb.AppendLine("    `"de`", `"da`", `"nb`", `"sv`", `"hu`", `"fi`" -> phrasesDe")
[void]$sb.AppendLine("    `"fr`" -> phrasesFr")
[void]$sb.AppendLine("    `"it`" -> phrasesIt")
[void]$sb.AppendLine("    `"pt-BR`", `"pt-PT`" -> phrasesPt")
[void]$sb.AppendLine("    `"ru`", `"bg`" -> phrasesRu")
[void]$sb.AppendLine("    `"ar`" -> phrasesAr")
[void]$sb.AppendLine("    `"hi`" -> phrasesHi")
[void]$sb.AppendLine("    `"zh-CN`" -> phrasesZh")
[void]$sb.AppendLine("    `"ja`" -> phrasesJa")
[void]$sb.AppendLine("    `"ko`" -> phrasesKo")
[void]$sb.AppendLine("    `"tr`" -> phrasesTr")
[void]$sb.AppendLine("    `"vi`" -> phrasesVi")
[void]$sb.AppendLine("    `"id`" -> phrasesId")
[void]$sb.AppendLine("    `"nl`" -> phrasesNl")
[void]$sb.AppendLine("    `"pl`", `"sk`", `"cs`" -> phrasesPl")
[void]$sb.AppendLine("    `"uk`" -> phrasesUk")
[void]$sb.AppendLine("    `"th`" -> phrasesTh")
[void]$sb.AppendLine("    `"el`" -> phrasesEl")
[void]$sb.AppendLine("    else -> emptyMap()")
[void]$sb.AppendLine("}")
[void]$sb.AppendLine("")

$nameMap = @{
  es="phrasesEs"; de="phrasesDe"; fr="phrasesFr"; it="phrasesIt"; pt="phrasesPt"
  ru="phrasesRu"; ar="phrasesAr"; hi="phrasesHi"; zh="phrasesZh"; ja="phrasesJa"
  ko="phrasesKo"; tr="phrasesTr"; vi="phrasesVi"; id="phrasesId"; nl="phrasesNl"
  pl="phrasesPl"; uk="phrasesUk"; th="phrasesTh"; el="phrasesEl"
}
foreach ($l in $langOrder) {
  [void]$sb.Append((EmitMap $nameMap[$l] $pack[$l]))
}

[System.IO.File]::WriteAllText($out, $sb.ToString(), [System.Text.UTF8Encoding]::new($false))
Write-Host ("Wrote $out bytes=" + (Get-Item $out).Length + " phrases=" + $phrases.Count + " esKeys=" + $pack.es.Count)
