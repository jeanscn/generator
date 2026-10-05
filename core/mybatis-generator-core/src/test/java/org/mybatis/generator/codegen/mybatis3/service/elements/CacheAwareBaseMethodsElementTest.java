package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.EnableCacheConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.config.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.rules.ConditionalModelRules;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.ToolProvider;

import static org.assertj.core.api.Assertions.assertThat;

class CacheAwareBaseMethodsElementTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldGuardEmptyResultsOnlyForCollectionQueries() {
        Context context = new Context(ModelType.CONDITIONAL);
        JavaModelGeneratorConfiguration model = new JavaModelGeneratorConfiguration();
        model.setTargetPackage("sample.entity");
        context.setJavaModelGeneratorConfiguration(model);
        TableConfiguration user = table(context, model, "user_");
        for (String method : List.of("selectBaseByPrimaryKey", "selectByName")) {
            SelectByColumnGeneratorConfiguration query = new SelectByColumnGeneratorConfiguration();
            query.setMethodName(method);
            IntrospectedColumn column = new IntrospectedColumn();
            column.setJavaProperty("id");
            column.setActualColumnName("id_");
            column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
            query.addColumn(column);
            user.getSelectByColumnGeneratorConfigurations().add(query);
        }
        TestIntrospectedTable introspectedTable = new TestIntrospectedTable();
        introspectedTable.setContext(context);
        introspectedTable.setTableConfiguration(user);
        introspectedTable.setRules(new ConditionalModelRules(introspectedTable) {
            @Override
            public boolean isGenerateCachePo() {
                return true;
            }
        });
        TestSelectElement element = new TestSelectElement();
        element.setContext(context);
        element.setIntrospectedTable(introspectedTable);
        element.initialize();
        TopLevelClass generated = new TopLevelClass("sample.codegen.AbstractUserImpl");
        element.addElements(generated);
        assertThat(generated.getMethods().stream().filter(method -> "selectBaseByPrimaryKey".equals(method.getName()))
                .findFirst().orElseThrow().getAnnotations().toString()).doesNotContain("#result.isEmpty()");
        assertThat(generated.getMethods().stream().filter(method -> "selectByName".equals(method.getName()))
                .findFirst().orElseThrow().getAnnotations().toString())
                .contains("#result instanceof T(java.util.Collection) && #result.isEmpty()");
    }

    @Test
    void shouldGenerateCompilableCacheEvictOverridesForInheritedMutations() throws Exception {
        Context context = new Context(ModelType.CONDITIONAL);
        JavaModelGeneratorConfiguration model = new JavaModelGeneratorConfiguration();
        model.setTargetPackage("sample.entity");
        context.setJavaModelGeneratorConfiguration(model);

        TableConfiguration user = table(context, model, "user_");
        user.setAlias("usr");
        addCachedQuery(user, "user_role", "userRolesCache");
        TableConfiguration role = table(context, model, "role");
        addCachedQuery(role, "user_", "usersByRoleCache");

        TestIntrospectedTable introspectedTable = new TestIntrospectedTable();
        introspectedTable.setContext(context);
        introspectedTable.setTableConfiguration(user);
        introspectedTable.setRules(new ConditionalModelRules(introspectedTable));

        TestElement element = new TestElement();
        element.setContext(context);
        element.setIntrospectedTable(introspectedTable);
        element.initialize();

        TopLevelClass generated = new TopLevelClass(new FullyQualifiedJavaType("sample.codegen.AbstractUserImpl"));
        generated.setSuperClass(new FullyQualifiedJavaType("sample.codegen.Base"));
        element.addElements(generated);

        assertThat(generated.getMethods()).extracting(Method::getName)
                .contains("insertBySql", "insertOrUpdateSelective", "updateByExampleSelectiveAndSelect");
        Method insertBySql = generated.getMethods().stream()
                .filter(method -> method.getName().equals("insertBySql"))
                .findFirst().orElseThrow();
        assertThat(insertBySql.getAnnotations())
                .anySatisfy(annotation -> assertThat(annotation)
                        .contains("userRolesCache#72h", "usersByRoleCache#72h")
                        .contains("#p0.table == null"));
        TestUpdateSqlElement updateElement = new TestUpdateSqlElement();
        updateElement.setContext(context);
        updateElement.setIntrospectedTable(introspectedTable);
        updateElement.initialize();
        TopLevelClass updateClass = new TopLevelClass("sample.codegen.UpdateUserImpl");
        updateElement.addElements(updateClass);
        assertThat(updateClass.getMethods().get(0).getAnnotations())
                .containsAll(insertBySql.getAnnotations());
        assertThat(insertBySql.getAnnotations().toString()).contains("user_ as usr");

        String source = new DefaultJavaFormatter().getFormattedContent(generated);
        CompilationUnit parsed = StaticJavaParser.parse(source);
        assertThat(parsed.findAll(com.github.javaparser.ast.body.MethodDeclaration.class))
                .extracting(com.github.javaparser.ast.body.MethodDeclaration::getNameAsString)
                .contains("insertBySql", "insertOrUpdateSelective", "updateByExampleSelectiveAndSelect");

        Path generatedSource = temporaryDirectory.resolve("AbstractUserImpl.java");
        Files.writeString(generatedSource, source);
        Path userSource = temporaryDirectory.resolve("User.java");
        Files.writeString(userSource, "package sample.entity; public class User {}");
        Path exampleSource = temporaryDirectory.resolve("UserExample.java");
        Files.writeString(exampleSource, "package sample.entity; public class UserExample {}");
        Path baseSource = temporaryDirectory.resolve("Base.java");
        Files.writeString(baseSource, """
                package sample.codegen;
                import com.vgosoft.core.adapter.ServiceResult;
                import com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder;
                import sample.entity.User;
                import sample.entity.UserExample;
                import java.util.List;
                public class Base {
                    public ServiceResult<Integer> insertBySql(InsertSqlBuilder builder) { return null; }
                    public ServiceResult<User> insertOrUpdateSelective(User record) { return null; }
                    public ServiceResult<List<User>> updateByExampleSelectiveAndSelect(User record, UserExample example) { return null; }
                }
                """);
        assertThat(ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "-proc:none",
                "-classpath", System.getProperty("java.class.path"),
                "-d", temporaryDirectory.toString(), generatedSource.toString(), userSource.toString(),
                exampleSource.toString(), baseSource.toString())).isZero();
    }

    private TableConfiguration table(Context context, JavaModelGeneratorConfiguration model, String tableName) {
        TableConfiguration table = new TableConfiguration(context, new ArrayList<>());
        table.setTableName(tableName);
        table.setDomainObjectName(tableName.equals("user_") ? "User" : "Role");
        table.setJavaModelGeneratorConfiguration(model);
        context.addTableConfiguration(table);
        return table;
    }

    private void addCachedQuery(TableConfiguration table, String relationTable, String cacheName) {
        SelectByTableGeneratorConfiguration query = new SelectByTableGeneratorConfiguration();
        query.setTableName(relationTable);
        query.setParameterName("recordIds");
        IntrospectedColumn thisColumn = new IntrospectedColumn();
        thisColumn.setJavaProperty("userId");
        query.setThisColumn(thisColumn);
        EnableCacheConfiguration cache = new EnableCacheConfiguration();
        cache.setCacheNames(List.of(cacheName));
        query.getCacheConfigurationList().add(cache);
        table.getSelectByTableGeneratorConfiguration().add(query);
    }

    private static final class TestElement extends CacheAwareBaseMethodsElement {
        private void initialize() {
            initGenerator();
        }
    }

    private static final class TestSelectElement extends SelectByColumnElement {
        private void initialize() {
            initGenerator();
        }
    }

    private static final class TestUpdateSqlElement extends UpdateBySqlElement {
        private void initialize() {
            initGenerator();
        }
    }

    private static final class TestIntrospectedTable extends IntrospectedTableMyBatis3Impl {
        @Override
        public String getBaseRecordType() {
            return "sample.entity.User";
        }

        @Override
        public String getExampleType() {
            return "sample.entity.UserExample";
        }

        @Override
        public String getControllerBeanName() {
            return "userService";
        }
    }
}
