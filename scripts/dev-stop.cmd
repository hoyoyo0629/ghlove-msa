@echo off
powershell -NoProfile -ExecutionPolicy Bypass -Command "& '%~dp0dev-stop.ps1' %*"
