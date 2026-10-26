package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.SensitiveGenerationValidator;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.List;
import java.util.Set;

import static org.mybatis.generator.codegen.mybatis3.SensitiveFieldResolver.isEnabled;

/**
 * 装配敏感受管写方法，在服务插件执行后校验参数、代理和事务并添加注解。
 *
 * @author vgosoft
 */
public class SensitiveWriteMethodsElement extends AbstractServiceElementGenerator {
    private static final String ANNOTATION_PACKAGE = "com.vgosoft.core.annotation.sensitive.";
    private static final Set<String> SINGLE_WRITES = Set.of("insert", "insertSelective",
            "updateByPrimaryKey", "updateByPrimaryKeySelective", "insertOrUpdate", "insertOrUpdateSelective");
    private static final Set<String> BATCH_WRITES = Set.of("insertBatch", "updateBatchByPrimaryKey");
    private static final Set<String> CAS_WRITES =
            Set.of("updateByPrimaryKeyCas", "updateByPrimaryKeySelectiveCas");
    private static final Set<String> CAS_DISABLED_WRITES = Set.of("updateByPrimaryKey",
            "updateByPrimaryKeySelective", "updateBatchByPrimaryKey", "insertOrUpdate", "insertOrUpdateSelective");

    @Override
    public void addElements(TopLevelClass parentElement) {
        addWriteAnnotations(parentElement, introspectedTable);
    }

    public static void addInheritedWriteMethods(TopLevelClass service, AbstractIntrospectedTable table) {
        if (!isEnabled(table) || table.getTableConfiguration().isCasWriteContractEnabled()) {
            return;
        }
        FullyQualifiedJavaType entity = new FullyQualifiedJavaType(table.getBaseRecordType());
        if (service.getMethods().stream().noneMatch(method -> "insertOrUpdateSelective".equals(method.getName()))) {
            Method method = new Method("insertOrUpdateSelective");
            method.setVisibility(JavaVisibility.PUBLIC);
            FullyQualifiedJavaType result = new FullyQualifiedJavaType("com.vgosoft.core.adapter.ServiceResult");
            result.addTypeArgument(entity);
            method.setReturnType(result);
            method.addParameter(new Parameter(entity, "record"));
            method.addAnnotation("@Override");
            method.addBodyLine("return super.insertOrUpdateSelective(record);");
            service.addMethod(method);
            service.addImportedType(result);
            service.addImportedType(entity);
        }
    }

    public static void addWriteAnnotations(TopLevelClass service, AbstractIntrospectedTable table) {
        if (!isEnabled(table)) {
            return;
        }
        SensitiveGenerationValidator.validate(table);
        addInheritedWriteMethods(service, table);
        boolean casContract = table.getTableConfiguration().isCasWriteContractEnabled();
        FullyQualifiedJavaType entity = new FullyQualifiedJavaType(table.getBaseRecordType());
        Set<String> found = new java.util.HashSet<>();
        for (Method method : service.getMethods()) {
            if (org.mybatis.generator.codegen.mybatis3.service.DisabledServiceEntries.isDisabled(table, method)) {
                found.add(method.getName());
                continue;
            }
            if (casContract && CAS_DISABLED_WRITES.contains(method.getName())) {
                continue;
            }
            boolean cas = CAS_WRITES.contains(method.getName());
            boolean batch = BATCH_WRITES.contains(method.getName());
            if (!batch && !cas && !SINGLE_WRITES.contains(method.getName())) {
                continue;
            }
            int expectedParameters = cas ? 2 : 1;
            if (!found.add(method.getName()) || method.getParameters().size() != expectedParameters) {
                throw failure(table, method.getName(), "受管写方法重复或实体参数角色不明确");
            }
            if (method.getVisibility() != JavaVisibility.PUBLIC || method.isStatic() || method.isFinal() || method.isAbstract()) {
                throw failure(table, method.getName(), "受管写方法必须是可代理的 public 非静态、非 final 具体方法");
            }
            FullyQualifiedJavaType parameter = method.getParameters().get(0).getType();
            boolean supported = cas
                    ? entity.equals(parameter)
                    && "java.lang.Integer".equals(method.getParameters().get(1).getType().getFullyQualifiedName())
                    : batch
                    ? "java.util.List".equals(parameter.getFullyQualifiedNameWithoutTypeParameters())
                    && parameter.getTypeArguments().size() == 1 && entity.equals(parameter.getTypeArguments().get(0))
                    : entity.equals(parameter);
            if (!supported) {
                throw failure(table, method.getName(), "只支持单实体或同类型有序 List 参数");
            }
            String operationKey = entity.getFullyQualifiedName() + ":" + method.getName();
            if (operationKey.length() > 256) {
                throw failure(table, method.getName(), "operationKey 超出确认存储的 256 字符上限");
            }
            String annotation = "@SensitiveWrite(operationKey = \"" + operationKey + "\", entityArgumentIndex = 0)";
            if (!method.getAnnotations().contains(annotation)) {
                method.addAnnotation(annotation);
            }
            service.addImportedType(ANNOTATION_PACKAGE + "SensitiveWrite");
            List<String> transactions = method.getAnnotations().stream().filter(value ->
                    value.startsWith("@Transactional")
                            || value.startsWith("@org.springframework.transaction.annotation.Transactional")).toList();
            if (transactions.isEmpty() && !casContract) {
                Mb3GenUtil.addTransactionalAnnotation(service, method, "READ_COMMITTED");
            } else if (transactions.size() != 1 || !supportedTransaction(transactions.get(0), casContract)) {
                throw failure(table, method.getName(), "事务声明不是已验证的 REQUIRED/Exception 回滚模板，须单独评审");
            }
        }
        Set<String> requiredWrites = casContract
                ? Set.of("insert", "insertSelective", "updateByPrimaryKeyCas", "updateByPrimaryKeySelectiveCas")
                : Set.of("insert", "insertSelective", "updateByPrimaryKey",
                        "updateByPrimaryKeySelective", "insertOrUpdateSelective");
        if (!found.containsAll(requiredWrites)) {
            throw failure(table, "*", "标准单实体写方法未完整装配");
        }
        if (table.getRules().generateInsertBatch() && !found.contains("insertBatch")
                || !casContract && table.getRules().generateUpdateBatch()
                        && !found.contains("updateBatchByPrimaryKey")
                || !casContract && table.getRules().generateInsertOrUpdate()
                        && !found.contains("insertOrUpdate")) {
            throw failure(table, "*", "已启用的批量或 upsert 方法未装配");
        }
    }

    private static boolean supportedTransaction(String annotation, boolean casContract) {
        if (casContract) {
            return annotation.equals("@org.springframework.transaction.annotation.Transactional("
                    + "propagation = org.springframework.transaction.annotation.Propagation.MANDATORY, "
                    + "rollbackFor = Exception.class)");
        }
        String normalized = annotation
                .replace("@org.springframework.transaction.annotation.Transactional(", "@Transactional(")
                .replace("org.springframework.transaction.annotation.Isolation.", "Isolation.");
        return normalized.matches("@Transactional\\(rollbackFor = Exception\\.class(, isolation = Isolation\\."
                + "(DEFAULT|READ_UNCOMMITTED|READ_COMMITTED|REPEATABLE_READ|SERIALIZABLE))?\\)");
    }

    public static void requireServiceGenerated(AbstractIntrospectedTable table) {
        if (isEnabled(table)) {
            throw failure(table, "*", "插件禁止生成 Service，无法落地受管写方法标记");
        }
    }

    private static IllegalArgumentException failure(AbstractIntrospectedTable table, String column, String message) {
        return new IllegalArgumentException("表 " + table.getTableConfiguration().getTableName()
                + "，列/方法 " + column + "：" + message);
    }
}
