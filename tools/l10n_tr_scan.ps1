$ErrorActionPreference = 'Stop'
$path = 'app\src\main\java\com\replica\cleaner\l10n\L10nPhrases.kt'
$t = [IO.File]::ReadAllText((Resolve-Path $path))

function Get-MapEntries([string]$body) {
  $entries = New-Object 'System.Collections.Generic.Dictionary[string,string]'
  $i = 0
  while ($i -lt $body.Length) {
    $q = $body.IndexOf('"', $i)
    if ($q -lt 0) { break }
    $key = Get-KotlinString $body ([ref]$q)
    $rest = $body.Substring($q).TrimStart()
    if (-not $rest.StartsWith('to ')) { $i = $q; continue }
    $q2 = $body.IndexOf('"', $q)
    if ($q2 -lt 0) { break }
    $val = Get-KotlinString $body ([ref]$q2)
    if (-not $entries.ContainsKey($key)) { $entries[$key] = $val }
    $i = $q2
  }
  return $entries
}

function Get-KotlinString([string]$s, [ref]$pos) {
  # $pos points at opening quote
  $j = $pos.Value + 1
  $sb = New-Object System.Text.StringBuilder
  while ($j -lt $s.Length) {
    $c = $s[$j]
    if ($c -eq '\') {
      $j++
      if ($j -ge $s.Length) { break }
      $n = $s[$j]
      switch ($n) {
        'n' { [void]$sb.Append("`n") }
        't' { [void]$sb.Append("`t") }
        'r' { [void]$sb.Append("`r") }
        '"' { [void]$sb.Append('"') }
        '\' { [void]$sb.Append('\') }
        default { [void]$sb.Append($n) }
      }
      $j++
      continue
    }
    if ($c -eq '"') { $j++; break }
    [void]$sb.Append($c); $j++
  }
  $pos.Value = $j
  return $sb.ToString()
}

$m = [regex]::Match($t, 'internal val phraseIdentity: Map<String, String> = mapOf\((.*?)\)\r?\n\r?\ninternal fun phrasesFor', [System.Text.RegularExpressions.RegexOptions]::Singleline)
$id = Get-MapEntries $m.Groups[1].Value
Write-Host "identity $($id.Count)"

# Collect tr("...") from ui and data
$trKeys = New-Object 'System.Collections.Generic.HashSet[string]'
Get-ChildItem -Path 'app\src\main\java' -Recurse -Filter *.kt | ForEach-Object {
  $c = [IO.File]::ReadAllText($_.FullName)
  $matches = [regex]::Matches($c, '(?:tr|l10n\.tr)\(\s*"((?:\\.|[^"\\])*)"')
  foreach ($mm in $matches) {
    $raw = $mm.Groups[1].Value
    # unescape lightly
    $k = $raw -replace '\\n', "`n" -replace '\\"', '"' -replace '\\\\', '\'
    [void]$trKeys.Add($k)
  }
}
Write-Host "tr() keys found: $($trKeys.Count)"
$missingId = @($trKeys | Where-Object { -not $id.ContainsKey($_) } | Sort-Object)
Write-Host "tr keys missing from identity: $($missingId.Count)"
[IO.File]::WriteAllLines((Join-Path (Get-Location) 'tools\tr_missing_identity.txt'), $missingId)
$missingId | Select-Object -First 80 | ForEach-Object { Write-Host "  $_" }
