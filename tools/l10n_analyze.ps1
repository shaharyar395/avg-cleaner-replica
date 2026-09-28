$ErrorActionPreference = 'Stop'
$path = 'app\src\main\java\com\replica\cleaner\l10n\L10nPhrases.kt'
$t = [IO.File]::ReadAllText((Resolve-Path $path))

function Get-MapKeys([string]$body) {
  $keys = New-Object System.Collections.Generic.List[string]
  $i = 0
  while ($i -lt $body.Length) {
    $q = $body.IndexOf('"', $i)
    if ($q -lt 0) { break }
    # parse string
    $sb = New-Object System.Text.StringBuilder
    $j = $q + 1
    while ($j -lt $body.Length) {
      $c = $body[$j]
      if ($c -eq '\') {
        $j++
        if ($j -lt $body.Length) { [void]$sb.Append($body[$j]); $j++ }
        continue
      }
      if ($c -eq '"') { $j++; break }
      [void]$sb.Append($c); $j++
    }
    $key = $sb.ToString()
    # expect " to "
    $rest = $body.Substring($j).TrimStart()
    if ($rest.StartsWith('to ')) {
      $keys.Add($key)
      # skip value string too
      $vStart = $body.IndexOf('"', $j)
      if ($vStart -lt 0) { break }
      $j = $vStart + 1
      while ($j -lt $body.Length) {
        $c = $body[$j]
        if ($c -eq '\') { $j += 2; continue }
        if ($c -eq '"') { $j++; break }
        $j++
      }
      $i = $j
    } else {
      $i = $j
    }
  }
  return $keys
}

$m = [regex]::Match($t, 'internal val phraseIdentity: Map<String, String> = mapOf\((.*?)\)\r?\n\r?\ninternal fun phrasesFor', [System.Text.RegularExpressions.RegexOptions]::Singleline)
$identityKeys = Get-MapKeys $m.Groups[1].Value
Write-Host "phraseIdentity: $($identityKeys.Count)"
[IO.File]::WriteAllLines((Join-Path (Get-Location) 'tools\identity_keys.txt'), $identityKeys)

$packMatches = [regex]::Matches($t, 'private val (phrases\w+): Map<String, String> = mapOf\((.*?)\)\r?\n(?=\r?\nprivate val|\r?\n?\Z)', [System.Text.RegularExpressions.RegexOptions]::Singleline)
foreach ($pm in $packMatches) {
  $name = $pm.Groups[1].Value
  $pk = Get-MapKeys $pm.Groups[2].Value
  $set = [System.Collections.Generic.HashSet[string]]::new([string[]]$pk)
  $missing = @($identityKeys | Where-Object { -not $set.Contains($_) })
  Write-Host "$name : $($pk.Count) present, $($missing.Count) missing"
}
[IO.File]::WriteAllLines((Join-Path (Get-Location) 'tools\missing_from_es.txt'), @(
  $packMatches | Where-Object { $_.Groups[1].Value -eq 'phrasesEs' } | ForEach-Object {
    $pk = Get-MapKeys $_.Groups[2].Value
    $set = [System.Collections.Generic.HashSet[string]]::new([string[]]$pk)
    $identityKeys | Where-Object { -not $set.Contains($_) }
  }
))
Write-Host 'Wrote tools\missing_from_es.txt'
