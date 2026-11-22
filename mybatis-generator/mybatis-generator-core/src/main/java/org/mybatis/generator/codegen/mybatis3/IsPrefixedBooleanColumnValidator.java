package org.mybatis.generator.codegen.mybatis3;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.ArrayList;
import java.util.List;

/**
 * 校验内省列派生的 Java 布尔属性名，禁止 is 前缀命名。
 * primitive boolean 的 isXxx 字段经 Lombok 生成 setXxx（去 is 前缀），Jackson 按 setter 名绑定 JSON 属性 xxx，
 * 字段名、访问器名与 JSON 属性名不一致会导致请求字段静默失效。
 *
 * @author vgosoft-agent
 */
public final class IsPrefixedBooleanColumnValidator {
    private IsPrefixedBooleanColumnValidator() { }

    /**
     * 在表内省初始化后校验全部列：布尔属性名以 is+大写字母开头时拒绝生成。
     *
     * @param table 内省表
     */
    public static void validate(AbstractIntrospectedTable table) {
        if (table.getTableConfiguration().isIgnore()) {
            return;
        }
        List<String> offending = new ArrayList<>();
        for (IntrospectedColumn column : table.getAllColumns()) {
            if (isPrefixedBooleanProperty(column)) {
                offending.add(column.getActualColumnName() + " -> " + column.getJavaProperty());
            }
        }
        if (!offending.isEmpty()) {
            throw failure(table, offending);
        }
    }

    private static boolean isPrefixedBooleanProperty(IntrospectedColumn column) {
        String javaProperty = column.getJavaProperty();
        FullyQualifiedJavaType javaType = column.getFullyQualifiedJavaType();
        if (javaProperty == null || javaType == null || javaProperty.length() <= 2
                || !javaProperty.startsWith("is") || !Character.isUpperCase(javaProperty.charAt(2))) {
            return false;
        }
        String typeName = javaType.getFullyQualifiedName();
        return "boolean".equals(typeName) || "java.lang.Boolean".equals(typeName);
    }

    private static IllegalArgumentException failure(AbstractIntrospectedTable table, List<String> offending) {
        return new IllegalArgumentException("表 " + table.getTableConfiguration().getTableName()
                + " 存在 is 前缀布尔属性 " + offending
                + "：boolean isXxx 经 Lombok 生成 setXxx（去 is 前缀），Jackson 按 setter 名绑定 JSON 属性 xxx，"
                + "字段名/访问器名/JSON 属性名不一致会导致请求字段静默失效。"
                + "请将列或 Java 属性名改为不带 is 前缀的语义名（如 is_deleted 改为 deleted），禁止生成。");
    }
}
