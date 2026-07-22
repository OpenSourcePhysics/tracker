param(
	[Parameter(Mandatory = $true)]
	[string]$CorePath
)

$resolvedCore = (Resolve-Path -LiteralPath $CorePath).Path
$coreText = [IO.File]::ReadAllText($resolvedCore)
$loaderStart = $coreText.IndexOf(',"loadTRZ$I",function(', [StringComparison]::Ordinal)
$loaderEnd = $coreText.IndexOf(',"finalizeVideoLoading$org_opensourcephysics_media_core_Video",function(', $loaderStart, [StringComparison]::Ordinal)

if ($loaderStart -lt 0 -or $loaderEnd -le $loaderStart) {
	throw "Could not locate TrackerIO.AsyncLoader TRZ/video methods in $resolvedCore"
}

$loaderBlock = $coreText.Substring($loaderStart, $loaderEnd - $loaderStart)
$resourceLoaderMatch = [regex]::Match(
	$loaderBlock,
	'(?<loader>[A-Za-z_$][A-Za-z0-9_$]*)\(12\)\.downloadToOSPCache\$S\$S\$Z'
)
if (!$resourceLoaderMatch.Success) {
	throw "Could not identify the ResourceLoader alias in TrackerIO.AsyncLoader"
}

$resourceLoader = $resourceLoaderMatch.Groups['loader'].Value
$encodedCachePath = [regex]::new(
	'this\.path=(?<file>[A-Za-z_$][A-Za-z0-9_$]*)\.toURI\$\(\)\.toString\(\)'
)
$matches = $encodedCachePath.Matches($loaderBlock)
if ($matches.Count -ne 2) {
	throw "Expected two encoded cached-file assignments in TrackerIO.AsyncLoader; found $($matches.Count)"
}

$patchedBlock = $encodedCachePath.Replace($loaderBlock, {
	param($match)
	$file = $match.Groups['file'].Value
	'this.path=' + $resourceLoader + '(12).getURIPath$S(' + $file + '.getAbsolutePath$())'
})
$patchedText = $coreText.Substring(0, $loaderStart) + $patchedBlock + $coreText.Substring($loaderEnd)

if ($patchedText -eq $coreText) {
	throw "TrackerIO cached-path patch made no change to $resolvedCore"
}

[IO.File]::WriteAllText($resolvedCore, $patchedText, [Text.UTF8Encoding]::new($false))
Write-Output "Patched TrackerIO cached TRZ/video paths in $resolvedCore"
