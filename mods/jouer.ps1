<#
  Lance Pet Racer modé dans game_joueur, un dossier de jeu à toi, pour jouer pendant que les IA s'entraînent dans
  game_dev (mods\entrainer-ia.ps1). Les deux jeux tournent en même temps sans se gêner : chacun a ses sauvegardes.

  game_joueur est mis à jour depuis game_dev à chaque lancement (classes du mod, données du jeu), sauf ce qui est à
  toi : sauvegardes et records (progress.cfg…), mémoire de ses IA (aimap.dat), réglages vidéo, journaux. Au tout
  premier lancement, il reprend ton profil, tes records et la mémoire des IA de game_dev, et les réglages vidéo
  d'avant l'entraînement.

  Tes records de game_joueur s'affichent dans le rapport (build\rapport_ia.html, refait toutes les 30 s pendant
  l'entraînement) : « Ton record (course ÷ tours) », à côté de l'IA chrono au même calcul.

  powershell -ExecutionPolicy Bypass -File mods\jouer.ps1
  -NoRun : prépare game_joueur sans lancer le jeu
#>
param([switch]$NoRun)

$ErrorActionPreference = 'Stop'

$Root   = Split-Path -Parent $PSScriptRoot
$Dev    = Join-Path $Root 'game_dev'
$Joueur = Join-Path $Root 'game_joueur'

if (-not (Test-Path (Join-Path $Dev 'PetRacer.exe'))) { throw 'game_dev introuvable : lance d''abord mods\build.ps1' }
if (Get-Process 'PetRacer' -ErrorAction SilentlyContinue | Where-Object { $_.Path -and $_.Path.StartsWith($Joueur, [StringComparison]::OrdinalIgnoreCase) }) {
    throw 'Pet Racer est déjà ouvert depuis game_joueur.'
}

$first = -not (Test-Path $Joueur)
# À toi, jamais écrasé : sauvegardes .dat/.cfg, journaux .log/.txt de la racine, réglages vidéo. Pas copiés : les
# fichiers de l'entraînement (entrainement.scr le lancerait ici aussi), du test réseau (nettest.scr rejoindrait une
# course en ligne) et la sauvegarde des réglages vidéo d'entraînement.
$own = @('VideoSettings.scr', 'VideoSettings.entrainement.scr', 'entrainement.scr', 'nettest.scr')
foreach ($d in @($Dev, $Joueur)) {
    if (Test-Path $d) {
        $own += @(Get-ChildItem $d -File | Where-Object { $_.Extension -in '.dat', '.cfg', '.log', '.txt' } | ForEach-Object { $_.Name })
    }
}
$own = @($own | Sort-Object -Unique)
Write-Host "[jouer] game_dev -> game_joueur$(if ($first) { ' (premier lancement : ~370 Mo)' })"
robocopy $Dev $Joueur /MIR /XF $own /NFL /NDL /NJH /NJS /NP | Out-Null
if ($LASTEXITCODE -ge 8) { throw "robocopy a échoué ($LASTEXITCODE)" }

if ($first) {
    foreach ($f in @('progress.cfg', 'grandprix.dat', 'mods.dat', 'online.dat', 'aimap.dat')) {
        $src = Join-Path $Dev $f
        if (Test-Path $src) { Copy-Item $src $Joueur }
    }
    # réglages vidéo d'avant l'entraînement (celui-ci passe game_dev en petite fenêtre et garde l'original à côté)
    $video = Join-Path $Dev 'Data\VideoSettings.entrainement.scr'
    if (-not (Test-Path $video)) { $video = Join-Path $Dev 'Data\VideoSettings.scr' }
    Copy-Item $video (Join-Path $Joueur 'Data\VideoSettings.scr')
    Write-Host '[jouer] profil, records et mémoire des IA repris de game_dev'
}

if ($NoRun) { exit 0 }
Write-Host '[jouer] Pet Racer (game_joueur)'
Start-Process (Join-Path $Joueur 'PetRacer.exe') -WorkingDirectory $Joueur
