@echo off
powershell -NoProfile -ExecutionPolicy Bypass -Command "& '%~dp0dev-all.ps1' %*"
