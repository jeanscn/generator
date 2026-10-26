package org.mybatis.generator.codegen.mybatis3.service;

import com.vgosoft.core.constant.enums.data.StatTypeEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.DictCacheOrderResolver;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.custom.ConstantsUtil.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-04 17:36
 * @version 3.0
 */
public class ServiceMethods {

    private final Context context;
    private final AbstractIntrospectedTable introspectedTable;
    private final FullyQualifiedJavaType entityType;
    private final FullyQualifiedJavaType exampleType;

    public ServiceMethods(Context context, AbstractIntrospectedTable introspectedTable) {
        this.context = context;
        this.introspectedTable = introspectedTable;
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
    }

    /**
     * 解析真实父类的公开方法及泛型绑定，补齐没有本地生成体的继承入口。
     *
     * @param parentType 已绑定 Mapper、实体和 Example 的父类
     * @param unit 接收签名依赖的编译单元
     * @return 可覆盖的完整继承方法集合
     */
    public List<Method> getInheritedServiceMethods(FullyQualifiedJavaType parentType, CompilationUnit unit) {
        return getInheritedServiceMethodMetadata(parentType, unit).stream()
                .map(InheritedServiceMethod::method).toList();
    }

    /**
     * 保存解析后的签名和真实声明，不用空方法体推断父实现的可调用性。
     *
     * @author vgosoft-agent
     */
    public record InheritedServiceMethod(Method method, java.lang.reflect.Method source) {
        /**
         * 判断是否存在可由生成子类通过 super 调用的具体类实现。
         *
         * @return 具体类公开实例方法时为真，接口默认实现也不作为支持的内部原语
         */
        public boolean callableFromSuper() {
            int modifiers = source.getModifiers();
            return !source.getDeclaringClass().isInterface()
                    && java.lang.reflect.Modifier.isPublic(modifiers)
                    && !java.lang.reflect.Modifier.isAbstract(modifiers)
                    && !java.lang.reflect.Modifier.isFinal(modifiers)
                    && !java.lang.reflect.Modifier.isStatic(modifiers)
                    && source.getTypeParameters().length == 0;
        }
    }

    /**
     * 按类优先、最近覆盖优先的 Java 解析顺序收集继承签名和声明元数据。
     *
     * @param parentType 已绑定泛型的真实父类
     * @param unit 接收签名依赖的编译单元
     * @return 继承方法及真实来源，不包含执行方法体
     */
    public List<InheritedServiceMethod> getInheritedServiceMethodMetadata(
            FullyQualifiedJavaType parentType, CompilationUnit unit) {
        try {
            Class<?> parent = org.mybatis.generator.internal.ObjectFactory.externalClassForName(
                    parentType.getFullyQualifiedNameWithoutTypeParameters());
            Map<java.lang.reflect.TypeVariable<?>, FullyQualifiedJavaType> bindings = new HashMap<>();
            for (int i = 0; i < parent.getTypeParameters().length; i++) {
                bindings.put(parent.getTypeParameters()[i], parentType.getTypeArguments().get(i));
            }
            Map<String, InheritedServiceMethod> methods = new LinkedHashMap<>();
            collectInherited(parent, bindings, unit, methods, false);
            collectInherited(parent, bindings, unit, methods, true);
            return new ArrayList<>(methods.values());
        } catch (ClassNotFoundException exception) {
            throw new IllegalArgumentException("disabledMethod 无法读取真实 Service 父类：" + parentType, exception);
        }
    }

    private void collectInherited(Class<?> type,
                                  Map<java.lang.reflect.TypeVariable<?>, FullyQualifiedJavaType> bindings,
                                  CompilationUnit unit, Map<String, InheritedServiceMethod> methods,
                                  boolean interfaces) {
        if (type == Object.class) { return; }
        for (var source : type.isInterface() == interfaces ? type.getDeclaredMethods()
                : new java.lang.reflect.Method[0]) {
            if (!java.lang.reflect.Modifier.isPublic(source.getModifiers())
                    || (type.isInterface() && java.lang.reflect.Modifier.isStatic(source.getModifiers()))
                    || source.isBridge() || source.isSynthetic()) { continue; }
            Method method = new Method(source.getName());
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setFinal(java.lang.reflect.Modifier.isFinal(source.getModifiers()));
            method.setStatic(java.lang.reflect.Modifier.isStatic(source.getModifiers()));
            method.setAbstract(java.lang.reflect.Modifier.isAbstract(source.getModifiers()));
            for (var variable : source.getTypeParameters()) {
                method.addTypeParameter(new TypeParameter(variable.getName()));
            }
            if (source.getReturnType() != void.class) {
                method.setReturnType(resolveInheritedType(source.getGenericReturnType(), bindings));
                unit.addImportedType(method.getReturnType().orElseThrow());
            }

            for (int i = 0; i < source.getGenericParameterTypes().length; i++) {
                FullyQualifiedJavaType parameter = resolveInheritedType(source.getGenericParameterTypes()[i], bindings);
                method.addParameter(new Parameter(parameter, "arg" + i));
                unit.addImportedType(parameter);
            }
            for (var exception : source.getGenericExceptionTypes()) {
                var resolved = resolveInheritedType(exception, bindings);
                method.addException(resolved);
                unit.addImportedType(resolved);
            }
            String signature = signature(method);
            var previous = methods.get(signature);
            if (previous == null || (previous.source().getDeclaringClass().isInterface()
                    && previous.source().getDeclaringClass().isAssignableFrom(source.getDeclaringClass()))) {
                methods.put(signature, new InheritedServiceMethod(method, source));
            }
        }
        List<java.lang.reflect.Type> ancestors = new ArrayList<>();
        if (type.getGenericSuperclass() != null) { ancestors.add(type.getGenericSuperclass()); }
        if (interfaces) { ancestors.addAll(List.of(type.getGenericInterfaces())); }
        for (var ancestor : ancestors) {
            Class<?> raw = ancestor instanceof java.lang.reflect.ParameterizedType parameterized
                    ? (Class<?>) parameterized.getRawType() : (Class<?>) ancestor;
            Map<java.lang.reflect.TypeVariable<?>, FullyQualifiedJavaType> next = new HashMap<>(bindings);
            if (ancestor instanceof java.lang.reflect.ParameterizedType parameterized) {
                for (int i = 0; i < raw.getTypeParameters().length; i++) {
                    next.put(raw.getTypeParameters()[i],
                            resolveInheritedType(parameterized.getActualTypeArguments()[i], bindings));
                }
            }
            collectInherited(raw, next, unit, methods, interfaces);
        }
    }

    /**
     * 收集父类可见名称，内部原语不得遮蔽既有受保护扩展钩子。
     *
     * @param parentType 真实父类类型
     * @return 公开或受保护的继承方法名
     */
    public Set<String> getInheritedServiceMethodNames(FullyQualifiedJavaType parentType) {
        try {
            Class<?> type = org.mybatis.generator.internal.ObjectFactory.externalClassForName(
                    parentType.getFullyQualifiedNameWithoutTypeParameters());
            Set<String> names = new HashSet<>();
            while (type != null) {
                for (var method : type.getDeclaredMethods()) {
                    if (!java.lang.reflect.Modifier.isPrivate(method.getModifiers())) {
                        names.add(method.getName());
                    }
                }
                type = type.getSuperclass();
            }
            return names;
        } catch (ClassNotFoundException exception) {
            throw new IllegalArgumentException("disabledMethod 无法读取真实 Service 父类：" + parentType, exception);
        }
    }

    private FullyQualifiedJavaType resolveInheritedType(java.lang.reflect.Type type,
                            Map<java.lang.reflect.TypeVariable<?>, FullyQualifiedJavaType> bindings) {
        if (type instanceof Class<?> raw) { return new FullyQualifiedJavaType(raw.getTypeName()); }
        if (type instanceof java.lang.reflect.TypeVariable<?> variable) {
            return bindings.getOrDefault(variable, new FullyQualifiedJavaType(variable.getBounds()[0].getTypeName()));
        }
        if (type instanceof java.lang.reflect.ParameterizedType parameterized) {
            var result = new FullyQualifiedJavaType(((Class<?>) parameterized.getRawType()).getName());
            for (var argument : parameterized.getActualTypeArguments()) {
                result.addTypeArgument(resolveInheritedType(argument, bindings));
            }
            return result;
        }
        if (type instanceof java.lang.reflect.GenericArrayType array) {
            return new FullyQualifiedJavaType(resolveInheritedType(array.getGenericComponentType(), bindings)
                    .getFullyQualifiedName() + "[]");
        }
        if (type instanceof java.lang.reflect.WildcardType wildcard) {
            return new FullyQualifiedJavaType("? extends " + resolveInheritedType(wildcard.getUpperBounds()[0], bindings)
                    .getFullyQualifiedName());
        }
        throw new IllegalArgumentException("disabledMethod 不支持的继承类型：" + type);
    }

    /**
     * 将共享方法描述转换为无空格的完整擦除参数签名。
     *
     * @param method 共用方法描述
     * @return 精确匹配配置的服务入口键
     */
    public static String signature(Method method) {
        return method.getParameters().stream()
                .map(p -> p.getType().getFullyQualifiedNameWithoutTypeParameters() + (p.isVarargs() ? "[]" : ""))
                .collect(Collectors.joining(",", method.getName() + "(", ")"));
    }

    /**
     * 构建接口与实现共用的 Excel 导入预览签名及依赖。
     *
     * @param parentElement 目标编译单元
     * @param isAbstract    是否为接口方法
     * @return 不含方法体的预览方法
     */
    public Method getPreviewExcelImportMethod(CompilationUnit parentElement, boolean isAbstract) {
        FullyQualifiedJavaType importVo = introspectedTable.getTableConfiguration()
                .getVoGeneratorConfiguration().getVoExcelImportWorkflowConfiguration().getExcelImportType();
        FullyQualifiedJavaType previewType =
                new FullyQualifiedJavaType("com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportPreviewVo");
        previewType.addTypeArgument(importVo);
        FullyQualifiedJavaType multipartFile = new FullyQualifiedJavaType("org.springframework.web.multipart.MultipartFile");
        FullyQualifiedJavaType ioException = new FullyQualifiedJavaType("java.io.IOException");
        Method method = new Method("previewExcelImport");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(isAbstract);
        method.setReturnType(previewType);
        Parameter file = new Parameter(multipartFile, "file");
        file.setRemark("待预览的 Excel 文件");
        method.addParameter(file);
        method.addException(ioException);
        method.setExceptionRemark("文件读取或 Excel 解析失败");
        method.setReturnRemark("预览结果VO对象");
        context.getCommentGenerator().addMethodJavaDocLine(method, isAbstract
                ? "解析并校验 Excel 导入文件，生成预览结果"
                : "按配置限额解析 Excel 文件并交由业务服务生成预览");
        parentElement.addImportedType(importVo);
        parentElement.addImportedType(previewType);
        parentElement.addImportedType(multipartFile);
        parentElement.addImportedType(ioException);
        return method;
    }

    /**
     * 构建接口与实现共用的 Excel 导入确认签名及依赖。
     *
     * @param parentElement 目标编译单元
     * @param isAbstract    是否为接口方法
     * @return 不含方法体的确认方法
     */
    public Method getConfirmExcelImportMethod(CompilationUnit parentElement, boolean isAbstract) {
        FullyQualifiedJavaType importVo = introspectedTable.getTableConfiguration()
                .getVoGeneratorConfiguration().getVoExcelImportWorkflowConfiguration().getExcelImportType();
        FullyQualifiedJavaType confirmType =
                new FullyQualifiedJavaType("com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportConfirmVo");
        FullyQualifiedJavaType resultType =
                new FullyQualifiedJavaType("com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportResultVo");
        FullyQualifiedJavaType rowsType = FullyQualifiedJavaType.getNewListInstance();
        rowsType.addTypeArgument(importVo);
        Method method = new Method("confirmExcelImport");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(isAbstract);
        method.setReturnType(resultType);
        Parameter request = new Parameter(confirmType, "request");
        request.setRemark("导入预览确认凭证");
        method.addParameter(request);
        Parameter rows = new Parameter(rowsType, "rows");
        rows.setRemark("确认时重新解析得到的有界导入行");
        method.addParameter(rows);
        method.setReturnRemark("导入结果");
        Parameter fileSha256 = new Parameter(FullyQualifiedJavaType.getStringInstance(), "fileSha256");
        fileSha256.setRemark("重新上传文件的 SHA-256 摘要");
        method.addParameter(fileSha256);
        context.getCommentGenerator().addMethodJavaDocLine(method, isAbstract
                ? "重新解析文件并确认、原子提交 Excel 导入预览"
                : "在事务边界内调用业务策略确认并提交 Excel 导入");
        parentElement.addImportedType(importVo);
        parentElement.addImportedType(confirmType);
        parentElement.addImportedType(resultType);
        parentElement.addImportedType(rowsType);
        return method;
    }

    public Method getSelectByPrimaryKeyMethod(CompilationUnit parentElement, boolean isAbstract, boolean isService) {
        List<Parameter> parameters = introspectedTable.getPrimaryKeyColumns().stream()
                .map(Parameter::new)
                .collect(Collectors.toList());
        Method method = getMethodByType(introspectedTable.getSelectByPrimaryKeyStatementId(),
                isService ? ReturnTypeEnum.SERVICE_RESULT_MODEL : ReturnTypeEnum.MODEL,
                entityType,
                introspectedTable.getRemarks(true) + (isService ? "对象ServiceResult封装" : "对象"),
                parameters,
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "基于主键的查询方法");
        return method;

    }

    /**
     * 创建完整主键锁定查询签名，供 Mapper 与服务接口、实现共用。
     *
     * @param unit       目标编译单元
     * @param isAbstract 是否为抽象方法
     * @param isService  是否包装服务返回值
     * @return 带锁查询契约的方法
     */
    public Method getSelectByPrimaryKeyForUpdateMethod(CompilationUnit unit, boolean isAbstract, boolean isService) {
        return getSelectForUpdateMethod(unit, false, isService, isAbstract);
    }

    /**
     * 创建独立旧版本的主键 CAS 签名，不从实体初始版本推导更新前置条件。
     *
     * @param unit           目标编译单元
     * @param selective      是否保持选择性非空更新语义
     * @param service        是否返回服务结果
     * @param abstractMethod 是否为接口方法
     * @return 完整方法签名
     */
    public Method getCasWriteMethod(CompilationUnit unit, boolean selective, boolean service, boolean abstractMethod) {
        Method method = new Method(selective ? "updateByPrimaryKeySelectiveCas" : "updateByPrimaryKeyCas");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(abstractMethod);
        FullyQualifiedJavaType model = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        unit.addImportedType(model);
        var record = new Parameter(model, "record");
        var version = new Parameter(FullyQualifiedJavaType.getIntegerInstance(), "expectedVersion");
        if (!service) {
            unit.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
            record.addAnnotation("@Param(\"record\")");
            version.addAnnotation("@Param(\"expectedVersion\")");
        }
        method.addParameter(record);
        method.addParameter(version);
        FullyQualifiedJavaType result = FullyQualifiedJavaType.getIntInstance();
        if (service) {
            result = new FullyQualifiedJavaType(SERVICE_RESULT);
            result.addTypeArgument(model);
            unit.addImportedType(result);
        }
        method.setReturnType(result);
        method.addJavaDocLine("/**");
        method.addJavaDocLine(selective ? " * 按独立旧版本更新非空业务字段，空业务选择拒绝。"
                : " * 按独立旧版本完整更新业务字段，保留可空字段的 NULL 语义。");
        method.addJavaDocLine(" * @param record 不被执行层回写的业务记录");
        method.addJavaDocLine(" * @param expectedVersion 请求显式提供的正旧版本，禁止从实体默认值补齐");
        method.addJavaDocLine(" * @return " + (service ? "更新成功的服务结果" : "严格为一的影响行数"));
        method.addJavaDocLine(" * @throws RuntimeException 缺版本、冲突或受管执行条件不满足，异常必须传播以回滚");
        method.addJavaDocLine(" */");
        return method;
    }

    /**
     * 创建条件锁定查询签名，供 Mapper 与服务接口、实现共用。
     *
     * @param unit       目标编译单元
     * @param isAbstract 是否为抽象方法
     * @param isService  是否包装服务返回值
     * @return 带锁查询契约的方法
     */
    public Method getSelectByExampleForUpdateMethod(CompilationUnit unit, boolean isAbstract, boolean isService) {
        return getSelectForUpdateMethod(unit, true, isService, isAbstract);
    }

    private Method getSelectForUpdateMethod(CompilationUnit unit, boolean byExample,
                                            boolean service, boolean abstractMethod) {
        var table = introspectedTable;
        Method method = new Method(byExample ? "selectByExampleForUpdate" : "selectByPrimaryKeyForUpdate");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(abstractMethod);
        FullyQualifiedJavaType result = new FullyQualifiedJavaType(table.getRules().generateRecordWithBlobsClass()
                ? table.getRecordWithBlobsType() : table.getRules().generateBaseRecordClass()
                ? table.getBaseRecordType() : table.getPrimaryKeyType());
        unit.addImportedType(result);
        if (byExample) {
            FullyQualifiedJavaType list = FullyQualifiedJavaType.getNewListInstance();
            list.addTypeArgument(result);
            result = list;
            unit.addImportedType(list);
            var type = new FullyQualifiedJavaType(table.getExampleType());
            unit.addImportedType(type);
            method.addParameter(new Parameter(type, "example"));
        } else if (table.getRules().generatePrimaryKeyClass()) {
            var type = new FullyQualifiedJavaType(table.getPrimaryKeyType());
            unit.addImportedType(type);
            method.addParameter(new Parameter(type, "key"));
        } else {
            for (IntrospectedColumn column : table.getPrimaryKeyColumns()) {
                Parameter parameter = new Parameter(column);
                if (!service && table.getPrimaryKeyColumns().size() > 1) {
                    unit.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
                    parameter.addAnnotation("@Param(\"" + parameter.getName() + "\")");
                }
                unit.addImportedType(parameter.getType());
                method.addParameter(parameter);
            }
        }
        if (service) {
            FullyQualifiedJavaType wrapper = new FullyQualifiedJavaType(SERVICE_RESULT);
            wrapper.addTypeArgument(result);
            result = wrapper;
            unit.addImportedType(wrapper);
        }
        method.setReturnType(result);
        method.addJavaDocLine("/**");
        method.addJavaDocLine(byExample ? " * 根据明确条件锁定并查询记录，禁止全表、去重、动态排序和分页。"
                : " * 根据完整主键锁定并查询记录。");
        method.addJavaDocLine(" * 必须在非只读既有事务中调用；锁持有至事务结束，数据库异常直接向外传播。");
        method.addJavaDocLine(" * 不经过查询缓存；可见版本取决于数据库和事务隔离级别。");
        for (Parameter parameter : method.getParameters()) {
            method.addJavaDocLine(" * @param " + parameter.getName() + (byExample ? " 非空白名单查询条件" : " 完整主键值"));
        }
        method.addJavaDocLine(" * @return " + (service ? "锁定查询结果的服务包装" : "锁定查询的实体" + (byExample ? "列表" : "，不存在时为空")));
        method.addJavaDocLine(" * @throws IllegalStateException 未开启事务、只读事务或存在分页上下文");
        method.addJavaDocLine(" * @throws IllegalArgumentException 查询条件不符合锁定查询约束");
        method.addJavaDocLine(" */");
        return method;
    }

    public Method getDeleteByExampleMethod(CompilationUnit parentElement, boolean isAbstract) {

        Method method = getMethodByType(introspectedTable.getDeleteByExampleStatementId(),
                ReturnTypeEnum.SERVICE_RESULT_MODEL,
                FullyQualifiedJavaType.getIntegerInstance(),
                "影响行数Service返回封装对象",
                Collections.singletonList(new Parameter(exampleType, "example").setRemark("检索条件对象")),
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "基于example的批量删除方法");
        }
        return method;
    }


    public Method getCleanupInvalidRecordsMethod(CompilationUnit parentElement, boolean isAbstract) {
        Method method = getMethodByType("cleanupInvalidRecords",
                ReturnTypeEnum.SERVICE_RESULT_MODEL,
                FullyQualifiedJavaType.getIntegerInstance(),
                "清理删除影响行数Service返回封装对象",
                new ArrayList<>(0),
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "清理无效数据（流程状态为4）的方法", "注意：如果包含关联表，如主子表数据，请重写该方法，以便于增加清理关联表的代码");
        }
        return method;
    }

    public Method getDeleteByPrimaryKeyMethod(CompilationUnit parentElement, boolean isAbstract) {
        List<Parameter> parameters = introspectedTable.getPrimaryKeyColumns().stream()
                .map(Parameter::new)
                .collect(Collectors.toList());
        Method method = getMethodByType(introspectedTable.getDeleteByPrimaryKeyStatementId(),
                ReturnTypeEnum.SERVICE_RESULT_MODEL,
                FullyQualifiedJavaType.getIntegerInstance(),
                "影响行数Service返回封装对象",
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "基于主键的删除方法");
        }
        return method;
    }

    public Method getUpdateByExample(CompilationUnit parentElement, boolean isAbstract, boolean isSelective) {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(entityType, "record").setRemark("数据对象"));
        parameters.add(new Parameter(exampleType, "example").setRemark("检索条件对象"));
        Method method = getMethodByType(
                isSelective ? introspectedTable.getUpdateByExampleSelectiveStatementId() : introspectedTable.getUpdateByExampleStatementId(),
                ReturnTypeEnum.SERVICE_RESULT_MODEL,
                FullyQualifiedJavaType.getIntegerInstance(),
                "影响行数Service返回封装对象",
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "基于example的单组数据批量更新的方法");
        }
        return method;
    }

    public Method getUpdateByPrimaryKey(CompilationUnit parentElement, boolean isAbstract, boolean isSelective, boolean isService) {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(entityType, "record").setRemark("主键及其它数据对象"));
        Method method = getMethodByType(
                isSelective ? introspectedTable.getUpdateByPrimaryKeySelectiveStatementId() : introspectedTable.getUpdateByPrimaryKeyStatementId(),
                isService ? ReturnTypeEnum.SERVICE_RESULT_MODEL : ReturnTypeEnum.MODEL,
                isService ? entityType : FullyQualifiedJavaType.getIntInstance(),
                introspectedTable.getRemarks(true) + (isService ? "对象ServiceResult封装" : "成功更新的记录数"),
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "基于主键的更新方法");
        }
        return method;
    }

    public Method getUpdateBySql(CompilationUnit parentElement, boolean isAbstract) {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(new FullyQualifiedJavaType("com.vgosoft.mybatis.sqlbuilder.UpdateSqlBuilder"), "updateSqlBuilder").setRemark("SQL语句构造对象"));
        Method method = getMethodByType(
                introspectedTable.getUpdateBySqlStatementId(),
                ReturnTypeEnum.SERVICE_RESULT_MODEL,
                FullyQualifiedJavaType.getIntegerInstance(),
                "影响行数Service返回封装对象",
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "基于sql构造器的更新方法");
        }
        return method;
    }

    public Method getInsertMethod(CompilationUnit parentElement, boolean isAbstract, boolean isSelective, boolean isService) {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(entityType, "record").setRemark("待插入的数据对象"));
        Method method = getMethodByType(
                isSelective ? introspectedTable.getInsertSelectiveStatementId() : introspectedTable.getInsertStatementId(),
                isService ? ReturnTypeEnum.SERVICE_RESULT_MODEL : ReturnTypeEnum.MODEL,
                isService ? entityType : FullyQualifiedJavaType.getIntInstance(),
                isService ? introspectedTable.getRemarks(true) + "对象ServiceResult封装" : "成功插入的记录数",
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "单条记录新增的方法");
        }
        return method;
    }

    public Method getInsertBatchMethod(CompilationUnit parentElement, boolean isAbstract, boolean isService) {
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(entityType);
        List<Parameter> parameters = new ArrayList<>();
        Parameter param = new Parameter(listInstance, entityType.getShortNameFirstLowCase() + "s");
        param.setRemark("待新增的数据对象列表");
        parameters.add(param);
        Method method = getMethodByType(introspectedTable.getInsertBatchStatementId(),
                isService ? ReturnTypeEnum.SERVICE_RESULT_LIST : ReturnTypeEnum.MODEL,
                isService ? entityType : FullyQualifiedJavaType.getIntInstance(),
                isService ? introspectedTable.getRemarks(true) + "对象列表ServiceResult封装" : "成功插入的记录数",
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "批量插入的方法");
        }
        return method;
    }

    public Method getUpdateBatchMethod(CompilationUnit parentElement,
                                       boolean isAbstract, boolean isService) {
        List<Parameter> parameters = new ArrayList<>();
        FullyQualifiedJavaType parameterType = FullyQualifiedJavaType.getNewListInstance();
        parameterType.addTypeArgument(entityType);
        Parameter param = new Parameter(parameterType, entityType.getShortNameFirstLowCase() + "s");
        param.setRemark("待更新的数据对象列表,该方法为选择性更新，如果对象属性为null则不更新且不返回。如果需要返回更新后的对象，请使用selectByExample方法再次查询");
        parameters.add(param);
        Method method = getMethodByType(introspectedTable.getUpdateBatchStatementId(),
                isService ? ReturnTypeEnum.SERVICE_RESULT_LIST : ReturnTypeEnum.MODEL,
                isService ? entityType : FullyQualifiedJavaType.getIntInstance(),
                introspectedTable.getRemarks(true) + (isService ? "对象列表ServiceResult封装" : "成功更新的记录数"),
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "批量更新方法");
        }
        return method;
    }

    /**
     * 生成原子递增方法。
     */
    public Method getAtomicIncrementMethod(CompilationUnit parentElement, boolean isAbstract) {
        List<Parameter> parameters = introspectedTable.getPrimaryKeyColumns().stream()
                .map(Parameter::new)
                .collect(Collectors.toList());
        Method method = getMethodByType(
                introspectedTable.getTableConfiguration().getAtomicIncrementStatementId(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                "成功更新的记录数",
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "按主键执行原子递增的方法");
        }
        return method;
    }

    public Method getUpdateDeleteFlagMethod(CompilationUnit parentElement, boolean isAbstract, boolean isService) {
        List<Parameter> parameters = new ArrayList<>();
        Parameter record = new Parameter(entityType, entityType.getShortNameFirstLowCase());
        record.setRemark("设置删除状态的数据对象");
        parameters.add(record);
        Parameter deleteFlag = new Parameter(FullyQualifiedJavaType.getIntInstance(), "deleteFlag");
        deleteFlag.setRemark("逻辑删除标识（1-删除，0-正常）");
        parameters.add(deleteFlag);
        Method method = getMethodByType(introspectedTable.getUpdateDeleteFlagStatementId(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntegerInstance(),
                introspectedTable.getRemarks(true) + "int 清理无效数据的影响行数(1-成功，0-失败)",
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "设置数据逻辑删除的状态（1-删除，0-正常）");
        }
        return method;
    }

    public Method getSelectByMultiStringIdsMethod(CompilationUnit parentElement, boolean isAbstract) {
        List<Parameter> parameters = new ArrayList<>();
        Parameter ids = new Parameter(FullyQualifiedJavaType.getStringInstance(), "ids");
        ids.setRemark("指定的数据范围的标识，多个以逗号分隔");
        ids.addAnnotation("@Nullable");
        parameters.add(ids);
        Method method = getMethodByType(introspectedTable.getSelectByPrimaryKeysStatementId(),
                ReturnTypeEnum.LIST,
                entityType,
                "符合标识列表的全数据列表",
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "根据给定的id列表，获取返回全数据对象列表的方法");
        }
        parentElement.addImportedType(new FullyQualifiedJavaType(ANNOTATION_NULLABLE));
        return method;
    }

    public Method getSelectWithChildrenCountMethod(CompilationUnit parentElement,
                                                   boolean isAbstract, boolean isService) {
        List<Parameter> parameters = new ArrayList<>();
        Parameter example = new Parameter(exampleType, "example");
        example.setRemark("查询条件example对象");
        parameters.add(example);
        Method method = getMethodByType(introspectedTable.getSelectByExampleWithChildrenCountStatementId(),
                isService ? ReturnTypeEnum.SERVICE_RESULT_LIST : ReturnTypeEnum.LIST,
                entityType,
                introspectedTable.getRemarks(true) + "对象列表",
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "带子集合数量的查询方法，该查询方法将执行所有children的count方法。");
        }
        return method;
    }

    public Method getInsertOrUpdateMethod(CompilationUnit parentElement, boolean isAbstract, boolean isService) {
        List<Parameter> parameters = new ArrayList<>();
        Parameter record = new Parameter(entityType, entityType.getShortNameFirstLowCase());
        record.setRemark("待新增或更新的数据对象");
        parameters.add(record);
        Method method = getMethodByType(introspectedTable.getInsertOrUpdateStatementId(),
                isService ? ReturnTypeEnum.SERVICE_RESULT_MODEL : ReturnTypeEnum.MODEL,
                isService ? entityType : FullyQualifiedJavaType.getIntInstance(),
                introspectedTable.getRemarks(true) + (isService ? "对象ServiceResult封装" : "成功插入或更新的记录数"),
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "插入数据时如果主键重复则更新数据。");
        }
        return method;
    }

    public Method getSelectWithRelationMethod(CompilationUnit parentElement, boolean isAbstract) {
        List<Parameter> parameters = new ArrayList<>();
        Parameter example = new Parameter(exampleType, "example");
        example.setRemark("查询条件example对象");
        parameters.add(example);
        Method method = getMethodByType(introspectedTable.getSelectByExampleWithRelationStatementId(),
                ReturnTypeEnum.LIST,
                entityType,
                introspectedTable.getRemarks(true) + "对象列表",
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "带所有子查询（集）的查询方法，该查询方法将执行所有子查询，大量数据返回时慎用。", "如果无需返回子查询请使用{@link #selectByExample}方法。");
        }
        return method;
    }

    public Method getSelectByColumnMethod(FullyQualifiedJavaType entityType,
                                          CompilationUnit parentElement,
                                          final SelectByColumnGeneratorConfiguration config,
                                          boolean isAbstract) {
        boolean isSelectBase = JavaBeansUtil.isSelectBaseByPrimaryKeyMethod(config.getMethodName());
        List<Parameter> parameters = config.getColumns().stream().map(column -> {
            if (config.getParameterList()) {
                FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
                listInstance.addTypeArgument(column.getFullyQualifiedJavaType());
                Parameter parameter = new Parameter(listInstance, column.getJavaProperty() + "s");
                parameter.setRemark(column.getRemarks(true));
                return parameter;
            } else {
                Parameter parameter = new Parameter(column.getFullyQualifiedJavaType(), column.getJavaProperty());
                parameter.setRemark(column.getRemarks(true));
                return parameter;
            }
        }).collect(Collectors.toList());

        Method method = getMethodByType(config.getMethodName(),
                isSelectBase ? ReturnTypeEnum.MODEL : ReturnTypeEnum.LIST,
                config.isReturnPrimaryKey() ? FullyQualifiedJavaType.getStringInstance() : entityType,
                introspectedTable.getRemarks(true) + (config.isReturnPrimaryKey() ? "唯一标识列表" : "对象列表"),
                parameters,
                isAbstract,
                parentElement);
        String collect = config.getColumns().stream().map(column -> column.getActualColumnName() + "(" + column.getRemarks(true) + ")").collect(Collectors.joining(","));
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "基于[" + collect + "]的查询方法。" + (config.getColumns().size() == 1 ? "该方法常用于为其它方法提供子查询。" : ""));
        }
        return method;
    }

    public Method getStatisticsTotalMethod(CompilationUnit parentElement, StatisticsDateDimGeneratorConfiguration configuration, boolean isAbstract, boolean isService) {
        Method method = new Method(configuration.getMethodName());
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(new FullyQualifiedJavaType(StatTypeEnum.TOTAL.dtoClass()));
        parentElement.addImportedType(new FullyQualifiedJavaType(StatTypeEnum.TOTAL.dtoClass()));
        method.setReturnRemark("按时间维度统计总数，返回的列表包含时间维度和对应的总数");
        method.setReturnType(listInstance);
        Parameter startDate = new Parameter(new FullyQualifiedJavaType("java.lang.String"), "startDate");
        startDate.setRemark("开始日期，格式yyyy-MM-dd");
        Parameter endDate = new Parameter(new FullyQualifiedJavaType("java.lang.String"), "endDate");
        endDate.setRemark("结束日期，格式yyyy-MM-dd");
        Parameter minDimension = new Parameter(new FullyQualifiedJavaType("java.lang.String"), "minDimension");
        minDimension.setRemark("最小时间维度（day、week、month、quarter、year），默认为month");
        if (!isService) {
            startDate.addAnnotation("@Param(\"startDate\")");
            endDate.addAnnotation("@Param(\"endDate\")");
            minDimension.addAnnotation("@Param(\"minDimension\")");
            parentElement.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        }
        method.addParameter(startDate);
        method.addParameter(endDate);
        method.addParameter(minDimension);
        method.setAbstract(isAbstract);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "按时间维度统计总数");
        }
        return method;
    }

    public Method getStatisticsCustomMethod(CompilationUnit parentElement, StatisticsCustomGeneratorConfiguration configuration, boolean isAbstract, boolean isService) {
        Method method = new Method(configuration.getMethodName());
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(new FullyQualifiedJavaType("java.util.Map<String, Object>"));
        parentElement.addImportedType(new FullyQualifiedJavaType("java.util.Map"));
        method.setReturnRemark("自定义聚合统计方法，返回的Map列表包含时间维度和对应的总数");
        method.setReturnType(listInstance);
        Parameter startDate = new Parameter(new FullyQualifiedJavaType("java.lang.String"), "startDate");
        startDate.setRemark("开始日期，格式yyyy-MM-dd");
        Parameter endDate = new Parameter(new FullyQualifiedJavaType("java.lang.String"), "endDate");
        endDate.setRemark("结束日期，格式yyyy-MM-dd");
        Parameter minDimension = new Parameter(new FullyQualifiedJavaType("java.lang.String"), "minDimension");
        minDimension.setRemark("最小时间维度（day、week、month、quarter、year），默认为month");
        if (!isService) {
            startDate.addAnnotation("@Param(\"startDate\")");
            endDate.addAnnotation("@Param(\"endDate\")");
            minDimension.addAnnotation("@Param(\"minDimension\")");
            parentElement.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        }
        minDimension.addAnnotation("@Nullable");
        parentElement.addImportedType(new FullyQualifiedJavaType("jakarta.annotation.Nullable"));
        method.addParameter(startDate);
        method.addParameter(endDate);
        method.addParameter(minDimension);
        method.setAbstract(isAbstract);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "自定义聚合统计方法");
        }
        return method;
    }

    public Method getDeleteByColumnMethod(CompilationUnit parentElement,
                                          SelectByColumnGeneratorConfiguration config,
                                          boolean isAbstract) {

        List<Parameter> parameters = config.getColumns().stream().map(column -> {
            if (config.getParameterList()) {
                FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
                listInstance.addTypeArgument(column.getFullyQualifiedJavaType());
                Parameter parameter = new Parameter(listInstance, column.getJavaProperty() + "s");
                parameter.setRemark(column.getRemarks(true));
                return parameter;
            } else {
                Parameter parameter = new Parameter(column.getFullyQualifiedJavaType(), column.getJavaProperty());
                parameter.setRemark(column.getRemarks(true));
                return parameter;
            }
        }).collect(Collectors.toList());

        Method method = getMethodByType(config.getDeleteMethodName(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                "成功删除的行数",
                parameters,
                isAbstract,
                parentElement);
        String collect = config.getColumns().stream().map(column -> column.getActualColumnName() + "(" + column.getRemarks(true) + ")").collect(Collectors.joining(","));
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "基于" + collect + "的删除方法。");
        }
        return method;
    }

    public Method getSelectByTableMethod(FullyQualifiedJavaType entityType,
                                         CompilationUnit parentElement,
                                         SelectByTableGeneratorConfiguration config,
                                         boolean isAbstract) {
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
        List<Parameter> parameters = new ArrayList<>();
        Parameter p1 = new Parameter("list".equals(config.getParameterType()) ? listInstance : FullyQualifiedJavaType.getStringInstance(),
                config.getParameterName() + ("list".equals(config.getParameterType()) ? "s" : ""));
        p1.setRemark("中间表中来自其他表的查询键值,字段名：" + config.getOtherPrimaryKeyColumn());
        parameters.add(p1);
        Method method = getMethodByType(config.getMethodName(),
                ReturnTypeEnum.LIST,
                config.isReturnPrimaryKey() ? FullyQualifiedJavaType.getStringInstance() : entityType,
                "相关数据库数据" + (config.isReturnPrimaryKey() ? "主键列表" : "对象列表"),
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "基于中间表（表名：" + config.getTableName() + "）的查询");
        }
        return method;
    }

    public Method getSelectBySqlMethodMethod(CompilationUnit parentElement,
                                             SelectBySqlMethodGeneratorConfiguration config,
                                             boolean isAbstract, boolean isService) {
        List<Parameter> parameters = new ArrayList<>();
        Parameter p1 = new Parameter(config.getParentIdColumn().getFullyQualifiedJavaType(), config.getParentIdColumn().getJavaProperty());
        p1.setRemark("基于sql函数[" + config.getSqlMethod() + "]的查询");
        parameters.add(p1);
        Method method = getMethodByType(config.getMethodName(),
                isService ? ReturnTypeEnum.SERVICE_RESULT_LIST : ReturnTypeEnum.LIST,
                entityType,
                introspectedTable.getRemarks(true) + (isService ? "对象列表ServiceResult封装" : "对象列表"),
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "基于sql函数[" + config.getSqlMethod() + "]的查询");
        }
        return method;
    }

    public Method getSelectByKeysDictMethod(CompilationUnit parentElement, VoCacheGeneratorConfiguration config,
                                            boolean isAbstract, boolean isService) {
        boolean isCache = introspectedTable.getRules().isGenerateCachePo();
        List<Parameter> parameters;
        if (isService) {
            parameters = getSelectByKeysDictMethodParameters();
        } else {
            FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType("com.vgosoft.core.pojo.parameter.SelDictByKeysParam");
            Parameter param = new Parameter(fullyQualifiedJavaType, "param");
            param.setRemark("SelDictByKeysParam 查询参数对象,包含keys列表、type列表和排除id列表");
            parameters = Collections.singletonList(param);
        }
        Method method = getMethodByType(
                introspectedTable.getSelectByKeysDictStatementId(),
                isCache ? (ReturnTypeEnum.LIST) : ReturnTypeEnum.MODEL,
                isService ? config.getFullyQualifiedJavaType() : entityType,
                introspectedTable.getRemarks(true) + (isService ? "缓存对象列表" : "实体对象列表"),
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "查询" + introspectedTable.getRemarks(true) + "缓存数据，将返回对象列表",
                    "对dao的方法参数进行对象封装，以便于处理为null参数");
        }
        return method;
    }

    public Method getSelectByKeysDictForPrefetchMethod(CompilationUnit parentElement,
                                                       VoCacheGeneratorConfiguration config,
                                                       boolean isAbstract) {
        List<Parameter> parameters = List.of(
                new Parameter(FullyQualifiedJavaType.getOptionalFullyQualifiedJavaType(
                        FullyQualifiedJavaType.getStringInstance()), "types").setRemark("可选字典类型"),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "keys").setRemark("逗号分隔的字典键"));
        Method method = getMethodByType(
                "selectByKeysDictForPrefetch",
                ReturnTypeEnum.LIST,
                config.getFullyQualifiedJavaType(),
                "供请求级字典预取使用的缓存模型列表",
                parameters,
                isAbstract,
                parentElement);
        List<String> orderProperties = DictCacheOrderResolver.resolve(introspectedTable).stream()
                .map(IntrospectedColumn::getJavaProperty)
                .toList();
        String keyProperty = DictCacheOrderResolver.requireColumn(introspectedTable, config.getKeyColumn(), "keyColumn")
                .getJavaProperty();
        String annotation = "@DictPrefetchProvider(keyProperty = \"" + keyProperty
                + "\", orderProperties = {" + orderProperties.stream()
                .map(property -> "\"" + property + "\"")
                .collect(Collectors.joining(", ")) + "}, uniqueOrderProperties = {"
                + introspectedTable.getPrimaryKeyColumns().stream()
                .map(column -> "\"" + column.getJavaProperty() + "\"")
                .collect(Collectors.joining(", ")) + "})";
        method.addAnnotation(annotation);
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" * 查询请求级字典预取所需的数据，不读取或写入 Spring 缓存。");
        method.addJavaDocLine(" * @param types 可选字典类型");
        method.addJavaDocLine(" * @param keys 逗号分隔的字典键");
        method.addJavaDocLine(" * @return 按配置排序的缓存模型列表");
        method.addJavaDocLine(" */");
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.annotation.DictPrefetchProvider"));
        return method;
    }

    /** 构建两个字典公开入口共用的私有查询签名，不附加缓存或预取注解。 */
    public Method getQueryByKeysDictMethod(CompilationUnit parentElement, VoCacheGeneratorConfiguration config) {
        Method method = getMethodByType("queryByKeysDict", ReturnTypeEnum.LIST,
                config.getFullyQualifiedJavaType(), "缓存模型列表",
                List.of(new Parameter(FullyQualifiedJavaType.getOptionalFullyQualifiedJavaType(
                                FullyQualifiedJavaType.getStringInstance()), "types").setRemark("可选字典类型"),
                        new Parameter(FullyQualifiedJavaType.getStringInstance(), "keys").setRemark("字典键")),
                false, parentElement);
        method.setVisibility(JavaVisibility.PRIVATE);
        return method;
    }

    public Method getSelectByKeysWithAllParentMethod(CompilationUnit parentElement,
                                                     boolean isAbstract, boolean isService) {
        List<Parameter> parameters = new ArrayList<>();
        Parameter p1 = new Parameter(new FullyQualifiedJavaType("List<String>"), "ids");
        p1.setRemark("数据标识列表");
        parameters.add(p1);
        Method method = getMethodByType(introspectedTable.getSelectByKeysWithAllParent(),
                isService ? ReturnTypeEnum.SERVICE_RESULT_LIST : ReturnTypeEnum.LIST,
                entityType,
                introspectedTable.getRemarks(true) + (isService ? "对象列表ServiceResult封装" : "对象列表"),
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "获取指定ID的数据及其所有父级，递归深度限制为10，防止性能问题");
        }
        return method;
    }

    public Method getSelectByKeysWithChildrenMethod(CompilationUnit parentElement,
                                                    boolean isAbstract, boolean isService) {
        List<Parameter> parameters = new ArrayList<>();
        Parameter p1 = new Parameter(new FullyQualifiedJavaType("List<String>"), "ids");
        p1.setRemark("数据标识列表");
        parameters.add(p1);
        Method method = getMethodByType(introspectedTable.getSelectByKeysWithAllChildren(),
                isService ? ReturnTypeEnum.SERVICE_RESULT_LIST : ReturnTypeEnum.LIST,
                entityType,
                introspectedTable.getRemarks(true) + (isService ? "对象列表ServiceResult封装" : "对象列表"),
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "获取指定ID的数据及其所有后代，递归深度限制为10，防止性能问题");
        }
        return method;
    }

    public List<IntrospectedColumn> getSelectDictParameterColumns(VoCacheGeneratorConfiguration config, AbstractIntrospectedTable abstractIntrospectedTable) {
        if (abstractIntrospectedTable.getRules().isGenerateCachePoWithMultiKey()) {
            return Stream.of(config.getTypeColumn(), config.getKeyColumn())
                    .map(n -> abstractIntrospectedTable.getColumn(n).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            return abstractIntrospectedTable.getPrimaryKeyColumns();
        }
    }

    public List<Parameter> getDictControllerMethodParameters(VoCacheGeneratorConfiguration voCacheGeneratorConfiguration) {
        //方法参数
        final List<Parameter> parameters = new ArrayList<>();
        introspectedTable.getColumn(voCacheGeneratorConfiguration.getKeyColumn()).ifPresent(introspectedColumn -> {
            Parameter keys = new Parameter(introspectedColumn.getFullyQualifiedJavaType(), "keys");
            keys.setRemark(introspectedColumn.getRemarks(false));
            keys.addAnnotation("@RequestParam");
            parameters.add(keys);
        });
        Optional<IntrospectedColumn> typeColumn = introspectedTable.getColumn(voCacheGeneratorConfiguration.getTypeColumn());
        if (typeColumn.isPresent()) {
            typeColumn.ifPresent(introspectedColumn -> {
                Parameter types = new Parameter(introspectedColumn.getFullyQualifiedJavaType(), "types");
                types.setRemark(introspectedColumn.getRemarks(false));
                types.addAnnotation("@RequestParam (required = false)");
                parameters.add(types);
            });
        } else {
            Parameter types = new Parameter(FullyQualifiedJavaType.getStringInstance(), "types");
            types.setRemark("类型参数占位符");
            types.addAnnotation("@RequestParam (required = false)");
            parameters.add(types);
        }
        Parameter excludeIds = new Parameter(FullyQualifiedJavaType.getStringInstance(), "eids");
        excludeIds.setRemark("排除的id列表");
        excludeIds.addAnnotation("@RequestParam (required = false)");
        parameters.add(excludeIds);
        return parameters;
    }

    public List<Parameter> getSelectByKeysDictMethodParameters() {
        //方法参数
        final List<Parameter> parameters = new ArrayList<>();
        VoCacheGeneratorConfiguration voCacheGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();
        Optional<IntrospectedColumn> typeColumn = introspectedTable.getColumn(voCacheGeneratorConfiguration.getTypeColumn());
        if (typeColumn.isPresent()) {
            typeColumn.ifPresent(introspectedColumn -> {
                Parameter types = new Parameter(FullyQualifiedJavaType.getOptionalFullyQualifiedJavaType(introspectedColumn.getFullyQualifiedJavaType()), "types");
                types.setRemark(introspectedColumn.getRemarks(false));
                parameters.add(types);
            });
        } else {
            Parameter types = new Parameter(FullyQualifiedJavaType.getOptionalFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance()), "types");
            types.setRemark("类型参数占位符");
            parameters.add(types);
        }
        introspectedTable.getColumn(voCacheGeneratorConfiguration.getKeyColumn()).ifPresent(introspectedColumn -> {
            Parameter keys = new Parameter(introspectedColumn.getFullyQualifiedJavaType(), "keys");
            keys.setRemark(introspectedColumn.getRemarks(false));
            parameters.add(keys);
        });
        return parameters;
    }

    public Method getSplitUnionByTableMethod(CompilationUnit parentElement,
                                             SelectByTableGeneratorConfiguration configuration,
                                             boolean isAbstract, boolean isInsert) {
        List<Parameter> parameters = new ArrayList<>();
        Parameter p1 = new Parameter(configuration.getThisColumn().getFullyQualifiedJavaType(), configuration.getThisColumn().getJavaProperty());
        p1.setRemark("当前对象的键值");
        parameters.add(p1);
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(configuration.getOtherColumn().getFullyQualifiedJavaType());
        Parameter p2 = new Parameter(listInstance, configuration.getOtherColumn().getJavaProperty() + "s");
        p2.setRemark(isInsert ? "待关联数据的主键列表" : "待解除关联数据的主键列表");
        parameters.add(p2);
        Method method = getMethodByType(
                isInsert ? configuration.getUnionMethodName() : configuration.getSplitMethodName(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                "影响记录条数",
                parameters,
                isAbstract,
                parentElement);
        if (isAbstract) {
            context.getCommentGenerator().addMethodJavaDocLine(method, "在关系中间表（表名：" + configuration.getTableName() + "）中" + (isInsert ? "添加" : "删除") + "关联数据记录");
        }
        return method;
    }

//    /**
//     * 根据给定信息构造类方法
//     *
//     * @param methodName         要构建的方法名称
//     * @param returnType         方法返回类型。l-list、m-model、r-ServiceResult、sl-ServiceResult<List>、sm-ServiceResult<model>
//     * @param returnTypeArgument 返回的参数类型T。
//     *                           returnType为l：List<T>、m：T、r：ServiceResult<T>、sl:ServiceResult<List<T>>、sl:ServiceResult<T>
//     * @param parameterType      方法的参数类型
//     * @param parameterName      方法的参数名称
//     * @param isAbstract         是否为抽象方法
//     * @param parameterRemark             参数的文字说明
//     * @param parentElement      父元素
//     */
//    public Method getMethodByType(String methodName,
//                                     ReturnTypeEnum returnType,
//                                     FullyQualifiedJavaType returnTypeArgument,
//                                     String returnRemark,
//                                     FullyQualifiedJavaType parameterType,
//                                     String parameterName,
//                                     String parameterRemark,
//                                     boolean isAbstract,
//                                     CompilationUnit parentElement) {
//        Parameter parameter = new Parameter(parameterType, parameterName).setRemark(parameterRemark);
//        return getMethodByType(methodName
//                ,returnType
//                ,returnTypeArgument
//                ,returnRemark
//                ,Collections.singletonList(parameter)
//                ,isAbstract
//                ,parentElement);
//    }

    public Method getMethodByType(String methodName,
                                  ReturnTypeEnum returnType,
                                  FullyQualifiedJavaType returnTypeArgument,
                                  String returnRemark,
                                  List<Parameter> parameters,
                                  boolean isAbstract,
                                  CompilationUnit parentElement) {
        Method method = new Method(methodName);
        method.setReturnRemark(returnRemark);
        if (isAbstract) {
            method.setAbstract(true);
        } else {
            method.setVisibility(JavaVisibility.PUBLIC);
        }
        method.getParameters().addAll(parameters);
        parameters.forEach(p -> parentElement.addImportedType(p.getType()));
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType serviceResult = new FullyQualifiedJavaType(SERVICE_RESULT);
        FullyQualifiedJavaType responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);
        switch (ReturnTypeEnum.ofCode(returnType.code())) {
            case LIST:
                listType.addTypeArgument(returnTypeArgument);
                method.setReturnType(listType);
                parentElement.addImportedType(listType);
                parentElement.addImportedType(returnTypeArgument);
                break;
            case SERVICE_RESULT_MODEL:
                serviceResult.addTypeArgument(returnTypeArgument);
                method.setReturnType(serviceResult);
                parentElement.addImportedType(serviceResult);
                parentElement.addImportedType(returnTypeArgument);
                break;
            case SERVICE_RESULT_LIST:
                listType.addTypeArgument(returnTypeArgument);
                serviceResult.addTypeArgument(listType);
                method.setReturnType(serviceResult);
                parentElement.addImportedType(serviceResult);
                parentElement.addImportedType(returnTypeArgument);
                parentElement.addImportedType(listType);
                break;
            case RESPONSE_RESULT_LIST:
                listType.addTypeArgument(returnTypeArgument);
                responseResult.addTypeArgument(listType);
                method.setReturnType(responseResult);
                parentElement.addImportedType(responseResult);
                parentElement.addImportedType(returnTypeArgument);
                parentElement.addImportedType(listType);
                break;
            case RESPONSE_RESULT_MODEL:
                responseResult.addTypeArgument(returnTypeArgument);
                method.setReturnType(responseResult);
                parentElement.addImportedType(responseResult);
                parentElement.addImportedType(returnTypeArgument);
                break;
            case VOID:
                break;
            default:
                method.setReturnType(returnTypeArgument);
                parentElement.addImportedType(returnTypeArgument);
                break;
        }
        return method;
    }

}
