@echo off
echo ============================================================
echo   Reset MySQL 9.6 root password  (to root123456)
echo   IMPORTANT: run this window AS ADMINISTRATOR
echo ============================================================
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Users\Administrator\Desktop\a\magao-platform\reset-mysql-root.ps1"
echo.
echo Script finished. Review the output above.
pause
