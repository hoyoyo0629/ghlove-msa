#!/usr/bin/env bash
# Git Bash 용 로그 뷰어. 서비스 로그는 Windows 콘솔 코드페이지(CP949)로 기록되므로,
# Git Bash(UTF-8)에서 그냥 tail 하면 한글이 깨진다. 여기서는 CP949->UTF-8 로 변환해 실시간으로 본다.
# (PowerShell 에서는 변환 없이 .\scripts\dev-logs.ps1 또는 Get-Content 로 그냥 보면 된다)
#
# 사용:
#   ./scripts/dev-logs.sh member          # member 로그 마지막 80줄 + 실시간(tail -f)
#   ./scripts/dev-logs.sh donation 200     # 마지막 200줄부터
#   (Ctrl+C 로 빠져나와도 서비스는 계속 떠 있음 - 로그 보기만 중단)
set -euo pipefail
svc="${1:-}"
tail_n="${2:-80}"
if [ -z "$svc" ]; then
  echo "사용법: $0 <서비스명> [줄수]   (member donation point gift order admin storefront)"
  exit 1
fi
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
log="$root/logs/$svc.log"
if [ ! -f "$log" ]; then
  echo "로그 파일 없음: $log"
  echo "먼저 dev-all 로 기동했는지, 서비스명이 맞는지 확인하세요."
  exit 1
fi
echo "==== $log  (CP949 -> UTF-8 변환, Ctrl+C 종료) ===="
# -c : 버퍼 경계에서 잘린 멀티바이트로 iconv 가 멈추지 않도록 불완전 문자는 건너뛴다
tail -n "$tail_n" -f "$log" | iconv -f CP949 -t UTF-8 -c
