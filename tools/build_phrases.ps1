# Build phrases_rows.tsv then generate L10nPhrases.kt
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..

$tsv = New-Object System.Collections.Generic.List[string]
function AddRow([string[]]$cols) {
  [void]$tsv.Add(($cols -join "`t"))
}

. "$PSScriptRoot\phrases_rows_data.ps1"

[System.IO.File]::WriteAllLines("$pwd\tools\phrases_rows.tsv", $tsv.ToArray(), [System.Text.UTF8Encoding]::new($false))
Write-Host ("tsv rows: " + $tsv.Count)
& "$PSScriptRoot\gen_phrases.ps1"
