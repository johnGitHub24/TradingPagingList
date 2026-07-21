# ═══════════════════════════════════════════════════════════════════════════
# start-all.ps1 — 後端 + 前端一鍵啟動（兩個視窗）
# ═══════════════════════════════════════════════════════════════════════════
#
# 會開兩個 PowerShell 視窗：
#   1) gradlew bootRun  → :8091
#   2) Vite npm run dev → :5174
#
# 瀏覽器請開：http://localhost:5174/

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent

Write-Host "Starting TradingPagingList full stack..." -ForegroundColor Yellow
Write-Host "  Backend : http://localhost:8091" -ForegroundColor DarkGray
Write-Host "  Frontend: http://localhost:5174  <-- open this in browser" -ForegroundColor Green

Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-File", (Join-Path $PSScriptRoot "bootRun.ps1")
)

Start-Sleep -Seconds 2

Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-File", (Join-Path $PSScriptRoot "start-frontend.ps1")
)

Write-Host ""
Write-Host "Two windows launched. Wait until Vite prints 'Local: http://localhost:5174/'" -ForegroundColor Cyan
Write-Host "Then open: http://localhost:5174/" -ForegroundColor Cyan
