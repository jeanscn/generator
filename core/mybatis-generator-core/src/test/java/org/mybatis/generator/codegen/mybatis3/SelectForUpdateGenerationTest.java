package org.mybatis.generator.codegen.mybatis3;

import com.github.javaparser.StaticJavaParser;
import com.vgosoft.core.db.query.DatabaseType;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.DefaultXmlFormatter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.javamapper.JavaMapperGenerator;
import org.mybatis.generator.codegen.mybatis3.service.JavaServiceGenerator;
import org.mybatis.generator.codegen.mybatis3.service.JavaServiceImplGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.XMLMapperGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.SelectForUpdateGuardGenerator;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.internal.rules.HierarchicalModelRules;

import java.io.StringReader;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.github.pagehelper.PageHelper;

import static org.assertj.core.api.Assertions.*;

/**
 * 人工元数据离线验证独立锁定查询配置、签名、多方言和事务缓存门禁。
 *
 * @author vgosoft
 */
class SelectForUpdateGenerationTest {
    @Test
    void offlineOutputBaseline() throws Exception {
        // 摘要来自重构前真实离线生成，仅统一换行；顺序为 Mapper、接口、实现、门禁及锁定 XML。
        String[][] expected = {
                {"5f0480fa4928e22b59be879b9aa1cc6eb06de8f022ae0040e760f0aed79f106d",
                 "f502155f9cdd95bb1727b7ee2717952dfc70d222da115c823279fb44284af5d6",
                 "096d3920b7b860207a64035e9b34a4406ce1a53002f2be9715483f009c861884",
                 "e611db88837710b8c92f58ffbe4cdfbf0a07b9abc4312fd94ab1730a9fdd6665",
                 "eebda94e07a4245684e409fa80cf09de777bd272aeb9ae740eb605a4da68fe21"},
                {"a64394bdb8c8f9bd0ce965427bcf6569318e03349504f11ecb83a12e14a2ed0c",
                 "15960a5431e4305e87051f8823d1c5cc289bee1cc122bf7202f2cb9c798e991e",
                 "ef3cc38566233af65b9fea79bc4c59a6790a7845789b03f37c4cf4e76b6022f9",
                 "b16e7ec0040c8c17e672f24ee763f40a14cd191443f047892297b75228236320",
                 "a9c7e59f754341745dcb756930339d91f2db43adcf76bd1a0e39101831e03e14"},
                {"0fe0e71b254648896e3df3812b18332cb4b3d4ba64bc74c85e8799ac62137c59",
                 "ace2a53dd32e667afc14ffbf507a4b8927ceddf17962fe10dbc3a6fb3d64e9d0",
                 "0a8108cc5ec768e1e2b43237527050e1c1f2922f0ef8097681f940b6785a1b65",
                 "b16e7ec0040c8c17e672f24ee763f40a14cd191443f047892297b75228236320",
                 "7abc3a4c962b4d25b3bd1145b99eb9bfd12657face4c34805b8c06fbfdd2dbee"}
        };
        for (int variant = 0; variant < 3; variant++) {
            var table = fixture(true);
            if (variant > 0) {
                table.addColumn(column("tenant_id", "tenantId", Types.VARCHAR));
                table.addPrimaryKeyColumn("tenant_id");
            }
            if (variant > 1) {
                table.addColumn(column("content_", "content", Types.BLOB));
                table.setRules(new HierarchicalModelRules(table));
            }
            var service = new JavaServiceGenerator("src");
            setup(service, table);
            List<String> outputs = List.of(format(actualMapper(table)), format(service.getCompilationUnits().get(0)),
                    format(actualService(table)), format(SelectForUpdateGuardGenerator.createGuard(table)), format(sql(table)));
            for (int layer = 0; layer < outputs.size(); layer++) {
                String digest = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                        .digest(outputs.get(layer).replace("\r\n", "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                assertThat(digest).as("重构前真实输出基线：模型 %s，层 %s", variant, layer)
                        .isEqualTo(expected[variant][layer]);
            }
        }
    }
    private static final String XML = """
            <!DOCTYPE generatorConfiguration PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
              "mybatis-generator-config_1_8_0.dtd">
            <generatorConfiguration>
              <context id="lock-test" defaultModelType="flat" targetRuntime="MyBatis3"
                appKeyword="sample" moduleKeyword="sample" moduleName="测试" jdkVersion="17" integrateMybatisPlus="false">
                <commentGenerator><property name="suppressDate" value="true"/></commentGenerator>
                <jdbcConnection driverClass="must.not.connect.Driver" connectionURL="jdbc:must-not-connect:test"/>
                <javaModelGenerator targetPackage="sample.entity" targetProject="src"/>
                <sqlMapGenerator targetPackage="sample.mapper" targetProject="src"/>
                <javaClientGenerator targetPackage="sample.dao" targetProject="src" type="XMLMAPPER"/>
                <table tableName="article_" domainObjectName="Article" %s>
                  <generateModel generate="true"/>
                  <generateService generate="true" generateUnitTest="false"/>
                  <generateController generate="false"/>
                  <generateSqlSchema generate="false"/>
                  <generateVo generate="false"/>
                </table>
              </context>
            </generatorConfiguration>
            """;

    @Test
    void currentDtdDefaultsBooleansAndInvalidValues() throws Exception {
        assertThat(parse("").getTableConfigurations().get(0).isSelectForUpdateEnabled()).isFalse();
        assertThat(parse("enableSelectForUpdate=\"false\"").getTableConfigurations().get(0).isSelectForUpdateEnabled()).isFalse();
        assertThat(parse("enableSelectForUpdate=\"true\"").getTableConfigurations().get(0).isSelectForUpdateEnabled()).isTrue();
        for (String value : List.of("yes", "TRUE", "1", "")) {
            assertThatThrownBy(() -> parse("enableSelectForUpdate=\"" + value + "\""))
                    .isInstanceOfAny(XMLParserException.class, IllegalArgumentException.class);
        }
    }

    @Test
    void rulesDefaultsCoverEveryModelAndDelegatesRespectBothKindsOfVeto() throws Exception {
        var table = fixture(true);
        for (var rules : List.of(new org.mybatis.generator.internal.rules.FlatModelRules(table),
                new HierarchicalModelRules(table), new org.mybatis.generator.internal.rules.ConditionalModelRules(table))) {
            table.setRules(rules);
            assertThat(rules.generateSelectByPrimaryKeyForUpdate()).isEqualTo(rules.generateSelectByPrimaryKey());
            assertThat(rules.generateSelectByExampleForUpdate())
                    .isEqualTo(rules.generateSelectByExampleWithoutBlobs() || rules.generateSelectByExampleWithBlobs());
            table.getTableConfiguration().setSelectForUpdateEnabled(false);
            assertThat(rules.generateSelectByPrimaryKeyForUpdate()).isFalse();
            assertThat(rules.generateSelectByExampleForUpdate()).isFalse();
            table.getTableConfiguration().setSelectForUpdateEnabled(true);
        }
        table.setRules(new org.mybatis.generator.internal.rules.FlatModelRules(table) {
            @Override
            public boolean generateSelectByPrimaryKeyForUpdate() {
                return false;
            }
        });
        var wrapped = new org.mybatis.generator.internal.rules.RulesDelegate(table.getRules());
        var nested = new org.mybatis.generator.internal.rules.RulesDelegate(wrapped);
        assertThat(nested.generateSelectByPrimaryKeyForUpdate()).isFalse();
        assertThat(nested.generateSelectByExampleForUpdate()).isTrue();
        assertThat(format(actualMapper(table))).doesNotContain("selectByPrimaryKeyForUpdate")
                .contains("selectByExampleForUpdate");
        assertThat(format(actualService(table))).doesNotContain("selectByPrimaryKeyForUpdate")
                .contains("selectByExampleForUpdate");
        var service = new JavaServiceGenerator("src");
        setup(service, table);
        assertThat(format(service.getCompilationUnits().get(0))).doesNotContain("selectByPrimaryKeyForUpdate")
                .contains("selectByExampleForUpdate");
        assertThat(format(sql(table))).doesNotContain("selectByPrimaryKeyForUpdate")
                .contains("selectByExampleForUpdate");
        var ordinaryVeto = new org.mybatis.generator.internal.rules.RulesDelegate(table.getRules()) {
            @Override
            public boolean generateSelectByPrimaryKey() {
                return false;
            }

            @Override
            public boolean generateSelectByExampleWithoutBlobs() {
                return false;
            }

            @Override
            public boolean generateSelectByExampleWithBlobs() {
                return false;
            }
        };
        assertThat(ordinaryVeto.generateSelectByPrimaryKeyForUpdate()).isFalse();
        assertThat(ordinaryVeto.generateSelectByExampleForUpdate()).isFalse();
    }

    @Test
    void dedicatedSelectHooksAreCompatibleAndShortCircuitWithoutCallingOrdinaryHooks() throws Exception {
        var table = fixture(true);
        table.getTableConfiguration().getAdditionalDbTypes().add(DatabaseType.SQL_SERVER);
        var legacy = new LockHookPlugin(true);
        var veto = new LockHookPlugin(false);
        var skipped = new LockHookPlugin(true);
        var plugins = (PluginAggregator) table.getContext().getPlugins();
        plugins.addPlugin(legacy);
        plugins.addPlugin(veto);
        plugins.addPlugin(skipped);
        Interface mapper = new Interface("sample.Mapper");
        addLockMapperMethods(mapper, table);
        assertThat(mapper.getMethods()).isEmpty();
        assertThat(sql(table).getElements()).isEmpty();
        assertThat(legacy.calls).containsExactly("mapper-pk", "mapper-example",
                "xml-pk", "xml-example", "xml-pk", "xml-example");
        assertThat(veto.calls).containsExactlyElementsOf(legacy.calls);
        assertThat(skipped.calls).isEmpty();
        assertThat(legacy.ordinaryCalls + veto.ordinaryCalls + skipped.ordinaryCalls).isZero();
        var defaults = new org.mybatis.generator.api.PluginAdapter() {
            @Override
            public boolean validate(List<String> warnings) {
                return true;
            }
        };
        var fresh = fixture(true);
        ((PluginAggregator) fresh.getContext().getPlugins()).addPlugin(defaults);
        assertThat(format(actualMapper(fresh))).isEqualTo(format(actualMapper(fixture(true))));
        assertThat(format(sql(fresh))).isEqualTo(format(sql(fixture(true))));
    }

    @Test
    void parserRejectsInvalidBooleanEvenWithoutDtdValidation() throws Exception {
        var factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        var document = factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(
                XML.substring(XML.indexOf("<generatorConfiguration>")).formatted("enableSelectForUpdate=\"yes\""))));
        var parser = new org.mybatis.generator.config.xml.MyBatisGeneratorConfigurationParser(new java.util.Properties(), new ArrayList<>());
        assertThatThrownBy(() -> parser.parseConfiguration(document.getDocumentElement()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("enableSelectForUpdate");
    }

    @Test
    void disabledHasNoAdditionalMethodsOrSqlAndDoesNotValidateUnsupportedDialect() throws Exception {
        var table = fixture(false);
        table.getTableConfiguration().getAdditionalDbTypes().add(DatabaseType.SQLITE);
        SelectForUpdateValidator.validate(table);
        Interface mapper = new Interface("sample.Mapper");
        addLockMapperMethods(mapper, table);
        assertThat(mapper.getMethods()).isEmpty();
        assertThat(sql(table).getElements()).isEmpty();
        assertThat(format(actualMapper(table))).isEqualTo(format(actualMapper(fixture(false))));
        assertThat(format(actualService(table))).isEqualTo(format(actualService(fixture(false))));
    }

    @Test
    void enabledDoesNotChangeOrdinaryMethodsAndQueries() throws Exception {
        var plain = fixture(false);
        var locked = fixture(true);
        var before = actualMapper(plain);
        var after = actualMapper(locked);
        after.getMethods().removeIf(method -> method.getName().contains("ForUpdate"));
        assertThat(format(after)).isEqualTo(format(before));
        var normal = new org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.SelectByPrimaryKeyElementGenerator();
        normal.setContext(plain.getContext());
        normal.setIntrospectedTable(plain);
        XmlElement beforeXml = new XmlElement("mapper");
        normal.addElements(beforeXml);
        normal.setContext(locked.getContext());
        normal.setIntrospectedTable(locked);
        XmlElement afterXml = new XmlElement("mapper");
        normal.addElements(afterXml);
        assertThat(format(afterXml)).isEqualTo(format(beforeXml));
    }

    @Test
    void ordinarySwitchesControlIndependentLockMethods() throws Exception {
        var table = fixture(true);
        table.getTableConfiguration().setSelectByExampleStatementEnabled(false);
        Interface mapper = new Interface("sample.Mapper");
        addLockMapperMethods(mapper, table);
        assertThat(mapper.getMethods()).extracting(Method::getName)
                .contains("selectByPrimaryKeyForUpdate").doesNotContain("selectByExampleForUpdate");
        table.getTableConfiguration().setSelectByExampleStatementEnabled(true);
        table.getTableConfiguration().setSelectByPrimaryKeyStatementEnabled(false);
        mapper = new Interface("sample.Mapper");
        addLockMapperMethods(mapper, table);
        assertThat(mapper.getMethods()).extracting(Method::getName)
                .contains("selectByExampleForUpdate").doesNotContain("selectByPrimaryKeyForUpdate");
        table.getTableConfiguration().setSelectByExampleStatementEnabled(false);
        assertThatThrownBy(() -> SelectForUpdateValidator.validate(table)).hasMessageContaining("均关闭");
    }

    @Test
    void rejectsUnsupportedConfigurationAndEveryAdditionalDialectBeforeAnyOutput() throws Exception {
        var table = fixture(true);
        for (DatabaseType database : DatabaseType.values()) {
            if (List.of(DatabaseType.MYSQL, DatabaseType.MARIADB, DatabaseType.POSTGRE_SQL, DatabaseType.ORACLE,
                    DatabaseType.H2, DatabaseType.SQL_SERVER, DatabaseType.SQL_SERVER2005).contains(database)) {
                continue;
            }
            table.getTableConfiguration().getAdditionalDbTypes().add(database);
            XmlElement mapper = new XmlElement("mapper");
            var generator = generator(table);
            assertThatThrownBy(() -> generator.addElements(mapper))
                    .hasMessageContaining("article_").hasMessageContaining(database.toString());
            assertThat(mapper.getElements()).isEmpty();
            table.getTableConfiguration().getAdditionalDbTypes().remove(database);
        }
        TableConfiguration tc = table.getTableConfiguration();
        tc.getAdditionalDbTypes().clear();
        assertThatThrownBy(() -> SelectForUpdateValidator.validate(table)).hasMessageContaining("additionalDbTypes");
        tc.getAdditionalDbTypes().add(DatabaseType.MYSQL);
        for (var output : List.of(tc.getJavaModelGeneratorConfiguration(), tc.getSqlMapGeneratorConfiguration(),
                tc.getJavaClientGeneratorConfiguration(), tc.getJavaServiceGeneratorConfiguration(),
                tc.getJavaServiceImplGeneratorConfiguration())) {
            output.setGenerate(false);
            assertThatThrownBy(() -> SelectForUpdateValidator.validate(table)).hasMessageContaining("Service");
            output.setGenerate(true);
        }
        tc.addProperty("immutable", "true");
        assertThatThrownBy(() -> SelectForUpdateValidator.validate(table)).hasMessageContaining("可变模型");
        tc.addProperty("immutable", "false");
        table.getContext().getJavaClientGeneratorConfiguration().setConfigurationType("ANNOTATEDMAPPER");
        assertThatThrownBy(() -> SelectForUpdateValidator.validate(table)).hasMessageContaining("XMLMAPPER");
        table.getContext().getJavaClientGeneratorConfiguration().setConfigurationType("XMLMAPPER");
        table.getContext().setTargetRuntime("MyBatis3Simple");
        assertThatThrownBy(() -> SelectForUpdateValidator.validate(table)).hasMessageContaining("MyBatis3");
        table.getContext().setTargetRuntime("MyBatis3");
        tc.addProperty("modelOnly", "true");
        assertThatThrownBy(() -> SelectForUpdateValidator.validate(table)).hasMessageContaining("可变模型");
        tc.addProperty("modelOnly", "false");
        table.getPrimaryKeyColumns().clear();
        assertThatThrownBy(() -> SelectForUpdateValidator.validate(table)).hasMessageContaining("主键");
    }

    @Test
    void compositePrimaryKeyAndHierarchicalKeyAndBlobSignaturesMatchXml() throws Exception {
        var table = fixture(true);
        table.addColumn(column("tenant_id", "tenantId", Types.VARCHAR));
        table.addPrimaryKeyColumn("tenant_id");
        Interface mapper = new Interface("sample.Mapper");
        addLockMapperMethods(mapper, table);
        Method pk = mapper.getMethods().stream().filter(method -> method.getName().equals("selectByPrimaryKeyForUpdate"))
                .findFirst().orElseThrow();
        assertThat(pk.getParameters()).extracting(Parameter::getName).containsExactly("id", "tenantId");
        assertThat(pk.getParameters()).allSatisfy(parameter -> assertThat(parameter.getAnnotations()).hasSize(1));
        assertThat(format(sql(table))).contains("parameterType=\"map\"", "#{id,jdbcType=VARCHAR}", "#{tenantId,jdbcType=VARCHAR}");
        compileLockMethods(table);
        table.addColumn(column("content_", "content", Types.BLOB));
        table.setRules(new HierarchicalModelRules(table));
        mapper = new Interface("sample.Mapper");
        addLockMapperMethods(mapper, table);
        pk = mapper.getMethods().stream().filter(method -> method.getName().equals("selectByPrimaryKeyForUpdate"))
                .findFirst().orElseThrow();
        assertThat(pk.getParameters()).hasSize(1);
        assertThat(pk.getParameters().get(0).getType().getFullyQualifiedName()).isEqualTo(table.getPrimaryKeyType());
        assertThat(pk.getReturnType().orElseThrow().getFullyQualifiedName()).isEqualTo(table.getRecordWithBlobsType());
        assertThat(format(sql(table))).contains("parameterType=\"" + table.getPrimaryKeyType() + "\"",
                "resultMap=\"" + table.getResultMapWithBlobsId() + "\"", "Blob_Column_List");
        StaticJavaParser.parse(format(mapper));
        compileLockMethods(table);
    }

    @Test
    void multiDialectSqlIsUncachedAndHasNoUnsafeSqlFeatures() throws Exception {
        var table = fixture(true);
        table.getTableConfiguration().getAdditionalDbTypes().addAll(List.of(DatabaseType.MARIADB,
                DatabaseType.POSTGRE_SQL, DatabaseType.ORACLE, DatabaseType.H2, DatabaseType.SQL_SERVER, DatabaseType.SQL_SERVER2005));
        var xml = sql(table);
        assertThat(xml.getElements()).hasSize(14);
        for (var element : xml.getElements()) {
            XmlElement select = (XmlElement) element;
            String source = format(select);
            assertThat(source).contains("useCache=\"false\"", "flushCache=\"true\"", "databaseId=",
                    "<bind", "@sample.codegen.dao.GenArticleMapperLockQueryGuard@requireSelectForUpdate")
                    .doesNotContain("distinct", "order by", "limit", "${orderByClause}");
            if (source.contains("with (updlock, holdlock)")) {
                assertThat(source).doesNotContain("for update");
            } else {
                assertThat(source).contains("for update");
            }
        }
    }

    @Test
    void generatedServiceAndMapperHaveChineseContractsAndMandatoryNoCacheNoCatch() throws Exception {
        var table = fixture(true);
        String mapper = format(actualMapper(table));
        String service = format(actualService(table));
        StaticJavaParser.parse(mapper);
        StaticJavaParser.parse(service);
        assertThat(mapper).contains("Article selectByPrimaryKeyForUpdate", "List<Article> selectByExampleForUpdate",
                "@param id 完整主键值");
        assertThat(format(SelectForUpdateGuardGenerator.createGuard(table)))
                .contains("禁止原始 SQL", "PageHelper.getLocalPage()", "isCurrentTransactionReadOnly()");
        assertThat(service).contains("ServiceResult<Article> selectByPrimaryKeyForUpdate",
                "ServiceResult<List<Article>> selectByExampleForUpdate", "Propagation.MANDATORY",
                "rollbackFor = Exception.class", "ServiceResult.success(mapper.selectByExampleForUpdate(example))");
        for (Method method : actualService(table).getMethods()) {
            if (method.getName().endsWith("ForUpdate")) {
                assertThat(method.getAnnotations()).hasSize(2).noneMatch(value -> value.contains("Cache"));
                assertThat(method.getBodyLines()).noneMatch(value -> value.contains("catch") || value.contains("failure("));
            }
        }
        var generator = new JavaServiceGenerator("src");
        setup(generator, table);
        assertThat(format(generator.getCompilationUnits().get(0))).contains("ServiceResult<List<Article>> selectByExampleForUpdate");
    }

    @Test
    void compilesGeneratedMethodsAndExecutesTransactionPageHelperAndExampleGuards() throws Exception {
        var table = fixture(true);
        ClassLoader loader = compileLockMethods(table);
        Class<?> mapper = loader.loadClass(SelectForUpdateGuardGenerator.mapperType(table));
        Class<?> guardType = loader.loadClass(SelectForUpdateGuardGenerator.guardType(table));
        Class<?> exampleType = loader.loadClass(table.getExampleType());
        var transaction = guardType.getMethod("requireSelectForUpdateTransaction");
        var exampleGuard = guardType.getMethod("requireSelectForUpdateExample", exampleType);
        try {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            assertGuardFailure(transaction, null, null, IllegalStateException.class);
            TransactionSynchronizationManager.setActualTransactionActive(true);
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
            assertGuardFailure(transaction, null, null, IllegalStateException.class);
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
            assertThat(transaction.invoke(null)).isEqualTo(true);
            PageHelper.startPage(1, 10);
            assertGuardFailure(transaction, null, null, IllegalStateException.class);
            PageHelper.clearPage();
            PageHelper.orderBy("id_");
            assertGuardFailure(transaction, null, null, IllegalStateException.class);
            PageHelper.clearPage();
            assertGuardFailure(exampleGuard, null, new Object[]{null}, IllegalArgumentException.class);
            Object example = exampleType.getConstructor().newInstance();
            assertGuardFailure(exampleGuard, null, new Object[]{example}, IllegalArgumentException.class);
            Object criteria = exampleType.getMethod("createCriteria").invoke(example);
            assertGuardFailure(exampleGuard, null, new Object[]{example}, IllegalArgumentException.class);
            criteria.getClass().getMethod("andIdEqualTo", String.class).invoke(criteria, "key");
            assertThat(exampleGuard.invoke(null, example)).isEqualTo(true);
            exampleType.getMethod("setDistinct", boolean.class).invoke(example, true);
            assertGuardFailure(exampleGuard, null, new Object[]{example}, IllegalArgumentException.class);
            exampleType.getMethod("setDistinct", boolean.class).invoke(example, false);
            exampleType.getMethod("setOrderByClause", String.class).invoke(example, "id_; drop table article_");
            assertGuardFailure(exampleGuard, null, new Object[]{example}, IllegalArgumentException.class);
            exampleType.getMethod("setOrderByClause", String.class).invoke(example, new Object[]{null});
            var values = (List<Object>) criteria.getClass().getMethod("getAllCriteria").invoke(criteria);
            Class<?> criterionType = loader.loadClass(table.getExampleType() + "$Criterion");
            var constructor = criterionType.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            values.add(constructor.newInstance("1 = 1 or id_ is null"));
            assertGuardFailure(exampleGuard, null, new Object[]{example}, IllegalArgumentException.class);
            values.remove(values.size() - 1);
            criteria.getClass().getMethod("andIdIn", List.class).invoke(criteria, List.of());
            assertGuardFailure(exampleGuard, null, new Object[]{example}, IllegalArgumentException.class);
            values.remove(values.size() - 1);
            assertThat(exampleGuard.invoke(null, example)).isEqualTo(true);
            Class<?> service = loader.loadClass("sample.LockService");
            Object instance = service.getConstructor().newInstance();
            var method = service.getMethod("selectByPrimaryKeyForUpdate", String.class);
            var annotation = method.getAnnotation(org.springframework.transaction.annotation.Transactional.class);
            assertThat(annotation.propagation()).isEqualTo(org.springframework.transaction.annotation.Propagation.MANDATORY);
            assertThat(annotation.rollbackFor()).containsExactly(Exception.class);
            var mapperField = service.getField("mapper");
            mapperField.set(instance, java.lang.reflect.Proxy.newProxyInstance(loader, new Class<?>[]{mapper},
                    (proxy, called, args) -> { throw new IllegalStateException("模拟数据库异常"); }));
            assertGuardFailure(method, instance, new Object[]{"key"}, IllegalStateException.class);
        } finally {
            PageHelper.clearPage();
            TransactionSynchronizationManager.clear();
        }
    }

    @Test
    void myBatisParsesDatabaseIdBranchesAndRunsXmlGuardsBeforeSqlAndCache() throws Exception {
        var table = fixture(true);
        table.getTableConfiguration().getAdditionalDbTypes().add(DatabaseType.SQL_SERVER);
        ClassLoader loader = compileLockMethods(table);
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            for (DatabaseType database : List.of(DatabaseType.MYSQL, DatabaseType.SQL_SERVER)) {
                var configuration = new org.apache.ibatis.session.Configuration();
                configuration.setDatabaseId(database.getCode());
                XmlElement root = sql(table);
                root.addAttribute(new org.mybatis.generator.api.dom.xml.Attribute("namespace", SelectForUpdateGuardGenerator.mapperType(table)));
                root.getElements().add(0, new XmlElement("cache"));
                XmlElement resultMap = new XmlElement("resultMap");
                resultMap.addAttribute(new org.mybatis.generator.api.dom.xml.Attribute("id", table.getBaseResultMapId()));
                resultMap.addAttribute(new org.mybatis.generator.api.dom.xml.Attribute("type", table.getBaseRecordType()));
                root.addElement(resultMap);
                XmlElement columns = new XmlElement("sql");
                columns.addAttribute(new org.mybatis.generator.api.dom.xml.Attribute("id", table.getBaseColumnListId()));
                columns.addElement(new org.mybatis.generator.api.dom.xml.TextElement("id_, title_"));
                root.addElement(columns);
                var where = new org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.ExampleWhereClauseElementGenerator(false);
                where.setContext(table.getContext());
                where.setIntrospectedTable(table);
                where.addElements(root);
                var document = new Document("-//mybatis.org//DTD Mapper 3.0//EN", "http://mybatis.org/dtd/mybatis-3-mapper.dtd");
                document.setRootElement(root);
                String xml = new DefaultXmlFormatter().getFormattedContent(document);
                new org.apache.ibatis.builder.xml.XMLMapperBuilder(new StringReader(xml), configuration,
                        "offline-lock-" + database.getCode(), configuration.getSqlFragments()).parse();
                String namespace = SelectForUpdateGuardGenerator.mapperType(table);
                var pk = configuration.getMappedStatement(namespace + ".selectByPrimaryKeyForUpdate");
                var byExample = configuration.getMappedStatement(namespace + ".selectByExampleForUpdate");
                assertThat(pk.isUseCache()).isFalse();
                assertThat(pk.isFlushCacheRequired()).isTrue();
                assertThat(pk.getCache()).isNotNull();
                assertThat(byExample.isUseCache()).isFalse();
                assertThat(byExample.isFlushCacheRequired()).isTrue();
                TransactionSynchronizationManager.setActualTransactionActive(false);
                assertThatThrownBy(() -> pk.getBoundSql("key")).hasStackTraceContaining("非只读既有事务");
                TransactionSynchronizationManager.setActualTransactionActive(true);
                TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
                Class<?> exampleType = loader.loadClass(table.getExampleType());
                Object example = exampleType.getConstructor().newInstance();
                assertThatThrownBy(() -> byExample.getBoundSql(example)).hasStackTraceContaining("非空条件");
                Object criteria = exampleType.getMethod("createCriteria").invoke(example);
                criteria.getClass().getMethod("andIdEqualTo", String.class).invoke(criteria, "key");
                String sql = byExample.getBoundSql(example).getSql();
                assertThat(sql).contains("WHERE", "id_ = ?");
                assertThat(sql).contains(database == DatabaseType.SQL_SERVER ? "with (updlock, holdlock)" : "for update");
                var delegate = new OfflineExecutor(configuration);
                var executor = new org.apache.ibatis.executor.CachingExecutor(delegate);
                executor.query(pk, "key", org.apache.ibatis.session.RowBounds.DEFAULT, null);
                executor.query(pk, "key", org.apache.ibatis.session.RowBounds.DEFAULT, null);
                assertThat(delegate.queries).as("重复锁查询不得命中 MyBatis 一级或二级缓存").isEqualTo(2);
                executor.commit(false);
                assertThat(pk.getCache().getSize()).isZero();
                executor.close(false);
                PageHelper.startPage(1, 10);
                assertThatThrownBy(() -> byExample.getBoundSql(example)).hasStackTraceContaining("PageHelper");
                var interceptor = new com.github.pagehelper.PageInterceptor();
                var properties = new java.util.Properties();
                properties.setProperty("helperDialect", "mysql");
                interceptor.setProperties(properties);
                var pageDelegate = new OfflineExecutor(configuration);
                var pageExecutor = (org.apache.ibatis.executor.Executor) interceptor.plugin(pageDelegate);
                assertThatThrownBy(() -> pageExecutor.query(byExample, example,
                        org.apache.ibatis.session.RowBounds.DEFAULT, null)).hasStackTraceContaining("PageHelper");
                assertThat(pageDelegate.queries).isZero();
                PageHelper.clearPage();
                TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
                assertThatThrownBy(() -> pk.getBoundSql("key")).hasStackTraceContaining("非只读既有事务");
                TransactionSynchronizationManager.clear();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
            PageHelper.clearPage();
            TransactionSynchronizationManager.clear();
        }
    }

    @Test
    void generatedServiceRequiresSpringExistingNonReadOnlyTransaction() throws Exception {
        var table = fixture(true);
        var loader = compileLockMethods(table);
        var serviceType = loader.loadClass("sample.LockService");
        var mapperType = loader.loadClass(SelectForUpdateGuardGenerator.mapperType(table));
        Object target = serviceType.getConstructor().newInstance();
        java.util.concurrent.atomic.AtomicInteger queries = new java.util.concurrent.atomic.AtomicInteger();
        serviceType.getField("mapper").set(target, java.lang.reflect.Proxy.newProxyInstance(loader,
                new Class<?>[]{mapperType}, (proxy, method, arguments) -> {
                    queries.incrementAndGet();
                    return loader.loadClass(table.getBaseRecordType()).getConstructor().newInstance();
                }));
        var manager = new OfflineTransactionManager();
        var factory = new org.springframework.aop.framework.ProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(new org.springframework.transaction.interceptor.TransactionInterceptor(manager,
                new org.springframework.transaction.annotation.AnnotationTransactionAttributeSource()));
        Object proxy = factory.getProxy(loader);
        var method = proxy.getClass().getMethod("selectByPrimaryKeyForUpdate", String.class);
        assertGuardFailure(method, proxy, new Object[]{"key"}, org.springframework.transaction.IllegalTransactionStateException.class);
        assertThat(queries.get()).isZero();
        var template = new org.springframework.transaction.support.TransactionTemplate(manager);
        template.executeWithoutResult(status -> {
            try {
                assertThat(method.invoke(proxy, "key")).isNotNull();
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        });
        assertThat(queries.get()).isEqualTo(1);
        assertThat(manager.commits).isEqualTo(1);
        template.setReadOnly(true);
        assertThatThrownBy(() -> template.executeWithoutResult(status -> {
            try {
                method.invoke(proxy, "key");
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        })).hasStackTraceContaining("非只读既有事务");
        assertThat(queries.get()).isEqualTo(1);
        assertThat(manager.rollbacks).isEqualTo(1);
    }

    private void assertGuardFailure(java.lang.reflect.Method method, Object target, Object[] arguments, Class<? extends Throwable> failure) {
        assertThatThrownBy(() -> method.invoke(target, arguments)).isInstanceOf(java.lang.reflect.InvocationTargetException.class)
                .hasCauseInstanceOf(failure);
    }

    private ClassLoader compileLockMethods(IntrospectedTableMyBatis3Impl table) throws Exception {
        Interface mapper = new Interface(SelectForUpdateGuardGenerator.mapperType(table));
        mapper.setVisibility(JavaVisibility.PUBLIC);
        addLockMapperMethods(mapper, table);
        var exampleGenerator = new org.mybatis.generator.codegen.mybatis3.model.ExampleGenerator("src");
        setup(exampleGenerator, table);
        CompilationUnit example = exampleGenerator.getCompilationUnits().get(0);
        TopLevelClass service = new TopLevelClass("sample.LockService");
        service.setVisibility(JavaVisibility.PUBLIC);
        Field mapperField = new Field("mapper", mapper.getType());
        mapperField.setVisibility(JavaVisibility.PUBLIC);
        service.addField(mapperField);
        service.addImportedType(mapper.getType());
        var serviceGenerator = new LockServiceGenerator();
        setup(serviceGenerator, table);
        serviceGenerator.addLocks(service);
        service.getMethods().forEach(method -> method.getAnnotations().remove("@Override"));
        String modelName = table.getBaseRecordType();
        String model = "package " + new FullyQualifiedJavaType(modelName).getPackageName()
                + "; public class Article { }";
        Map<String, String> sources = new HashMap<>(Map.of(mapper.getType().getFullyQualifiedName(), format(mapper),
                SelectForUpdateGuardGenerator.guardType(table), format(SelectForUpdateGuardGenerator.createGuard(table)),
                example.getType().getFullyQualifiedName(), format(example), modelName, model,
                "sample.LockService", format(service)));
        if (table.getRules().generatePrimaryKeyClass()) {
            var key = new FullyQualifiedJavaType(table.getPrimaryKeyType());
            sources.put(key.getFullyQualifiedName(), "package " + key.getPackageName()
                    + "; public class " + key.getShortName() + " { }");
        }
        if (table.getRules().generateRecordWithBlobsClass()) {
            var blob = new FullyQualifiedJavaType(table.getRecordWithBlobsType());
            sources.put(blob.getFullyQualifiedName(), "package " + blob.getPackageName()
                    + "; public class " + blob.getShortName() + " extends Article { }");
        }
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("必须使用完整 JDK 验证生成代码").isNotNull();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Map<String, ByteArrayOutputStream> bytes = new HashMap<>();
        try (var manager = new ForwardingJavaFileManager<StandardJavaFileManager>(
                compiler.getStandardFileManager(diagnostics, null, null)) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) {
                var output = new ByteArrayOutputStream();
                bytes.put(name, output);
                return new SimpleJavaFileObject(URI.create("memory:///" + name.replace('.', '/') + kind.extension), kind) {
                    @Override
                    public OutputStream openOutputStream() {
                        return output;
                    }
                };
            }
        }) {
            List<JavaFileObject> files = new ArrayList<>();
            sources.forEach((name, source) -> files.add(new SimpleJavaFileObject(
                    URI.create("string:///" + name.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return source;
                }
            }));
            assertThat(compiler.getTask(null, manager, diagnostics, List.of("-proc:none",
                    "-classpath", System.getProperty("java.class.path")), null, files).call())
                    .withFailMessage("生成代码编译失败：%s", diagnostics.getDiagnostics()).isTrue();
        }
        return new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                var output = bytes.get(name);
                if (output == null) {
                    return super.findClass(name);
                }
                var content = output.toByteArray();
                return defineClass(name, content, 0, content.length);
            }
        };
    }

    private Context parse(String attributes) throws Exception {
        return new ConfigurationParser(new ArrayList<>()).parseConfiguration(new StringReader(XML.formatted(attributes)))
                .getContexts().get(0);
    }

    private IntrospectedTableMyBatis3Impl fixture(boolean enabled) throws Exception {
        Context context = parse("enableSelectForUpdate=\"" + enabled + "\"");
        var field = Context.class.getDeclaredField("pluginAggregator");
        field.setAccessible(true);
        field.set(context, new PluginAggregator());
        var table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(context.getTableConfigurations().get(0));
        table.getTableConfiguration().getAdditionalDbTypes().add(DatabaseType.MYSQL);
        table.setFullyQualifiedTable(new FullyQualifiedTable(null, null, "article_", "Article", null,
                true, null, null, null, false, null, context));
        table.setRemarks("文章");
        table.addColumn(column("id_", "id", Types.VARCHAR));
        table.addPrimaryKeyColumn("id_");
        table.addColumn(column("title_", "title", Types.VARCHAR));
        table.initialize();
        return table;
    }

    private IntrospectedColumn column(String name, String property, int type) {
        var column = new IntrospectedColumn();
        column.setActualColumnName(name);
        column.setJavaProperty(property);
        column.setFullyQualifiedJavaType(type == Types.BLOB ? new FullyQualifiedJavaType("byte[]") : FullyQualifiedJavaType.getStringInstance());
        column.setJdbcType(type);
        column.setJdbcTypeName(type == Types.BLOB ? "BLOB" : "VARCHAR");
        column.setLength(128);
        column.setRemarks("测试字段");
        return column;
    }

    private LockXmlGenerator generator(IntrospectedTableMyBatis3Impl table) {
        var generator = new LockXmlGenerator();
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        return generator;
    }

    private void addLockMapperMethods(Interface mapper, IntrospectedTableMyBatis3Impl table) {
        var generator = new LockMapperGenerator();
        setup(generator, table);
        generator.addLocks(mapper);
    }

    /**
     * 记录独立锁查询扩展点调用及否决顺序，不接触业务数据库。
     *
     * @author vgosoft
     */
    private static final class LockHookPlugin extends org.mybatis.generator.api.PluginAdapter {
        private final boolean allow;
        private final List<String> calls = new ArrayList<>();
        private int ordinaryCalls;

        LockHookPlugin(boolean allow) {
            this.allow = allow;
        }

        @Override
        public boolean validate(List<String> warnings) {
            return true;
        }

        @Override
        public boolean clientSelectByPrimaryKeyForUpdateMethodGenerated(Method method, Interface interfaze,
                                                                       org.mybatis.generator.api.AbstractIntrospectedTable table) {
            calls.add("mapper-pk");
            return allow;
        }

        @Override
        public boolean clientSelectByExampleForUpdateMethodGenerated(Method method, Interface interfaze,
                                                                    org.mybatis.generator.api.AbstractIntrospectedTable table) {
            calls.add("mapper-example");
            return allow;
        }

        @Override
        public boolean sqlMapSelectByPrimaryKeyForUpdateElementGenerated(XmlElement element,
                                                                         org.mybatis.generator.api.AbstractIntrospectedTable table) {
            calls.add("xml-pk");
            return allow;
        }

        @Override
        public boolean sqlMapSelectByExampleForUpdateElementGenerated(XmlElement element,
                                                                      org.mybatis.generator.api.AbstractIntrospectedTable table) {
            calls.add("xml-example");
            return allow;
        }

        @Override
        public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
                                                               org.mybatis.generator.api.AbstractIntrospectedTable table) {
            ordinaryCalls++;
            return false;
        }

        @Override
        public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element,
                                                                org.mybatis.generator.api.AbstractIntrospectedTable table) {
            ordinaryCalls++;
            return false;
        }
    }

    /**
     * 通过实际 XML 装配入口验证独立锁定元素。
     *
     * @author vgosoft
     */
    private static final class LockXmlGenerator extends XMLMapperGenerator {
        void addElements(XmlElement root) {
            addSelectForUpdateElements(root);
        }
    }

    /**
     * 通过实际 Mapper 装配入口验证独立锁定方法。
     *
     * @author vgosoft
     */
    private static final class LockMapperGenerator extends JavaMapperGenerator {
        LockMapperGenerator() {
            super("src");
        }

        void addLocks(Interface mapper) {
            addSelectByPrimaryKeyForUpdateMethod(mapper);
            addSelectByExampleForUpdateMethod(mapper);
        }
    }

    /**
     * 通过服务元素的真实初始化流程输出可内存编译的锁定方法。
     *
     * @author vgosoft
     */
    private static final class LockServiceGenerator extends JavaServiceImplGenerator {
        LockServiceGenerator() {
            super("src");
        }

        void addLocks(TopLevelClass service) {
            if (introspectedTable.getRules().generateSelectByPrimaryKeyForUpdate()) {
                initializeAndExecuteGenerator(
                        new org.mybatis.generator.codegen.mybatis3.service.elements.SelectByPrimaryKeyForUpdateElement(), service);
            }
            if (introspectedTable.getRules().generateSelectByExampleForUpdate()) {
                initializeAndExecuteGenerator(
                        new org.mybatis.generator.codegen.mybatis3.service.elements.SelectByExampleForUpdateElement(), service);
            }
        }
    }

    private XmlElement sql(IntrospectedTableMyBatis3Impl table) {
        XmlElement root = new XmlElement("mapper");
        generator(table).addElements(root);
        return root;
    }

    private Interface actualMapper(IntrospectedTableMyBatis3Impl table) {
        var generator = new JavaMapperGenerator("src");
        setup(generator, table);
        return (Interface) generator.getCompilationUnits().get(0);
    }

    private TopLevelClass actualService(IntrospectedTableMyBatis3Impl table) {
        var generator = new JavaServiceImplGenerator("src");
        setup(generator, table);
        return (TopLevelClass) generator.getCompilationUnits().get(0);
    }

    private void setup(org.mybatis.generator.codegen.AbstractGenerator generator, IntrospectedTableMyBatis3Impl table) {
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        generator.setProgressCallback(new SilentProgress());
        generator.setWarnings(new ArrayList<>());
    }

    private String format(CompilationUnit unit) {
        return new DefaultJavaFormatter().getFormattedContent(unit);
    }

    private String format(XmlElement root) {
        Document document = new Document();
        document.setRootElement(root);
        return new DefaultXmlFormatter().getFormattedContent(document);
    }

    /**
     * 静默离线生成进度，不触发在线任务。
     *
     * @author vgosoft
     */
    private static final class SilentProgress implements ProgressCallback {
    }

    /**
     * 仅验证 Spring 传播行为的无资源事务管理器，不建立任何数据库连接。
     *
     * @author vgosoft
     */
    private static final class OfflineTransactionManager
            extends org.springframework.transaction.support.AbstractPlatformTransactionManager {
        private int commits;
        private int rollbacks;

        @Override
        protected Object doGetTransaction() {
            return TransactionSynchronizationManager.isActualTransactionActive();
        }

        @Override
        protected boolean isExistingTransaction(Object transaction) {
            return (Boolean) transaction;
        }

        @Override
        protected void doBegin(Object transaction, org.springframework.transaction.TransactionDefinition definition) {
        }

        @Override
        protected void doCommit(org.springframework.transaction.support.DefaultTransactionStatus status) {
            commits++;
        }

        @Override
        protected void doRollback(org.springframework.transaction.support.DefaultTransactionStatus status) {
            rollbacks++;
        }

        @Override
        protected void doSetRollbackOnly(org.springframework.transaction.support.DefaultTransactionStatus status) {
        }
    }

    /**
     * 只执行 MyBatis 缓存流程的无数据库执行器，查询结果在内存中提供。
     *
     * @author vgosoft
     */
    private static final class OfflineExecutor extends org.apache.ibatis.executor.BaseExecutor {
        private int queries;

        private OfflineExecutor(org.apache.ibatis.session.Configuration configuration) {
            super(configuration, new org.apache.ibatis.transaction.Transaction() {
                @Override
                public java.sql.Connection getConnection() {
                    throw new UnsupportedOperationException("离线测试禁止数据库连接");
                }

                @Override
                public void commit() {
                }

                @Override
                public void rollback() {
                }

                @Override
                public void close() {
                }

                @Override
                public Integer getTimeout() {
                    return null;
                }
            });
        }

        @Override
        protected int doUpdate(org.apache.ibatis.mapping.MappedStatement statement, Object parameter) {
            throw new UnsupportedOperationException("离线测试禁止写数据库");
        }

        @Override
        protected List<org.apache.ibatis.executor.BatchResult> doFlushStatements(boolean rollback) {
            return List.of();
        }

        @Override
        protected <E> List<E> doQuery(org.apache.ibatis.mapping.MappedStatement statement, Object parameter,
                org.apache.ibatis.session.RowBounds bounds, org.apache.ibatis.session.ResultHandler handler,
                org.apache.ibatis.mapping.BoundSql sql) {
            queries++;
            return new ArrayList<>();
        }

        @Override
        protected <E> org.apache.ibatis.cursor.Cursor<E> doQueryCursor(org.apache.ibatis.mapping.MappedStatement statement,
                Object parameter, org.apache.ibatis.session.RowBounds bounds, org.apache.ibatis.mapping.BoundSql sql) {
            throw new UnsupportedOperationException("离线测试禁止数据库游标");
        }
    }
}
