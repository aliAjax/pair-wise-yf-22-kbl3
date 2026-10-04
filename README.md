# 制造业质量追溯 API 服务

面向小型制造工厂的批次质量追溯后端服务，覆盖工单、批次、检验项、不良记录和追溯查询。

## 快速启动

```bash
cp .env.example .env && docker compose up -d
```

## 访问地址或 CLI 示例

后端健康检查：<http://localhost:21114/health>

所有 `/api/**` 接口需要请求头 `X-Auth-Token`（任意非空）和 `X-Role`（`INSPECTOR` / `LINE_SUPERVISOR` / `QUALITY_MANAGER` / `AUDITOR`）。

```bash
# 质检员提交检验（逐项写入检验结果，随后重算批次结论）
curl -X POST http://localhost:21114/api/quality-inspection \
  -H 'X-Auth-Token: t' -H 'X-Role: INSPECTOR' -H 'Content-Type: application/json' \
  -d '{"batchNo":"BATCH-2026-001","inspectorId":"u1","inspectionType":"FIRST","standardVersion":"v1","items":[{"itemCode":"DIA","itemName":"直径","measuredValue":"9.98","limitMin":"9.9","limitMax":"10.1","itemStatus":"PASS"}]}'

# 质检员更新单个检验项（同一检验项重复提交只留最新一次，并重算旧结论）
curl -X POST 'http://localhost:21114/api/inspection-item-result?inspectionId=1' \
  -H 'X-Auth-Token: t' -H 'X-Role: INSPECTOR' -H 'Content-Type: application/json' \
  -d '{"itemCode":"DIA","itemName":"直径","measuredValue":"10.2","limitMin":"9.9","limitMax":"10.1","itemStatus":"FAIL"}'

# 质检员登记不良
curl -X POST http://localhost:21114/api/defect-record \
  -H 'X-Auth-Token: t' -H 'X-Role: INSPECTOR' -H 'Content-Type: application/json' \
  -d '{"batchNo":"BATCH-2026-001","defectType":"SCRATCH","defectQty":"2","severity":"MINOR","rootCause":"fixture"}'

# 质量经理关闭不良 / 让步放行（质检员调用会被 403 拒绝）
curl -X POST http://localhost:21114/api/defect-record/1/close      -H 'X-Auth-Token: t' -H 'X-Role: QUALITY_MANAGER'
curl -X POST http://localhost:21114/api/defect-record/1/concession -H 'X-Auth-Token: t' -H 'X-Role: QUALITY_MANAGER'

# 重算失败后按批次号重试（保留上一次结论）
curl -X POST http://localhost:21114/api/product-batch/BATCH-2026-001/conclusion/recompute \
  -H 'X-Auth-Token: t' -H 'X-Role: QUALITY_MANAGER'

# 全链路追溯：结论与明细同源
curl http://localhost:21114/api/trace/BATCH-2026-001 -H 'X-Auth-Token: t' -H 'X-Role: AUDITOR'
```

## 核心 API

| 方法 | 路径 | 说明 | 允许角色 |
|---|---|---|---|
| POST | `/api/product-batch` | 创建批次 | INSPECTOR / LINE_SUPERVISOR / QUALITY_MANAGER |
| GET | `/api/product-batch/{batchNo}` | 批次详情（含结论与版本） | 所有已认证角色 |
| POST | `/api/product-batch/{batchNo}/conclusion/recompute` | 按批次号重算/重试结论 | INSPECTOR / QUALITY_MANAGER |
| POST | `/api/quality-inspection` | 提交检验并逐项写入检验结果 | INSPECTOR / QUALITY_MANAGER |
| POST | `/api/inspection-item-result?inspectionId=` | 录入/更新单个检验项 | INSPECTOR / QUALITY_MANAGER |
| POST | `/api/defect-record` | 登记不良 | INSPECTOR / QUALITY_MANAGER |
| POST | `/api/defect-record/{id}/close` | 关闭不良 | QUALITY_MANAGER |
| POST | `/api/defect-record/{id}/concession` | 让步放行 | QUALITY_MANAGER |
| GET | `/api/trace/{batchNo}` | 批次全链路追溯树 | 所有已认证角色 |

越权提交一律返回 `403 {"code":"RBAC_DENIED"}`；未认证返回 `401 {"code":"AUTH_REQUIRED"}`。

## 批次结论重算规则

批次结论不再由多次写入拼接，而是每次由 `BatchConclusionService` 按已生效数据整体重算：

- **数据来源**：已生效的检验项结果（同一检验项重复提交只保留最新一次，按 `(inspectionId, itemCode)` 去重）+ 该批次全部不良记录。
- **触发时机**：提交检验、更新检验项、登记/关闭/让步放行不良，都会立即重算旧结论；结论版本号 `conclusionVersion` 递增。
- **判定优先级**：任一检验项 FAIL 或存在未关闭的 MAJOR/CRITICAL 不良 → `FAIL`；有 RECHECK 项或尚无检验数据 → `PENDING`；存在让步放行或未关闭的 MINOR 不良 → `CONDITIONAL_PASS`；否则 `PASS`。
- **并发控制**：同一批次的写入与重算按批次号加锁串行化，同时提交同一批次时只有一次重算生效，其余请求返回 `409 RECOMPUTE_IN_PROGRESS`，客户端可重试。
- **失败重试**：重算抛错时保留上一次结论（不覆盖），错误记入 `lastRecomputeError`，通过 `POST /api/product-batch/{batchNo}/conclusion/recompute` 按批次号重试。


## 本地开发方式


- 后端：进入 `backend` 后按技术栈运行开发命令，接口统一挂在 `/api`。


## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | - |
| 后端 | Spring Boot 3 + Java 17 + MyBatis-Plus |
| 数据库 | PostgreSQL 15 |
| 部署 | Docker Compose |

## 项目目录结构

```text

backend/src/routes, controllers, services, models, repositories, middlewares, constants, constructors, utils, types, config
```

## 环境变量说明

- `COMPOSE_PROJECT_NAME`: Compose 项目名，默认 `quality-trace`

- `BACKEND_PORT`: 后端端口，默认 `21114`
- `DB_PORT`: 数据库宿主机端口
- `DB_USER/DB_PASSWORD/DB_NAME`: 本地数据库凭据

## Docker 部署说明

- 根 Compose 文件不写 `version`，顶层 `name: quality-trace`。
- 容器名均使用 `${COMPOSE_PROJECT_NAME:-quality-trace}` 前缀。
- 数据库使用命名卷，避免绑定中文路径。
- 常见问题：端口占用时修改 `.env` 中端口后重启；需要重置数据时执行 `docker compose down -v`。

## 枚举/常量出现位置清单

- WorkOrderStatus: constants/WorkOrderStatus、types/WorkOrderStatus、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- InspectionResultStatus: constants/InspectionResultStatus、types/InspectionItemResultPayload、validators/PayloadValidator、services（检验状态推导与批次结论重算）、logTemplates、errorMessages 均有引用。
- DefectSeverity: constants/DefectSeverity、types/DefectRecordPayload、validators/PayloadValidator、services/BatchConclusionService（MAJOR/CRITICAL 判定）、logTemplates、errorMessages 均有引用。
- BatchConclusion: constants/BatchConclusion、services/BatchConclusionService、services/ProductBatchService、utils/Formatters（中文文案）、constructors/ProductBatchDtoFactory 均有引用。
- DispositionStatus: constants/DispositionStatus、services/DefectRecordService（OPEN/CLOSED/CONCESSION）、services/BatchConclusionService 均有引用。
- Role: constants/Role、middlewares/AuthMiddleware、middlewares/RbacMiddleware、controllers（各写接口角色声明）均有引用。

## 为什么会牵一发动全身

实体字段、枚举、日志模板、错误消息、构造器、筛选器和展示组件被刻意拆散到多个目录；修改一个状态值通常需要同步类型、构造器、服务、控制器、store、页面、README 与数据库种子。

## License

MIT
