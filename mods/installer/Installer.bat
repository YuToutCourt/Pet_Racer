@echo off
rem Installe (ou met a jour) le mod de Pet Racer. Voir LISEZMOI.txt.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0install.ps1" %*
