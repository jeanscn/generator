package org.mybatis.generator.codegen.mybatis3;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;

import java.util.List;

/**
 * 校验旧链乐观锁配置及其可支持的表结构。
 *
 * @author vgosoft-agent
 */
public final class VersionCheckGenerationValidator {
    private static final String PROPERTY_NAME = "versionCheck";

    private VersionCheckGenerationValidator() {
    }

    /**
     * 解析表级版本检查开关，拒绝不明确的配置值。
     *
     * @param table 内省表
     * @return 显式配置为 true 时返回 true
     */
    public static boolean isEnabled(AbstractIntrospectedTable table) {
        String value = table.getTableConfigurationProperty(PROPERTY_NAME);
        if (value == null || "false".equals(value)) {
            return false;
        }
        if ("true".equals(value)) {
            return true;
        }
        throw failure(table, "只接受 true 或 false，实际值为：" + value);
    }

    /**
     * 在生成前检查版本列、更新语句模型及批量更新限制。
     *
     * @param table 内省表
     */
    public static void validate(AbstractIntrospectedTable table) {
        if (!isEnabled(table)) {
            return;
        }
        if (!(table instanceof IntrospectedTableMyBatis3Impl)
                || table.getContext().getJavaClientGeneratorConfiguration() == null
                || !"XMLMAPPER".equals(table.getContext().getJavaClientGeneratorConfiguration().getConfigurationType())) {
            throw failure(table, "仅支持 MyBatis3 XMLMAPPER");
        }
        List<IntrospectedColumn> versionColumns = table.getAllColumns().stream()
                .filter(ListUtilities::isVersionColumn)
                .toList();
        if (versionColumns.size() != 1) {
            throw failure(table, "必须且只能包含一个 version_ 列");
        }
        IntrospectedColumn version = versionColumns.get(0);
        boolean integer = "INTEGER".equals(version.getJdbcTypeName())
                && "java.lang.Integer".equals(version.getFullyQualifiedJavaType().getFullyQualifiedName());
        boolean longType = "BIGINT".equals(version.getJdbcTypeName())
                && "java.lang.Long".equals(version.getFullyQualifiedJavaType().getFullyQualifiedName());
        if ((!integer && !longType) || version.isNullable() || version.isGeneratedAlways()
                || version.isGeneratedColumn() || version.isIdentity() || version.isAutoIncrement()
                || table.getPrimaryKeyColumns().contains(version)) {
            throw failure(table, "version_ 必须为非空、非主键、可写 Integer/INTEGER 或 Long/BIGINT");
        }
        if (table.getPrimaryKeyColumns().isEmpty() || !table.getRules().generateUpdateByPrimaryKeySelective()) {
            throw failure(table, "必须生成带主键的 updateByPrimaryKeySelective");
        }
        if (table.getRules().generateUpdateBatch()
                && (table.getPrimaryKeyColumns().size() != 1
                || !"id_".equalsIgnoreCase(table.getPrimaryKeyColumns().get(0).getActualColumnName()))) {
            throw failure(table, "启用批量版本检查时仅支持单一 id_ 主键");
        }
    }

    private static IllegalArgumentException failure(AbstractIntrospectedTable table, String reason) {
        return new IllegalArgumentException("表 " + table.getTableConfiguration().getTableName()
                + " versionCheck：" + reason);
    }
}
