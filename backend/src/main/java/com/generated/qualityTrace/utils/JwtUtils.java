package com.generated.qualityTrace.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.generated.qualityTrace.types.LoginUser;

/**
 * 轻量 JWT 工具（HMAC-SHA256），不引入额外依赖。
 * 结构：header.payload.signature，payload 含 uid / username / role / exp。
 */
@Component
public class JwtUtils {

  private static final String ALG = "HmacSHA256";
  private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder B64_DEC = Base64.getUrlDecoder();

  @Value("${jwt.secret:local-dev-secret}")
  private String secret;

  @Value("${jwt.ttl-ms:86400000}")
  private long ttlMs;

  public String issue(Long userId, String username, String role) {
    long exp = System.currentTimeMillis() + ttlMs;
    String header = B64.encodeToString(("{\"alg\":\"HS256\",\"typ\":\"JWT\"}").getBytes(StandardCharsets.UTF_8));
    String payload = B64.encodeToString(
        ("{\"uid\":" + userId + ",\"username\":\"" + username + "\",\"role\":\"" + role + "\",\"exp\":" + exp + "}")
            .getBytes(StandardCharsets.UTF_8));
    String sig = sign(header + "." + payload);
    return header + "." + payload + "." + sig;
  }

  /** 解析并校验令牌，失败抛出 IllegalArgumentException */
  public LoginUser parse(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        throw new IllegalArgumentException("malformed token");
      }
      String expected = sign(parts[0] + "." + parts[1]);
      if (!expected.equals(parts[2])) {
        throw new IllegalArgumentException("bad signature");
      }
      String payload = new String(B64_DEC.decode(parts[1]), StandardCharsets.UTF_8);
      long exp = Long.parseLong(extract(payload, "exp"));
      if (System.currentTimeMillis() > exp) {
        throw new IllegalArgumentException("token expired");
      }
      Long uid = Long.parseLong(extract(payload, "uid"));
      String username = extract(payload, "username");
      String role = extract(payload, "role");
      return new LoginUser(uid, username, role);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("invalid token", e);
    }
  }

  private String sign(String content) {
    try {
      Mac mac = Mac.getInstance(ALG);
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALG));
      return B64.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("jwt sign failed", e);
    }
  }

  /** 极简 JSON 字段提取（仅用于受控的内部 payload） */
  private static String extract(String json, String key) {
    String needle = "\"" + key + "\":";
    int i = json.indexOf(needle);
    if (i < 0) {
      throw new IllegalArgumentException("missing field " + key);
    }
    int start = i + needle.length();
    char c = json.charAt(start);
    if (c == '"') {
      int end = json.indexOf('"', start + 1);
      return json.substring(start + 1, end);
    }
    int end = start;
    while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
      end++;
    }
    return json.substring(start, end).trim();
  }
}
