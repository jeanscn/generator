package org.mybatis.generator.codegen.mybatis3.service;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.EnableCacheConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.JavaServiceImplGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.rules.ConditionalModelRules;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrimaryKeyRelationCacheGenerationTest {

    @Test
    void shouldGenerateWriteWrappersCacheConfigAndDeletionEvictionForIdOnlyQueries() throws Exception {
        Context context = new Context(ModelType.CONDITIONAL);
        JavaModelGeneratorConfiguration model = new JavaModelGeneratorConfiguration();
        model.setTargetPackage("sample.entity");
        context.setJavaModelGeneratorConfiguration(model);
        TableConfiguration role = new TableConfiguration(context, new ArrayList<>());
        role.setTableName("org_role");
        role.setDomainObjectName("OrgRole");
        role.setJavaModelGeneratorConfiguration(model);
        role.setJavaServiceImplGeneratorConfiguration(new JavaServiceImplGeneratorConfiguration(context));
        context.addTableConfiguration(role);

        SelectByTableGeneratorConfiguration query = new SelectByTableGeneratorConfiguration();
        query.setTableName("org_r_role_permission");
        query.setReturnTypeParam("primaryKey");
        EnableCacheConfiguration cache = new EnableCacheConfiguration();
        cache.setCacheNames(List.of("permissionIdRoleIdsCache#12h"));
        query.getCacheConfigurationList().add(cache);
        role.getSelectByTableGeneratorConfiguration().add(query);

        IntrospectedTableMyBatis3Impl introspected = new IntrospectedTableMyBatis3Impl();
        introspected.setContext(context);
        introspected.setTableConfiguration(role);
        introspected.setBaseRecordType("sample.entity.OrgRole");
        introspected.setExampleType("sample.entity.OrgRoleExample");
        introspected.setDeleteByPrimaryKeyStatementId("deleteByPrimaryKey");
        IntrospectedColumn id = new IntrospectedColumn();
        id.setActualColumnName("id_");
        id.setJavaProperty("id");
        id.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
        introspected.getPrimaryKeyColumns().add(id);
        introspected.setRules(new ConditionalModelRules(introspected));
        assertThat(introspected.getRules().isGenerateCachePo()).isFalse();

        JavaServiceImplGenerator generator = new JavaServiceImplGenerator(".");
        generator.setContext(context);
        generator.setIntrospectedTable(introspected);
        generator.tc = role;
        generator.entityType = new FullyQualifiedJavaType(introspected.getBaseRecordType());
        java.lang.reflect.Field configuration = JavaServiceImplGenerator.class
                .getDeclaredField("javaServiceImplGeneratorConfiguration");
        configuration.setAccessible(true);
        configuration.set(generator, role.getJavaServiceImplGeneratorConfiguration());

        TopLevelClass generated = new TopLevelClass("sample.codegen.AbstractOrgRoleImpl");
        assertThat(generator.addCacheAwareBaseMethodsAndConfig(generated)).isTrue();
        assertThat(generated.getAnnotations()).contains("@CacheConfig(cacheManager = CACHE_MANAGER_NAME)");
        assertThat(generated.getMethods()).extracting(Method::getName)
                .containsExactly("insertBySql", "insertOrUpdateSelective", "updateByExampleSelectiveAndSelect");
        for (Method method : generated.getMethods()) {
            assertThat(method.getAnnotations().toString()).contains("permissionIdRoleIdsCache#12h");
        }

        java.lang.reflect.Method deleteGenerator = JavaServiceImplGenerator.class
                .getDeclaredMethod("addDeleteByPrimaryKeyElement", TopLevelClass.class);
        deleteGenerator.setAccessible(true);
        deleteGenerator.invoke(generator, generated);
        Method deletion = generated.getMethods().stream()
                .filter(method -> method.getName().equals("deleteByPrimaryKey"))
                .findFirst().orElseThrow();
        assertThat(deletion.getAnnotations().toString())
                .contains("permissionIdRoleIdsCache#12h", "allEntries = true")
                .doesNotContain("OrgRole#8h");
    }
}
