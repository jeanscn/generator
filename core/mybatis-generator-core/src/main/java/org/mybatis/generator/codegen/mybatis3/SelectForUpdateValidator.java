package org.mybatis.generator.codegen.mybatis3;

import com.vgosoft.core.db.query.DatabaseType;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.dbstrategy.SelectForUpdateDialect;

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
        if (!"MyBatis3".equals(table.getContext().getTargetRuntime())
                || !(table instanceof IntrospectedTableMyBatis3Impl)
                || tc.getJavaClientGeneratorConfiguration() == null
                || table.getContext().getJavaClientGeneratorConfiguration() == null
                || !"XMLMAPPER".equalsIgnoreCase(table.getContext().getJavaClientGeneratorConfiguration().getConfigurationType())
                || !tc.getJavaClientGeneratorConfiguration().isGenerate()
                || tc.getSqlMapGeneratorConfiguration() == null || !tc.getSqlMapGeneratorConfiguration().isGenerate()
                || tc.getJavaModelGeneratorConfiguration() == null || !tc.getJavaModelGeneratorConfiguration().isGenerate()
                || tc.getJavaServiceGeneratorConfiguration() == null || !tc.getJavaServiceGeneratorConfiguration().isGenerate()
                || tc.getJavaServiceImplGeneratorConfiguration() == null || !tc.getJavaServiceImplGeneratorConfiguration().isGenerate()
                || "true".equalsIgnoreCase(tc.getProperty("modelOnly"))
                || table.isImmutable()) {
            throw failure(table, "要求 MyBatis3/XMLMAPPER、可变模型以及 Mapper/XML/Service 接口和实现同时生成");
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
