param(
	[string]$CorePath = (Join-Path $PSScriptRoot "..\cdn_cores\tracker6.1.6\core_tracker.z.js")
)

$resolvedCore = (Resolve-Path -LiteralPath $CorePath).Path
$content = [System.IO.File]::ReadAllText($resolvedCore)
$old = 'lk.hitRect=g(vI(1,1).c$$I$I$I$I,[-4,-4,8,8])'
$new = 'lk.hitRect=g(vI(1,1).c$$I$I$I$I,[-12,-12,24,24])'

if ($content.Contains($new)) {
	Write-Output "Touch hit region is already patched: $resolvedCore"
	exit 0
}

$matches = ([regex]::Matches($content, [regex]::Escape($old))).Count
if ($matches -ne 1) {
	throw "Expected one Step hit-region signature, found $matches in $resolvedCore"
}

[System.IO.File]::WriteAllText($resolvedCore, $content.Replace($old, $new), [System.Text.UTF8Encoding]::new($false))
Write-Output "Expanded shared Tracker Step hit region to 24 x 24 pixels: $resolvedCore"
