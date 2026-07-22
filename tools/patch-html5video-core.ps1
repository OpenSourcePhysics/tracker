param(
	[Parameter(Mandatory = $true)]
	[string]$CorePath
)

$resolvedCore = (Resolve-Path -LiteralPath $CorePath).Path
$coreText = [IO.File]::ReadAllText($resolvedCore)
$createIconStart = $coreText.IndexOf(',"createIcon",function(', [StringComparison]::Ordinal)
$createLabelStart = $coreText.IndexOf(',"createLabel",function(', $createIconStart, [StringComparison]::Ordinal)

if ($createIconStart -lt 0 -or $createLabelStart -le $createIconStart) {
	throw "Could not locate HTML5Video.createIcon in $resolvedCore"
}

$createIconBlock = $coreText.Substring($createIconStart, $createLabelStart - $createIconStart)
$unmarkedByteIcon = [regex]::new(
	'g\(\[(?<bytes>.*?)\],(?<icon>[A-Za-z_$][A-Za-z0-9_$]*\(6,1\))\.c\$\$BA\)',
	[Text.RegularExpressions.RegexOptions]::Singleline
)
$matches = $unmarkedByteIcon.Matches($createIconBlock)

if ($matches.Count -ne 2) {
	throw "Expected two unmarked File/String ImageIcon constructors in HTML5Video.createIcon; found $($matches.Count)"
}

$patchedBlock = $unmarkedByteIcon.Replace($createIconBlock, {
	param($match)
	'g([' + $match.Groups['bytes'].Value + ',"jsvideo"],' +
		$match.Groups['icon'].Value + '.c$$BA$S)'
})
$patchedText = $coreText.Substring(0, $createIconStart) + $patchedBlock + $coreText.Substring($createLabelStart)

if ($patchedText -eq $coreText) {
	throw "HTML5Video core patch made no change to $resolvedCore"
}

[IO.File]::WriteAllText($resolvedCore, $patchedText, [Text.UTF8Encoding]::new($false))
Write-Output "Patched HTML5Video.createIcon File/String video markers in $resolvedCore"
