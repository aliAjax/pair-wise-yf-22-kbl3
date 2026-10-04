package com.generated.qualityTrace.middlewares;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.types.ApiException;

/** Simple fixed-window rate limit per client IP. */
@Component
public class RateLimitMiddleware implements HandlerInterceptor {
  private static final Logger log = LoggerFactory.getLogger(RateLimitMiddleware.class);
  private static final long WINDOW_MS = 60_000L;
  private static final int MAX_REQUESTS = 120;

  private final Map<String, long[]> windows = new ConcurrentHashMap<>();

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String ip = request.getRemoteAddr();
    long now = System.currentTimeMillis();
    long[] slot = windows.computeIfAbsent(ip, k -> new long[]{now, 0L});
    synchronized (slot) {
      if (now - slot[0] > WINDOW_MS) {
        slot[0] = now;
        slot[1] = 0L;
      }
      slot[1]++;
      if (slot[1] > MAX_REQUESTS) {
        log.warn(LogTemplates.RATE_LIMIT, ip);
        throw new ApiException(ErrorCodes.RATE_LIMITED, ErrorMessages.RATE_LIMITED, 429);
      }
    }
    return true;
  }
}
