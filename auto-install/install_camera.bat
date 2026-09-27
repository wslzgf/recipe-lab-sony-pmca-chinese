@echo off
setlocal
title Film Lab RecipeLab CN - Camera Auto Installer

echo ============================================================
echo   Film Lab (RecipeLab) Chinese Version - Camera Auto Install
echo ============================================================
echo.

rem ---- Check for administrator rights (pmca-console needs USB access) ----
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo.
    echo   Not running as administrator.
    echo   A window will now open requesting administrator privileges.
    echo   IMPORTANT: Please click "Yes" in the popup to continue.
    echo.
    echo   Waiting for the administrator window to finish...
    echo.
    powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs -Wait"
    echo.
    echo   The administrator window has finished.
    echo   If nothing was installed, please reconnect the camera,
    echo   set USB mode to Mass Storage, and run this file again.
    echo.
    pause
    exit /b
)

echo   Running as administrator.
echo.
echo   Running the installer script...
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0install_camera.ps1"

echo.
echo ============================================================
echo   Script finished.
echo ============================================================
echo.
pause
endlocal
