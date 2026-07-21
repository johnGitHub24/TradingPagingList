# ═══════════════════════════════════════════════════════════════════════════
# bootRun.ps1 — 本機啟動後端 :8091（ASCII banner，Console 不亂碼）
# ═══════════════════════════════════════════════════════════════════════════

$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

# UTF-8 code page helps IntelliJ/Windows Terminal show logs cleanly
chcp 65001 > $null
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)

Write-Host "=== TradingPagingList bootRun (:8091) ===" -ForegroundColor Yellow
Write-Host "Frontend is NOT included. Open another terminal:" -ForegroundColor DarkYellow
Write-Host "  .\scripts\start-frontend.ps1" -ForegroundColor Cyan
Write-Host "Or use: .\scripts\start-all.ps1" -ForegroundColor Cyan
Write-Host ""

.\gradlew.bat bootRun
exit $LASTEXITCODE
