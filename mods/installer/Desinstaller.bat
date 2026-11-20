@echo off
rem Retire le mod de Pet Racer et remet les fichiers d'origine. Voir LISEZMOI.txt.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0install.ps1" -Uninstall %*
