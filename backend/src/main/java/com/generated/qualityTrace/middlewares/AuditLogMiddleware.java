package com.generated.qualityTrace.middlewares;

import org.springframework.stereotype.Component;
import com.generated.qualityTrace.models.AuditLog;
import com.generated.qualityTrace.repositories.AuditLogRepository;
import com.generated.qualityTrace.utils.UserContext;

/**
 * 操作日志中间件：所有写操作调用 log(...) 写入 audit_log。
 * 字段变更时同步改日志模板和调用处。
 */
@Component
public class AuditLogMiddleware {

  private final AuditLogRepository repo;

  public AuditLogMiddleware(AuditLogRepository repo) {
    this.repo = repo;
  }

  public void log(String action, String targetType, String targetId, String detail) {
    AuditLog e = new AuditLog();
    e.setActor(UserContext.getUsername() == null ? "anonymous" : UserContext.getUsername());
    e.setAction(action);
    e.setTargetType(targetType);
    e.setTargetId(targetId);
    e.setDetail(detail);
    e.setCreatedAt(java.time.Instant.now().toString());
    repo.insert(e);
  }
}
