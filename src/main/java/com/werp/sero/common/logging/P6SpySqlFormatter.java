package com.werp.sero.common.logging;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.util.StringUtils;

public class P6SpySqlFormatter implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        // statement 카테고리만 쿼리 카운트에 포함
        if ("statement".equals(category) && StringUtils.hasText(sql)) {
            QueryCounter.increment();
            QueryCounter.addTime(elapsed);
        }

        sql = formatSql(category, sql);
        return String.format("[%s] | %d ms | %s", category, elapsed, sql);
    }

    private String formatSql(String category, String sql) {
        if (StringUtils.hasText(sql) && "statement".equals(category)) {
            String trimmedSql = sql.trim().toLowerCase();
            if (trimmedSql.startsWith("create") || trimmedSql.startsWith("alter") || trimmedSql.startsWith("comment")) {
                return FormatStyle.DDL.getFormatter().format(sql);
            }
            return FormatStyle.BASIC.getFormatter().format(sql);
        }
        return sql;
    }
}
