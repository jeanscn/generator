package org.mybatis.generator.internal;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.codegen.mybatis3.SelectForUpdateValidator;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3SimpleImpl;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.runtime.dynamic.sql.IntrospectedTableMyBatis3DynamicSqlImpl;
import org.mybatis.generator.runtime.kotlin.IntrospectedTableKotlinImpl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 离线验证配置验证与实际内省共用的运行时解析，不创建 JDBC 连接。
 *
 * @author vgosoft
 */
class ObjectFactoryRuntimeTest {
    @Test
    void absentNullEmptyAndBlankResolveToMyBatis3WithoutRewritingRawConfiguration() {
        for (String value : Arrays.asList(null, "", " ", "\t\r\n")) {
            Context context = new Context(ModelType.FLAT);
            context.setTargetRuntime(value);
            assertThat(context.getTargetRuntime()).isEqualTo(value);
            assertThat(context.getEffectiveTargetRuntime()).isEqualTo("MyBatis3");
            assertFactories(context, IntrospectedTableMyBatis3Impl.class);
        }
    }

    @Test
    void contextGetterOverridesRemainTheSourceForFactoryResolution() {
        Context context = new Context(ModelType.FLAT) {
            @Override
            public String getTargetRuntime() {
                return "MyBatis3Simple";
            }
        };
        assertThat(context.getEffectiveTargetRuntime()).isEqualTo("MyBatis3Simple");
        assertFactories(context, IntrospectedTableMyBatis3SimpleImpl.class);
    }

    @Test
    void currentDtdOmittedEmptyAndBlankRuntimeUseTheSameFactory() throws Exception {
        for (String attribute : Arrays.asList("", "targetRuntime=\"\"", "targetRuntime=\" &#x9; &#xA; \"",
                "targetRuntime=\"mYbAtIs3\"")) {
            var xml = """
                    <!DOCTYPE generatorConfiguration PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
                      "mybatis-generator-config_1_8_0.dtd">
                    <generatorConfiguration>
                      <context id="offline" appKeyword="test" moduleKeyword="test" moduleName="测试" jdkVersion="17" %s>
                        <jdbcConnection driverClass="must.not.connect.Driver" connectionURL="jdbc:must-not-connect:test"/>
                        <javaModelGenerator targetPackage="sample.entity" targetProject="src"/>
                        <sqlMapGenerator targetPackage="sample.mapper" targetProject="src"/>
                        <javaClientGenerator type="XMLMAPPER" targetPackage="sample.dao" targetProject="src"/>
                        <table tableName="article_" domainObjectName="Article"/>
                      </context>
                    </generatorConfiguration>
                    """.formatted(attribute);
            Context context = new ConfigurationParser(new ArrayList<>()).parseConfiguration(new StringReader(xml))
                    .getContexts().get(0);
            assertFactories(context, IntrospectedTableMyBatis3Impl.class);
        }
    }

    @Test
    void explicitBuiltInRuntimeAliasesPreserveCaseInsensitiveSelection() {
        Map<String, Class<? extends AbstractIntrospectedTable>> types = Map.of(
                "MyBatis3", IntrospectedTableMyBatis3Impl.class,
                "MyBatis3Simple", IntrospectedTableMyBatis3SimpleImpl.class,
                "MyBatis3DynamicSql", IntrospectedTableMyBatis3DynamicSqlImpl.class,
                "MyBatis3Kotlin", IntrospectedTableKotlinImpl.class);
        types.forEach((runtime, implementation) -> {
            for (String value : Arrays.asList(runtime, runtime.toLowerCase(java.util.Locale.ROOT))) {
                Context context = new Context(ModelType.FLAT);
                context.setTargetRuntime(value);
                assertThat(context.getEffectiveTargetRuntime()).isEqualTo(value);
                assertFactories(context, implementation);
            }
        });
    }

    @Test
    void explicitCustomImplementationsRemainCustomAndUnsupportedLockRuntimeFails() {
        Context context = new Context(ModelType.FLAT);
        context.setTargetRuntime(CustomSimpleTable.class.getName());
        assertFactories(context, CustomSimpleTable.class);
        var table = ObjectFactory.createIntrospectedTableForValidation(context);
        var tc = new TableConfiguration(context, new ArrayList<>());
        tc.setTableName("article_");
        tc.setSelectForUpdateEnabled(true);
        table.setTableConfiguration(tc);
        assertThatThrownBy(() -> SelectForUpdateValidator.validate(table))
                .hasMessageContaining("运行时").hasMessageContaining("不支持锁定查询");
    }

    @Test
    void customConstructionExceptionsRemainExplicitInsteadOfSelectingDefaultRuntime() {
        Context context = new Context(ModelType.FLAT);
        context.setTargetRuntime(FailingCustomTable.class.getName());
        assertThatThrownBy(() -> ObjectFactory.createIntrospectedTableForValidation(context))
                .isInstanceOf(RuntimeException.class).hasMessageContaining(FailingCustomTable.class.getName())
                .hasRootCauseInstanceOf(IllegalStateException.class).hasRootCauseMessage("自定义运行时构造失败");
        assertThatThrownBy(() -> ObjectFactory.createIntrospectedTable(
                new TableConfiguration(context, new ArrayList<>()), qualifiedTable(context), context))
                .hasRootCauseInstanceOf(IllegalStateException.class).hasRootCauseMessage("自定义运行时构造失败");
    }

    @Test
    void unknownOrPaddedRuntimeDoesNotSilentlyDefault() {
        for (String value : Arrays.asList("not.existing.Runtime", "MyBatis4", " MyBatis3 ")) {
            Context context = new Context(ModelType.FLAT);
            context.setTargetRuntime(value);
            assertThat(context.getEffectiveTargetRuntime()).isEqualTo(value);
            assertThatThrownBy(() -> ObjectFactory.createIntrospectedTableForValidation(context))
                    .isInstanceOf(RuntimeException.class);
            assertThatThrownBy(() -> ObjectFactory.createIntrospectedTable(
                    new TableConfiguration(context, new ArrayList<>()), qualifiedTable(context), context))
                    .isInstanceOf(RuntimeException.class);
            var errors = new ArrayList<String>();
            context.validate(errors);
            assertThat(errors).anyMatch(error -> error.contains("targetRuntime") && error.contains("invalid"));
        }
    }

    @Test
    void defaultRuntimeNowRequiresXmlWhileExplicitDynamicSqlStillDoesNot() {
        Context context = new Context(ModelType.FLAT);
        var client = new org.mybatis.generator.config.JavaClientGeneratorConfiguration();
        client.setConfigurationType("XMLMAPPER");
        context.setJavaClientGeneratorConfiguration(client);
        assertThat(ObjectFactory.createIntrospectedTableForValidation(context).requiresXmlGenerator()).isTrue();
        context.setTargetRuntime("MyBatis3DynamicSql");
        assertThat(ObjectFactory.createIntrospectedTableForValidation(context).requiresXmlGenerator()).isFalse();
    }

    @Test
    void rowBoundsPluginUsesEffectiveRuntimeWithoutChangingExplicitDynamicSqlRejection() {
        Context context = new Context(ModelType.FLAT);
        var plugin = new org.mybatis.generator.plugins.RowBoundsPlugin();
        plugin.setContext(context);
        var warnings = new ArrayList<String>();
        assertThat(plugin.validate(warnings)).isTrue();
        assertThat(warnings).isEmpty();
        context.setTargetRuntime("mYbAtIs3DyNaMiCsQl");
        assertThat(plugin.validate(warnings)).isFalse();
        assertThat(warnings).hasSize(1);
    }

    @Test
    void allNonMyBatis3BuiltInRuntimesAreRejectedBeforeOutputValidation() {
        for (String value : Arrays.asList("MyBatis3Simple", "mYbAtIs3DyNaMiCsQl", "MyBatis3Kotlin",
                CustomMyBatis3Table.class.getName())) {
            Context context = new Context(ModelType.FLAT);
            context.setTargetRuntime(value);
            var table = ObjectFactory.createIntrospectedTableForValidation(context);
            var tc = new TableConfiguration(context, new ArrayList<>());
            tc.setTableName("article_");
            tc.setSelectForUpdateEnabled(true);
            table.setTableConfiguration(tc);
            assertThatThrownBy(() -> SelectForUpdateValidator.validate(table))
                    .hasMessage("表 article_ enableSelectForUpdate：运行时 " + value
                            + " 不支持锁定查询，要求 MyBatis3 内省实现");
        }
    }

    private void assertFactories(Context context, Class<? extends AbstractIntrospectedTable> implementation) {
        var validation = ObjectFactory.createIntrospectedTableForValidation(context);
        assertThat(validation).isExactlyInstanceOf(implementation);
        assertThat(validation.getContext()).isSameAs(context);
        var tc = new TableConfiguration(context, new ArrayList<>());
        var qualified = qualifiedTable(context);
        var actual = ObjectFactory.createIntrospectedTable(tc, qualified, context);
        assertThat(actual).isExactlyInstanceOf(implementation);
        assertThat(actual.getContext()).isSameAs(context);
        assertThat(actual.getTableConfiguration()).isSameAs(tc);
        assertThat(actual.getFullyQualifiedTable()).isSameAs(qualified);
    }

    private FullyQualifiedTable qualifiedTable(Context context) {
        return new FullyQualifiedTable(null, null, "article_", "Article", null,
                true, null, null, null, false, null, context);
    }

    /**
     * 保留自定义运行时反射构造路径的简单查询实现。
     *
     * @author vgosoft
     */
    public static class CustomSimpleTable extends IntrospectedTableMyBatis3SimpleImpl {
    }

    /**
     * 验证自定义派生实现仍由工厂构造，但不自动授予锁查询输出契约。
     *
     * @author vgosoft
     */
    public static class CustomMyBatis3Table extends IntrospectedTableMyBatis3Impl {
    }

    /**
     * 验证工厂保留自定义实现的构造错误，不静默回退。
     *
     * @author vgosoft
     */
    public static class FailingCustomTable extends IntrospectedTableMyBatis3Impl {
        public FailingCustomTable() {
            throw new IllegalStateException("自定义运行时构造失败");
        }
    }
}
