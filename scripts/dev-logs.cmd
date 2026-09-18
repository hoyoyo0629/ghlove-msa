@echo off
powershell -NoProfile -ExecutionPolicy Bypass -Command "& '%~dp0dev-logs.ps1' %*"
