. "$PSScriptRoot\env.ps1"
# ═══════════════════════════════════════════════════════════════════════════
# check.ps1 — 一鍵驗證（unit + integration）
# ═══════════════════════════════════════════════════════════════════════════
Set-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "`n=== TradingPagingList: gradlew check ===" -ForegroundColor Yellow
.\gradlew.bat check
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "`n[OK] check passed" -ForegroundColor Green
