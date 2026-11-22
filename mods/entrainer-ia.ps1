<#
  Entraînement automatique des IA (notes/ia.md, « Entraînement automatique ») : game_dev enchaîne tout seul des
  courses de 6 IA, sans joueur, sur tous les circuits, pour remplir leur mémoire (game_dev\aimap.dat) sans jouer.
  Ensuite : mods\package.ps1 -AiMap copie cette mémoire dans mods\data\Data\aimap_depart.dat (celle des amis).

  Pendant l'entraînement, game_dev passe en fenêtré 1280×720 (on peut se servir du PC à côté) et muet ; son
  Data\VideoSettings.scr d'origine est remis à la fin. Le jeu se ferme tout seul après la dernière course.
  Échap dans le jeu arrête l'entraînement (le jeu reste ouvert) ; relancer le script reprend à la course
  interrompue (game_dev\entrainement_suite.txt), sauf avec -Nouveau.

  powershell -ExecutionPolicy Bypass -File mods\entrainer-ia.ps1                 # 3 passes sur les 31 circuits
  powershell -ExecutionPolicy Bypass -File mods\entrainer-ia.ps1 -Passes 6       # plus long (une nuit)
  powershell -ExecutionPolicy Bypass -File mods\entrainer-ia.ps1 -Vitesse 6      # temps x6 au plus (défaut x12)
  powershell -ExecutionPolicy Bypass -File mods\entrainer-ia.ps1 -Circuits 0,6,30 -Passes 1   # quelques circuits
  powershell -ExecutionPolicy Bypass -File mods\entrainer-ia.ps1 -Nouveau        # repart de la 1re course
  powershell -ExecutionPolicy Bypass -File mods\entrainer-ia.ps1 -Package        # puis package.ps1 -AiMap

  -Niveaux : difficulté des IA à chaque passe, en boucle (expert par défaut, pour une courbe d'évolution lisible) :
             facile, normal, difficile, expert ou auto ; ex. -Niveaux normal,expert,difficile
  -NouvelHistorique : range game_dev\entrainement_historique.txt en entrainement_historique_<date>.txt et repart
             d'un rapport vide (l'historique s'accumule sinon d'un entraînement à l'autre)
  -Tours   : tours par course (3)
  -Chrono  : « IA chrono » (notes/ia.md, « IA chrono ») : les 6 IA, du même animal et traversables, essaient chacune
             une variante du meilleur plan de course connu (trajectoire et vitesse à chaque porte) ; les variantes plus
             rapides sur les tours lancés deviennent la nouvelle base. Mémoire à part : game_dev\aichrono.dat (aimap.dat,
             celle des IA du jeu et du multi, n'est pas touchée). Par défaut avec -Chrono : Forêt 1 (-Circuits 0),
             100 passes de 8 tours (~2,8 min par course : ~4 h 40), expert. -Circuits tous : les 31 circuits, 20 passes
             par défaut (une course par circuit à chaque passe, ~29 h) ; chaque circuit garde son plan, on peut arrêter (Échap)
             et reprendre quand on veut.
             powershell -ExecutionPolicy Bypass -File mods\entrainer-ia.ps1 -Chrono
             powershell -ExecutionPolicy Bypass -File mods\entrainer-ia.ps1 -Chrono -Circuits tous
             powershell -ExecutionPolicy Bypass -File mods\entrainer-ia.ps1 -Chrono -Circuits 0,6,12 -Passes 200
  -Animal  : avec -Chrono, l'animal des 6 voitures (Ours par défaut : stats équilibrées) ; un plan par animal
  -Relance : le jeu est fermé et relancé toutes les 31 courses (0 = jamais) : il ralentit quand il tourne longtemps
             (2026-11-21 : toutes les voitures +6 à 9 % en 7 h, images/s 1200 -> 700, pas de physique 6,8 -> 10,6 ms)
  -Son     : garde le son
  -Bonus   : garde les boîtes bonus (sans bonus par défaut : un tir ou un piège qui envoie l'IA dans le décor
             serait appris comme un mur ou un endroit dangereux)
  -PleinEcran : garde les réglages vidéo de game_dev
  -NoBuild : sans rebuild
  -SansRapport : n'ouvre pas build\rapport_ia.html (rapport HTML, refait en direct toutes les 30 s pendant les
             courses, depuis game_dev\entrainement_historique.txt ; à la main : python mods\rapport-ia.py --ouvrir)
  -Restore : remet seulement les réglages vidéo de game_dev et retire entrainement.scr (après un script interrompu)
  Circuits (numéros de RacerModule.MAPNAMES) : 0-5 Forêt, 6-11 Désert, 12-17 Hiver, 18-23 Tropiques, 24-29 Lune,
  30 Royal (dans chaque monde : les 3 circuits, puis leurs variantes Track2).
#>
param(
    [int]$Passes = 3,
    [int]$Tours = 3,
    [int]$Vitesse = 12,
    [string[]]$Niveaux = @('expert'),
    [string[]]$Circuits = @(),
    [switch]$Son,
    [switch]$Bonus,
    [switch]$PleinEcran,
    [switch]$Nouveau,
    [switch]$NoBuild,
    [switch]$Package,
    [switch]$SansRapport,
    [switch]$NouvelHistorique,
    [switch]$Restore,
    [switch]$Chrono,
    [string]$Animal = 'Ours',
    [int]$Relance = 31
)

$ErrorActionPreference = 'Stop'

$Root     = Split-Path -Parent $PSScriptRoot
$Dev      = Join-Path $Root 'game_dev'
$Video    = Join-Path $Dev 'Data\VideoSettings.scr'
$Backup   = Join-Path $Dev 'Data\VideoSettings.entrainement.scr'
$Config   = Join-Path $Dev 'entrainement.scr'
$Progress = Join-Path $Dev 'entrainement_suite.txt'
$Restart  = Join-Path $Dev 'entrainement_relance.txt' # écrit par le jeu quand il se ferme pour être relancé (-Relance)
$Log      = Join-Path $Dev 'crash.log'
$History  = Join-Path $Dev 'entrainement_historique.txt'
$Report   = Join-Path $Root 'build\rapport_ia.html'
$RefreshSeconds = 30
$enc      = [Text.Encoding]::GetEncoding(1252)

function Write-Report([bool]$Live, [bool]$Open) {
    $a = @((Join-Path $PSScriptRoot 'rapport-ia.py'), '--jeu', $Dev, '--sortie', $Report)
    if ($Live) { $a += @('--direct', "$RefreshSeconds") }
    if ($Open) { $a += '--ouvrir' }
    & python @a
    if ($LASTEXITCODE -ne 0) { Write-Host '[rapport] échec (python et Pillow installés ?)' -ForegroundColor Yellow }
}

function Restore-All {
    if (Test-Path $Backup) {
        Copy-Item $Backup $Video -Force
        Remove-Item $Backup
        Write-Host '[video] game_dev : réglages vidéo d''origine remis'
    }
    if (Test-Path $Config) {
        Remove-Item $Config
        Write-Host '[entrainement] game_dev\entrainement.scr retiré (le jeu redémarre normalement)'
    }
}

# seulement le jeu de game_dev : celui de game_joueur (mods\jouer.ps1, pour jouer pendant l'entraînement) ne gêne pas
if (Get-Process 'PetRacer' -ErrorAction SilentlyContinue | Where-Object { -not $_.Path -or $_.Path.StartsWith($Dev + '\', [StringComparison]::OrdinalIgnoreCase) }) {
    throw 'Pet Racer est déjà ouvert depuis game_dev : ferme-le avant l''entraînement.'
}

Restore-All # reste d'un entraînement interrompu
if ($Restore) { exit 0 }

# difficultés : noms -> ModAI (1 auto, 2 facile, 3 normal, 4 difficile, 5 expert)
$levelIds = @{ 'auto' = 1; 'facile' = 2; 'normal' = 3; 'difficile' = 4; 'expert' = 5 }
$levels = @()
foreach ($n in ($Niveaux -join ',').Split(',')) {
    $k = $n.Trim().ToLower()
    if ($k -eq '') { continue }
    if (-not $levelIds.ContainsKey($k)) { throw "Niveau inconnu : $n (auto, facile, normal, difficile, expert)" }
    $levels += $levelIds[$k]
}
if ($levels.Count -eq 0) { throw 'Aucun niveau' }
if ($Chrono) {
    # IA chrono : beaucoup de courses par circuit (Forêt 1 seul par défaut ; -Circuits tous : les 31)
    if (-not $PSBoundParameters.ContainsKey('Circuits')) { $Circuits = @('0') }
    # 8 tours : 7 tours lancés mesurés par voiture au lieu de 2, pour ~30 s de plus par course (le chargement domine) ;
    # un accident (+8 s) pèse 3,5 fois moins dans la note d'un plan
    if (-not $PSBoundParameters.ContainsKey('Tours')) { $Tours = 8 }
    $animals = @('Panthere', 'Dino', 'Poulet', 'Grenouille', 'Tortue', 'Requin', 'Taureau', 'Girafe', 'Ours', 'Gorille', 'Robot', 'Lion')
    if (-not ($animals -contains $Animal)) { throw "Animal inconnu : $Animal ($($animals -join ', '))" }
}
# « -Circuits 5,14,17,18 » arrive en un seul texte avec powershell -File (et devenait 5141718) : découpé à la main
$tracks = @()
foreach ($n in ($Circuits -join ',').Split(',')) {
    $t = $n.Trim()
    if ($t -eq '') { continue }
    if (@('tous', 'all') -contains $t.ToLower()) { $tracks = @(); break } # tous les circuits (liste vide)
    $c = 0
    if (-not [int]::TryParse($t, [ref]$c) -or $c -lt 0 -or $c -gt 30) { throw "Circuit $t hors de 0..30" }
    $tracks += $c
}
if ($Chrono -and -not $PSBoundParameters.ContainsKey('Passes')) {
    $Passes = if ($tracks.Count -eq 1) { 100 } else { 20 }
}

if (-not $NoBuild) {
    & (Join-Path $PSScriptRoot 'build.ps1')
    if ($LASTEXITCODE -ne 0) { throw 'Échec du build' }
}

if ($NouvelHistorique -and (Test-Path $History)) {
    $archive = Join-Path $Dev ('entrainement_historique_' + (Get-Item $History).LastWriteTime.ToString('yyyy-MM-dd_HHmm') + '.txt')
    Move-Item $History $archive
    Write-Host "[rapport] ancien historique rangé dans $archive"
}

# la reprise ne vaut que pour les mêmes réglages : « course 4 » d'un Forêt 1 seul n'est pas la course 4 de
# -Circuits tous (2026-11-21 : reprise à la course 4 = circuit n° 3, Forêt 1, 2 et 3 sautés). -Passes peut changer.
$cfgFile = Join-Path $Dev 'entrainement_suite_reglages.txt'
$cfg = "chrono=$([int][bool]$Chrono) circuits=$(if ($tracks.Count -gt 0) { $tracks -join ',' } else { 'tous' }) tours=$Tours niveaux=$($levels -join ',') animal=$Animal"
$cfgOld = if (Test-Path $cfgFile) { (Get-Content $cfgFile -TotalCount 1).Trim() } else { $null }
if ($Nouveau -and (Test-Path $Progress)) {
    Remove-Item $Progress
    Write-Host '[entrainement] reprise effacée : départ à la 1re course'
} elseif ((Test-Path $Progress) -and $cfgOld -and $cfgOld -ne $cfg) {
    Remove-Item $Progress
    Write-Host "[entrainement] réglages changés depuis le dernier lancement ($cfgOld) : départ à la 1re course"
} elseif (Test-Path $Progress) {
    $n = [int]((Get-Content $Progress -TotalCount 1).Trim()) + 1
    Write-Host "[entrainement] reprise à la course $n (-Nouveau pour repartir de zéro)"
}
Set-Content -Path $cfgFile -Value $cfg -Encoding ASCII

$nTracks = if ($tracks.Count -gt 0) { $tracks.Count } else { 31 }
$total = $Passes * $nTracks
$scr = "/* Entrainement automatique des IA (mods\entrainer-ia.ps1, ModTraining) : retire a la fin */`r`n" +
       "!Passes(i)`r`n!Tours(i)`r`n!Vitesse(i)`r`n!Niveaux(s)`r`n!Circuits(s)`r`n!Son(i)`r`n!Bonus(i)`r`n!Relance(i)`r`n`r`n" +
       "Passes($Passes)`r`nTours($Tours)`r`nVitesse($Vitesse)`r`nNiveaux(`"$($levels -join ',')`")`r`nRelance($([Math]::Max(0, $Relance)))`r`n" +
       $(if ($tracks.Count -gt 0) { "Circuits(`"$($tracks -join ',')`")`r`n" } else { '' }) +
       "Son($(if ($Son) { 1 } else { 0 }))`r`nBonus($(if ($Bonus) { 1 } else { 0 }))`r`n" +
       $(if ($Chrono) { "!Chrono(i)`r`n!Animal(s)`r`nChrono(1)`r`nAnimal(`"$Animal`")`r`n" } else { '' })

$started = Get-Date
$memBefore = if (Test-Path (Join-Path $Dev 'aimap.dat')) { (Get-Item (Join-Path $Dev 'aimap.dat')).Length } else { 0 }
try {
    [IO.File]::WriteAllText($Config, $scr, [Text.Encoding]::ASCII)
    if (-not $PleinEcran) {
        Copy-Item $Video $Backup
        $txt = [IO.File]::ReadAllText($Video, $enc)
        $txt = $txt -replace '(?m)^Fullscreen\(\)\r?\n', '' -replace '(?m)^Resolution\(\d+,\d+\)', 'Resolution(1280,720)'
        [IO.File]::WriteAllText($Video, $txt, $enc)
        Write-Host '[video] game_dev : fenêtré 1280×720 pendant l''entraînement'
    }

    if ($Chrono) {
        $est = [TimeSpan]::FromMinutes((1.2 + 0.2 * $Tours) * $total) # ~1 min de chargement + ~12 s par tour (Forêt 1, x6)
        Write-Host "[chrono] IA chrono : 6 x $Animal, $Passes course(s) par circuit, plans dans game_dev\aichrono.dat (aimap.dat pas touché) ; $Tours tours, ~$([Math]::Round(1.2 + 0.2 * $Tours, 1)) min par course : environ $([Math]::Floor($est.TotalHours)) h $($est.Minutes.ToString('00'))"
    }
    Write-Host "[entrainement] $total courses ($Passes passe(s) x $nTracks circuits), $Tours tours, temps jusqu'à x$Vitesse (selon les images/s : physique par pas de 7 ms au plus), niveaux $($Niveaux -join ', '), $(if ($Bonus) { 'avec' } else { 'sans' }) bonus"
    Write-Host '[entrainement] Échap dans le jeu pour arrêter ; le jeu se ferme tout seul à la fin'
    if ($Relance -gt 0) { Write-Host "[entrainement] jeu fermé et relancé toutes les $Relance courses (-Relance 0 : jamais)" }
    if (Test-Path $Restart) { Remove-Item $Restart }
    $nLaunch = 0
    while ($true) {
        $nLaunch++
        $p = Start-Process (Join-Path $Dev 'PetRacer.exe') -WorkingDirectory $Dev -PassThru
        if ($SansRapport) {
            Wait-Process -Id $p.Id -ErrorAction SilentlyContinue
        } else {
            # rapport en direct : refait à chaque nouvelle course, la page se recharge toute seule
            Write-Report -Live $true -Open ($nLaunch -eq 1)
            $lastSize = -1
            while (-not $p.HasExited) {
                Wait-Process -Id $p.Id -Timeout $RefreshSeconds -ErrorAction SilentlyContinue
                $size = if (Test-Path $History) { (Get-Item $History).Length } else { 0 }
                if ($size -ne $lastSize -and -not $p.HasExited) {
                    $lastSize = $size
                    Write-Report -Live $true -Open $false
                }
            }
        }

        # fermé pour être relancé (Relance) : on repart ; sinon (fin, Échap puis fermeture, plantage) c'est fini
        if (-not (Test-Path $Restart)) { break }
        $next = (Get-Content $Restart -TotalCount 1).Trim()
        Remove-Item $Restart
        Write-Host "[entrainement] $(Get-Date -Format 'HH:mm') : jeu relancé (lancement $($nLaunch + 1)), reprise à la course $next"
        Start-Sleep -Seconds 3 # le temps que le jeu libère ses fichiers
    }
} finally {
    Restore-All
    if (-not $SansRapport) { Write-Report -Live $false -Open $false }
}

$mins = [int]((Get-Date) - $started).TotalMinutes
Write-Host "--- $Log (entraînement, $mins min) ---"
Select-String -Path $Log -Pattern '\[mod\] (entrainement|IA carte .*memoire enregistree|chrono .*finie)' -ErrorAction SilentlyContinue | ForEach-Object { $_.Line }
$memAfter = if (Test-Path (Join-Path $Dev 'aimap.dat')) { (Get-Item (Join-Path $Dev 'aimap.dat')).Length } else { 0 }
Write-Host "[memoire] game_dev\aimap.dat : $memBefore -> $memAfter octets"

if (Test-Path $Progress) {
    Write-Host '[entrainement] pas terminé : relance le script pour reprendre'
} elseif ($Package) {
    & (Join-Path $PSScriptRoot 'package.ps1') -AiMap -NoBuild
} else {
    Write-Host '[entrainement] terminé : mods\package.ps1 -AiMap pour mettre cette mémoire dans aimap_depart.dat'
}
