package org.mybatis.generator.codegen.mybatis3;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.config.SelectByColumnGeneratorConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 核验按列逻辑删除的列、版本条件与受管模式边界，不承载业务删除规则。
 *
 * @author vgosoft-agent
 */
public final class LogicalDeleteGenerationValidator {
    private LogicalDeleteGenerationValidator() { }

    /** 返回当前表显式启用的逻辑删除配置。 */
    public static List<SelectByColumnGeneratorConfiguration> configurations(AbstractIntrospectedTable table) {
        return table.getTableConfiguration().getSelectByColumnGeneratorConfigurations().stream()
                .filter(SelectByColumnGeneratorConfiguration::isLogicalDelete).toList();
    }

    /** 返回按默认字段规范识别的逻辑删除列。 */
    public static IntrospectedColumn flag(AbstractIntrospectedTable table) {
        return table.getColumn(DefaultColumnNameEnum.DELETE_FLAG.columnName()).orElseThrow(
                () -> failure(table, "缺少 delete_flag"));
    }

    /** 在内省和插件初始化后验证实际列及支持范围。 */
    public static void validate(AbstractIntrospectedTable table) {
        for (var config : configurations(table)) {
            if (!config.isEnableDelete()) { throw failure(table, "要求 enableDelete=true"); }
            if (table.getClass() != IntrospectedTableMyBatis3Impl.class) {
                throw failure(table, "仅支持 MyBatis3 XMLMAPPER");
            }
            var client = table.getContext().getJavaClientGeneratorConfiguration();
            if (client == null || !"XMLMAPPER".equals(client.getConfigurationType())) {
                throw failure(table, "仅支持 XMLMAPPER");
            }
            if (config.getColumns().isEmpty() || config.getColumnNames().size() != config.getColumns().size()
                    || !new HashSet<>(table.getAllColumns()).containsAll(config.getColumns())
                    || new HashSet<>(config.getColumnNames()).size() != config.getColumnNames().size()) {
                throw failure(table, "条件列为空、重复或不存在：" + config.getColumnNames());
            }
            var flag = flag(table);
            if (!"INTEGER".equals(flag.getJdbcTypeName())
                    || !"java.lang.Integer".equals(flag.getFullyQualifiedJavaType().getFullyQualifiedName())
                    || flag.isNullable() || !writable(flag) || config.getColumns().contains(flag)) {
                throw failure(table, "delete_flag 必须为非空可写 Integer/INTEGER，且不能作为删除条件列");
            }
            var version = table.getColumn(DefaultColumnNameEnum.VERSION.columnName());
            if (version.isPresent()) {
                var column = version.orElseThrow();
                boolean integer = "INTEGER".equals(column.getJdbcTypeName())
                        && "java.lang.Integer".equals(column.getFullyQualifiedJavaType().getFullyQualifiedName());
                boolean longType = "BIGINT".equals(column.getJdbcTypeName())
                        && "java.lang.Long".equals(column.getFullyQualifiedJavaType().getFullyQualifiedName());
                if ((!integer && !longType) || column.isNullable() || !writable(column)
                        || config.getParameterList() || table.getPrimaryKeyColumns().isEmpty()
                        || !config.getColumns().contains(column)
                        || !config.getColumns().containsAll(table.getPrimaryKeyColumns())) {
                    throw failure(table, "版本表要求非空可写 Integer/Long version_、单值完整主键和旧版本条件；不支持独立多值列表");
                }
            }
            if (table.getTableConfiguration().isCasWriteContractEnabled()
                    && (config.getColumns().size() != 2
                    || !Set.copyOf(config.getColumns()).equals(Set.of(table.getPrimaryKeyColumns().get(0),
                    table.getColumn("version_").orElseThrow())))) {
                throw failure(table, "受管 CAS 逻辑删除仅支持 id_,version_ 单值条件");
            }
            if (table.getTableConfiguration().getSelectByColumnGeneratorConfigurations().stream()
                    .filter(c -> c.isEnableDelete() && c.getDeleteMethodName().equals(config.getDeleteMethodName())).count() != 1) {
                throw failure(table, "删除方法配置重复：" + config.getDeleteMethodName());
            }
        }
    }

    private static boolean writable(IntrospectedColumn column) {
        return !column.isGeneratedColumn() && !column.isAutoIncrement() && !column.isIdentity();
    }

    private static IllegalArgumentException failure(AbstractIntrospectedTable table, String reason) {
        return new IllegalArgumentException("表 " + table.getTableConfiguration().getTableName()
                + " selectByColumn isLogicalDelete：" + reason);
    }
}
