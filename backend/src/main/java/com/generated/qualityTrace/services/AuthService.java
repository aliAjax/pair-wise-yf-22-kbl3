package com.generated.qualityTrace.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.exceptions.UnauthorizedException;
import com.generated.qualityTrace.middlewares.AuditLogMiddleware;
import com.generated.qualityTrace.models.SysUser;
import com.generated.qualityTrace.repositories.SysUserRepository;
import com.generated.qualityTrace.types.LoginPayload;
import com.generated.qualityTrace.types.LoginUser;
import com.generated.qualityTrace.utils.JwtUtils;

/**
 * 认证服务：校验用户名密码，签发 JWT。
 */
@Service
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final SysUserRepository userRepo;
  private final JwtUtils jwtUtils;
  private final AuditLogMiddleware auditLog;

  public AuthService(SysUserRepository userRepo, JwtUtils jwtUtils, AuditLogMiddleware auditLog) {
    this.userRepo = userRepo;
    this.jwtUtils = jwtUtils;
    this.auditLog = auditLog;
  }

  public String login(LoginPayload payload) {
    if (payload == null || payload.username() == null || payload.password() == null) {
      throw new UnauthorizedException("username and password required");
    }
    SysUser user = userRepo.selectByUsername(payload.username());
    if (user == null || !user.getPassword().equals(payload.password())) {
      throw new UnauthorizedException("invalid credentials");
    }
    LoginUser principal = new LoginUser(user.getId(), user.getUsername(), user.getRole());
    String token = jwtUtils.issue(principal.userId(), principal.username(), principal.role());

    auditLog.log("AUTH_LOGIN", "SYS_USER", user.getUsername(), "role=" + user.getRole());
    log.info(LogTemplates.AUTH_LOGIN, user.getUsername(), user.getRole());
    return token;
  }
}
