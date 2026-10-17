@echo off
cd /d %~dp0frontend
call npm run dev > vite-dev.log 2>&1