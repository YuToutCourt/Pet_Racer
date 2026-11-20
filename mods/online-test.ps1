<#
  Lance deux jeux côte à côte pour tester le multijoueur en ligne sur un seul PC (notes/online.md).

  Le plein écran empêche d'avoir une autre fenêtre ouverte : pendant le test, game_dev passe en fenêtré 1280×720
  (comme game_dev_b), puis son Data\VideoSettings.scr d'origine est remis quand les deux jeux sont fermés.
  Si le script a été interrompu avant (fenêtre PowerShell fermée), la sauvegarde est remise au lancement suivant,
  ou à la main avec -Restore.

  powershell -ExecutionPolicy Bypass -File mods\online-test.ps1            # build -Second, puis les deux jeux
  powershell -ExecutionPolicy Bypass -File mods\online-test.ps1 -NoBuild   # sans rebuild
  powershell -ExecutionPolicy Bypass -File mods\online-test.ps1 -Echo      # + serveur d'écho (server\echo_test.py)
  powershell -ExecutionPolicy Bypass -File mods\online-test.ps1 -Server    # phase 3 : par le serveur des salons
      (server\server.py --code TEST dans sa propre fenêtre ; NetRoom("TEST") ajouté le temps du test aux deux
       nettest.scr, qui sont remis ensuite)
  powershell -ExecutionPolicy Bypass -File mods\online-test.ps1 -Screen    # phase 3 : écran « En ligne » à la main
      (server\server.py -v sans code imposé ; les lignes NetAuto / NetPeer / NetRoom sont retirées des deux
       nettest.scr le temps du test : on crée le salon dans un jeu et on tape son code dans l'autre)
  powershell -ExecutionPolicy Bypass -File mods\online-test.ps1 -Restore   # remet seulement le plein écran de game_dev
#>
param([switch]$NoBuild, [switch]$Echo, [switch]$Server, [switch]$Screen, [switch]$Restore)

$ErrorActionPreference = 'Stop'

$Root   = Split-Path -Parent $PSScriptRoot
$Dev    = Join-Path $Root 'game_dev'
$DevB   = Join-Path $Root 'game_dev_b'
$Video  = Join-Path $Dev 'Data\VideoSettings.scr'
$Backup = Join-Path $Dev 'Data\VideoSettings.fullscreen.scr'
$enc    = [Text.Encoding]::GetEncoding(1252)

function Restore-NetTest {
    foreach ($d in @($Dev, $DevB)) {
        $f = Join-Path $d 'nettest.scr'
        $b = Join-Path $d 'nettest.direct.scr'
        if (Test-Path $b) {
            Copy-Item $b $f -Force
            Remove-Item $b
            Write-Host "[salon] $f remis (course en direct)"
        }
    }
}

function Restore-Video {
    if (Test-Path $Backup) {
        Copy-Item $Backup $Video -Force
        Remove-Item $Backup
        Write-Host '[video] game_dev : réglages vidéo d''origine remis'
    }
}

if (Get-Process 'PetRacer' -ErrorAction SilentlyContinue) {
    throw 'Pet Racer est déjà ouvert : ferme-le avant le test.'
}

Restore-Video # reste d'un test interrompu
Restore-NetTest
if ($Restore) { exit 0 }

if (-not $NoBuild -or -not (Test-Path $DevB)) {
    & (Join-Path $PSScriptRoot 'build.ps1') -Second
    if ($LASTEXITCODE -ne 0) { throw 'Échec du build' }
}

# game_dev en fenêtré 1280×720 le temps du test
Copy-Item $Video $Backup
$txt = [IO.File]::ReadAllText($Video, $enc)
$txt = $txt -replace '(?m)^Fullscreen\(\)\r?\n', '' -replace '(?m)^Resolution\(\d+,\d+\)', 'Resolution(1280,720)'
[IO.File]::WriteAllText($Video, $txt, $enc)
Write-Host '[video] game_dev : fenêtré 1280×720 pendant le test'

$echoProc = $null
$serverProc = $null
try {
    if ($Echo) {
        $echoProc = Start-Process python -ArgumentList '-I', (Join-Path $Root 'server\echo_test.py') -PassThru
    }
    if ($Screen) {
        # écran « En ligne » : pas de course automatique, chaque jeu garde seulement son port
        foreach ($d in @($Dev, $DevB)) {
            $f = Join-Path $d 'nettest.scr'
            Copy-Item $f (Join-Path $d 'nettest.direct.scr')
            $lines = [IO.File]::ReadAllLines($f, $enc) | Where-Object { $_ -notmatch '^(NetAuto|NetPeer|NetRoom|NetTest)\(' }
            [IO.File]::WriteAllLines($f, [string[]]$lines, $enc)
        }
        Write-Host '[salon] course automatique retiree des deux nettest.scr pendant le test (ecran En ligne)'
        # --fill 5 : partie rapide à 2 lancée après 5 s d'attente (15 par défaut)
        $serverProc = Start-Process python -ArgumentList (Join-Path $Root 'server\server.py'), '-v', '--fill', '5' -WorkingDirectory (Join-Path $Root 'server') -PassThru
        Start-Sleep -Seconds 1
    } elseif ($Server) {
        # les deux jeux passent par le serveur : l'hôte crée le salon (code imposé TEST), l'invité le rejoint
        foreach ($d in @($Dev, $DevB)) {
            $f = Join-Path $d 'nettest.scr'
            Copy-Item $f (Join-Path $d 'nettest.direct.scr')
            $txt = [IO.File]::ReadAllText($f, $enc)
            [IO.File]::WriteAllText($f, "!NetRoom(s)`r`n" + $txt.TrimEnd() + "`r`nNetRoom(`"TEST`")`r`n", $enc)
        }
        Write-Host '[salon] NetRoom("TEST") dans les deux nettest.scr pendant le test'
        $serverProc = Start-Process python -ArgumentList (Join-Path $Root 'server\server.py'), '--code', 'TEST', '-v' -WorkingDirectory (Join-Path $Root 'server') -PassThru
        Start-Sleep -Seconds 1
    }

    $a = Start-Process (Join-Path $Dev 'PetRacer.exe') -WorkingDirectory $Dev -PassThru
    Start-Sleep -Seconds 3 # le second après le premier : les deux ne se battent pas pour le chargement
    $b = Start-Process (Join-Path $DevB 'PetRacer.exe') -WorkingDirectory $DevB -PassThru
    Write-Host '[test] deux jeux lancés ; ferme-les tous les deux pour finir (le plein écran de game_dev sera remis)'
    Wait-Process -Id $a.Id, $b.Id -ErrorAction SilentlyContinue
} finally {
    if ($echoProc -and -not $echoProc.HasExited) { Stop-Process -Id $echoProc.Id }
    if ($serverProc -and -not $serverProc.HasExited) { Stop-Process -Id $serverProc.Id }
    Restore-NetTest
    Restore-Video
}

foreach ($d in @($Dev, $DevB)) {
    Write-Host "--- $d\crash.log ---"
    Select-String -Path (Join-Path $d 'crash.log') -Pattern '\[mod\] (net|IA )' -ErrorAction SilentlyContinue | ForEach-Object { $_.Line }
}
