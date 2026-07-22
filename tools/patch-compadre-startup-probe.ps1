param(
	[string]$CorePath = (Join-Path $PSScriptRoot "..\cdn_cores\tracker6.1.6\core_tracker.z.js")
)

$resolvedCore = (Resolve-Path -LiteralPath $CorePath).Path
$content = [System.IO.File]::ReadAllText($resolvedCore)
$oldLine = 'error:function(){System.err.println("ResourceLoader.webTestOK = "+(H.webTestOK=Boolean.FALSE));alert("The ComPADRE server could not be reached.  You may not be connected to the internet.")},'
$newLine = 'error:function(){System.err.println("ResourceLoader startup probe failed; library access will retry on demand");H.webTestOK=null},'
$new = $newLine + [Environment]::NewLine + 'timeout:8E3}'
$oldPattern = [regex]::Escape($oldLine) + '\r?\n' + [regex]::Escape('timeout:1E3}')
$changed = $false

if (-not $content.Contains($new)) {
	$matches = ([regex]::Matches($content, $oldPattern)).Count
	if ($matches -ne 1) {
		throw "Expected one ComPADRE startup-probe signature, found $matches in $resolvedCore"
	}
	$content = [regex]::Replace($content, $oldPattern, $new)
	$changed = $true
}

$timeoutReplacements = @(
	@('timeout 1000");J2S.$ajax', 'timeout 8000");J2S.$ajax'),
	@('e.setConnectTimeout$I(1E3)', 'e.setConnectTimeout$I(8E3)')
)
foreach ($replacement in $timeoutReplacements) {
	if ($content.Contains($replacement[0])) {
		$content = $content.Replace($replacement[0], $replacement[1])
		$changed = $true
	} elseif (-not $content.Contains($replacement[1])) {
		throw "Could not find either timeout signature '$($replacement[0])' or '$($replacement[1])' in $resolvedCore"
	}
}

if ($changed) {
	[System.IO.File]::WriteAllText($resolvedCore, $content, [System.Text.UTF8Encoding]::new($false))
	Write-Output "Made the ComPADRE connectivity checks non-blocking with 8-second timeouts: $resolvedCore"
} else {
	Write-Output "ComPADRE connectivity checks are already patched: $resolvedCore"
}
