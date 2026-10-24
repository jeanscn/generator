package org.mybatis.generator.codegen.mybatis3.model;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.SensitiveGenerationValidator;
import org.mybatis.generator.custom.annotations.SensitiveCheckDesc;
import org.mybatis.generator.config.SensitiveCheckConfiguration;

import java.util.List;
import java.util.Map;

import static org.mybatis.generator.codegen.mybatis3.SensitiveFieldResolver.isEnabled;
import static org.mybatis.generator.codegen.mybatis3.SensitiveFieldResolver.resolve;

/**
 * 在模型插件执行后验证实际实体字段并装配敏感注解。
 *
 * @author vgosoft
 */
public final class SensitiveModelAnnotations {
    private SensitiveModelAnnotations() {
    }

    public static void addModelAnnotations(TopLevelClass model, AbstractIntrospectedTable table) {
        if (!isEnabled(table)) {
            return;
        }
        SensitiveGenerationValidator.validate(table);
        for (Map.Entry<IntrospectedColumn, SensitiveCheckConfiguration> entry : resolve(table).entrySet()) {
            IntrospectedColumn column = entry.getKey();
            SensitiveCheckConfiguration configuration = entry.getValue();
            List<Field> matches = model.getFields().stream().filter(item -> column.getJavaProperty().equals(item.getName())).toList();
            if (matches.size() != 1) {
                throw failure(table, column.getActualColumnName(), "实体字段未实际生成或字段重名");
            }
            Field field = matches.get(0);
            if (!FullyQualifiedJavaType.getStringInstance().equals(field.getType()) || field.isStatic() || field.isFinal()) {
                throw failure(table, column.getActualColumnName(), "实体字段类型或可写属性被插件改变");
            }
            SensitiveCheckDesc description = new SensitiveCheckDesc(configuration);
            model.addImportedTypes(description.getImportedTypes());
            String annotation = description.toAnnotation();
            if (!field.getAnnotations().contains(annotation)) {
                field.addAnnotation(annotation);
            }
        }
    }

    public static void requireModelGenerated(AbstractIntrospectedTable table) {
        if (isEnabled(table)) {
            throw failure(table, "*", "插件禁止生成实体，无法落地敏感字段元数据");
        }
    }

    private static IllegalArgumentException failure(AbstractIntrospectedTable table, String column, String message) {
        return new IllegalArgumentException("表 " + table.getTableConfiguration().getTableName()
                + "，列/方法 " + column + "：" + message);
    }
}
