<#
  Fabrique le paquet du mod à donner aux amis : build\PetRacerMod.zip (voir notes\online.md, « Phase 5 »).

  Le paquet contient le mod, pas le jeu : les classes compilées (fichiers\java), les fichiers de données de
  mods\data (fichiers\Data) et l'installeur de mods\installer, qui les applique sur la copie du jeu de chacun.
  Rien de personnel n'y entre : ni sauvegardes ou réglages (*.dat de la racine du jeu : profils, mods.dat,
  online.dat, grandprix.dat), ni nettest.scr, ni journaux.

      powershell -ExecutionPolicy Bypass -File mods\package.ps1 -Server monadresse.duckdns.org

  -Server  : adresse du serveur écrite dans le online.scr du paquet (nom ou IP publique de celui qui héberge
             server\server.py). Sans elle, l'installeur la demande à chaque ami.
  -Port    : port UDP du serveur (7778 par défaut)
  -NoBuild : ne relance pas mods\build.ps1 avant (les classes de build\classes sont prises telles quelles)
  -AiMap   : copie d'abord la mémoire des IA de game_dev (aimap.dat, ce qu'elles ont appris sur chaque circuit) dans
             mods\data\Data\aimap_depart.dat. L'installeur la donne aux amis qui n'ont pas encore d'aimap.dat.
#>
param([string]$Server = '', [int]$Port = 7778, [switch]$NoBuild, [switch]$AiMap)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$Root     = Split-Path -Parent $PSScriptRoot
$Pristine = Join-Path $Root 'game_pristine'
$Classes  = Join-Path $Root 'build\classes'
$DataSrc  = Join-Path $PSScriptRoot 'data'
$Tools    = Join-Path $PSScriptRoot 'installer'
$Out      = Join-Path $Root 'build\PetRacerMod'
$Files    = Join-Path $Out 'fichiers'
$Zip      = Join-Path $Root 'build\PetRacerMod.zip'
$enc      = [Text.Encoding]::GetEncoding(1252)   # les .scr sont en cp1252 + CRLF
$bom      = New-Object Text.UTF8Encoding($true)

if ($Server -and $Server -notmatch '^[A-Za-z0-9.\-]+$') { throw "« $Server » n'est pas une adresse de serveur." }

# Mémoire des IA de départ (notes\ia.md, « Carte des circuits ») : celle de game_dev, si demandé
$aiMapSrc = Join-Path $DataSrc 'Data\aimap_depart.dat'
if ($AiMap) {
    $mine = Join-Path $Root 'game_dev\aimap.dat'
    if (-not (Test-Path $mine)) { throw 'game_dev\aimap.dat n''existe pas : fais d''abord quelques courses avec des IA.' }
    Copy-Item $mine $aiMapSrc -Force
    Write-Host "[aimap] game_dev\aimap.dat -> mods\data\Data\aimap_depart.dat ($(@([IO.File]::ReadAllLines($aiMapSrc) | Where-Object { $_ }).Count) lignes)"
}

if (-not $NoBuild) {
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'build.ps1') | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Le build a échoué (jeu ouvert ?) : lance mods\build.ps1 pour voir pourquoi.' }
}
$classFiles = @(Get-ChildItem $Classes -Recurse -Filter *.class -File -ErrorAction SilentlyContinue)
if ($classFiles.Count -eq 0) { throw 'build\classes est vide : lance d''abord mods\build.ps1.' }

if (Test-Path $Out) { Remove-Item $Out -Recurse -Force }
New-Item -ItemType Directory -Force (Join-Path $Files 'java') | Out-Null

# Classes du mod : posées dans java\ du jeu, comme le fait build.ps1 (même arborescence que build\classes)
robocopy $Classes (Join-Path $Files 'java') *.class /E /NFL /NDL /NJH /NJS /NP | Out-Null
if ($LASTEXITCODE -ge 8) { throw "robocopy a échoué ($LASTEXITCODE)" }

# Données : tout mods\data (déjà dans l'arborescence du jeu : Data\...)
robocopy $DataSrc $Files /E /NFL /NDL /NJH /NJS /NP | Out-Null
if ($LASTEXITCODE -ge 8) { throw "robocopy a échoué ($LASTEXITCODE)" }
$dataCount = @(Get-ChildItem $Files -Recurse -File).Count - $classFiles.Count

# online.scr du paquet : l'adresse du serveur pour les amis. Sans -Server elle reste 127.0.0.1, et
# l'installeur la demande.
$onlinePath = Join-Path $Files 'Data\online.scr'
$text = [IO.File]::ReadAllText($onlinePath, $enc)
$address = if ($Server) { $Server } else { '127.0.0.1' }
$newText = [regex]::Replace($text, '(?m)^Server\("[^"]*",\s*\d+\)', "Server(`"$address`", $Port)")
if ($newText -eq $text -and $text -notmatch [regex]::Escape("Server(`"$address`", $Port)")) { throw 'Ligne Server(...) introuvable dans Data\online.scr.' }
[IO.File]::WriteAllText($onlinePath, $newText, $enc)

# originaux.txt : empreinte des fichiers du jeu d'origine que le mod remplace. L'installeur prévient si ceux de
# l'ami sont différents (autre édition du jeu).
$originals = @()
foreach ($f in Get-ChildItem $Files -Recurse -File) {
    $rel = $f.FullName.Substring($Files.Length + 1)
    $orig = Join-Path $Pristine $rel
    if (Test-Path $orig) { $originals += "$((Get-FileHash $orig -Algorithm SHA256).Hash.ToLower()) $rel" }
}
[IO.File]::WriteAllLines((Join-Path $Out 'originaux.txt'), [string[]]$originals, [Text.Encoding]::UTF8)

# Installeur. PowerShell 5 et le Bloc-notes ne lisent les accents d'un fichier UTF-8 qu'avec sa marque
# d'en-tête (BOM) ; cmd.exe veut des fins de ligne CRLF dans un .bat.
foreach ($name in 'install.ps1', 'LISEZMOI.txt') {
    $content = [IO.File]::ReadAllText((Join-Path $Tools $name), [Text.Encoding]::UTF8) -replace "`r?`n", "`r`n"
    [IO.File]::WriteAllText((Join-Path $Out $name), $content, $bom)
}
foreach ($name in 'Installer.bat', 'Desinstaller.bat') {
    $content = [IO.File]::ReadAllText((Join-Path $Tools $name), [Text.Encoding]::ASCII) -replace "`r?`n", "`r`n"
    [IO.File]::WriteAllText((Join-Path $Out $name), $content, [Text.Encoding]::ASCII)
}

# mod.txt : ce que l'installeur affiche du paquet (version en ligne = ModNet._VERSION, la même chez tous)
$version = [regex]::Match([IO.File]::ReadAllText((Join-Path $PSScriptRoot 'java\ModNet.java')), '_VERSION\s*=\s*(\d+)').Groups[1].Value
$info = @("version=$version", "date=$(Get-Date -Format 'yyyy-MM-dd')")
[IO.File]::WriteAllLines((Join-Path $Out 'mod.txt'), [string[]]$info, [Text.Encoding]::ASCII)

# Rien de personnel dans le paquet : il part chez d'autres joueurs. Les .dat de Data\Panel sont des polices.
$leaks = @(Get-ChildItem $Out -Recurse -File | Where-Object {
    ($_.Extension -in '.dat', '.cfg' -and $_.DirectoryName -notlike "$Files\Data*") -or $_.Extension -in '.log', '.db' -or
    $_.Name -in 'nettest.scr', 'VideoSettings.scr', 'debugconf.scr' })
if ($leaks.Count -gt 0) { throw "Fichiers qui ne doivent pas partir : $($leaks.FullName -join ', ')" }

if (Test-Path $Zip) { Remove-Item $Zip -Force }
[IO.Compression.ZipFile]::CreateFromDirectory($Out, $Zip, [IO.Compression.CompressionLevel]::Optimal, $true)

$size = [Math]::Round((Get-Item $Zip).Length / 1MB, 1)
Write-Host "[ok] build\PetRacerMod.zip : $size Mo, $($classFiles.Count) classes, $dataCount fichiers de données, $($originals.Count) originaux remplacés, version en ligne $version"
if ($Server) {
    Write-Host "     serveur écrit dans le paquet : $Server, port UDP $Port"
} else {
    Write-Host '     pas d''adresse de serveur dans le paquet (-Server) : l''installeur la demandera à chaque ami'
}
if (Test-Path $aiMapSrc) {
    $lines = @([IO.File]::ReadAllLines($aiMapSrc) | Where-Object { $_ })
    $walls = @($lines | Where-Object { $_ -match '\|murs=' }).Count
    Write-Host "     mémoire des IA de départ : $($lines.Count) lignes, dont $walls avec des murs appris (du $((Get-Item $aiMapSrc).LastWriteTime.ToString('yyyy-MM-dd HH:mm')))"
} else {
    Write-Host '     pas de mémoire des IA de départ (-AiMap pour prendre celle de game_dev)'
}
exit 0
