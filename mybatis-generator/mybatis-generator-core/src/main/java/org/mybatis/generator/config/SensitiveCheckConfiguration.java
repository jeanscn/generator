package org.mybatis.generator.config;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 描述表内物理列的敏感检测元数据，不包含检测运行时逻辑。
 *
 * @author vgosoft
 */
public final class SensitiveCheckConfiguration {
    private static final Set<String> STRATEGIES = Set.of("INHERIT", "BLOCK", "CONFIRM", "REPLACE", "WARN");
    private static final Set<String> CONTENT_TYPES = Set.of("PLAIN_TEXT", "RICH_TEXT");
    private final List<String> columns;
    private final boolean enable;
    private final String strategy;
    private final String contentType;

    public SensitiveCheckConfiguration(String columns, String enable, String strategy, String contentType) {
        if (columns == null || columns.isBlank()) {
            throw new IllegalArgumentException("sensitiveCheck.columns 必须指定物理列名");
        }
        this.columns = Arrays.stream(columns.split(",", -1)).map(String::trim).toList();
        if (this.columns.stream().anyMatch(String::isEmpty)
                || this.columns.stream().distinct().count() != this.columns.size()) {
            throw new IllegalArgumentException("sensitiveCheck.columns 存在空列名或重复列名：" + columns);
        }
        if (enable != null && !"true".equals(enable) && !"false".equals(enable)) {
            throw new IllegalArgumentException("sensitiveCheck.enable 只能为 true 或 false");
        }
        this.enable = enable == null || "true".equals(enable);
        this.strategy = strategy == null ? "INHERIT" : strategy;
        this.contentType = contentType == null ? "PLAIN_TEXT" : contentType;
        if (!STRATEGIES.contains(this.strategy)) {
            throw new IllegalArgumentException("sensitiveCheck.strategy 不支持：" + this.strategy);
        }
        if (!CONTENT_TYPES.contains(this.contentType)) {
            throw new IllegalArgumentException("sensitiveCheck.contentType 不支持：" + this.contentType);
        }
    }

    public List<String> getColumns() {
        return columns;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getStrategy() {
        return strategy;
    }

    public String getContentType() {
        return contentType;
    }
}
