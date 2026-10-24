package org.mybatis.generator.codegen.mybatis3;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.config.SensitiveCheckConfiguration;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mybatis.generator.codegen.mybatis3.SensitiveFieldResolver.isEnabled;
import static org.mybatis.generator.codegen.mybatis3.SensitiveFieldResolver.resolve;

/**
 * 内省后预检敏感生成支持的模型、父类和受管写入配置，不装配生成产物。
 *
 * @author vgosoft
 */
public final class SensitiveGenerationValidator {
    private SensitiveGenerationValidator() {
    }

    public static void validate(AbstractIntrospectedTable table) {
        if (!isEnabled(table)) {
            return;
        }
        Map<IntrospectedColumn, SensitiveCheckConfiguration> fields = resolve(table);
        if (!(table instanceof IntrospectedTableMyBatis3Impl)
                || table.getTableConfiguration().getJavaModelGeneratorConfiguration() == null
                || !table.getTableConfiguration().getJavaModelGeneratorConfiguration().isGenerate()
                || !table.getRules().generateBaseRecordClass()
                || table.getRules().generatePrimaryKeyClass()
                || table.getRules().generateRecordWithBlobsClass()
                || table.isImmutable()) {
            throw failure(table, "*", "仅支持 MyBatis3 可变单实体模型；分层模型和其他运行时尚未接入");
        }
        String rootClass = JavaBeansUtil.getRootClass(table);
        if (rootClass != null) {
            try {
                ObjectFactory.externalClassForName(new FullyQualifiedJavaType(rootClass).getFullyQualifiedNameWithoutTypeParameters());
            } catch (Exception exception) {
                throw failure(table, "*", "父类元数据无法加载：" + rootClass);
            }
        }
        for (IntrospectedColumn column : fields.keySet()) {
            List<String> warnings = new ArrayList<>();
            boolean inherited = RootClassInfo.getInstance(JavaBeansUtil.getRootClass(table), warnings).containsProperty(column);
            if (!warnings.isEmpty()) {
                throw failure(table, column.getActualColumnName(), "父类元数据无法可靠解析：" + String.join("；", warnings));
            }
            if (inherited) {
                throw failure(table, column.getActualColumnName(), "字段由父类持有，不能在子类生成字段注解");
            }
        }
        if (isEnabled(table)) {
            if (table.getTableConfiguration().getJavaServiceImplGeneratorConfiguration() == null
                    || !table.getTableConfiguration().getJavaServiceImplGeneratorConfiguration().isGenerate()) {
                throw failure(table, "*", "启用敏感字段必须同时生成受管 Service");
            }
            if ("true".equalsIgnoreCase(table.getTableConfiguration().getProperty("modelOnly"))) {
                throw failure(table, "*", "modelOnly 不生成受管写方法，不能启用敏感字段");
            }
            if (table.getTableConfiguration().isUpdateByExampleStatementEnabled()) {
                throw failure(table, "*", "updateByExample 影响范围未受控；请关闭 enableUpdateByExample 后另行评审接入");
            }
            if (table.getTableConfiguration().isCasWriteContractEnabled()
                    && table.getRules().generateUpdateBatch()) {
                throw failure(table, "*", "敏感检测与受管 CAS 同时启用时没有受支持的 CAS 批量更新；请关闭 update batch");
            }
            if (table.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                    .anyMatch(relation -> relation.isEnableInsert() || relation.isEnableUpdate() || relation.isEnableInsertOrUpdate())) {
                throw failure(table, "*", "主子表级联包含多实体写入，须先提供全部实体的写前检测适配");
            }
        }
    }

    private static IllegalArgumentException failure(AbstractIntrospectedTable table, String column, String message) {
        return new IllegalArgumentException("表 " + table.getTableConfiguration().getTableName()
                + "，列/方法 " + column + "：" + message);
    }
}
