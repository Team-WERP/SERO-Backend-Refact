package com.werp.sero.common.logging;

/**
 * API 요청별 쿼리 실행 횟수 및 총 소요 시간을 추적하는 카운터
 * ThreadLocal을 사용하여 각 요청(스레드)별로 독립적으로 카운트
 */
public class QueryCounter {

    private static final ThreadLocal<Integer> queryCount = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Long> totalTimeMs = ThreadLocal.withInitial(() -> 0L);
    private static final ThreadLocal<Long> startTimeNano = ThreadLocal.withInitial(() -> 0L);

    public static void start() {
        startTimeNano.set(System.nanoTime());
    }

    public static void increment() {
        queryCount.set(queryCount.get() + 1);
    }

    public static void addTime(long elapsedMs) {
        totalTimeMs.set(totalTimeMs.get() + elapsedMs);
    }

    public static int getCount() {
        return queryCount.get();
    }

    public static long getTotalTimeMs() {
        return totalTimeMs.get();
    }

    /**
     * API 전체 소요시간 (나노초 → 밀리초, 소수점 3자리)
     */
    public static double getApiElapsedMs() {
        long elapsedNano = System.nanoTime() - startTimeNano.get();
        return elapsedNano / 1_000_000.0;
    }

    public static void clear() {
        queryCount.remove();
        totalTimeMs.remove();
        startTimeNano.remove();
    }
}
