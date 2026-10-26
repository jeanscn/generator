package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.DisabledServiceMethodConfiguration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 解析一次有效停用策略，供服务元素、测试生成及最终插件输出校验共同使用。
 *
 * @author vgosoft-agent
 */
public final class DisabledServiceEntries {
    private static final String ATTRIBUTE = DisabledServiceEntries.class.getName();
    private final AbstractIntrospectedTable table;
    private final Map<String, Entry> entries = new LinkedHashMap<>();
    private final List<Method> testContract = new ArrayList<>();
    private String testBaseType;

    /**
     * 保存精确入口、拒绝签名及显式内部原语的生成快照。
     *
     * @author vgosoft-agent
     */
    private record Entry(DisabledServiceMethodConfiguration configuration, Method rejection, Method internal) { }

    /**
     * 保存插件前的接口继承与显式入口签名契约。
     *
     * @author vgosoft-agent
     */
    private record InterfaceContract(String type, List<String> parents, List<Method> methods) { }

    private DisabledServiceEntries(AbstractIntrospectedTable table) {
        this.table = table;
    }

    public static List<DisabledServiceMethodConfiguration> configurations(AbstractIntrospectedTable table) {
        var config = table.getTableConfiguration().getJavaServiceGeneratorConfiguration();
        return config == null ? List.of() : config.getDisabledMethods();
    }

    public static DisabledServiceEntries effective(AbstractIntrospectedTable table) {
        var result = (DisabledServiceEntries) table.getAttribute(ATTRIBUTE);
        if (result == null && !configurations(table).isEmpty()) {
            throw failure(table, "未生成必需 Service 实现，无法解析有效停用策略");
        }
        return result;
    }

    public static boolean isDisabled(AbstractIntrospectedTable table, Method method) {
        return configurations(table).stream().anyMatch(c -> c.signature().equals(ServiceMethods.signature(method)));
    }

    public static boolean isInternal(AbstractIntrospectedTable table, Method method) {
        return configurations(table).stream().anyMatch(c -> c.internalMethod() != null
                && c.internalMethod().equals(method.getName())
                && ServiceMethods.signature(method).equals(c.internalMethod()
                    + c.signature().substring(c.signature().indexOf('('))));
    }

    public static void captureInterface(AbstractIntrospectedTable table, Interface service) {
        if (configurations(table).isEmpty()) { return; }
        table.setAttribute(ATTRIBUTE + ".interface", new InterfaceContract(
                service.getType().getFullyQualifiedName(), service.getSuperInterfaceTypes().stream()
                    .map(FullyQualifiedJavaType::getFullyQualifiedName).sorted().toList(),
                service.getMethods().stream().filter(m -> isDisabled(table, m))
                    .map(DisabledServiceEntries::snapshot).toList()));
    }

    /**
     * 基于共用签名和真实父类补齐拒绝覆盖，不依赖本类是否已有生成方法。
     *
     * @param table 当前内省表
     * @param service 目标服务实现
     * @param methods 共用签名工厂
     */
    public static void apply(AbstractIntrospectedTable table, TopLevelClass service, ServiceMethods methods) {
        if (configurations(table).isEmpty()) { return; }
        var policy = new DisabledServiceEntries(table);
        Map<String, Method> candidates = new LinkedHashMap<>();
        for (var inherited : methods.getInheritedServiceMethods(service.getSuperClass().orElseThrow(), service)) {
            candidates.put(ServiceMethods.signature(inherited), inherited);
        }
        Set<String> local = new HashSet<>();
        for (var method : service.getMethods()) {
            if (!method.isConstructor()) {
                String signature = ServiceMethods.signature(method);
                if (!local.add(signature)) { throw failure(table, "方法签名歧义：" + signature); }
                candidates.put(signature, method);
            }
        }
        Set<String> reservedNames = candidates.values().stream().map(Method::getName)
                .collect(Collectors.toCollection(HashSet::new));
        reservedNames.addAll(methods.getInheritedServiceMethodNames(service.getSuperClass().orElseThrow()));
        for (var config : configurations(table)) {
            Method original = candidates.get(config.signature());
            if (original == null) {
                String name = config.signature().substring(0, config.signature().indexOf('('));
                var available = candidates.values().stream().filter(m -> m.getName().equals(name))
                        .map(ServiceMethods::signature).sorted().toList();
                throw failure(table, "未知或不存在的完整方法签名：" + config.signature()
                        + "；实际同名签名：" + available + "；实际 Example 类型：" + table.getExampleType()
                        + "。直接调用请核对 ConfigurationParser.customConfig 与实际生成配置是否一致");
            }
            if (original.getVisibility() != JavaVisibility.PUBLIC || original.isFinal()
                    || original.isStatic() || !original.getTypeParameters().isEmpty()) {
                throw failure(table, "入口不可安全覆盖：" + config.signature());
            }
            Method internal = null;
            if (config.internalMethod() != null) {
                if (!reservedNames.add(config.internalMethod())) {
                    throw failure(table, "internalMethod 名称冲突：" + config.internalMethod());
                }
                if (original.getBodyLines().isEmpty()
                        || original.getBodyLines().contains("throw new IllegalStateException(\"ENTRY_DISABLED\");")) {
                    throw failure(table, "internalMethod 必须对应已有且未被 CAS 禁用的生成原语：" + config.signature());
                }
                internal = new Method(original);
                internal.setName(config.internalMethod());
                internal.setVisibility(JavaVisibility.PROTECTED);
                internal.setFinal(false);
                // 内部原语不暴露代理注解，事务和缓存边界由业务入口负责。
                internal.getAnnotations().clear();
                internal.getJavaDocLines().clear();
                internal.addJavaDocLine("/** 仅供业务事务编排调用的生成原语，不作为公开服务入口。 */");
                service.addMethod(internal);
            }
            Method rejection = new Method(original);
            rejection.setVisibility(JavaVisibility.PUBLIC);
            rejection.setAbstract(false);
            rejection.setFinal(false);
            rejection.getAnnotations().clear();
            rejection.addAnnotation("@Override");
            rejection.getJavaDocLines().clear();
            rejection.addJavaDocLine("/** 显式停用通用服务入口，立即拒绝且不访问持久层。 */");
            rejection.getBodyLines().clear();
            rejection.addBodyLine("throw new VgoServiceEntryDisabledException("
                    + literal(config.signature()) + ", " + literal(config.reason()) + ");");
            service.getMethods().removeIf(m -> ServiceMethods.signature(m).equals(config.signature()));
            service.addMethod(rejection);
            service.addImportedType("com.vgosoft.core.exception.VgoServiceEntryDisabledException");
            policy.entries.put(config.signature(), new Entry(config, snapshot(rejection),
                    internal == null ? null : snapshot(internal)));
        }
        table.setAttribute(ATTRIBUTE, policy);
    }

    /**
     * 生成完整映射与父类未覆盖的补充拒绝测试，始终调用真实业务服务。
     *
     * @param test 生成的业务服务测试类
     */
    public void addTests(TopLevelClass test) {
        if (test.getSuperClass().isEmpty()) {
            throw failure(table, "停用测试必须具备已支持的真实服务测试基类");
        }
        testBaseType = test.getSuperClass().orElseThrow().getFullyQualifiedName();
        testContract.add(snapshot(test.getMethods().stream().filter(m -> m.getName().equals("setUp"))
                .findFirst().orElseThrow()));
        Method contract = new Method("disabledServiceEntries");
        contract.setVisibility(JavaVisibility.PROTECTED);
        contract.setReturnType(new FullyQualifiedJavaType("java.util.Map<java.lang.String, java.lang.String>"));
        test.addImportedType("java.util.Map");
        contract.addAnnotation("@Override");
        contract.addJavaDocLine("/** 返回完整停用签名及原因，供真实业务服务拒绝测试共用。 */");
        contract.addBodyLine("java.util.Map<String, String> entries = new java.util.LinkedHashMap<>();");
        for (var entry : entries.values()) {
            var config = entry.configuration();
            contract.addBodyLine("entries.put(" + literal(config.signature()) + ", " + literal(config.reason()) + ");");
        }
        contract.addBodyLine("return java.util.Collections.unmodifiableMap(entries);");
        test.addMethod(contract);
        testContract.add(snapshot(contract));
        int index = 0;
        for (var entry : entries.values()) {
            if (baseTestSignatures(test).contains(entry.configuration().signature())) { continue; }
            Method check = new Method("testDisabledServiceEntry" + index++);
            check.addAnnotation("@org.junit.jupiter.api.Test");
            check.addJavaDocLine("/** 验证有效参数调用真实业务服务时立即拒绝，且不访问 Mapper。 */");
            String arguments = entry.rejection().getParameters().stream()
                    .map(p -> argument(p.getType())).collect(Collectors.joining(", "));
            check.addBodyLine("org.junit.jupiter.api.Assertions.assertTrue(assertDisabledServiceEntry("
                    + literal(entry.configuration().signature()) + ", () -> serviceImplUnderTest."
                    + entry.rejection().getName() + "(" + arguments + ")));");
            test.addMethod(check);
            testContract.add(snapshot(check));
        }
    }

    private Set<String> baseTestSignatures(TopLevelClass test) {
        String entity = table.getBaseRecordType();
        String example = table.getExampleType();
        Set<String> covered = new HashSet<>(Set.of("countByExample(" + example + ")", "deleteByExample(" + example + ")",
                "deleteByPrimaryKey(java.lang.String)", "insert(" + entity + ")", "insertSelective(" + entity + ")",
                "selectByExample(" + example + ")", "selectByPrimaryKey(java.lang.String)",
                "selectBySqlCondition(com.vgosoft.core.entity.ComSelSqlParameter)",
                "selectBySqlConditionSub(com.vgosoft.core.entity.ComSelSqlParameter)",
                "updateByExampleSelective(" + entity + "," + example + ")",
                "updateByExample(" + entity + "," + example + ")",
                "updateByPrimaryKeySelective(" + entity + ")", "updateByPrimaryKey(" + entity + ")",
                "selectBySql(com.vgosoft.mybatis.sqlbuilder.SelectSqlBuilder)",
                "selectMapBySql(com.vgosoft.mybatis.sqlbuilder.SelectSqlBuilder)",
                "insertBySql(com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder)",
                "updateBySql(com.vgosoft.mybatis.sqlbuilder.UpdateSqlBuilder)",
                "countBySql(com.vgosoft.mybatis.sqlbuilder.SelectSqlBuilder)",
                "listBySql(com.vgosoft.mybatis.sqlbuilder.SelectSqlBuilder)"));
        String base = test.getSuperClass().orElseThrow().getFullyQualifiedNameWithoutTypeParameters();
        if (base.contains("Blob")) {
            covered.addAll(Set.of("selectByExampleWithBLOBs(" + example + ")",
                    "updateByExampleWithBLOBs(" + entity + "," + example + ")",
                    "updateByPrimaryKeyWithBLOBs(" + entity + ")",
                    "updateWithInputStream(" + entity + ",java.io.InputStream)",
                    "insertWithInputStream(" + entity + ",java.io.InputStream)",
                    "getBytesByPrimaryKey(java.lang.String)", "setBytesByPrimaryKey(java.lang.String,byte[])"));
            if (base.contains("String")) {
                covered.addAll(Set.of("getBlobContentByPrimaryKey(java.lang.String)",
                        "insertBlobWithContent(" + entity + ",java.lang.String)",
                        "updateBlobWithContent(" + entity + ",java.lang.String)"));
            }
        }
        return covered;
    }

    private String argument(FullyQualifiedJavaType type) {
        String name = type.getFullyQualifiedNameWithoutTypeParameters();
        if (name.equals(table.getBaseRecordType())) { return "record"; }
        if (name.equals(table.getExampleType())) { return "example"; }
        if ("java.lang.String".equals(name)) { return "\"id\""; }
        if ("java.util.List".equals(name) || "java.util.Collection".equals(name)) {
            if (type.getTypeArguments().size() != 1) { throw failure(table, "无法构造有效集合测试参数：" + type); }
            return "java.util.Collections.singletonList(" + argument(type.getTypeArguments().get(0)) + ")";
        }
        if ("java.util.Set".equals(name)) {
            if (type.getTypeArguments().size() != 1) { throw failure(table, "无法构造有效集合测试参数：" + type); }
            return "java.util.Collections.singleton(" + argument(type.getTypeArguments().get(0)) + ")";
        }
        if ("boolean".equals(name) || "java.lang.Boolean".equals(name)) { return "true"; }
        if ("long".equals(name) || "java.lang.Long".equals(name)) { return "1L"; }
        if ("int".equals(name) || "java.lang.Integer".equals(name)) { return "1"; }
        if ("java.math.BigDecimal".equals(name)) { return "java.math.BigDecimal.ONE"; }
        if ("java.math.BigInteger".equals(name)) { return "java.math.BigInteger.ONE"; }
        if ("byte".equals(name) || "java.lang.Byte".equals(name)) { return "(byte) 1"; }
        if ("short".equals(name) || "java.lang.Short".equals(name)) { return "(short) 1"; }
        if ("float".equals(name) || "java.lang.Float".equals(name)) { return "1.0f"; }
        if ("double".equals(name) || "java.lang.Double".equals(name)) { return "1.0d"; }
        if ("char".equals(name) || "java.lang.Character".equals(name)) { return "'a'"; }
        if ("java.time.LocalDate".equals(name)) { return "java.time.LocalDate.of(2000, 1, 1)"; }
        if ("java.time.LocalTime".equals(name)) { return "java.time.LocalTime.of(1, 0)"; }
        if ("java.time.LocalDateTime".equals(name)) { return "java.time.LocalDateTime.of(2000, 1, 1, 1, 0)"; }
        if ("java.time.Instant".equals(name)) { return "java.time.Instant.EPOCH"; }
        if ("java.util.Date".equals(name)) { return "new java.util.Date(1L)"; }
        if ("java.sql.Date".equals(name)) { return "java.sql.Date.valueOf(\"2000-01-01\")"; }
        if ("java.sql.Time".equals(name)) { return "java.sql.Time.valueOf(\"01:00:00\")"; }
        if ("java.sql.Timestamp".equals(name)) { return "java.sql.Timestamp.valueOf(\"2000-01-01 01:00:00\")"; }
        if ("com.vgosoft.core.entity.ComSelSqlParameter".equals(name)) { return "comSelSqlParameter"; }
        throw failure(table, "尚无已验证的有效补充测试参数构造方式：" + type);
    }

    /**
     * 所有最终插件完成后检查必需拒绝、内部原语、接口边界及真实服务测试。
     *
     * @param table 当前表
     * @param files 插件完成后的全部 Java 输出
     */
    public static void validateFinal(AbstractIntrospectedTable table, List<GeneratedJavaFile> files) {
        if (configurations(table).isEmpty()) { return; }
        var policy = effective(table);
        if (!configurations(table).equals(policy.entries.values().stream().map(Entry::configuration).toList())) {
            throw failure(table, "插件修改了已经解析的停用配置");
        }
        var config = table.getTableConfiguration();
        if (!config.getJavaServiceGeneratorConfiguration().isGenerate()
                || !config.getJavaServiceImplGeneratorConfiguration().isGenerate()
                || !config.getJavaServiceImplGeneratorConfiguration().isGenerateUnitTest()) {
            throw failure(table, "显式停用要求生成 Service、实现及真实业务 Service 测试");
        }
        String model = config.getDomainObjectName();
        var contract = (InterfaceContract) table.getAttribute(ATTRIBUTE + ".interface");
        if (contract == null) { throw failure(table, "必需 Service 接口契约未生成"); }
        var interfaces = files.stream().map(GeneratedJavaFile::getCompilationUnit)
                .filter(unit -> unit.getType().getFullyQualifiedName().equals(contract.type())).toList();
        if (interfaces.size() != 1 || !(interfaces.get(0) instanceof Interface)) {
            throw failure(table, "插件后必需 Service 接口缺失或重复");
        }
        Interface service = (Interface) interfaces.get(0);
        if (!service.getSuperInterfaceTypes().stream().map(FullyQualifiedJavaType::getFullyQualifiedName)
                .sorted().toList().equals(contract.parents())) {
            throw failure(table, "插件后 Service 接口继承契约被修改");
        }
        for (Method expected : contract.methods()) {
            var found = service.getMethods().stream().filter(m ->
                    ServiceMethods.signature(m).equals(ServiceMethods.signature(expected))).toList();
            if (found.size() != 1 || !methodShape(found.get(0)).equals(methodShape(expected))) {
                throw failure(table, "插件后 Service 接口停用签名被修改：" + ServiceMethods.signature(expected));
            }
        }
        TopLevelClass implementation = requireClass(table, files, "Abstract" + model + "Impl.java",
                config.getJavaServiceImplGeneratorConfiguration().getTargetPackageGen());
        for (var entry : policy.entries.values()) {
            requireMethod(table, implementation, entry.rejection());
            if (entry.internal() != null) {
                if (implementation.getMethods().stream()
                        .filter(m -> m.getName().equals(entry.internal().getName())).count() != 1) {
                    throw failure(table, "internalMethod 被重复定义或重载：" + entry.internal().getName());
                }
                requireMethod(table, implementation, entry.internal());
                for (var file : files) {
                    var unit = file.getCompilationUnit();
                    if (!file.getFileName().equals("IGen" + model + ".java")
                            && !file.getFileName().equals("I" + model + ".java")
                            && !file.getFileName().equals(org.mybatis.generator.internal.util.JavaBeansUtil
                                .getFirstCharacterUppercase(table.getControllerBeanName()) + ".java")) {
                        continue;
                    }
                    List<Method> methods = unit instanceof Interface inf ? inf.getMethods()
                            : unit instanceof TopLevelClass cls ? cls.getMethods() : List.of();
                    if (!unit.getType().equals(implementation.getType()) && methods.stream()
                            .anyMatch(m -> m.getName().equals(entry.internal().getName()))) {
                        throw failure(table, "internalMethod 不得在接口或其他公开生成类暴露：" + entry.internal().getName());
                    }
                }
            }
        }
        String serviceName = org.mybatis.generator.internal.util.JavaBeansUtil
                .getFirstCharacterUppercase(table.getControllerBeanName());
        for (var file : files) {
            if (file.getFileName().equals(serviceName + ".java")
                    && file.getTargetPackage().equals(config.getJavaServiceImplGeneratorConfiguration().getTargetPackage())
                    && file.getCompilationUnit() instanceof TopLevelClass business) {
                if (!business.getSuperClass().orElseThrow().equals(implementation.getType())) {
                    throw failure(table, "插件改变真实业务 Service 的继承关系");
                }
                for (var entry : policy.entries.values()) {
                    if (business.getMethods().stream().anyMatch(m ->
                            ServiceMethods.signature(m).equals(entry.configuration().signature()))) {
                        requireMethod(table, business, entry.rejection());
                    }
                }
            }
        }
        TopLevelClass test = requireClass(table, files, serviceName + "Test.java",
                config.getJavaServiceImplGeneratorConfiguration().getTargetPackage());
        if (policy.testContract.isEmpty()) { throw failure(table, "必需停用测试契约未生成"); }
        if (!test.getSuperClass().map(FullyQualifiedJavaType::getFullyQualifiedName)
                .orElse("").equals(policy.testBaseType)) {
            throw failure(table, "插件修改了停用测试的基类契约");
        }
        for (var method : policy.testContract) { requireMethod(table, test, method); }
        var setup = test.getMethods().stream().filter(m -> m.getName().equals("setUp")).toList();
        if (setup.size() != 1 || setup.get(0).getBodyLines().stream().noneMatch(line ->
                line.equals("this.serviceImplUnderTest = new " + serviceName + "(mockMapper);"))) {
            throw failure(table, "停用测试必须实例化真实业务 Service");
        }
    }

    private static TopLevelClass requireClass(AbstractIntrospectedTable table, List<GeneratedJavaFile> files,
                                               String name, String targetPackage) {
        var found = files.stream().filter(f -> f.getFileName().equals(name)
                && f.getTargetPackage().equals(targetPackage)).toList();
        if (found.size() != 1 || !(found.get(0).getCompilationUnit() instanceof TopLevelClass)) {
            throw failure(table, "必需输出缺失或重复：" + targetPackage + "." + name);
        }
        return (TopLevelClass) found.get(0).getCompilationUnit();
    }

    private static void requireMethod(AbstractIntrospectedTable table, TopLevelClass unit, Method expected) {
        var found = unit.getMethods().stream()
                .filter(m -> ServiceMethods.signature(m).equals(ServiceMethods.signature(expected))).toList();
        if (found.size() != 1) { throw failure(table, "插件后必需方法缺失或重复：" + ServiceMethods.signature(expected)); }
        var actual = found.get(0);
        if (actual.getVisibility() != expected.getVisibility() || actual.isFinal() != expected.isFinal()
                || actual.isStatic() || actual.isAbstract()
                || !actual.getReturnType().equals(expected.getReturnType())
                || !actual.getAnnotations().equals(expected.getAnnotations())
                || !actual.getBodyLines().equals(expected.getBodyLines())
                || !actual.getExceptions().equals(expected.getExceptions())
                || !actual.getParameters().stream().map(Parameter::toString).toList()
                    .equals(expected.getParameters().stream().map(Parameter::toString).toList())
                || actual.isNative() || actual.isSynchronized() != expected.isSynchronized()
                || !actual.getTypeParameters().equals(expected.getTypeParameters())) {
            throw failure(table, "插件后拒绝、内部原语或测试契约被修改：" + ServiceMethods.signature(expected));
        }

    }

    private static Method snapshot(Method source) {
        Method result = new Method(source);
        result.getParameters().clear();
        for (var parameter : source.getParameters()) {
            var copy = new Parameter(new FullyQualifiedJavaType(parameter.getType().getFullyQualifiedName()),
                    parameter.getName(), parameter.isVarargs());
            copy.getAnnotations().addAll(parameter.getAnnotations());
            result.addParameter(copy);
        }
        source.getReturnType().ifPresent(type ->
                result.setReturnType(new FullyQualifiedJavaType(type.getFullyQualifiedName())));
        result.getExceptions().clear();
        for (var exception : source.getExceptions()) {
            result.addException(new FullyQualifiedJavaType(exception.getFullyQualifiedName()));
        }
        return result;
    }

    private static List<Object> methodShape(Method method) {
        return List.of(method.getName(), method.getVisibility(), method.getReturnType(),
                method.getParameters().stream().map(Parameter::toString).toList(), method.getExceptions(),
                method.getTypeParameters(), method.getAnnotations(), method.getBodyLines(),
                method.isAbstract(), method.isStatic(), method.isFinal(), method.isNative(), method.isDefault());
    }

    public static String literal(String value) {
        StringBuilder result = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\' -> result.append("\\\\");
                case '"' -> result.append("\\\"");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 32 || c == 0x2028 || c == 0x2029) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else { result.append(c); }
                }
            }
        }
        return result.append('"').toString();
    }

    private static IllegalArgumentException failure(AbstractIntrospectedTable table, String message) {
        return new IllegalArgumentException("表 " + table.getTableConfiguration().getTableName()
                + " disabledMethod：" + message);
    }
}
