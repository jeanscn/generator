package org.mybatis.generator.offline;

import com.vgosoft.core.db.query.DatabaseType;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.api.dom.xml.render.ElementRenderer;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.codegen.mybatis3.controller.elements.DeleteByTableGenerator;
import org.mybatis.generator.codegen.mybatis3.controller.elements.InsertByTableGenerator;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.codegen.mybatis3.service.ManagedRelationGeneration;
import org.mybatis.generator.codegen.mybatis3.service.elements.DeleteByTableElement;
import org.mybatis.generator.codegen.mybatis3.service.elements.InsertByTableElement;
import org.mybatis.generator.codegen.mybatis3.service.elements.SelectByTableElement;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlSchemaScriptGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.DeleteByTableElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.InsertByTableElementGenerator;
import org.mybatis.generator.config.EnableCacheConfiguration;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.config.xml.MyBatisGeneratorConfigurationParser;
import org.mybatis.generator.exception.XMLParserException;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Properties;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ManagedRelationGenerationTest {
    @Test
    void addsDeferredInvalidationToEntityWritesWithoutChangingBodiesOrLegacyRelationWrites() {
        IntrospectedTableMyBatis3Impl fixture = fixture(relation(false));
        SelectByTableGeneratorConfiguration other = relation(true);
        other.setRelationIdKey(null);
        other.setTableName("org_role_user");
        other.getCacheConfigurationList().get(0).setCacheNames(List.of("roleIdUserIdsCache#72h"));
        fixture.getTableConfiguration().getSelectByTableGeneratorConfiguration().add(other);
        String input = """
                class Fixture {
                    @CacheEvict(cacheNames={"SysMenu#8h","roleIdMenuIdsCache#72h","menuIdRoleIdsCache#72h","roleIdMenuIdsCacheOther#72h"},allEntries=true,condition="#p0 != null")
                    public int update(String id) { return mapper.update(id); }

                    @CacheEvict(cacheNames={"otherCache#72h"},allEntries=true)
                    public int insertByTableForMenu(String id) { return mapper.union(id); }
                }
                """;
        TopLevelClass parent = new TopLevelClass("Fixture");
        String output = ManagedRelationOfflineRegenerator.regenerateEntityMutationAnnotations(input, fixture, parent);
        assertThat(output).contains("@RelationMutation(relation = \"org_role_menu\"",
                "orgRoleMenuRelations#72h", "public int update(String id) { return mapper.update(id); }");
        MethodDeclaration updated = StaticJavaParser.parse(output).findAll(MethodDeclaration.class).stream()
                .filter(method -> method.getNameAsString().equals("update")).findFirst().orElseThrow();
        assertThat(updated.getAnnotationByName("CacheEvict").orElseThrow().toString())
                .contains("SysMenu#8h", "orgRoleMenuRelations#72h", "roleIdUserIdsCache#72h",
                        "roleIdMenuIdsCacheOther#72h", "#p0 != null")
                .doesNotContain("\"roleIdMenuIdsCache#72h\"", "\"menuIdRoleIdsCache#72h\"");
        assertThat(updated.getAnnotationByName("RelationMutation").orElseThrow().toString())
                .doesNotContain("roleIdMenuIdsCache", "menuIdRoleIdsCache", "SysMenu#8h", "roleIdUserIdsCache");
        assertThat(output.substring(output.indexOf("@CacheEvict(cacheNames={\"otherCache")))
                .isEqualTo(input.substring(input.indexOf("@CacheEvict(cacheNames={\"otherCache")));
        assertThat(ManagedRelationOfflineRegenerator.regenerateEntityMutationAnnotations(output, fixture, parent)).isEqualTo(output);
    }

    @Test
    void rejectsMissingSharedCacheUnsupportedPrimaryKeyAndInvalidColumnMappings() {
        SelectByTableGeneratorConfiguration relation = relation(false);
        IntrospectedTableMyBatis3Impl fixture = fixture(relation);
        relation.getCacheConfigurationList().clear();
        assertThatThrownBy(() -> ManagedRelationGeneration.validate(fixture, relation))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("explicit enabled cacheNames");
        SelectByTableGeneratorConfiguration validRelation = relation(false);
        fixture.getPrimaryKeyColumns().clear();
        assertThatThrownBy(() -> ManagedRelationGeneration.validate(fixture, validRelation))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("single String");
        fixture.getPrimaryKeyColumns().add(ManagedRelationOfflineRegenerator.column("id_", "id", "", false));
        validRelation.getThisColumn().setActualColumnName("unrelated_id");
        assertThatThrownBy(() -> ManagedRelationGeneration.validate(fixture, validRelation))
                .isInstanceOf(IllegalArgumentException.class);
        SelectByTableGeneratorConfiguration invalidKey = relation(false);
        invalidKey.setRelationIdKey("invalid key");
        assertThatThrownBy(() -> ManagedRelationGeneration.validate(fixture, invalidKey))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("relationIdKey");
    }

    @Test
    void parserReadsManagedMetadataAndBothCacheEnableSpellings() throws Exception {
        MyBatisGeneratorConfigurationParser parser = new MyBatisGeneratorConfigurationParser(new Properties(), new ArrayList<>());
        java.lang.reflect.Method cacheParser = MyBatisGeneratorConfigurationParser.class.getDeclaredMethod("parseEnableCache", Node.class);
        cacheParser.setAccessible(true);
        EnableCacheConfiguration disabled = (EnableCacheConfiguration) cacheParser.invoke(parser, element("<enableCache enable=\"false\"/>"));
        assertThat(disabled.isEnableCache()).isFalse();
        EnableCacheConfiguration alias = (EnableCacheConfiguration) cacheParser.invoke(parser,
                element("<enableCache enable=\"false\" enableCache=\"true\"/>"));
        assertThat(alias.isEnableCache()).isTrue();
        IntrospectedTableMyBatis3Impl fixture = fixture(relation(false));
        TableConfiguration configuration = fixture.getTableConfiguration();
        java.lang.reflect.Method relationParser = MyBatisGeneratorConfigurationParser.class.getDeclaredMethod("parseSelectByTable", TableConfiguration.class, Node.class);
        relationParser.setAccessible(true);
        relationParser.invoke(parser, configuration, element("""
                <selectByTable table="org_role_menu" thisColumn="role_id" otherColumn="menu_id"
                  methodSuffix="ForMenu" relationIdKey="org_role_menu">
                  <enableCache cacheNames="orgRoleMenuRelations#72h"/>
                </selectByTable>
                """));
        SelectByTableGeneratorConfiguration parsed = configuration.getSelectByTableGeneratorConfiguration().get(1);
        assertThat(parsed.isManagedRelation()).isTrue();
        assertThat(parsed.getRelationIdKey()).isEqualTo("org_role_menu");
        assertThat(parsed.getCacheConfigurationList().get(0).getCacheNames()).containsExactly("orgRoleMenuRelations#72h");
    }

    private static org.w3c.dom.Element element(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml))).getDocumentElement();
    }

    @Test
    void currentDtdAcceptsRelationIdKeyAndRejectsRemovedAttributes() throws Exception {
        String configuration = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE generatorConfiguration
                  PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
                  "mybatis-generator-config_1_8_0.dtd">
                <generatorConfiguration>
                  <context id="offline" appKeyword="system" moduleKeyword="system" moduleName="system" jdkVersion="21">
                    <jdbcConnection driverClass="unused.Driver" connectionURL="jdbc:unused"/>
                    <table tableName="sys_menu">
                      <selectByTable table="org_role_menu" thisColumn="menu_id" otherColumn="role_id"
                        methodSuffix="ForRole" relationIdKey="org_role_menu">
                        <enableCache cacheNames="orgRoleMenuRelations#72h"/>
                      </selectByTable>
                    </table>
                  </context>
                </generatorConfiguration>
                """;
        ConfigurationParser parser = new ConfigurationParser(new Properties(), new ArrayList<>());
        var parsed = parser.parseConfiguration(new StringReader(configuration)).getContexts().get(0)
                .getTableConfigurations().get(0).getSelectByTableGeneratorConfiguration().get(0);
        assertThat(parsed.getRelationIdKey()).isEqualTo("org_role_menu");
        assertThat(parsed.isManagedRelation()).isTrue();
        assertThatThrownBy(() -> parser.parseConfiguration(new StringReader(
                configuration.replace("relationIdKey=", "relationId="))))
                .isInstanceOf(XMLParserException.class);
        assertThatThrownBy(() -> parser.parseConfiguration(new StringReader(
                configuration.replace("relationIdKey=", "legacyCacheNames=\"retired#72h\" relationIdKey="))))
                .isInstanceOf(XMLParserException.class);
    }

    @Test
    void generatesPortableRowsForEveryDatabaseAndBothDirections() {
        for (boolean reverse : List.of(false, true)) {
            SelectByTableGeneratorConfiguration relation = relation(reverse);
            IntrospectedTableMyBatis3Impl fixture = fixture(relation);
            for (DatabaseType type : DatabaseType.values()) {
                fixture.getTableConfiguration().getAdditionalDbTypes().clear();
                fixture.getTableConfiguration().getAdditionalDbTypes().add(type);
                XmlElement root = new XmlElement("mapper");
                InsertByTableElementGenerator generator = new InsertByTableElementGenerator();
                ManagedRelationOfflineRegenerator.configure(generator, fixture);
                boolean supported = switch (type) {
                    case MYSQL, MARIADB, ORACLE, POSTGRE_SQL, DM, HIGHGO, KINGBASE_ES, GAUSS_DB, UXDB,
                            H2, HSQL, SQL_SERVER, SQL_SERVER2005, SQLITE -> true;
                    default -> false;
                };
                if (!supported) {
                    assertThatThrownBy(() -> generator.addElements(root)).isInstanceOf(IllegalArgumentException.class)
                            .hasMessageContaining(type.getCode());
                    assertThat(root.getElements()).isEmpty();
                    continue;
                }
                generator.addElements(root);
                String generated = xml(root);
                assertThat(root.getElements()).hasSize(2);
                assertThat(generated).contains("insert into org_role_menu (id_, " + relation.getPrimaryKeyColumn()
                                + ", " + relation.getOtherPrimaryKeyColumn() + ")",
                        "values (#{relationId,jdbcType=VARCHAR}", "select id_ from org_role_menu",
                        "resultType=\"java.lang.String\"", "useCache=\"false\"", "flushCache=\"true\"", "@mbg.generated");
                assertThat(generated.toLowerCase()).doesNotContain("md5", "concat(", "insert ignore",
                        "on conflict", "not exists", "count(1)");
                assertThat(generated).contains("databaseId=\"" + type.getCode() + "\"");
                if (type == DatabaseType.SQL_SERVER || type == DatabaseType.SQL_SERVER2005) {
                    assertThat(generated).contains("with (updlock, holdlock)").doesNotContain("for update");
                } else if (type == DatabaseType.SQLITE) {
                    assertThat(generated).doesNotContain("for update", "updlock");
                } else {
                    assertThat(generated).contains("for update");
                }
            }
        }
    }

    @Test
    void generatesManagedWriteAndReadAnnotationsWithOnlySharedRegion() throws Exception {
        for (boolean reverse : List.of(false, true)) {
            SelectByTableGeneratorConfiguration relation = relation(reverse);
            TopLevelClass service = service(fixture(relation));
            String generated = service.getMethods().stream()
                    .map(method -> ManagedRelationOfflineRegenerator.renderMethod(method, service, false))
                    .collect(Collectors.joining("\n"));
            assertThat(generated).contains("@RelationMutation(relation = \"org_role_menu\"",
                    "@Cacheable", "orgRoleMenuRelations#72h",
                    "'vgosoft-cache-key-v3'", "getCurrentTenantId", "'java.lang.String',#p0",
                    "isActualTransactionActive()", "RelationWriteSupport.normalize(", "relationWriteSupport.union(",
                    "mapper." + relation.getRowInsertMethodName(), "mapper." + relation.getExistsMethodName());
            assertThat(generated).contains("otherId) != null);").doesNotContain("otherId) > 0");
            assertThat(generated).doesNotContain("@CacheEvict", "@RelationRead", "vgosoft-relation-cache-key",
                    "roleIdMenuIdsCache", "menuIdRoleIdsCache");
            assertThat(generated).contains("(otherId, relationId) ->", "otherId, relationId)");
            Method read = service.getMethods().stream().filter(method -> method.getName().equals(relation.getMethodName()))
                    .findFirst().orElseThrow();
            String cache = read.getAnnotations().stream().filter(annotation -> annotation.startsWith("@Cacheable")).findFirst().orElseThrow();
            MethodDeclaration parsed = StaticJavaParser.parse("class Fixture { " + cache + " void read() {} }")
                    .findFirst(MethodDeclaration.class).orElseThrow();
            parsed.getAnnotationByName("Cacheable").orElseThrow().asNormalAnnotationExpr().getPairs().stream()
                    .filter(pair -> pair.getNameAsString().equals("key") || pair.getNameAsString().equals("condition"))
                    .forEach(pair -> new SpelExpressionParser().parseExpression(pair.getValue().asStringLiteralExpr().asString()));
            assertThat(cache).contains("#p0").doesNotContain("+ #");
            relation.setRelationIdKey(null);
            TopLevelClass legacyService = service(fixture(relation));
            Method legacyRead = legacyService.getMethods().stream()
                    .filter(method -> method.getName().equals(relation.getMethodName())).findFirst().orElseThrow();
            String legacyCache = legacyRead.getAnnotations().stream()
                    .filter(annotation -> annotation.startsWith("@Cacheable")).findFirst().orElseThrow();
            String managedKey = parsed.getAnnotationByName("Cacheable").orElseThrow().asNormalAnnotationExpr()
                    .getPairs().stream().filter(pair -> pair.getNameAsString().equals("key"))
                    .findFirst().orElseThrow().getValue().asStringLiteralExpr().asString();
            var legacyAnnotation = StaticJavaParser.parse("class Fixture { " + legacyCache + " void read() {} }")
                    .findFirst(MethodDeclaration.class).orElseThrow().getAnnotationByName("Cacheable")
                    .orElseThrow().asNormalAnnotationExpr();
            assertThat(legacyAnnotation.getPairs().stream().filter(pair -> pair.getNameAsString().equals("key"))
                    .findFirst().orElseThrow().getValue().asStringLiteralExpr().asString()).isEqualTo(managedKey);
            assertThat(service.getFields()).extracting(field -> field.getName()).contains("relationWriteSupport", "VGOSOFT_CACHE_KEY_MAPPER");
        }
    }

    @Test
    void retainsLegacyBatchGenerationAndHonorsPerRelationSqlSwitches() {
        SelectByTableGeneratorConfiguration managed = relation(false);
        SelectByTableGeneratorConfiguration disabled = relation(true);
        disabled.setEnableSplit(false);
        disabled.setEnableUnion(false);
        IntrospectedTableMyBatis3Impl fixture = fixture(managed);
        fixture.getTableConfiguration().getSelectByTableGeneratorConfiguration().add(disabled);
        XmlElement root = new XmlElement("mapper");
        InsertByTableElementGenerator insert = new InsertByTableElementGenerator();
        ManagedRelationOfflineRegenerator.configure(insert, fixture);
        insert.addElements(root);
        DeleteByTableElementGenerator delete = new DeleteByTableElementGenerator();
        ManagedRelationOfflineRegenerator.configure(delete, fixture);
        delete.addElements(root);
        assertThat(xml(root)).doesNotContain("ForMenu");
        managed.setRelationIdKey(null);
        root = new XmlElement("mapper");
        insert.addElements(root);
        assertThat(xml(root)).contains("md5(", "insert ignore into org_role_menu", "on conflict do nothing");
    }

    @Test
    void schemaOverlayGeneratesNotNullUniqueAndReverseIndexWithoutFk() {
        IntrospectedTableMyBatis3Impl fixture = ManagedRelationOfflineRegenerator.fixture("organization", "RoleMenu", "org_role_menu", null);
        fixture.setRemarks("角色和菜单关联表");
        fixture.getBaseColumns().add(ManagedRelationOfflineRegenerator.column("role_id", "roleId", "角色ID", true));
        fixture.getBaseColumns().add(ManagedRelationOfflineRegenerator.column("menu_id", "menuId", "菜单ID", true));
        fixture.getTableConfiguration().setRelationKeyColumns(List.of("role_id", "menu_id"));
        for (DatabaseType database : List.of(DatabaseType.MYSQL, DatabaseType.POSTGRE_SQL, DatabaseType.DM)) {
            String ddl = new SqlSchemaScriptGenerator(fixture, database).getSqlScript();
            assertThat(ddl).contains("uk_org_role_menu", "idx_org_role_menu_reverse", "UNIQUE", "NOT NULL");
            assertThat(ddl).doesNotContain("FOREIGN KEY", "tenant");
        }
        assertThat(fixture.getIndexes()).hasSize(2);
        assertThat(fixture.getColumn("role_id").orElseThrow().isNullable()).isFalse();
        fixture.getTableConfiguration().setRelationKeyColumns(List.of("role_id", "role_id"));
        assertThatThrownBy(() -> new SqlSchemaScriptGenerator(fixture, DatabaseType.MYSQL).getSqlScript())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void managedControllersUseJsonEvenWithoutVueAndKeepIncrementalRoutes() {
        IntrospectedTableMyBatis3Impl fixture = fixture(relation(false));
        TopLevelClass controller = new TopLevelClass("sample.Controller");
        InsertByTableGenerator union = new InsertByTableGenerator();
        ManagedRelationOfflineRegenerator.configure(union, fixture);
        union.setCommentGenerator(fixture.getContext().getCommentGenerator());
        union.setServiceBeanName("sysMenuImpl");
        union.addElements(controller);
        DeleteByTableGenerator split = new DeleteByTableGenerator();
        ManagedRelationOfflineRegenerator.configure(split, fixture);
        split.setCommentGenerator(fixture.getContext().getCommentGenerator());
        split.setServiceBeanName("sysMenuImpl");
        split.addElements(controller);
        String generated = controller.getMethods().stream()
                .map(method -> ManagedRelationOfflineRegenerator.renderMethod(method, controller, false))
                .collect(Collectors.joining("\n"));
        assertThat(generated).contains("@RequestParam String menuId", "@RequestBody List<String> roleIds",
                "union/for-role", "split/for-role", "ResponseResult<Long>");
        assertThat(generated).doesNotContain("@RequestParamSplit", "save/");
    }

    @Test
    void selectedMethodSplicingPreservesOtherGeneratedMarkersAndManualContent() {
        String input = "package sample;\n\nclass Sample {\n\n"
                + "    /** @VCF.generated */\n    @Cacheable(key = \"union(id)\")\n    @Override\n    public int union(String id) {\n"
                + "        return 0;\n    }\n\n"
                + "    /** @mbg.generated untouched */\n    public String manual() {\n        return \"{}\";\n    }\n}\n";
        String replaced = ManagedRelationOfflineRegenerator.spliceMethod(input, "union",
                "    /** @VCF.generated */\n    public int union(String id) {\n        return 1;\n    }", true);
        assertThat(replaced.substring(replaced.indexOf("    /** @mbg.generated untouched */")))
                .isEqualTo(input.substring(input.indexOf("    /** @mbg.generated untouched */")));
        assertThatThrownBy(() -> ManagedRelationOfflineRegenerator.spliceMethod(input, "missing", "", true))
                .isInstanceOf(IllegalArgumentException.class);
        String windows = input.replace("\n", "\r\n");
        String windowsReplaced = ManagedRelationOfflineRegenerator.spliceMethod(windows, "union",
                "    public int union(String id) {\n        return 1;\n    }", true);
        assertThat(windowsReplaced).startsWith("package sample;\r\n\r\nclass Sample {")
                .contains("return 1;\r\n")
                .endsWith(windows.substring(windows.indexOf("    /** @mbg.generated untouched */")));
    }

    @Test
    void regeneratesSelectedBusinessOutputsOfflineWhenExplicitlyRequested() throws Exception {
        String basic = System.getProperty("offline.basicRoot");
        String organization = System.getProperty("offline.organizationModule");
        assumeTrue(basic != null && organization != null);
        ManagedRelationOfflineRegenerator.regenerate(Path.of(basic), Path.of(organization),
                Path.of("target\\offline-relations"), Boolean.getBoolean("offline.write"));
        try (var staged = Files.list(Path.of("target\\offline-relations"))) {
            for (Path java : staged.filter(path -> path.toString().endsWith(".java")).toList()) {
                var unit = StaticJavaParser.parse(Files.readString(java));
                if (!java.getFileName().toString().endsWith("Impl.java")) {
                    continue;
                }
                List<MethodDeclaration> entityWrites = unit.findAll(MethodDeclaration.class).stream()
                        .filter(method -> method.getAnnotationByName("CacheEvict").isPresent())
                        .filter(method -> !method.getNameAsString().startsWith("insertByTable")
                                && !method.getNameAsString().startsWith("deleteByTable")).toList();
                assertThat(entityWrites).isNotEmpty();
                for (MethodDeclaration method : entityWrites) {
                    assertThat(method.getAnnotationByName("CacheEvict").orElseThrow().toString())
                            .contains("orgRoleMenuRelations#72h",
                                    java.getFileName().toString().contains("SysMenu") ? "SysMenu#8h" : "OrgRole#8h")
                            .doesNotContain("roleIdMenuIdsCache", "menuIdRoleIdsCache");
                    assertThat(method.getAnnotationByName("RelationMutation").orElseThrow().toString())
                            .contains("org_role_menu", "orgRoleMenuRelations#72h")
                            .doesNotContain("roleIdMenuIdsCache", "menuIdRoleIdsCache");
                }
            }
        }
        if (Boolean.getBoolean("offline.write")) {
            java.util.Map<Path, String> before = new java.util.LinkedHashMap<>();
            try (var staged = Files.list(Path.of("target\\offline-relations"))) {
                for (Path file : staged.toList()) {
                    before.put(file, Files.readString(file));
                }
            }
            ManagedRelationOfflineRegenerator.regenerate(Path.of(basic), Path.of(organization),
                    Path.of("target\\offline-relations"), false);
            for (var file : before.entrySet()) {
                assertThat(Files.readString(file.getKey())).as("Idempotent regeneration of %s", file.getKey())
                        .isEqualTo(file.getValue());
            }
        }
    }

    private static String xml(XmlElement element) {
        return element.accept(new ElementRenderer()).collect(Collectors.joining("\n"));
    }

    private static TopLevelClass service(IntrospectedTableMyBatis3Impl fixture) throws Exception {
        TopLevelClass service = new TopLevelClass("sample.Service");
        for (AbstractServiceElementGenerator generator : List.of(new SelectByTableElement(), new DeleteByTableElement(), new InsertByTableElement())) {
            ManagedRelationOfflineRegenerator.configure(generator, fixture);
            java.lang.reflect.Method initialize = AbstractServiceElementGenerator.class.getDeclaredMethod("initGenerator");
            initialize.setAccessible(true);
            initialize.invoke(generator);
            generator.addElements(service);
        }
        return service;
    }

    private static IntrospectedTableMyBatis3Impl fixture(SelectByTableGeneratorConfiguration relation) {
        IntrospectedTableMyBatis3Impl fixture = ManagedRelationOfflineRegenerator.fixture("system", "SysMenu", "sys_menu", relation);
        fixture.setRemarks("菜单管理");
        return fixture;
    }

    private static SelectByTableGeneratorConfiguration relation(boolean reverse) {
        SelectByTableGeneratorConfiguration relation = new SelectByTableGeneratorConfiguration();
        relation.setRelationIdKey("org_role_menu");
        relation.setTableName("org_role_menu");
        relation.setMethodSuffix(reverse ? "ForMenu" : "ForRole");
        relation.setPrimaryKeyColumn(reverse ? "role_id" : "menu_id");
        relation.setOtherPrimaryKeyColumn(reverse ? "menu_id" : "role_id");
        relation.setThisColumn(ManagedRelationOfflineRegenerator.column(relation.getPrimaryKeyColumn(),
                reverse ? "roleId" : "menuId", "当前ID", false));
        relation.setOtherColumn(ManagedRelationOfflineRegenerator.column(relation.getOtherPrimaryKeyColumn(),
                reverse ? "menuId" : "roleId", "对端ID", false));
        relation.setParameterName(relation.getOtherColumn().getJavaProperty());
        EnableCacheConfiguration cache = new EnableCacheConfiguration();
        cache.setCacheNames(List.of("orgRoleMenuRelations#72h"));
        relation.getCacheConfigurationList().add(cache);
        return relation;
    }
}
