package org.mybatis.generator.codegen.mybatis3;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.config.SensitiveCheckConfiguration;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.ObjectFactory;

import javax.lang.model.SourceVersion;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 统一校验并渲染敏感字段、受管写方法和前端显示元数据。
 *
 * @author vgosoft
 */
public final class SensitiveGeneration {
    private static final String ANNOTATION_PACKAGE = "com.vgosoft.core.annotation.sensitive.";
    private static final Set<String> SINGLE_WRITES = Set.of("insert", "insertSelective",
            "updateByPrimaryKey", "updateByPrimaryKeySelective", "insertOrUpdate", "insertOrUpdateSelective");
    private static final Set<String> BATCH_WRITES = Set.of("insertBatch", "updateBatchByPrimaryKey");

    private SensitiveGeneration() {
    }

    public static boolean isEnabled(AbstractIntrospectedTable table) {
        return table.getTableConfiguration().getSensitiveCheckConfigurations().stream()
                .anyMatch(SensitiveCheckConfiguration::isEnable);
    }

    public static Map<IntrospectedColumn, SensitiveCheckConfiguration> resolve(AbstractIntrospectedTable table) {
        Map<IntrospectedColumn, SensitiveCheckConfiguration> result = new LinkedHashMap<>();
        for (SensitiveCheckConfiguration configuration : table.getTableConfiguration().getSensitiveCheckConfigurations()) {
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

    public static void validate(AbstractIntrospectedTable table) {
        if (table.getTableConfiguration().getSensitiveCheckConfigurations().isEmpty()) {
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
            if (table.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                    .anyMatch(relation -> relation.isEnableInsert() || relation.isEnableUpdate() || relation.isEnableInsertOrUpdate())) {
                throw failure(table, "*", "主子表级联包含多实体写入，须先提供全部实体的写前检测适配");
            }
        }
    }

    public static void addModelAnnotations(TopLevelClass model, AbstractIntrospectedTable table) {
        validate(table);
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
            model.addImportedType(ANNOTATION_PACKAGE + "SensitiveCheck");
            model.addImportedType(ANNOTATION_PACKAGE + "SensitiveCheckStrategy");
            model.addImportedType(ANNOTATION_PACKAGE + "SensitiveContentType");
            String annotation = "@SensitiveCheck(enable = " + configuration.isEnable()
                    + ", strategy = SensitiveCheckStrategy." + configuration.getStrategy()
                    + ", contentType = SensitiveContentType." + configuration.getContentType() + ")";
            if (!field.getAnnotations().contains(annotation)) {
                field.addAnnotation(annotation);
            }
        }
    }

    public static void addInheritedWriteMethods(TopLevelClass service, AbstractIntrospectedTable table) {
        if (!isEnabled(table)) {
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
        validate(table);
        addInheritedWriteMethods(service, table);
        FullyQualifiedJavaType entity = new FullyQualifiedJavaType(table.getBaseRecordType());
        Set<String> found = new java.util.HashSet<>();
        for (Method method : service.getMethods()) {
            boolean batch = BATCH_WRITES.contains(method.getName());
            if (!batch && !SINGLE_WRITES.contains(method.getName())) {
                continue;
            }
            if (!found.add(method.getName()) || method.getParameters().size() != 1) {
                throw failure(table, method.getName(), "受管写方法重复或实体参数角色不明确");
            }
            if (method.getVisibility() != JavaVisibility.PUBLIC || method.isStatic() || method.isFinal() || method.isAbstract()) {
                throw failure(table, method.getName(), "受管写方法必须是可代理的 public 非静态、非 final 具体方法");
            }
            FullyQualifiedJavaType parameter = method.getParameters().get(0).getType();
            boolean supported = batch
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
            List<String> transactions = method.getAnnotations().stream().filter(value -> value.startsWith("@Transactional")).toList();
            if (transactions.isEmpty()) {
                Mb3GenUtil.addTransactionalAnnotation(service, method, "READ_COMMITTED");
            } else if (transactions.size() != 1 || !transactions.get(0).matches(
                    "@Transactional\\(rollbackFor = Exception\\.class(, isolation = Isolation\\."
                            + "(DEFAULT|READ_UNCOMMITTED|READ_COMMITTED|REPEATABLE_READ|SERIALIZABLE))?\\)")) {
                throw failure(table, method.getName(), "事务声明不是已验证的 REQUIRED/Exception 回滚模板，须单独评审");
            }
        }
        if (!found.containsAll(Set.of("insert", "insertSelective", "updateByPrimaryKey",
                "updateByPrimaryKeySelective", "insertOrUpdateSelective"))) {
            throw failure(table, "*", "标准单实体写方法未完整装配");
        }
        if (table.getRules().generateInsertBatch() && !found.contains("insertBatch")
                || table.getRules().generateUpdateBatch() && !found.contains("updateBatchByPrimaryKey")
                || table.getRules().generateInsertOrUpdate() && !found.contains("insertOrUpdate")) {
            throw failure(table, "*", "已启用的批量或 upsert 方法未装配");
        }
    }

    public static List<Map<String, String>> frontendFields(AbstractIntrospectedTable table) {
        List<Map<String, String>> fields = new ArrayList<>();
        resolve(table).forEach((column, configuration) -> {
            if (configuration.isEnable()) {
                Map<String, String> field = new LinkedHashMap<>();
                field.put("table", table.getTableConfiguration().getTableName());
                field.put("entity", table.getBaseRecordType());
                field.put("property", column.getJavaProperty());
                field.put("label", column.getRemarks(true) == null ? column.getJavaProperty() : column.getRemarks(true));
                field.put("contentType", configuration.getContentType());
                fields.add(field);
            }
        });
        return fields;
    }

    public static void requireModelGenerated(AbstractIntrospectedTable table) {
        if (!table.getTableConfiguration().getSensitiveCheckConfigurations().isEmpty()) {
            throw failure(table, "*", "插件禁止生成实体，无法落地敏感字段元数据");
        }
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
