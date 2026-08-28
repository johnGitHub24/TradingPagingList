# TradingPagingList — 專案規則（薄）

繼承：EngineeringOS eos-minimal @ **0.1.13**
公版：`EngineeringOS/eos-minimal/`
權威規格：本 [README.md](README.md) + [docs/architecture.md](docs/architecture.md)

## 與公版差異

- Backend port: **8091**
- Frontend: Vue 3 + Vite（:5174，optional-frontend: yes）
- DB: H2（dev/test）／PostgreSQL（prod）
- 驗證入口：`.\scripts\check.ps1`（載入 JDK 21 後 `gradlew check`＝unit + integration）
- 本機 Demo：IntelliJ／Gradle `bootRun`（**勿**對 `*Application` 綠箭頭）；前端另開 `cd frontend && npm run dev`
- Gate **不**要求 npm；Vite 僅本機 Demo

## 本專案專屬

- Domain: Product CRUD + 伺服器分頁（default pageSize=10，cap 100）
- Case：PRODUCT-001 create／002 get／003 list／004 update／005 delete／006 404（單元+整合成對）
- 架構：`docs/architecture.md`；測試：`docs/testing.md`
- 前端：Vue 3 分頁表格（`frontend/`）

## 註解深度
- comment_verbosity: **detailed**
- 權威：`EngineeringOS/eos-minimal/knowledge/comments.md` §0／§3b（eos-minimal @ 0.1.13）
- 結構：【職責】【技巧】【概念】；簡單 getter 可併入類別說明

## Git Remote
- 帳號：`johnGitHub24`；一專案一 repo
- 規範：`EngineeringOS/eos-minimal/knowledge/專案上船-GitHub.md`

## 回寫

問題與公版改善建議 → `EngineeringOS/eos-minimal/feedback/SYNC_LOG.md`
