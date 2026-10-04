-- quality-trace 批次质量追溯数据库脚本
-- 批次结论由已生效的检验项和不良记录重算，不接受逐次拼接。

CREATE TABLE IF NOT EXISTS sys_user (
  id SERIAL PRIMARY KEY,
  username TEXT UNIQUE NOT NULL,
  password TEXT NOT NULL,
  role TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS work_order (
  id SERIAL PRIMARY KEY,
  order_no TEXT UNIQUE,
  product_code TEXT,
  product_name TEXT,
  planned_qty INTEGER,
  line_code TEXT,
  start_at TEXT,
  status TEXT
);

CREATE TABLE IF NOT EXISTS product_batch (
  id SERIAL PRIMARY KEY,
  batch_no TEXT UNIQUE,
  work_order_id INTEGER REFERENCES work_order(id),
  quantity INTEGER,
  material_lot_no TEXT,
  produced_at TEXT,
  batch_status TEXT,
  -- 批次结论：由已生效检验项 + 不良记录重算得出，PASS / FAIL / CONDITIONAL_PASS
  conclusion_status TEXT,
  conclusion_updated_at TEXT,
  -- 质量经理让步放行标记
  concession_granted BOOLEAN DEFAULT false,
  version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS quality_inspection (
  id SERIAL PRIMARY KEY,
  batch_id INTEGER REFERENCES product_batch(id),
  inspector_id INTEGER REFERENCES sys_user(id),
  inspection_type TEXT,
  standard_version TEXT,
  result_status TEXT,
  inspected_at TEXT
);

CREATE TABLE IF NOT EXISTS inspection_item_result (
  id SERIAL PRIMARY KEY,
  inspection_id INTEGER REFERENCES quality_inspection(id),
  batch_id INTEGER REFERENCES product_batch(id),
  item_code TEXT,
  item_name TEXT,
  measured_value TEXT,
  limit_min TEXT,
  limit_max TEXT,
  item_status TEXT,
  -- 同一检验项重复提交只留最新一次：旧记录 effective=false，最新一条 effective=true
  version INTEGER DEFAULT 1,
  effective BOOLEAN DEFAULT true,
  submitted_at TEXT
);

-- 数据库层面保证同一 (batch_id, item_code) 至多一条已生效记录
CREATE UNIQUE INDEX IF NOT EXISTS uq_effective_item
  ON inspection_item_result (batch_id, item_code)
  WHERE effective;

CREATE TABLE IF NOT EXISTS defect_record (
  id SERIAL PRIMARY KEY,
  batch_id INTEGER REFERENCES product_batch(id),
  defect_type TEXT,
  defect_qty INTEGER,
  severity TEXT,
  root_cause TEXT,
  -- OPEN / CLOSED，关闭不良归质量经理
  disposition_status TEXT DEFAULT 'OPEN',
  recorded_at TEXT,
  closed_at TEXT,
  closed_by INTEGER REFERENCES sys_user(id)
);

CREATE TABLE IF NOT EXISTS audit_log (
  id SERIAL PRIMARY KEY,
  actor TEXT,
  action TEXT,
  target_type TEXT,
  target_id TEXT,
  detail TEXT,
  created_at TEXT
);

-- 种子用户（明文密码仅用于本地演示）
INSERT INTO sys_user (username, password, role) VALUES
  ('inspector',  'inspector123',  'INSPECTOR'),
  ('manager',    'manager123',    'MANAGER'),
  ('supervisor', 'supervisor123', 'SUPERVISOR'),
  ('auditor',    'auditor123',    'AUDITOR')
ON CONFLICT (username) DO NOTHING;

-- 种子工单
INSERT INTO work_order (order_no, product_code, product_name, planned_qty, line_code, start_at, status)
VALUES ('WO-2026-0001', 'P-1001', '产品A', 1000, 'L1', '2026-10-01T08:00:00', 'RUNNING')
ON CONFLICT (order_no) DO NOTHING;

-- 种子批次（结论初始 PASS，后续由重算维护）
INSERT INTO product_batch (batch_no, work_order_id, quantity, material_lot_no, produced_at, batch_status, conclusion_status, concession_granted)
SELECT 'B-2026-0001', w.id, 500, 'LOT-001', '2026-10-01T10:00:00', 'IN_PROGRESS', 'PASS', false
FROM work_order w WHERE w.order_no = 'WO-2026-0001'
ON CONFLICT (batch_no) DO NOTHING;
