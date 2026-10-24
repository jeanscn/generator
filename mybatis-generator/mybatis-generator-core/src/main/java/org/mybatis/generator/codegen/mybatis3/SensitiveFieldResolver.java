package org.mybatis.generator.codegen.mybatis3;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.SensitiveCheckConfiguration;

import javax.lang.model.SourceVersion;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析启用的敏感配置与最终内省字段，供预检、模型及前端生成共享。
 *
 * @author vgosoft
 */
public final class SensitiveFieldResolver {
    private SensitiveFieldResolver() {
    }

    private static List<SensitiveCheckConfiguration> enabledConfigurations(AbstractIntrospectedTable table) {
        return table.getTableConfiguration().getSensitiveCheckConfigurations().stream()
                .filter(SensitiveCheckConfiguration::isEnable)
                .toList();
    }

    public static boolean isEnabled(AbstractIntrospectedTable table) {
        return !enabledConfigurations(table).isEmpty();
    }

    public static Map<IntrospectedColumn, SensitiveCheckConfiguration> resolve(AbstractIntrospectedTable table) {
        Map<IntrospectedColumn, SensitiveCheckConfiguration> result = new LinkedHashMap<>();
        for (SensitiveCheckConfiguration configuration : enabledConfigurations(table)) {
            for (String name : configuration.getColumns()) {
                List<IntrospectedColumn> matches = table.getAllColumns().stream()
                        .filter(column -> name.equals(column.getActualColumnName())).toList();
                if (matches.size() != 1) {
                    throw failure(table, name, "物理列必须唯一存在且不能被忽略");
                }
                IntrospectedColumn column = matches.get(0);
                if (result.putIfAbsent(column, configuration) != null) {
                    throw failure(table, name, "敏感配置重复或冲突");
                }
                if (!FullyQualifiedJavaType.getStringInstance().equals(column.getFullyQualifiedJavaType())
                        || column.isGeneratedAlways()) {
                    throw failure(table, name, "仅支持最终 Java 类型为 String 的可写列");
                }
                ColumnOverride override = table.getTableConfiguration().getColumnOverride(name);
                if (override != null && override.getProperty("logicalType") != null
                        && !override.getProperty("logicalType").isBlank()) {
                    throw failure(table, name, "逻辑类型内容尚无纯文本适配器，不能配置敏感检测");
                }
                if (table.getTableConfiguration().getOverridePropertyConfigurations().stream()
                        .anyMatch(configurationItem -> java.util.Arrays.stream(configurationItem.getSourceColumnName().split(","))
                                .map(String::trim).anyMatch(name::equals))) {
                    throw failure(table, name, "字段存在派生或双表示映射，须先验证持久化值与检测文本一致");
                }
                if (column.getJavaProperty() == null || !SourceVersion.isIdentifier(column.getJavaProperty())
                        || SourceVersion.isKeyword(column.getJavaProperty())
                        || table.getAllColumns().stream()
                        .filter(other -> column.getJavaProperty().equals(other.getJavaProperty())).count() != 1) {
                    throw failure(table, name, "最终 Java 属性缺失、非法或重名");
                }
            }
        }
        return result;
    }

    private static IllegalArgumentException failure(AbstractIntrospectedTable table, String column, String message) {
        return new IllegalArgumentException("表 " + table.getTableConfiguration().getTableName()
                + "，列/方法 " + column + "：" + message);
    }
}
