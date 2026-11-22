<#
  Installe le mod de Pet Racer sur une copie du jeu déjà installée (le jeu lui-même n'est pas fourni).

  Double-clique sur Installer.bat, ou :
      powershell -ExecutionPolicy Bypass -File install.ps1 [-Game "C:\Jeux\Pet Racer\game"] [-Server adresse] [-Port 7778]
      powershell -ExecutionPolicy Bypass -File install.ps1 -Uninstall

  Ce que fait l'installation :
    - garde une copie des fichiers d'origine qu'elle remplace dans <jeu>\mod_backup (une seule fois) ;
    - copie les fichiers du mod (dossier « fichiers » du paquet) dans le dossier du jeu : les classes Java dans
      java\, les données dans Data\ ;
    - écrit l'adresse du serveur dans Data\online.scr.
  Les sauvegardes (*.dat à la racine du jeu : profils, réglages du mod, compte en ligne) ne sont jamais touchées.
  Seule exception : la mémoire des IA de départ (Data\aimap_depart.dat) est copiée en aimap.dat si le jeu n'en a pas.
  Relancer l'installation met le mod à jour.

  -Game      : dossier du jeu (celui de PetRacer.exe, ou le dossier « Pet Racer » qui le contient dans « game »).
               Sans lui, le dossier est cherché, puis demandé.
  -Server    : adresse du serveur de jeu en ligne (nom ou IP), donnée par celui qui l'héberge
  -Port      : port UDP du serveur (7778 par défaut)
  -Uninstall : remet les fichiers d'origine et retire ceux du mod
  -Quiet     : ne pose aucune question et n'attend pas à la fin (installation automatique)
#>
param([string]$Game = '', [string]$Server = '', [int]$Port = 0, [switch]$Uninstall, [switch]$Quiet)

$ErrorActionPreference = 'Stop'

$Here        = $PSScriptRoot
$Files       = Join-Path $Here 'fichiers'
$Enc         = [Text.Encoding]::GetEncoding(1252)   # les .scr du jeu sont en cp1252 + CRLF
$LocalHost   = '127.0.0.1'
$DefaultPort = 7778

function Stop-Install([string]$Message) {
    Write-Host ''
    Write-Host "ERREUR : $Message" -ForegroundColor Red
    if (-not $Quiet) { Read-Host 'Appuie sur Entrée pour fermer' | Out-Null }
    exit 1
}

function Test-GameDir([string]$Dir) {
    return [bool]($Dir -and (Test-Path -LiteralPath (Join-Path $Dir 'PetRacer.exe')) `
        -and (Test-Path -LiteralPath (Join-Path $Dir 'java\RacerGame.class')) -and (Test-Path -LiteralPath (Join-Path $Dir 'Data')))
}

# Le dossier du jeu peut être donné tel quel ou par un dossier parent : l'installeur d'origine met le jeu dans
# « <dossier choisi>\Pet Racer\game ».
function Resolve-GameDir([string]$Dir) {
    if (-not $Dir) { return $null }
    foreach ($sub in '', 'game', 'Pet Racer\game', 'Pet Racer') {
        $candidate = if ($sub) { Join-Path $Dir $sub } else { $Dir }
        if (Test-GameDir $candidate) { return (Resolve-Path -LiteralPath $candidate).Path }
    }
    return $null
}

# Le dossier du jeu : celui donné, sinon celui que l'installeur d'origine a noté dans le registre, sinon les
# endroits habituels, sinon on le demande.
function Find-GameDir {
    if ($Game) {
        $dir = Resolve-GameDir $Game
        if ($dir) { return $dir }
        Stop-Install "Pet Racer n'est pas dans « $Game » (PetRacer.exe, java et Data doivent s'y trouver)."
    }
    $found = @()
    foreach ($key in 'HKLM:\SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\Pet Racer',
                     'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Pet Racer',
                     'HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Pet Racer') {
        $props = Get-ItemProperty $key -ErrorAction SilentlyContinue
        if (-not $props) { continue }
        foreach ($value in @($props.InstallLocation, $props.DisplayIcon, $props.UninstallString)) {
            if (-not $value) { continue }
            $path = ([string]$value).Trim().Trim('"')
            if ($path -match '^(.*?\.exe)') { $path = $Matches[1] }
            if (Test-Path -LiteralPath $path -PathType Leaf) { $path = Split-Path -Parent $path }
            $found += $path
        }
    }
    $found += 'C:\Jeux', 'C:\Games', "${env:ProgramFiles(x86)}", "$env:ProgramFiles", "${env:ProgramFiles(x86)}\Techland",
              "$env:ProgramFiles\Techland"
    foreach ($base in $found) {
        $dir = Resolve-GameDir $base
        if (-not $dir) { continue }
        if ($Quiet) { return $dir }
        # trouvé tout seul : on demande avant de toucher à ce dossier (il peut y avoir plusieurs copies du jeu)
        Write-Host "Pet Racer trouvé dans : $dir"
        $verb = if ($Uninstall) { 'Retirer le mod de' } else { 'Installer le mod dans' }
        if ((Read-Host "$verb ce dossier ? (o/n)") -match '^[oOyY]') { return $dir }
        break
    }
    if ($Quiet) { Stop-Install 'Dossier du jeu introuvable : donne-le avec -Game.' }
    Write-Host 'Choisis le dossier de Pet Racer (celui de PetRacer.exe) dans la fenêtre qui s''ouvre.'
    Add-Type -AssemblyName System.Windows.Forms
    $dialog = New-Object System.Windows.Forms.FolderBrowserDialog
    $dialog.Description = 'Dossier où Pet Racer est installé (celui de PetRacer.exe, souvent « Pet Racer\game »)'
    $dialog.ShowNewFolderButton = $false
    if ($dialog.ShowDialog() -ne [System.Windows.Forms.DialogResult]::OK) { Stop-Install 'Aucun dossier choisi.' }
    $dir = Resolve-GameDir $dialog.SelectedPath
    if (-not $dir) {
        Stop-Install "Pet Racer n'est pas dans « $($dialog.SelectedPath) » (PetRacer.exe, java et Data doivent s'y trouver)."
    }
    return $dir
}

function Test-CanWrite([string]$Dir) {
    $probe = Join-Path $Dir ('mod_ecriture_' + [Guid]::NewGuid().ToString('N') + '.tmp')
    try {
        [IO.File]::WriteAllText($probe, 'x')
        Remove-Item -LiteralPath $probe -Force
        # le jeu écrit aussi dans java\ (classes du mod) : vérifié à part, un dossier peut être en lecture seule
        $probe = Join-Path $Dir ('java\mod_ecriture_' + [Guid]::NewGuid().ToString('N') + '.tmp')
        [IO.File]::WriteAllText($probe, 'x')
        Remove-Item -LiteralPath $probe -Force
        return $true
    } catch {
        return $false
    }
}

function Get-RelativeFiles([string]$Root) {
    if (-not (Test-Path -LiteralPath $Root)) { return @() }
    return @(Get-ChildItem -LiteralPath $Root -Recurse -File | ForEach-Object { $_.FullName.Substring($Root.Length + 1) })
}

function Read-List([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path)) { return @() }
    return @([IO.File]::ReadAllLines($Path) | Where-Object { $_ })
}

function Copy-File([string]$From, [string]$To) {
    New-Item -ItemType Directory -Force (Split-Path -Parent $To) | Out-Null
    Copy-Item -LiteralPath $From $To -Force
}

function Get-Sha([string]$Path) {
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLower()
}

# Adresse et port du serveur écrits dans un online.scr, ou $null
function Get-ServerLine([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path)) { return $null }
    $match = [regex]::Match([IO.File]::ReadAllText($Path, $Enc), '(?m)^Server\("([^"]*)",\s*(\d+)\)')
    if ($match.Success) { return @{ Host = $match.Groups[1].Value; Port = [int]$match.Groups[2].Value } }
    return $null
}

# ---------------------------------------------------------------------------------------------------------------

# Une erreur imprévue ne doit pas fermer la fenêtre avant qu'on ait pu la lire
trap { Stop-Install $_.Exception.Message }

Write-Host '=== Mod de Pet Racer ==='
$modInfo = @{}
foreach ($line in Read-List (Join-Path $Here 'mod.txt')) {
    $parts = $line.Split('=', 2)
    if ($parts.Count -eq 2) { $modInfo[$parts[0].Trim()] = $parts[1].Trim() }
}

$GameDir = Find-GameDir
Write-Host "Jeu : $GameDir"
if (Get-Process 'PetRacer' -ErrorAction SilentlyContinue) {
    Stop-Install 'Pet Racer est ouvert : ferme le jeu, puis relance l''installation.'
}

if (-not (Test-CanWrite $GameDir)) {
    # Jeu installé dans Program Files : il faut les droits d'administrateur
    $admin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole(
        [Security.Principal.WindowsBuiltInRole]::Administrator)
    if ($admin -or $Quiet) { Stop-Install "Impossible d'écrire dans « $GameDir »." }
    Write-Host 'Ce dossier demande les droits d''administrateur : Windows va te demander de les accorder.'
    $relaunch = @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', "`"$PSCommandPath`"", '-Game', "`"$GameDir`"")
    if ($Server) { $relaunch += @('-Server', "`"$Server`"") }
    if ($Port -gt 0) { $relaunch += @('-Port', $Port) }
    if ($Uninstall) { $relaunch += '-Uninstall' }
    Start-Process powershell -Verb RunAs -ArgumentList $relaunch
    exit 0
}

$Backup   = Join-Path $GameDir 'mod_backup'
$ListPath = Join-Path $Backup 'fichiers_du_mod.txt'   # tout ce que le mod a posé dans le jeu (chemins relatifs)
$known    = Read-List $ListPath

# --- désinstallation ---
if ($Uninstall) {
    if (-not (Test-Path -LiteralPath $ListPath)) { Stop-Install 'Le mod n''est pas installé dans ce dossier (pas de mod_backup).' }
    $originals = @(Get-RelativeFiles $Backup | Where-Object { $_ -ne 'fichiers_du_mod.txt' })
    $removed = 0
    foreach ($rel in $known) {
        # fichier ajouté par le mod : retiré. Ceux qui remplaçaient un original sont remis juste après.
        $target = Join-Path $GameDir $rel
        if (($originals -notcontains $rel) -and (Test-Path -LiteralPath $target)) { Remove-Item -LiteralPath $target -Force; $removed++ }
    }
    foreach ($rel in $originals) {
        Copy-File (Join-Path $Backup $rel) (Join-Path $GameDir $rel)
    }
    Remove-Item -LiteralPath $Backup -Recurse -Force
    Write-Host "Mod retiré : $($originals.Count) fichiers d'origine remis, $removed fichiers du mod supprimés." -ForegroundColor Green
    Write-Host 'Tes sauvegardes (*.dat) sont restées en place.'
    if (-not $Quiet) { Read-Host 'Appuie sur Entrée pour fermer' | Out-Null }
    exit 0
}

# --- installation ---
$modFiles = @(Get-RelativeFiles $Files)
if (-not ($modFiles | Where-Object { $_ -like 'java\*.class' }) -or -not ($modFiles | Where-Object { $_ -like 'Data\*' })) {
    Stop-Install 'Le paquet du mod est incomplet (dossier « fichiers »). Décompresse tout le zip avant de lancer Installer.bat.'
}
$first = -not (Test-Path -LiteralPath $ListPath)

# Version du jeu : les fichiers d'origine que le mod remplace doivent être ceux pour lesquels il est fait (sinon
# autre édition du jeu, ou jeu déjà modifié à la main). On regarde la copie de mod_backup quand il y en a une.
$different = @()
foreach ($line in Read-List (Join-Path $Here 'originaux.txt')) {
    $parts = $line.Split(' ', 2)
    if ($parts.Count -ne 2) { continue }
    $rel = $parts[1]
    $saved = Join-Path $Backup $rel
    $current = Join-Path $GameDir $rel
    if (Test-Path -LiteralPath $saved) { $path = $saved }
    elseif ($known -contains $rel) { continue }   # posé par une version précédente du mod, sans original
    else { $path = $current }
    if (-not (Test-Path -LiteralPath $path) -or (Get-Sha $path) -ne $parts[0]) { $different += $rel }
}
if ($different.Count -gt 0) {
    Write-Host ''
    Write-Host "ATTENTION : $($different.Count) fichier(s) de ton jeu ne sont pas ceux de la version pour laquelle ce mod est fait" -ForegroundColor Yellow
    Write-Host "(autre édition du jeu, ou fichiers déjà modifiés), par exemple : $(($different | Select-Object -First 3) -join ', ')" -ForegroundColor Yellow
    Write-Host 'Le jeu risque de ne pas démarrer. La désinstallation (Desinstaller.bat) remet tes fichiers comme avant.'
    if (-not $Quiet -and (Read-Host 'Installer quand même ? (o/n)') -notmatch '^[oOyY]') {
        Stop-Install 'Installation annulée, rien n''a été changé.'
    }
}

$oldServer = Get-ServerLine (Join-Path $GameDir 'Data\online.scr')   # mise à jour : l'adresse déjà réglée
New-Item -ItemType Directory -Force $Backup | Out-Null
# Une liste vide pour commencer : une installation coupée en route laisse de quoi désinstaller
if ($first) { [IO.File]::WriteAllLines($ListPath, [string[]]@()) }

# Copie des originaux, une seule fois : une mise à jour du mod repart toujours du jeu d'origine
foreach ($rel in $modFiles) {
    $target = Join-Path $GameDir $rel
    $saved  = Join-Path $Backup $rel
    # un fichier du jeu que le mod remplace : gardé, sauf s'il vient déjà d'une version précédente du mod
    if ((Test-Path -LiteralPath $target) -and -not (Test-Path -LiteralPath $saved) -and ($known -notcontains $rel)) {
        Copy-File $target $saved
    }
}
# Mise à jour : ce qu'une version précédente avait posé et que celle-ci n'a plus est remis d'origine ou retiré
$dropped = 0
foreach ($rel in $known) {
    if ($modFiles -contains $rel) { continue }
    $target = Join-Path $GameDir $rel
    $saved  = Join-Path $Backup $rel
    if (Test-Path -LiteralPath $saved) {
        Copy-File $saved $target
        Remove-Item -LiteralPath $saved -Force
    } elseif (Test-Path -LiteralPath $target) {
        Remove-Item -LiteralPath $target -Force
    }
    $dropped++
}
[IO.File]::WriteAllLines($ListPath, [string[]]@(@($known + $modFiles) | Sort-Object -Unique))

# Fichiers du mod
foreach ($rel in $modFiles) {
    Copy-File (Join-Path $Files $rel) (Join-Path $GameDir $rel)
}
[IO.File]::WriteAllLines($ListPath, [string[]]@($modFiles | Sort-Object -Unique))

# Mémoire des IA (aimap.dat : ce qu'elles ont appris sur chaque circuit) : celle de départ du paquet, seulement si
# ce jeu n'en a pas encore. Jamais écrasée ensuite, et laissée en place à la désinstallation (c'est une sauvegarde).
$aiMap = Join-Path $GameDir 'aimap.dat'
$aiMapStart = Join-Path $GameDir 'Data\aimap_depart.dat'
$aiMapNote = $null
if (Test-Path -LiteralPath $aiMapStart) {
    if (Test-Path -LiteralPath $aiMap) {
        $aiMapNote = 'Mémoire des IA : la tienne est gardée (aimap.dat).'
    } else {
        Copy-Item -LiteralPath $aiMapStart $aiMap
        $aiMapNote = 'Mémoire des IA : celle de départ du mod est copiée (aimap.dat).'
    }
}

# Adresse du serveur : celle demandée ici, sinon celle du paquet, sinon celle déjà réglée dans le jeu, sinon on la demande
$onlinePath = Join-Path $GameDir 'Data\online.scr'
$packed = Get-ServerLine $onlinePath
$address = $Server
$udpPort = $Port
if (-not $address -and $packed -and $packed.Host -ne $LocalHost) { $address = $packed.Host; if ($udpPort -le 0) { $udpPort = $packed.Port } }
if (-not $address -and $oldServer -and $oldServer.Host -ne $LocalHost) { $address = $oldServer.Host; if ($udpPort -le 0) { $udpPort = $oldServer.Port } }
if (-not $address -and -not $Quiet) {
    Write-Host ''
    Write-Host 'Adresse du serveur de jeu en ligne : celui qui l''héberge te la donne (un nom ou une adresse IP).'
    $address = (Read-Host 'Adresse du serveur (Entrée pour la régler plus tard)').Trim()
}
if ($udpPort -le 0) { $udpPort = if ($packed) { $packed.Port } else { $DefaultPort } }
if ($address) {
    if ($address -notmatch '^[A-Za-z0-9.\-]+$') { Stop-Install "« $address » n'est pas une adresse de serveur (lettres, chiffres, points et tirets)." }
    $text = [IO.File]::ReadAllText($onlinePath, $Enc)
    $text = [regex]::Replace($text, '(?m)^Server\("[^"]*",\s*\d+\)', "Server(`"$address`", $udpPort)")
    [IO.File]::WriteAllText($onlinePath, $text, $Enc)
}

$classCount = @($modFiles | Where-Object { $_ -like 'java\*' }).Count
Write-Host ''
Write-Host "Mod installé (version en ligne $($modInfo['version']), paquet du $($modInfo['date'])) : $classCount classes, $($modFiles.Count - $classCount) fichiers de données." -ForegroundColor Green
if ($dropped -gt 0) { Write-Host "$dropped fichier(s) de l'ancienne version du mod retirés." }
if ($aiMapNote) { Write-Host $aiMapNote }
if ($address) {
    Write-Host "Serveur en ligne : $address, port UDP $udpPort"
} else {
    Write-Host 'Serveur en ligne : pas encore réglé. Relance Installer.bat quand tu as l''adresse,'
    Write-Host "ou écris-la dans Data\online.scr, ligne Server(`"adresse`", $DefaultPort)."
}
Write-Host 'Lance le jeu comme d''habitude : le bouton « En ligne » et l''engrenage des réglages du mod sont au menu principal.'
Write-Host 'Fichiers d''origine gardés dans mod_backup : Desinstaller.bat les remet.'
if (-not $Quiet) { Read-Host 'Appuie sur Entrée pour fermer' | Out-Null }
exit 0
