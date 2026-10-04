# 制造业质量追溯 API 服务（quality-trace）

面向小型制造工厂的批次质量追溯后端服务，覆盖工单、批次、检验项、不良记录和追溯查询。

**批次结论按已生效的检验项和不良记录整体重算**：同一检验项重复提交只留最新一次，检验项一更新旧结论即重算；同一批次并发提交通过行锁串行化，只让一次重算生效；重算失败按批次号重试并保留上一次结论。质检员录检验和不良，关闭不良与让步放行归质量经理，越权提交拒绝。

## 快速启动

```bash
cp .env.example .env && docker compose up -d
```

## 访问地址或 CLI 示例

- 后端健康检查：<http://localhost:21114/health>
- 登录获取令牌：

```bash
curl -s -X POST http://localhost:21114/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"inspector","password":"inspector123"}'
```

- 提交检验（含检验项，触发批次结论重算）：

```bash
curl -s -X POST http://localhost:21114/api/inspections \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
    "batchNo":"B-2026-0001",
    "inspectionType":"FIRST",
    "standardVersion":"v1",
    "items":[
      {"itemCode":"DIM-001","itemName":"尺寸","measuredValue":"10.02","limitMin":"9.95","limitMax":"10.05","itemStatus":"PASS"},
      {"itemCode":"DIM-002","itemName":"外观","measuredValue":"ok","itemStatus":"PASS"}
    ]
  }'
```

- 登记不良：

```bash
curl -s -X POST http://localhost:21114/api/defects \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{"batchNo":"B-2026-0001","defectType":"SCRATCH","defectQty":3,"severity":"MINOR","rootCause":"transport"}'
```

- 追溯查询（结论与明细一致）：

```bash
curl -s http://localhost:21114/api/trace/B-2026-0001 -H 'Authorization: Bearer <token>'
```

## 角色与权限（RBAC）

| 角色 | 权限 |
|---|---|
| INSPECTOR（质检员） | 提交检验、登记不良、查询 |
| MANAGER（质量经理） | 关闭不良、让步放行、提交检验、登记不良 |
| SUPERVISOR（产线主管） | 创建工单、创建批次、查询 |
| AUDITOR（审计员） | 查询 |

越权提交（如质检员关闭不良或让步放行）返回 `403 RBAC_DENIED`。

## 核心 API

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| POST | `/api/auth/login` | 公开 | 登录签发 JWT |
| POST | `/api/work-orders` | SUPERVISOR, MANAGER | 创建工单 |
| GET | `/api/work-orders` | 全部登录角色 | 工单列表 |
| POST | `/api/batches` | SUPERVISOR, MANAGER | 创建批次 |
| GET | `/api/batches` | 全部登录角色 | 批次列表 |
| POST | `/api/inspections` | INSPECTOR, MANAGER | 提交检验（含检验项），触发重算 |
| GET | `/api/inspections` | 全部登录角色 | 检验列表 |
| POST | `/api/defects` | INSPECTOR, MANAGER | 登记不良，触发重算 |
| POST | `/api/defects/{id}/close` | MANAGER | 关闭不良，触发重算 |
| POST | `/api/batches/{batchNo}/concession` | MANAGER | 让步放行，触发重算 |
| GET | `/api/trace/{batchNo}` | 全部登录角色 | 追溯查询 |
| GET | `/api/inspection-items` | 全部登录角色 | 检验项查询（可按 batchNo 过滤已生效项） |

## 批次结论重算规则

- **整体重算**：结论由已生效检验项（`effective=true`）与全部不良记录重算得出，不接受逐次拼接。
- **只留最新一次**：同一 `(batch_id, item_code)` 重复提交，旧记录置 `effective=false`，新版本号递增；数据库部分唯一索引保证至多一条已生效。
- **并发串行**：提交时 `SELECT ... FOR UPDATE` 锁定批次行，同一批次的并发提交在数据库层面串行化，只让一次重算写结论。
- **失败重试**：重算失败按批次号重试（`recalc.max-attempts`，默认 3 次，退避 `recalc.backoff-ms`），每次重试独立事务；重试耗尽保留上一次结论（不覆盖）。

## 结论判定

- 任一已生效检验项 `FAIL`，或 存在未关闭的 `CRITICAL` 不良且未让步放行 → `FAIL`
- 否则存在条件放行/复检项，或 存在未关闭不良，或 已让步放行且有不良 → `CONDITIONAL_PASS`
- 其余 → `PASS`

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.3 + Java 17 + MyBatis-Plus 3.5.7 |
| 数据库 | PostgreSQL 15 |
| 认证 | JWT（HMAC-SHA256）+ RBAC |
| 部署 | Docker Compose |

## 项目目录结构

```text
backend/src/main/java/com/generated/qualityTrace/
├── routes/               # 按实体分文件的路由常量
├── controllers/          # 按实体分文件
├── services/             # 按实体分文件（含 BatchConclusionService 重算核心）
├── models/               # 按实体分文件（MyBatis-Plus 实体）
├── repositories/         # 数据访问层（MyBatis-Plus Mapper）
├── middlewares/          # 认证、RBAC、审计、异常、限流
├── constants/            # 枚举、错误码、日志模板
├── constructors/         # 响应 DTO 构造器
├── validators/           # 入参校验
├── utils/                # JWT、格式化、用户上下文
├── types/                # 请求/响应 DTO
├── exceptions/           # 业务异常
└── config/               # 应用配置
```

## 环境变量说明

- `COMPOSE_PROJECT_NAME`: Compose 项目名，默认 `quality-trace`
- `BACKEND_PORT`: 后端端口，默认 `21114`
- `DB_PORT`: 数据库宿主机端口
- `DB_USER/DB_PASSWORD/DB_NAME`: 本地数据库凭据
- `JWT_SECRET`: JWT 签名密钥
- `RECALC_MAX_ATTEMPTS` / `RECALC_BACKOFF_MS`: 重算重试次数与退避

## Docker 部署说明

- 根 Compose 文件不写 `version`，顶层 `name: quality-trace`。
- 容器名均使用 `${COMPOSE_PROJECT_NAME:-quality-trace}` 前缀。
- 数据库使用命名卷 `db_data`，避免绑定中文路径。
- 数据库服务配置 healthcheck，后端通过 `depends_on: condition: service_healthy` 等待数据库。
- 后端提供 `/health` 健康检查。
- 常见问题：端口占用时修改 `.env` 中端口后重启；需要重置数据时执行 `docker compose down -v`。

## 枚举/常量出现位置清单

- WorkOrderStatus: constants/WorkOrderStatus、utils/Formatters、services/WorkOrderService、数据库种子。
- InspectionResultStatus: constants/InspectionResultStatus、utils/Formatters、services/BatchConclusionService、services/QualityInspectionService、数据库种子。
- DefectSeverity: constants/DefectSeverity、utils/Formatters、services/BatchConclusionService、services/DefectRecordService。
- ErrorCodes: constants/ErrorCodes、middlewares/ErrorHandlerMiddleware、middlewares/AuthMiddleware、middlewares/RateLimitMiddleware。
- ErrorMessages: constants/ErrorMessages、middlewares/RbacMiddleware、exceptions 各异常类。
- LogTemplates: constants/LogTemplates、services 各写操作、middlewares/AuditLogMiddleware。

## 为什么会牵一发动全身

实体字段、枚举、日志模板、错误消息、构造函数、校验器被拆散到多个目录；修改一个状态值通常需要同步类型、构造器、服务、控制器、日志模板与数据库种子。批次结论重算横跨检验项与不良记录两个数据源，任一变更都需保证结论与明细一致。

## License

MIT
