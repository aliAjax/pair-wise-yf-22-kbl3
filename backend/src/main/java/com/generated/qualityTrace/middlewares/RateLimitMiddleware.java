package com.generated.qualityTrace.middlewares;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.generated.qualityTrace.constants.ErrorCodes;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 限流中间件：按 IP 做简单滑动窗口计数，超限返回 429。
 */
@Component
@Order(0)
public class RateLimitMiddleware extends OncePerRequestFilter {

  /** 每分钟每 IP 最大请求数 */
  private static final int MAX_PER_MINUTE = 120;
  private static final long WINDOW_MS = 60_000L;

  private final Map<String, Window> windows = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String ip = request.getRemoteAddr();
    long now = System.currentTimeMillis();
    Window w = windows.compute(ip, (k, existing) -> {
      if (existing == null || now - existing.start >= WINDOW_MS) {
        return new Window(now, new AtomicInteger(1));
      }
      existing.count.incrementAndGet();
      return existing;
    });
    if (w.count.get() > MAX_PER_MINUTE) {
      response.setStatus(429);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"code\":\"" + ErrorCodes.RATE_LIMITED + "\",\"message\":\"rate limit exceeded\"}");
      return;
    }
    chain.doFilter(request, response);
  }

  private static final class Window {
    final long start;
    final AtomicInteger count;
    Window(long start, AtomicInteger count) { this.start = start; this.count = count; }
  }
}
