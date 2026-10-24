package org.mybatis.generator.codegen.mybatis3;

import com.vgosoft.core.db.query.DatabaseType;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.dbstrategy.SelectForUpdateDialect;
import org.mybatis.generator.internal.ObjectFactory;

/**
 * 在输出之前预检锁定查询的完整配置与方言能力，不装配生成物。
 *
 * @author vgosoft
 */
public final class SelectForUpdateValidator {
    private SelectForUpdateValidator() {
    }

    /**
     * 验证配置及全部目标方言，默认关闭时不改变原有生成流程。
     *
     * @param table 内省表信息
     */
    public static void validate(AbstractIntrospectedTable table) {
        var tc = table.getTableConfiguration();
        if (!tc.isSelectForUpdateEnabled()) {
            return;
        }
        var runtime = ObjectFactory.createIntrospectedTableForValidation(table.getContext());
        if (runtime.getClass() != IntrospectedTableMyBatis3Impl.class
                || !(table instanceof IntrospectedTableMyBatis3Impl)
                || table instanceof IntrospectedTableMyBatis3SimpleImpl
                || table.getTargetRuntime() != AbstractIntrospectedTable.TargetRuntime.MYBATIS3) {
            throw failure(table, "运行时 " + table.getContext().getEffectiveTargetRuntime()
                    + " 不支持锁定查询，要求 MyBatis3 内省实现");
        }
        var client = table.getContext().getJavaClientGeneratorConfiguration();
        if (client == null || !"XMLMAPPER".equalsIgnoreCase(client.getConfigurationType())) {
            throw failure(table, "Context javaClientGenerator 必须配置为 XMLMAPPER");
        }
        if ("true".equalsIgnoreCase(tc.getProperty("modelOnly")) || table.isImmutable()) {
            throw failure(table, "要求可变模型，禁止 immutable 或 modelOnly");
        }
        if (tc.getJavaModelGeneratorConfiguration() == null || !tc.getJavaModelGeneratorConfiguration().isGenerate()) {
            throw failure(table, "必须启用 Model 输出（generateModel）");
        }
        if (tc.getJavaClientGeneratorConfiguration() == null || !tc.getJavaClientGeneratorConfiguration().isGenerate()) {
            throw failure(table, "必须启用 Mapper/DAO 输出（generateDao）");
        }
        if (tc.getSqlMapGeneratorConfiguration() == null || !tc.getSqlMapGeneratorConfiguration().isGenerate()) {
            throw failure(table, "必须启用 XML 输出（generateSqlMap）");
        }
        if (tc.getJavaServiceGeneratorConfiguration() == null || !tc.getJavaServiceGeneratorConfiguration().isGenerate()) {
            throw failure(table, "必须启用 Service 接口输出（generateService）");
        }
        if (tc.getJavaServiceImplGeneratorConfiguration() == null || !tc.getJavaServiceImplGeneratorConfiguration().isGenerate()) {
            throw failure(table, "必须启用 Service 实现输出（generateService 的实现配置）");
        }
        if (tc.isSelectByPrimaryKeyStatementEnabled() && !table.getRules().generateSelectByPrimaryKey()) {
            throw failure(table, "enableSelectByPrimaryKey 已开启但模型没有可查询主键");
        }
        if (tc.isSelectByExampleStatementEnabled()
                && (!(table.getRules().generateSelectByExampleWithoutBlobs()
                || table.getRules().generateSelectByExampleWithBlobs()) || !table.getRules().generateExampleClass())) {
            throw failure(table, "enableSelectByExample 已开启但模型不支持 Example");
        }
        if (!table.getRules().generateSelectByPrimaryKey()
                && !table.getRules().generateSelectByExampleWithoutBlobs()
                && !table.getRules().generateSelectByExampleWithBlobs()) {
            throw failure(table, "普通查询开关均关闭，没有可生成的锁定方法");
        }
        if (tc.getAdditionalDbTypes().isEmpty()) {
            throw failure(table, "additionalDbTypes 为空，无法可靠预检锁语法");
        }
        for (DatabaseType database : tc.getAdditionalDbTypes()) {
            try {
                SelectForUpdateDialect.usesTableHint(database);
            } catch (IllegalArgumentException exception) {
                throw failure(table, exception.getMessage());
            }
        }
    }

    private static IllegalArgumentException failure(AbstractIntrospectedTable table, String reason) {
        return new IllegalArgumentException("表 " + table.getTableConfiguration().getTableName()
                + " enableSelectForUpdate：" + reason);
    }
}
