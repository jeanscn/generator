package org.mybatis.generator.codegen.mybatis3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 使用真实解析、离线元数据内省和最终插件验证显式停用入口。
 *
 * @author vgosoft-agent
 */
class DisabledServiceEntryGenerationTest {
    private static final String ENTITY = "com.vgosoft.sensitive.entity.SensitiveRuleRevision";
    private static final String EXAMPLE = "com.vgosoft.sensitive.entity.example.SensitiveRuleRevisionExample";
    private static final String REASON = "规则版本只能通过 advance 推进";

    private static String disabled(String signature) {
        return "<disabledMethod signature=\"" + signature + "\" reason=\"" + REASON + "\"/>";
    }

    private static Configuration configuration(String entries) throws Exception {
        return configuration(entries, true);
    }

    private static Configuration configuration(String entries, boolean customize) throws Exception {
        String xml = CasConsumerBaselineTest.consumerXml(null);
        int start = xml.indexOf("<table ignore=\"false\" tableName=\"sensitive_rule_revision\"");
        int end = xml.indexOf("</table>", start);
        String revision = xml.substring(start, end);
        revision = revision.replaceAll("(?s)\\s*<disabledMethod\\b[^>]*/>", "")
                .replace("<generateService generate=\"true\" generateUnitTest=\"true\">",
                        "<generateService generate=\"true\" generateUnitTest=\"true\">" + entries);
        xml = xml.substring(0, start) + revision + xml.substring(end);
        var parser = new ConfigurationParser(new ArrayList<>());
        Configuration config = parser.parseConfiguration(new StringReader(xml));
        if (customize) { parser.customConfig(config); }
        return config;
    }

    private static MyBatisGenerator generate(Configuration config) throws Exception {
        var generator = new MyBatisGenerator(config, new DefaultShellCallback(true), new ArrayList<>());
        generator.generate(null, null, null, false);
        return generator;
    }

    private static TopLevelClass output(MyBatisGenerator generator, String name) {
        return (TopLevelClass) generator.getGeneratedJavaFiles().stream()
                .filter(f -> f.getFileName().equals(name)).findFirst().orElseThrow().getCompilationUnit();
    }

    private static Method method(TopLevelClass unit, String name) {
        return unit.getMethods().stream().filter(m -> m.getName().equals(name)).findFirst().orElseThrow();
    }

    /** 未配置时不新增拒绝覆盖、内部原语或测试映射，保持继承行为。 */
    @Test
    void preservesDefaultServiceAndTests() throws Exception {
        var generator = generate(configuration(""));
        var implementation = output(generator, "AbstractSensitiveRuleRevisionImpl.java");
        assertThat(implementation.getMethods()).noneMatch(m -> m.getName().equals("insert")
                || m.getName().equals("deleteByPrimaryKey") || m.getName().equals("incrementRevisionInternal"));
        assertThat(output(generator, "SensitiveRuleRevisionImplTest.java").getMethods())
                .noneMatch(m -> m.getName().equals("disabledServiceEntries"));
    }

    /** CRUD SQL 开关全部关闭时，原始与完整配置链仍按实际 Example 类型封闭继承删除入口。 */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void closesInheritedMethodsWithDisabledSqlFlagsAndActualExampleType(boolean customize) throws Exception {
        String example = customize ? EXAMPLE : "com.vgosoft.sensitive.entity.SensitiveRuleRevisionExample";
        var config = configuration(disabled("deleteByExample(" + example + ")")
                + disabled("deleteByPrimaryKey(java.lang.String)") + disabled("insert(" + ENTITY + ")"), customize);
        var table = config.getContexts().get(0).getTableConfigurations().stream()
                .filter(t -> t.getDomainObjectName().equals("SensitiveRuleRevision")).findFirst().orElseThrow();
        assertThat(table.isDeleteByExampleStatementEnabled()).isFalse();
        assertThat(table.isDeleteByPrimaryKeyStatementEnabled()).isFalse();
        assertThat(table.isInsertStatementEnabled()).isFalse();
        var implementation = output(generate(config), "AbstractSensitiveRuleRevisionImpl.java");
        assertThat(method(implementation, "deleteByExample").getParameters().get(0).getType().getFullyQualifiedName())
                .isEqualTo(example);
        for (String name : List.of("deleteByExample", "deleteByPrimaryKey", "insert")) {
            assertThat(method(implementation, name).getBodyLines()).singleElement().asString()
                    .contains("VgoServiceEntryDisabledException");
        }
    }

    /** 漏掉完整配置链造成 Example 包不一致时，拒绝并明确展示真实签名，而不放宽全限定匹配。 */
    @Test
    void diagnosesExamplePackageMismatchWithoutAcceptingAliases() throws Exception {
        var config = configuration(disabled("deleteByExample(" + EXAMPLE + ")"), false);
        assertThatThrownBy(() -> generate(config)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("实际同名签名")
                .hasMessageContaining("deleteByExample(com.vgosoft.sensitive.entity.SensitiveRuleRevisionExample)")
                .hasMessageContaining("ConfigurationParser.customConfig");
    }

    /** 不依赖任何自定义插件时，直接设置绝对 Example 包也能匹配正式签名并封闭继承方法。 */
    @Test
    void acceptsExplicitExamplePackageWithoutCustomPlugins() throws Exception {
        var config = configuration(disabled("deleteByExample(" + EXAMPLE + ")"), false);
        config.getContexts().get(0).getJavaModelGeneratorConfiguration().addProperty(
                PropertyRegistry.MODEL_GENERATOR_EXAMPLE_PACKAGE, "com.vgosoft.sensitive.entity.example");
        var implementation = output(generate(config), "AbstractSensitiveRuleRevisionImpl.java");
        assertThat(method(implementation, "deleteByExample").getParameters().get(0).getType().getFullyQualifiedName())
                .isEqualTo(EXAMPLE);
        assertThat(method(implementation, "deleteByExample").getBodyLines()).singleElement().asString()
                .contains("VgoServiceEntryDisabledException");
    }

    /** 离线元数据把主键解析为 Object 时，递增候选按实际类型拒绝 String 配置并明确诊断。 */
    @Test
    void diagnosesAtomicPrimaryKeyTypeMismatch() throws Exception {
        var config = configuration(disabled("incrementRevisionByPrimaryKey(java.lang.String)"));
        var plugin = new PluginConfiguration();
        plugin.setConfigurationType(HostilePlugin.class.getName());
        plugin.addProperty("mode", "atomic-object-key");
        config.getContexts().get(0).addPluginConfiguration(plugin);
        assertThatThrownBy(() -> generate(config)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("实际同名签名：[incrementRevisionByPrimaryKey(java.lang.Object)]");
    }

    /** 根实体同步默认保留；离线显式关闭后不尝试结构写入且仍保留继承与内部递增。 */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void controlsRootClassSchemaSynchronizationWithoutChangingService(boolean enabled) throws Exception {
        var config = configuration("<disabledMethod signature=\"incrementRevisionByPrimaryKey(java.lang.String)\""
                + " reason=\"" + REASON + "\" internalMethod=\"incrementRevisionInternal\"/>");
        var context = config.getContexts().get(0);
        assertThat(context.isRootClassSchemaSynchronizationEnabled()).isTrue();
        context.setRootClassSchemaSynchronizationEnabled(enabled);
        context.getTableConfigurations().removeIf(t -> !t.getTableName().equals("sensitive_rule_revision"));
        context.getConnectionFactoryConfiguration().setConfigurationType(SchemaRecordingFactory.class.getName());
        SchemaRecordingFactory.writes = 0;
        var warnings = new ArrayList<String>();
        var generator = new MyBatisGenerator(config, new DefaultShellCallback(true), warnings);
        generator.generate(null, null, null, false);
        var implementation = output(generator, "AbstractSensitiveRuleRevisionImpl.java");
        assertThat(implementation.getSuperClass()).isPresent();
        assertThat(method(implementation, "incrementRevisionInternal").getBodyLines())
                .containsExactly("return mapper.incrementRevisionByPrimaryKey(id);");
        if (enabled) {
            assertThat(SchemaRecordingFactory.writes).isPositive();
            assertThat(warnings).anyMatch(warning -> warning.contains("更新数据库Sql"));
        } else {
            assertThat(SchemaRecordingFactory.writes).isZero();
            assertThat(warnings).noneMatch(warning -> warning.contains("更新数据库"));
        }
    }

    /**
     * 记录结构写入尝试并立即拒绝，确保测试不对任何数据库执行 DDL。
     *
     * @author vgosoft-agent
     */
    public static class SchemaRecordingFactory implements ConnectionFactory {
        private static int writes;
        @Override
        public void addConfigurationProperties(java.util.Properties properties) { }
        @Override
        public java.sql.Connection getConnection() throws java.sql.SQLException {
            java.sql.Connection delegate = new CasConsumerBaselineTest.MetadataFactory().getConnection();
            return (java.sql.Connection) java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[]{java.sql.Connection.class}, (proxy, method, arguments) -> {
                        if (method.getName().equals("createStatement")) {
                            writes++;
                            throw new java.sql.SQLException("隔离测试禁止执行结构写入");
                        }
                        try {
                            return method.invoke(delegate, arguments);
                        } catch (java.lang.reflect.InvocationTargetException exception) {
                            throw exception.getCause();
                        }
                    });
        }
    }

    /** 无本地方法体的继承 CRUD 仍生成准确公开拒绝，擦除 List 签名共享完整测试映射。 */
    @Test
    void closesInheritedCrudAndGeneratesOnlyMissingTests() throws Exception {
        var generator = generate(configuration(disabled("deleteByPrimaryKey(java.lang.String)")
                + disabled("insert(" + ENTITY + ")")
                + disabled("updateBatchByPrimaryKey(java.util.List)")
                + disabled("updateByExampleSelectiveAndSelect(" + ENTITY + "," + EXAMPLE + ")")));
        var implementation = output(generator, "AbstractSensitiveRuleRevisionImpl.java");
        for (String name : List.of("deleteByPrimaryKey", "insert", "updateBatchByPrimaryKey",
                "updateByExampleSelectiveAndSelect")) {
            Method method = method(implementation, name);
            assertThat(method.getAnnotations()).containsExactly("@Override");
            assertThat(method.isFinal()).isFalse();
            assertThat(method.getVisibility()).isEqualTo(JavaVisibility.PUBLIC);
            assertThat(method.getBodyLines()).singleElement().asString()
                    .contains("throw new com.vgosoft.core.exception.VgoServiceEntryDisabledException(", REASON);
        }
        var test = output(generator, "SensitiveRuleRevisionImplTest.java");
        assertThat(method(test, "disabledServiceEntries").getBodyLines()).anyMatch(line ->
                line.contains("updateBatchByPrimaryKey(java.util.List)"));
        assertThat(test.getMethods().stream().filter(m -> m.getName().startsWith("testDisabledServiceEntry")))
                .hasSize(2).allSatisfy(m -> assertThat(String.join("\n", m.getBodyLines()))
                        .contains("assertDisabledServiceEntry", "serviceImplUnderTest.")
                        .doesNotContain("null", "mock("));
    }

    /** 完整 revision 场景关闭十四个通用入口，只补四个基类未覆盖测试且保留一个内部递增。 */
    @Test
    void generatesCompleteRevisionDisableContract() throws Exception {
        List<String> signatures = List.of("deleteByExample(" + EXAMPLE + ")",
                "deleteByPrimaryKey(java.lang.String)", "insert(" + ENTITY + ")",
                "insertSelective(" + ENTITY + ")", "insertOrUpdateSelective(" + ENTITY + ")",
                "updateByExampleSelective(" + ENTITY + "," + EXAMPLE + ")",
                "updateByExampleSelectiveAndSelect(" + ENTITY + "," + EXAMPLE + ")",
                "updateByExample(" + ENTITY + "," + EXAMPLE + ")", "updateByPrimaryKey(" + ENTITY + ")",
                "updateByPrimaryKeySelective(" + ENTITY + ")",
                "insertBySql(com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder)",
                "updateBySql(com.vgosoft.mybatis.sqlbuilder.UpdateSqlBuilder)",
                "updateBatchByPrimaryKey(java.util.List)");
        String entries = signatures.stream().map(DisabledServiceEntryGenerationTest::disabled)
                .collect(java.util.stream.Collectors.joining());
        var generator = generate(configuration(entries
                + "<disabledMethod signature=\"incrementRevisionByPrimaryKey(java.lang.String)\""
                + " reason=\"" + REASON + "\" internalMethod=\"incrementRevisionInternal\"/>"));
        var implementation = output(generator, "AbstractSensitiveRuleRevisionImpl.java");
        for (String signature : signatures) {
            assertThat(implementation.getMethods()).anySatisfy(method -> {
                assertThat(org.mybatis.generator.codegen.mybatis3.service.ServiceMethods.signature(method))
                        .isEqualTo(signature);
                assertThat(method.getAnnotations()).containsExactly("@Override");
                assertThat(method.getBodyLines()).singleElement().asString().contains("VgoServiceEntryDisabledException");
            });
        }
        var test = output(generator, "SensitiveRuleRevisionImplTest.java");
        assertThat(method(test, "disabledServiceEntries").getBodyLines().stream()
                .filter(line -> line.startsWith("entries.put("))).hasSize(14);
        assertThat(test.getMethods().stream().filter(m -> m.getName().startsWith("testDisabledServiceEntry"))).hasSize(4);
        assertThat(method(implementation, "incrementRevisionInternal").getVisibility()).isEqualTo(JavaVisibility.PROTECTED);
    }

    /** 显式命名的内部递增保留原 Mapper 原语为 protected，公开入口无副作用注解。 */
    @Test
    void keepsOnlyExplicitInternalPrimitive() throws Exception {
        var config = configuration("<property name=\"entityEvent\" value=\"PRE_UPDATE\"/>"
                + "<disabledMethod signature=\"incrementRevisionByPrimaryKey(java.lang.String)\""
                + " reason=\"" + REASON + "\" internalMethod=\"incrementRevisionInternal\"/>");
        assertThat(config.getContexts().get(0).getTableConfigurations().stream()
                .filter(t -> t.getDomainObjectName().equals("SensitiveRuleRevision")).findFirst().orElseThrow()
                .getJavaServiceImplGeneratorConfiguration().getEntityEvent()).contains("PRE_UPDATE");
        var generator = generate(config);
        var unit = output(generator, "AbstractSensitiveRuleRevisionImpl.java");
        Method internal = method(unit, "incrementRevisionInternal");
        assertThat(internal.getVisibility()).isEqualTo(JavaVisibility.PROTECTED);
        assertThat(internal.getAnnotations()).isEmpty();
        assertThat(internal.getBodyLines()).containsExactly("return mapper.incrementRevisionByPrimaryKey(id);");
        assertThat(method(unit, "incrementRevisionByPrimaryKey").getAnnotations()).containsExactly("@Override");
        assertThat(generator.getGeneratedJavaFiles().stream()
                .filter(f -> f.getCompilationUnit() instanceof Interface)
                .map(GeneratedJavaFile::getFormattedContent)).noneMatch(s -> s.contains("incrementRevisionInternal"));
        assertThat(method(output(generator, "SensitiveRuleRevisionImplTest.java"),
                "testDisabledServiceEntry0").getBodyLines()).anyMatch(s ->
                s.contains("serviceImplUnderTest.incrementRevisionByPrimaryKey(\"id\")"));
    }

    /** 原始递增即使带缓存、事务或事件注解，内部复制也只保留原语，不改变私有 Mapper 字段。 */
    @Test
    void stripsInterceptionAnnotationsFromInternalPrimitive() throws Exception {
        var config = configuration("");
        var generator = generate(config);
        var context = config.getContexts().get(0);
        var table = context.getIntrospectedTables().stream()
                .filter(t -> t.getTableConfiguration().getDomainObjectName().equals("SensitiveRuleRevision"))
                .findFirst().orElseThrow();
        var unit = output(generator, "AbstractSensitiveRuleRevisionImpl.java");
        var original = method(unit, "incrementRevisionByPrimaryKey");
        List<String> originalBody = List.copyOf(original.getBodyLines());
        original.addAnnotation("@org.springframework.cache.annotation.CacheEvict(allEntries = true)");
        original.addAnnotation("@org.springframework.transaction.annotation.Transactional");
        original.addAnnotation("@SensitiveWrite");
        original.addAnnotation("@EntityMutation");
        table.getTableConfiguration().getJavaServiceGeneratorConfiguration().addDisabledMethod(
                new DisabledServiceMethodConfiguration("incrementRevisionByPrimaryKey(java.lang.String)",
                        REASON, "incrementRevisionInternal"));
        org.mybatis.generator.codegen.mybatis3.service.DisabledServiceEntries.apply(table, unit,
                new org.mybatis.generator.codegen.mybatis3.service.ServiceMethods(context, table));
        var internal = method(unit, "incrementRevisionInternal");
        assertThat(internal.getVisibility()).isEqualTo(JavaVisibility.PROTECTED);
        assertThat(internal.getAnnotations()).isEmpty();
        assertThat(internal.getBodyLines()).isEqualTo(originalBody)
                .containsExactly("return mapper.incrementRevisionByPrimaryKey(id);");
        var mapper = unit.getFields().stream().filter(f -> f.getName().equals("mapper")).findFirst().orElseThrow();
        assertThat(mapper.getVisibility()).isEqualTo(JavaVisibility.PRIVATE);
        assertThat(mapper.isFinal()).isTrue();
        assertThat(method(unit, "incrementRevisionByPrimaryKey").getAnnotations()).containsExactly("@Override");
    }

    /** 未显式配置停用时 CAS 仍输出原 ENTRY_DISABLED，不引入新异常或内部旁路。 */
    @Test
    void preservesLegacyCasDefaultRejection() throws Exception {
        var config = configuration("");
        config.getContexts().get(0).getTableConfigurations().get(0).setCasWriteContractEnabled(true);
        var implementation = output(generate(config), "AbstractSensitiveCategoryImpl.java");
        assertThat(method(implementation, "updateByPrimaryKey").getBodyLines())
                .containsExactly("throw new IllegalStateException(\"ENTRY_DISABLED\");");
        assertThat(implementation.getMethods()).noneMatch(m -> m.getBodyLines().stream()
                .anyMatch(line -> line.contains("VgoServiceEntryDisabledException")));
    }

    /** 原因中的引号、反斜杠和换行在拒绝体与映射中一致转义，不破坏生成 Java。 */
    @Test
    void escapesReasonConsistently() throws Exception {
        var config = configuration("");
        var table = config.getContexts().get(0).getTableConfigurations().stream()
                .filter(t -> t.getDomainObjectName().equals("SensitiveRuleRevision")).findFirst().orElseThrow();
        table.getJavaServiceGeneratorConfiguration().addDisabledMethod(new DisabledServiceMethodConfiguration(
                "deleteByPrimaryKey(java.lang.String)", "引用\"\\路径\n下一行", null));
        var generator = generate(config);
        String escaped = "\"引用\\\"\\\\路径\\n下一行\"";
        assertThat(String.join("\n", method(output(generator, "AbstractSensitiveRuleRevisionImpl.java"),
                "deleteByPrimaryKey").getBodyLines())).contains(escaped);
        assertThat(String.join("\n", method(output(generator, "SensitiveRuleRevisionImplTest.java"),
                "disabledServiceEntries").getBodyLines())).contains(escaped);
    }

    /** 未知、短类型、错误重载及含空格的签名不得被默默忽略。 */
    @ParameterizedTest
    @ValueSource(strings = {"missing(java.lang.String)", "deleteByPrimaryKey(String)",
            "deleteByPrimaryKey(java.lang.Integer)", "deleteByPrimaryKey( java.lang.String)",
            "updateBatchByPrimaryKey(java.util.List<com.example.Record>)"})
    void rejectsUnknownOrMalformedSignatures(String signature) {
        assertThatThrownBy(() -> generate(configuration(disabled(signature))))
                .isInstanceOfAny(IllegalArgumentException.class, org.mybatis.generator.exception.XMLParserException.class);
    }

    /** 重复配置、非法内部名、继承方法无原语及与现有方法重名均立即失败。 */
    @ParameterizedTest
    @ValueSource(strings = {"duplicate", "illegal", "inherited", "collision"})
    void rejectsDuplicateAndUnsafeInternalMethods(String mode) {
        String entry = switch (mode) {
            case "duplicate" -> disabled("insert(" + ENTITY + ")") + disabled("insert(" + ENTITY + ")");
            case "illegal" -> "<disabledMethod signature=\"incrementRevisionByPrimaryKey(java.lang.String)\""
                    + " reason=\"原因\" internalMethod=\"public\"/>";
            case "inherited" -> "<disabledMethod signature=\"insert(" + ENTITY
                    + ")\" reason=\"原因\" internalMethod=\"insertInternal\"/>";
            case "collision" -> "<disabledMethod signature=\"incrementRevisionByPrimaryKey(java.lang.String)\""
                    + " reason=\"原因\" internalMethod=\"selectByPrimaryKey\"/>";
            default -> throw new IllegalArgumentException(mode);
        };
        assertThatThrownBy(() -> generate(configuration(entry))).isInstanceOf(IllegalArgumentException.class);
    }

    /** 必需 Service、测试或内部原语被插件否决、放开、加注解或删除时最终阶段拒绝。 */
    @ParameterizedTest
    @ValueSource(strings = {"veto-impl", "veto-test", "veto-service", "enable", "annotate", "remove-internal",
            "public-internal", "test-contract", "late-enable", "late-remove", "interface-internal",
            "interface-signature", "interface-parent", "parameter", "subclass-enable",
            "annotate-internal", "override-internal"})
    void rejectsFinalPluginChanges(String mode) throws Exception {
        var config = configuration("<disabledMethod signature=\"incrementRevisionByPrimaryKey(java.lang.String)\""
                + " reason=\"原因\" internalMethod=\"incrementRevisionInternal\"/>");
        var plugin = new PluginConfiguration();
        plugin.setConfigurationType(HostilePlugin.class.getName());
        plugin.addProperty("mode", mode);
        config.getContexts().get(0).addPluginConfiguration(plugin);
        assertThatThrownBy(() -> generate(config)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("disabledMethod");
    }

    /** 显式拒绝可增强既有 CAS 拒绝，但不得保留被 CAS 禁止的内部旁路。 */
    @Test
    void strengthensCasRejectionWithoutReopening() throws Exception {
        var config = configuration("");
        var table = config.getContexts().get(0).getTableConfigurations().get(0);
        table.setCasWriteContractEnabled(true);
        table.getJavaServiceImplGeneratorConfiguration().setGenerateUnitTest(true);
        table.getJavaServiceGeneratorConfiguration().addDisabledMethod(new DisabledServiceMethodConfiguration(
                "updateByPrimaryKey(com.vgosoft.sensitive.entity.SensitiveCategory)", "只允许 CAS", null));
        var generator = generate(config);
        assertThat(method(output(generator, "AbstractSensitiveCategoryImpl.java"), "updateByPrimaryKey").getBodyLines())
                .singleElement().asString().contains("VgoServiceEntryDisabledException", "只允许 CAS");
        var unsafe = configuration("");
        var unsafeTable = unsafe.getContexts().get(0).getTableConfigurations().get(0);
        unsafeTable.setCasWriteContractEnabled(true);
        unsafeTable.getJavaServiceImplGeneratorConfiguration().setGenerateUnitTest(true);
        unsafeTable.getJavaServiceGeneratorConfiguration().addDisabledMethod(new DisabledServiceMethodConfiguration(
                "updateByPrimaryKeySelective(com.vgosoft.sensitive.entity.SensitiveCategory)",
                "只允许 CAS", "unsafeInternal"));
        assertThatThrownBy(() -> generate(unsafe)).hasMessageContaining("CAS");
    }

    /**
     * 在服务与最终附加文件回调中破坏输出，验证最终校验而非插件前快照。
     *
     * @author vgosoft-agent
     */
    public static class HostilePlugin extends PluginAdapter {
        private String mode;
        private TopLevelClass implementation;
        private Interface service;
        @Override
        public boolean validate(List<String> warnings) { mode = properties.getProperty("mode"); return true; }
        @Override
        public void initialized(AbstractIntrospectedTable table) {
            if (mode.equals("atomic-object-key")
                    && table.getTableConfiguration().getDomainObjectName().equals("SensitiveRuleRevision")) {
                table.getPrimaryKeyColumns().get(0)
                        .setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.lang.Object"));
            }
        }
        @Override
        public boolean serviceGenerated(Interface unit, AbstractIntrospectedTable table) {
            if (unit.getType().getShortName().equals("IGenSensitiveRuleRevision")) {
                service = unit;
                if (mode.equals("veto-service")) { return false; }
            }
            return true;
        }
        @Override
        public boolean serviceImplGenerated(TopLevelClass unit, AbstractIntrospectedTable table) {
            if (unit.getType().getShortName().equals("AbstractSensitiveRuleRevisionImpl")) {
                implementation = unit;
                if (mode.equals("veto-impl")) { return false; }
                if (mode.equals("enable")) {
                    method(unit, "incrementRevisionByPrimaryKey").getBodyLines().clear();
                }
                if (mode.equals("annotate")) {
                    method(unit, "incrementRevisionByPrimaryKey").addAnnotation("@Transactional");
                }
                if (mode.equals("remove-internal")) {
                    unit.getMethods().removeIf(m -> m.getName().equals("incrementRevisionInternal"));
                }
                if (mode.equals("public-internal")) {
                    method(unit, "incrementRevisionInternal").setVisibility(JavaVisibility.PUBLIC);
                }
                if (mode.equals("annotate-internal")) {
                    method(unit, "incrementRevisionInternal")
                            .addAnnotation("@org.springframework.transaction.annotation.Transactional");
                }
                if (mode.equals("override-internal")) {
                    method(unit, "incrementRevisionInternal").addAnnotation("@Override");
                }
            } else if (unit.getType().getShortName().equals("SensitiveRuleRevisionImplTest")) {
                if (mode.equals("veto-test")) { return false; }
                if (mode.equals("test-contract")) { method(unit, "disabledServiceEntries").getBodyLines().clear(); }
            }
            return true;
        }
        @Override
        public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
            if (mode.equals("late-enable")) {
                method(implementation, "incrementRevisionByPrimaryKey").getBodyLines().clear();
            }
            if (mode.equals("late-remove")) { implementation.getMethods().clear(); }
            if (mode.equals("interface-internal")) {
                Method exposed = new Method(method(implementation, "incrementRevisionInternal"));
                exposed.setVisibility(JavaVisibility.PUBLIC);
                service.addMethod(exposed);
            }
            if (mode.equals("interface-signature")) {
                service.getMethods().removeIf(m -> m.getName().equals("incrementRevisionByPrimaryKey"));
            }
            if (mode.equals("interface-parent")) { service.getSuperInterfaceTypes().clear(); }
            if (mode.equals("parameter")) {
                method(implementation, "incrementRevisionByPrimaryKey").getParameters().get(0)
                        .setType(FullyQualifiedJavaType.getIntegerInstance());
            }
            return List.of();
        }
        @Override
        public boolean subServiceImplGenerated(TopLevelClass unit, AbstractIntrospectedTable table) {
            if (mode.equals("subclass-enable") && unit.getType().getShortName().equals("SensitiveRuleRevisionImpl")) {
                Method opened = new Method(method(implementation, "incrementRevisionByPrimaryKey"));
                opened.getBodyLines().clear();
                opened.addBodyLine("return 1;");
                unit.addMethod(opened);
            }
            return true;
        }
    }
}
