package org.mybatis.generator.codegen.mybatis3;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 统一受管 CAS 的内省能力、列角色和允许入口，默认关闭时不影响旧生成链。
 *
 * @author vgosoft
 */
public final class CasWriteGenerationValidator {
    public static final String LANGUAGE_DRIVER = "com.vgosoft.mybatis.write.StaticWriteLanguageDriver";
    public static final Map<String, String> OPERATIONS = Map.of(
            "insert", "INSERT_FULL", "insertSelective", "INSERT_SELECTIVE", "insertBatch", "INSERT_BATCH",
            "updateByPrimaryKeyCas", "UPDATE_FULL", "updateByPrimaryKeySelectiveCas", "UPDATE_SELECTIVE");
    private static final Map<String, String> ROLES = Map.of(
            "id_", "primary-key", "version_", "version", "created_", "created", "modified_", "modified",
            "created_id", "created-id", "modified_id", "modified-id");
    private CasWriteGenerationValidator() { }

    /**
     * 核验首期模型、各层输出、真实内省列及所有声明方言。
     *
     * @param table 当前内省表
     */
    public static void validate(AbstractIntrospectedTable table) {
        var tc = table.getTableConfiguration();
        if (!tc.isCasWriteContractEnabled()) { return; }
        if (table.getClass() != IntrospectedTableMyBatis3Impl.class || table.isImmutable()
                || "true".equalsIgnoreCase(tc.getProperty("modelOnly"))) {
            throw failure(table, "仅支持可变 MyBatis3 模型");
        }
        var client = table.getContext().getJavaClientGeneratorConfiguration();
        if (client == null || !"XMLMAPPER".equals(client.getConfigurationType())) {
            throw failure(table, "要求 XMLMAPPER");
        }
        if (tc.getJavaModelGeneratorConfiguration() == null || !tc.getJavaModelGeneratorConfiguration().isGenerate()
                || tc.getJavaClientGeneratorConfiguration() == null || !tc.getJavaClientGeneratorConfiguration().isGenerate()
                || tc.getSqlMapGeneratorConfiguration() == null || !tc.getSqlMapGeneratorConfiguration().isGenerate()
                || tc.getJavaServiceGeneratorConfiguration() == null || !tc.getJavaServiceGeneratorConfiguration().isGenerate()
                || tc.getJavaServiceImplGeneratorConfiguration() == null || !tc.getJavaServiceImplGeneratorConfiguration().isGenerate()) {
            throw failure(table, "必须同时输出 Model、Mapper、XML、Service 接口及实现");
        }
        if (tc.getGeneratedKey().isPresent() || table.getPrimaryKeyColumns().size() != 1
                || !"id_".equals(table.getPrimaryKeyColumns().get(0).getActualColumnName())
                || !tc.getRelationGeneratorConfigurations().isEmpty()
                || tc.getJavaModelGeneratorConfiguration().isEnableChildren()) {
            throw failure(table, "要求单一非自增 id_ 主键、无 generatedKey、无关系级联写入");
        }
        if (!Set.of("com.vgosoft.core.entity.abs.AbstractPersistenceLockEntity",
                "com.vgosoft.core.entity.abs.AbstractPersistenceLockSortableEntity")
                .contains(tc.getProperty(PropertyRegistry.ROOT_CLASS))) {
            throw failure(table, "父类必须使用已核验的继承 Integer version 模型");
        }
        if (tc.getAdditionalDbTypes().isEmpty()) { throw failure(table, "additionalDbTypes 不能为空"); }
        tc.getAdditionalDbTypes().forEach(db -> {
            if (!Set.of("mysql", "h2").contains(db.getCode())) {
                throw failure(table, "不支持方言 " + db.getCode() + "，仅核验 mysql/h2，无静默回退");
            }
        });
        for (String required : ROLES.keySet()) {
            if (table.getColumn(required).isEmpty()) { throw failure(table, "缺少列 " + required); }
        }
        for (var column : table.getAllColumns()) {
            String java = column.getFullyQualifiedJavaType().getFullyQualifiedName();
            String jdbc = column.getJdbcTypeName();
            String role = role(column);
            boolean compatible = "java.lang.String".equals(java) && Set.of("VARCHAR", "CHAR").contains(jdbc)
                    || "java.lang.Integer".equals(java) && "INTEGER".equals(jdbc)
                    || "java.time.LocalDateTime".equals(java) && "TIMESTAMP".equals(jdbc);
            if (!compatible || column.isIdentity() || column.isAutoIncrement() || column.isGeneratedColumn()
                    || role.equals("primary-key") && !"java.lang.String".equals(java)
                    || role.equals("version") && (!"java.lang.Integer".equals(java) || column.isNullable())
                    || Set.of("created", "modified").contains(role) && !"java.time.LocalDateTime".equals(java)
                    || role.endsWith("-id") && !"java.lang.String".equals(java)
                    || Set.of("record", "expectedVersion", "_parameter", "_databaseId", "_writeAudit").contains(column.getJavaProperty())) {
                throw failure(table, "列 " + column.getActualColumnName() + " 类型/可写性不受支持：" + java + "/" + jdbc);
            }
        }
        if (!table.getRules().generateInsertBatch()) { throw failure(table, "必须保留 insertBatch 输出"); }
    }

    public static String role(IntrospectedColumn column) {
        return ROLES.getOrDefault(column.getActualColumnName(), "business");
    }

    public static List<IntrospectedColumn> writes(AbstractIntrospectedTable table, String operation) {
        if ("LOGICAL_DELETE".equals(operation)) {
            return table.getAllColumns().stream().filter(c -> "delete_flag".equals(c.getActualColumnName())
                    || Set.of("version", "modified", "modified-id").contains(role(c))).toList();
        }
        return table.getAllColumns().stream().filter(c -> operation.startsWith("INSERT")
                || !Set.of("primary-key", "created", "created-id").contains(role(c))).toList();
    }

    /** 按表追加显式逻辑删除入口，不放开其他旧删除方法。 */
    public static Map<String, String> operations(AbstractIntrospectedTable table) {
        Map<String, String> operations = new java.util.TreeMap<>(OPERATIONS);
        LogicalDeleteGenerationValidator.configurations(table).forEach(c -> {
            if (operations.putIfAbsent(c.getDeleteMethodName(), "LOGICAL_DELETE") != null) {
                throw failure(table, "受管逻辑删除方法重复：" + c.getDeleteMethodName());
            }
        });
        return operations;
    }

    public static IllegalArgumentException failure(AbstractIntrospectedTable table, String reason) {
        return new IllegalArgumentException("表 " + table.getTableConfiguration().getTableName()
                + " enableCasWriteContract：" + reason);
    }
}
