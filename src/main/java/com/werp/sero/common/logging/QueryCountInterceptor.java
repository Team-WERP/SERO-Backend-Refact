package com.werp.sero.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * API 요청 완료 시 총 쿼리 수와 DB 소요 시간을 로깅하는 인터셉터
 */
@Component
public class QueryCountInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(QueryCountInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        QueryCounter.clear();
        QueryCounter.start();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        int count = QueryCounter.getCount();
        long dbTime = QueryCounter.getTotalTimeMs();
        double apiTime = QueryCounter.getApiElapsedMs();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.info("========== [쿼리 통계] {} {} | 총 쿼리: {}개 | DB 소요시간: {}ms | API 총 소요시간: {}ms ==========",
                method, uri, count, dbTime, String.format("%.3f", apiTime));

        QueryCounter.clear();
    }
}
