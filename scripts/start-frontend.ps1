# ═══════════════════════════════════════════════════════════════════════════
# start-frontend.ps1 — 啟動 Vue/Vite 前端 (:5174)
# ═══════════════════════════════════════════════════════════════════════════
#
# 前提：後端已在 http://localhost:8091 運行（否則 API 代理會失敗）。
# 用法：在專案根目錄執行  .\scripts\start-frontend.ps1

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$frontend = Join-Path $root "frontend"

Set-Location $frontend

if (-not (Test-Path "package.json")) {
    Write-Host "[FAIL] frontend/package.json not found" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path "node_modules")) {
    Write-Host "=== npm install (first time) ===" -ForegroundColor Yellow
    npm install
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

Write-Host "=== Vite dev server http://localhost:5174/ ===" -ForegroundColor Green
Write-Host "Proxy /api -> http://localhost:8091" -ForegroundColor DarkGray
npm run dev
exit $LASTEXITCODE
