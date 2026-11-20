<#
  Construit le jeu modé dans game_dev à partir de game_pristine + mods.

  Pet Racer n'a pas de code.pak : la JVM du moteur charge les classes en vrac depuis game\java\
  (-Djava.class.path=Java/). Il suffit donc de déposer les .class compilés dans game_dev\java\.

  mods\java\*.java  -> compilés (JDK 8, cible 1.3, contre le rt.jar du JRE 1.x embarqué) dans game_dev\java\
  mods\data\...     -> copiés tels quels par-dessus game_dev (même arborescence que le dossier game)

  -Clean : remet game_dev à l'identique de game_pristine avant d'appliquer les mods
  -Run   : lance le jeu après le build
  -Second : recopie game_dev dans game_dev_b, un second dossier de jeu pour tester le multijoueur en ligne sur un
            seul PC (game_dev_b garde ses sauvegardes, journaux, réglages vidéo et nettest.scr ; fenêtré en 1280×720
            au premier passage). Crée aussi un nettest.scr dans chacun s'il manque : course auto à 2 (game_dev hôte).
#>
param([switch]$Clean, [switch]$Run, [switch]$Second)

$ErrorActionPreference = 'Stop'

$Root     = Split-Path -Parent $PSScriptRoot
$Pristine = Join-Path $Root 'game_pristine'
$Dev      = Join-Path $Root 'game_dev'
$Javac    = Join-Path $Root 'tools\jdk8\bin\javac.exe'
$Classes  = Join-Path $Root 'build\classes'
$JavaSrc  = Join-Path $PSScriptRoot 'java'
$DataSrc  = Join-Path $PSScriptRoot 'data'

if (Get-Process 'PetRacer' -ErrorAction SilentlyContinue) {
    throw 'Pet Racer est ouvert : ferme le jeu avant de lancer le build.'
}

if ($Clean -or -not (Test-Path $Dev)) {
    Write-Host '[clean] game_dev <- game_pristine'
    robocopy $Pristine $Dev /MIR /NFL /NDL /NJH /NJS /NP | Out-Null
    if ($LASTEXITCODE -ge 8) { throw "robocopy a échoué ($LASTEXITCODE)" }
}

# --- Java : repart des classes d'origine, puis compile et injecte les classes modifiées ---
$devJava = Join-Path $Dev 'java'
robocopy (Join-Path $Pristine 'java') $devJava /MIR /NFL /NDL /NJH /NJS /NP | Out-Null
if ($LASTEXITCODE -ge 8) { throw "robocopy a échoué ($LASTEXITCODE)" }

$sources = @(Get-ChildItem $JavaSrc -Recurse -Filter *.java -ErrorAction SilentlyContinue)
if ($sources.Count -gt 0) {
    if (Test-Path $Classes) { Remove-Item $Classes -Recurse -Force }
    New-Item -ItemType Directory -Force $Classes | Out-Null

    Write-Host "[javac] $($sources.Count) fichier(s)"
    & $Javac -nowarn -source 1.3 -target 1.3 -encoding UTF-8 `
        -bootclasspath (Join-Path $Pristine 'jre\lib\rt.jar') `
        -classpath (Join-Path $Pristine 'java') `
        -d $Classes $sources.FullName
    if ($LASTEXITCODE -ne 0) { throw 'Échec de compilation' }

    foreach ($cls in Get-ChildItem $Classes -Recurse -Filter *.class) {
        $rel = $cls.FullName.Substring($Classes.Length + 1)
        $dst = Join-Path $devJava $rel
        if (Test-Path $dst) { $tag = 'remplace' } else { $tag = 'ajoute' }
        New-Item -ItemType Directory -Force (Split-Path $dst) | Out-Null
        Copy-Item $cls.FullName $dst -Force
        Write-Host "  [$tag] $rel"
    }
}

# --- Données : superpose mods\data sur game_dev ---
if ((Test-Path $DataSrc) -and (Get-ChildItem $DataSrc -Recurse -File)) {
    Write-Host '[data] mods\data -> game_dev'
    # Un fichier modifié directement dans game_dev (plus récent que sa source) n'est plus écrasé (/XO) : on le
    # signale pour qu'il soit recopié dans mods\data, sinon un -Clean le perdrait.
    foreach ($f in Get-ChildItem $DataSrc -Recurse -File) {
        $rel = $f.FullName.Substring($DataSrc.Length + 1)
        $dst = Join-Path $Dev $rel
        if ((Test-Path $dst) -and (Get-Item $dst).LastWriteTime -gt $f.LastWriteTime) {
            Write-Host "[attention] game_dev\$rel est plus récent que mods\data\$rel : gardé tel quel (recopie-le dans mods\data)" -ForegroundColor Yellow
        }
    }
    robocopy $DataSrc $Dev /E /XX /XO /NDL /NJH /NJS /NP
    if ($LASTEXITCODE -ge 8) { throw "robocopy a échoué ($LASTEXITCODE)" }
}

# --- Second dossier de jeu (multijoueur en ligne sur un seul PC, notes/online.md) ---
if ($Second) {
    $DevB = Join-Path $Root 'game_dev_b'
    $first = -not (Test-Path $DevB)
    Write-Host '[second] game_dev -> game_dev_b'
    # Les fichiers exclus ne sont ni copiés ni supprimés : game_dev_b garde les siens (sauvegardes .dat/.cfg,
    # journaux .log/.txt de la racine, réglages vidéo, nettest.scr). Liste de noms et pas *.dat : les .dat de
    # Data\ (polices) font partie du jeu.
    $own = @('nettest.scr', 'VideoSettings.scr')
    foreach ($d in @($Dev, $DevB)) {
        if (Test-Path $d) {
            $own += @(Get-ChildItem $d -File | Where-Object { $_.Extension -in '.dat', '.cfg', '.log', '.txt' } | ForEach-Object { $_.Name })
        }
    }
    $own = @($own | Sort-Object -Unique)
    robocopy $Dev $DevB /MIR /XF $own /NFL /NDL /NJH /NJS /NP | Out-Null
    if ($LASTEXITCODE -ge 8) { throw "robocopy a échoué ($LASTEXITCODE)" }
    if ($first) {
        # Premier passage : même profil et mêmes réglages que game_dev, mais en fenêtré 1280×720
        Copy-Item (Join-Path $Dev '*.dat') $DevB
        Copy-Item (Join-Path $Dev '*.cfg') $DevB
        $enc = [Text.Encoding]::GetEncoding(1252)
        $video = [IO.File]::ReadAllText((Join-Path $Dev 'Data\VideoSettings.scr'), $enc)
        $video = $video -replace '(?m)^Fullscreen\(\)\r?\n', '' -replace '(?m)^Resolution\(\d+,\d+\)', 'Resolution(1280,720)'
        [IO.File]::WriteAllText((Join-Path $DevB 'Data\VideoSettings.scr'), $video, $enc)
        Write-Host '[second] game_dev_b en fenêtré 1280×720 (Data\VideoSettings.scr)'
    }
    # Réglages de test réseau (ModOnline / ModNet) : game_dev héberge une course à 2, game_dev_b la rejoint
    $head = "!NetPort(i)`r`n!NetTest(s,i)`r`n!NetAuto(s)`r`n!NetPeer(s,i)`r`n!NetPlayers(i)`r`n!NetTrack(i)`r`n!NetLaps(i)`r`n!NetAnimal(i)`r`n!NetBonus(i)`r`n`r`n"
    $tests = @(
        @($Dev, "/* Test reseau (notes/online.md) : hote d'une course a 2 */`r`n$($head)NetPort(7781)`r`nNetAuto(`"host`")`r`nNetPlayers(2)`r`nNetTrack(0)`r`nNetLaps(2)`r`nNetAnimal(0)`r`nNetBonus(1)`r`n"),
        @($DevB, "/* Test reseau (notes/online.md) : invite d'une course a 2 */`r`n$($head)NetPort(7782)`r`nNetAuto(`"join`")`r`nNetPeer(`"127.0.0.1`", 7781)`r`nNetAnimal(3)`r`n"))
    foreach ($t in $tests) {
        $f = Join-Path $t[0] 'nettest.scr'
        if (-not (Test-Path $f)) {
            [IO.File]::WriteAllText($f, $t[1], [Text.Encoding]::ASCII)
            Write-Host "[second] $f créé"
        }
    }
}

Write-Host '[ok] build terminé'

if ($Run) {
    Start-Process (Join-Path $Dev 'PetRacer.exe') -WorkingDirectory $Dev
}
exit 0
