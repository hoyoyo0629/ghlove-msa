@echo off
powershell -NoProfile -ExecutionPolicy Bypass -Command "& '%~dp0dev-status.ps1' %*"
