param(
	[Parameter(Mandatory = $true)]
	[string]$InputTrz,

	[Parameter(Mandatory = $true)]
	[string]$OutputTrz,

	[string]$FfmpegPath = "ffmpeg"
)

$resolvedInput = (Resolve-Path -LiteralPath $InputTrz).Path
$outputPath = [IO.Path]::GetFullPath($OutputTrz)
$workRoot = Join-Path ([IO.Path]::GetTempPath()) ("tracker-gif-upgrade-" + [Guid]::NewGuid().ToString("N"))
$expandedPath = Join-Path $workRoot "expanded"
$archivePath = Join-Path $workRoot "project.zip"

try {
	New-Item -ItemType Directory -Path $expandedPath -Force | Out-Null
	Copy-Item -LiteralPath $resolvedInput -Destination $archivePath
	Expand-Archive -LiteralPath $archivePath -DestinationPath $expandedPath

	$trackFiles = @(Get-ChildItem -LiteralPath $expandedPath -Recurse -Filter "*.trk")
	if ($trackFiles.Count -ne 1) {
		throw "Expected exactly one TRK file; found $($trackFiles.Count)."
	}

	$trackPath = $trackFiles[0].FullName
	$trackXml = [IO.File]::ReadAllText($trackPath)
	$videoPattern = [regex]::new(
		'<object class="org\.opensourcephysics\.media\.gif\.GifVideo">\s*<property name="path" type="string">(?<path>[^<]+\.gif)</property>\s*</object>',
		[Text.RegularExpressions.RegexOptions]::IgnoreCase
	)
	$videoMatch = $videoPattern.Match($trackXml)
	if (!$videoMatch.Success) {
		throw "The TRK file does not contain one directly embedded GifVideo path."
	}

	$gifRelativePath = $videoMatch.Groups["path"].Value.Replace('/', [IO.Path]::DirectorySeparatorChar)
	$gifPath = Join-Path $expandedPath $gifRelativePath
	if (!(Test-Path -LiteralPath $gifPath)) {
		throw "Embedded GIF was not found: $gifRelativePath"
	}

	$mp4RelativePath = [IO.Path]::ChangeExtension($gifRelativePath, ".mp4")
	$mp4Path = Join-Path $expandedPath $mp4RelativePath
	& $FfmpegPath -hide_banner -loglevel error -y -i $gifPath -an -c:v libx264 `
		-pix_fmt yuv420p -movflags +faststart $mp4Path
	if ($LASTEXITCODE -ne 0 -or !(Test-Path -LiteralPath $mp4Path)) {
		throw "ffmpeg could not convert the embedded GIF."
	}

	$mp4XmlPath = $mp4RelativePath.Replace([IO.Path]::DirectorySeparatorChar, '/')
	$movieXml = '<object class="org.opensourcephysics.media.xuggle.XuggleVideo">' +
		"`r`n            " + '<property name="path" type="string">' + $mp4XmlPath + '</property>' +
		"`r`n        </object>"
	$updatedXml = $videoPattern.Replace($trackXml, $movieXml, 1)
	if ($updatedXml -eq $trackXml) {
		throw "The TRK video declaration was not updated."
	}

	[IO.File]::WriteAllText($trackPath, $updatedXml, [Text.UTF8Encoding]::new($false))
	Remove-Item -LiteralPath $gifPath

	$outputDirectory = Split-Path -Parent $outputPath
	if ($outputDirectory) {
		New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
	}
	$outputArchive = Join-Path $workRoot "upgraded.zip"
	Add-Type -AssemblyName System.IO.Compression
	$archiveStream = [IO.File]::Open($outputArchive, [IO.FileMode]::CreateNew)
	try {
		$zipArchive = [IO.Compression.ZipArchive]::new(
			$archiveStream,
			[IO.Compression.ZipArchiveMode]::Create,
			$false
		)
		try {
			foreach ($file in Get-ChildItem -LiteralPath $expandedPath -Recurse -File) {
				$entryName = $file.FullName.Substring($expandedPath.Length + 1).Replace('\', '/')
				$entry = $zipArchive.CreateEntry($entryName, [IO.Compression.CompressionLevel]::Optimal)
				$entryStream = $entry.Open()
				$sourceStream = $file.OpenRead()
				try {
					$sourceStream.CopyTo($entryStream)
				}
				finally {
					$sourceStream.Dispose()
					$entryStream.Dispose()
				}
			}
		}
		finally {
			$zipArchive.Dispose()
		}
	}
	finally {
		$archiveStream.Dispose()
	}
	Copy-Item -LiteralPath $outputArchive -Destination $outputPath -Force

	Write-Output "Upgraded legacy GifVideo project to MP4: $outputPath"
}
finally {
	if (Test-Path -LiteralPath $workRoot) {
		Remove-Item -LiteralPath $workRoot -Recurse -Force
	}
}
