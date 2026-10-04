package com.generated.qualityTrace.controllers;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.services.AuthService;
import com.generated.qualityTrace.types.LoginPayload;

/**
 * 认证控制器：登录签发 JWT。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public Map<String, Object> login(@RequestBody LoginPayload payload) {
    String token = authService.login(payload);
    return Map.of("token", token, "tokenType", "Bearer");
  }
}
