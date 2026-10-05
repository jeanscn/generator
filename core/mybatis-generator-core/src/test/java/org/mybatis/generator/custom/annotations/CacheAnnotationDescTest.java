package org.mybatis.generator.custom.annotations;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import com.vgosoft.core.adapter.security.CurrentUserProvider;
import com.vgosoft.tool.core.SpringContextHolder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.lang.reflect.Proxy;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.tools.ToolProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CacheAnnotationDescTest {

    private static final Path FIXTURE_DIRECTORY = Path.of("target", "cache-key-fixture");
    private static URLClassLoader fixtureLoader;
    private static Object keyTarget;

    @BeforeAll
    static void compileGeneratedSerializationSupport() throws Exception {
        TopLevelClass generated = new TopLevelClass("CacheKeyRuntimeFixture");
        generated.setVisibility(JavaVisibility.PUBLIC);
        generated.addImportedType("org.springframework.cache.annotation.Cacheable");
        generated.addImportedType("org.springframework.cache.annotation.CacheEvict");
        CacheAnnotationDesc descriptor = new CacheAnnotationDesc("SysDictData");
        descriptor.setOtherKey("com.vgosoft.system.entity.SysDictData#selectByKeysDict");
        descriptor.setParameters(List.of(
                new Parameter(new FullyQualifiedJavaType("java.util.Optional<java.lang.String>"), "types"),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "keys")));
        Method query = new Method("query");
        query.setVisibility(JavaVisibility.PUBLIC);
        descriptor.getParameters().forEach(query::addParameter);
        descriptor.getParameters().forEach(parameter -> generated.addImportedType(parameter.getType()));
        query.addAnnotation(descriptor.toCacheableAnnotation(generated));
        generated.addMethod(query);
        Method evict = new Method("evict");
        evict.setVisibility(JavaVisibility.PUBLIC);
        descriptor.getParameters().forEach(evict::addParameter);
        evict.addAnnotation(descriptor.toCacheEvictAnnotation(false, generated));
        generated.addMethod(evict);
        assertThat(generated.getFields()).hasSize(1);
        assertThat(generated.getMethods()).extracting(Method::getName)
                .containsExactly("vgosoftCacheKeyJson", "query", "evict");
        String source = new DefaultJavaFormatter().getFormattedContent(generated);
        assertThat(source).contains("private static final JsonMapper", ".builder()")
                .doesNotContain("JsonUtil", "org.mybatis.generator");
        Files.createDirectories(FIXTURE_DIRECTORY);
        Path sourceFile = FIXTURE_DIRECTORY.resolve("CacheKeyRuntimeFixture.java");
        Files.writeString(sourceFile, source);
        assertThat(ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "-proc:none", "-classpath", System.getProperty("java.class.path"),
                "-d", FIXTURE_DIRECTORY.toString(), sourceFile.toString())).isZero();
        fixtureLoader = new URLClassLoader(new java.net.URL[]{FIXTURE_DIRECTORY.toUri().toURL()},
                CacheAnnotationDescTest.class.getClassLoader());
        keyTarget = fixtureLoader.loadClass("CacheKeyRuntimeFixture").getConstructor().newInstance();
    }

    @AfterAll
    static void cleanGeneratedFixture() throws Exception {
        if (fixtureLoader != null) {
            fixtureLoader.close();
        }
        Files.deleteIfExists(FIXTURE_DIRECTORY.resolve("CacheKeyRuntimeFixture.java"));
        Files.deleteIfExists(FIXTURE_DIRECTORY.resolve("CacheKeyRuntimeFixture.class"));
        Files.deleteIfExists(FIXTURE_DIRECTORY);
    }

    @Test
    void shouldGenerateTypedStructuredKeysAndPreserveCustomTtl() {
        CacheAnnotationDesc descriptor = new CacheAnnotationDesc("OrgUser");
        descriptor.setCacheNames(List.of("permissionCache#12h", "unqualifiedCache"));
        descriptor.setOtherKey("selectByTableForPermission");
        descriptor.setParameters(List.of(
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "types"),
                new Parameter(new FullyQualifiedJavaType("java.util.List<String>"), "keys"),
                new Parameter(new FullyQualifiedJavaType("java.lang.String[]"), "values"),
                new Parameter(new FullyQualifiedJavaType("java.util.Optional<String>"), "optional")));

        String annotation = descriptor.toCacheableAnnotation();

        assertThat(annotation)
                .contains("\"permissionCache#12h\"")
                .contains("\"unqualifiedCache#72h\"")
                .contains("'selectByTableForPermission'")
                .contains("VCurrentUserUtil).getCurrentTenantId()")
                .doesNotContain("com.vgosoft.security")
                .contains("java.lang.String")
                .contains("java.util.List<String>")
                .contains("java.lang.String[]")
                .contains(".isPresent()")
                .contains("#root.target.vgosoftCacheKeyJson({'vgosoft-cache-key-v3'")
                .doesNotContain("JsonUtil", "toJsonString", "present:");

        MethodDeclaration method = StaticJavaParser.parse("class CacheKeyFixture { " + annotation
                        + " void query(String value) {} }")
                .findFirst(MethodDeclaration.class)
                .orElseThrow();
        StringLiteralExpr key = method.getAnnotationByName("Cacheable").orElseThrow()
                .asNormalAnnotationExpr().getPairs().stream()
                .filter(pair -> pair.getNameAsString().equals("key"))
                .findFirst().orElseThrow().getValue().asStringLiteralExpr();
        assertThatCode(() -> new SpelExpressionParser().parseExpression(key.asString()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldIncludeMethodIdentityInKeysSharingTheSameCacheName() {
        CacheAnnotationDesc first = cacheForMethod("selectByTableForPermission");
        CacheAnnotationDesc second = cacheForMethod("selectByTableForPermissionAction");

        assertThat(first.toCacheableAnnotation()).isNotEqualTo(second.toCacheableAnnotation());
    }

    @Test
    void shouldUseTheSameKeyAcrossCacheGroupsForOneMethod() {
        CacheAnnotationDesc first = cacheForMethod("sample.User#find");
        first.setServiceKey("User");
        first.setCacheNames(List.of("firstCache"));
        CacheAnnotationDesc second = cacheForMethod("sample.User#find");
        second.setServiceKey("User");
        second.setCacheNames(List.of("secondCache"));

        assertThat(keyPart(first.toCacheableAnnotation()))
                .isEqualTo(keyPart(second.toCacheableAnnotation()));
    }

    @Test
    void shouldKeepParameterBoundariesTenantAndOptionalStateDistinctAtRuntime() {
        CacheAnnotationDesc descriptor = new CacheAnnotationDesc("OrgUser");
        descriptor.setOtherKey("sample.User#query");
        descriptor.setParameters(List.of(
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "types"),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "keys")));
        assertThat(evaluateKey(descriptor, "tenant", "ab", "c"))
                .isNotEqualTo(evaluateKey(descriptor, "tenant", "a", "bc"));
        assertThat(evaluateKey(descriptor, "tenant", null, ""))
                .isNotEqualTo(evaluateKey(descriptor, "tenant", "", ""));
        assertThat(evaluateKey(descriptor, "tenant1", "a", "bc"))
                .isNotEqualTo(evaluateKey(descriptor, "tenant2", "a", "bc"));

        descriptor.setParameters(List.of(new Parameter(
                new FullyQualifiedJavaType("java.util.List<String>"), "keys")));
        assertThat(evaluateKey(descriptor, "tenant", List.of("a, b", "c")))
                .isNotEqualTo(evaluateKey(descriptor, "tenant", List.of("a", "b, c")));
        descriptor.setParameters(List.of(new Parameter(
                new FullyQualifiedJavaType("java.util.Optional<String>"), "keys")));
        assertThat(evaluateKey(descriptor, "tenant", Optional.empty()))
                .isNotEqualTo(evaluateKey(descriptor, "tenant", Optional.of("")))
                .isNotEqualTo(evaluateKey(descriptor, "tenant", new Object[]{null}));

        descriptor.setParameters(List.of(new Parameter(
                new FullyQualifiedJavaType("java.lang.Integer[]"), "keys")));
        assertThat(evaluateKey(descriptor, "tenant", (Object) new Integer[]{1, 2}))
                .isNotEqualTo(evaluateKey(descriptor, "tenant", (Object) new Integer[]{12}));
    }

    @Test
    void shouldSerializeExactV3TupleOnlyOnceRegardlessOfApplicationMapperSettings() {
        CacheAnnotationDesc descriptor = new CacheAnnotationDesc("SysDictData");
        descriptor.setOtherKey("com.vgosoft.system.entity.SysDictData#selectByKeysDict");
        descriptor.setParameters(List.of(
                new Parameter(new FullyQualifiedJavaType("java.util.Optional<java.lang.String>"), "types"),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "keys")));
        String expected = "[\"vgosoft-cache-key-v3\",\"SysDictData\","
                + "\"com.vgosoft.system.entity.SysDictData#selectByKeysDict\",\"10000\","
                + "[\"java.util.Optional<java.lang.String>\",\"present\",\"idCardType\"],"
                + "[\"java.lang.String\",\"1\"]]";
        assertThat(evaluateKey(descriptor, "10000", Optional.of("idCardType"), "1")).isEqualTo(expected);
        ObjectMapper applicationMapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class).build(), DefaultTyping.NON_FINAL)
                .build();
        assertThat(applicationMapper.writeValueAsString(new LinkedHashMap<>(Map.of("value", "idCardType"))))
                .contains("\n", "java.util.");
        assertThat(evaluateKeyWithMapper(descriptor, applicationMapper, "10000",
                Optional.of("idCardType"), "1")).isEqualTo(expected);
        assertThat(evaluateKeyWithMapper(descriptor, null, "10000",
                Optional.of("idCardType"), "1")).isEqualTo(expected);
    }

    @Test
    void shouldPreserveEscapingAndAllOptionalAndStringStates() {
        CacheAnnotationDesc descriptor = cacheForMethod("sample.User#find");
        descriptor.setParameters(List.of(new Parameter(
                new FullyQualifiedJavaType("java.util.Optional<java.lang.String>"), "value")));
        String prefix = "[\"vgosoft-cache-key-v3\",\"\",\"sample.User#find\",\"tenant\","
                + "[\"java.util.Optional<java.lang.String>\",";
        assertThat(evaluateKey(descriptor, "tenant", (Object) null)).isEqualTo(prefix + "\"null\",null]]");
        assertThat(evaluateKey(descriptor, "tenant", Optional.empty())).isEqualTo(prefix + "\"empty\",null]]");
        assertThat(evaluateKey(descriptor, "tenant", Optional.of(""))).isEqualTo(prefix + "\"present\",\"\"]]");
        descriptor.setParameters(List.of(new Parameter(FullyQualifiedJavaType.getStringInstance(), "value")));
        String value = "a'b\"c\\d\n\t中文";
        String key = evaluateKey(descriptor, "tenant", value);
        assertThat(key).endsWith("[\"java.lang.String\",\"a'b\\\"c\\\\d\\n\\t中文\"]]");
        assertThat(new JsonMapper().readTree(key).get(4).get(1).asString()).isEqualTo(value);
        assertThat(evaluateKey(descriptor, "tenant", (Object) null)).endsWith("[\"java.lang.String\",null]]");
        assertThat(evaluateKey(descriptor, "tenant", "")).endsWith("[\"java.lang.String\",\"\"]]");
    }

    @Test
    void shouldKeepArraysCollectionsTypesAndMethodIdentityDistinct() {
        CacheAnnotationDesc descriptor = cacheForMethod("sample.User#find");
        descriptor.setParameters(List.of(new Parameter(new FullyQualifiedJavaType("int[]"), "value")));
        String primitiveArrayKey = evaluateKey(descriptor, "tenant", (Object) new int[]{1, 2});
        assertThat(primitiveArrayKey).endsWith("[\"int[]\",[1,2]]]");
        descriptor.setParameters(List.of(new Parameter(new FullyQualifiedJavaType("java.util.List<Integer>"), "value")));
        String listKey = evaluateKey(descriptor, "tenant", List.of(1, 2));
        assertThat(listKey).endsWith("[\"java.util.List<Integer>\",[1,2]]]").isNotEqualTo(primitiveArrayKey);
        descriptor.setOtherKey("sample.User#findOther");
        assertThat(evaluateKey(descriptor, "tenant", List.of(1, 2))).isNotEqualTo(listKey);
        descriptor.setParameters(List.of(new Parameter(new FullyQualifiedJavaType("java.lang.String[]"), "value")));
        assertThat(evaluateKey(descriptor, "tenant", (Object) new String[]{"a", "b"}))
                .endsWith("[\"java.lang.String[]\",[\"a\",\"b\"]]]");
    }

    @Test
    void shouldSerializeCustomExpressionValuesAndSortMapsDeterministically() {
        CacheAnnotationDesc descriptor = cacheForMethod("sample.User#find");
        descriptor.setKey("#p0");
        assertThat(evaluateKey(descriptor, "tenant", "a\"b")).endsWith("[\"custom\",\"a\\\"b\"]]");
        assertThat(evaluateKey(descriptor, "tenant", (Object) null)).endsWith("[\"custom\",null]]");
        Map<String, Object> reversed = new LinkedHashMap<>();
        reversed.put("z", List.of(1, 2));
        reversed.put("a", "value");
        assertThat(evaluateKey(descriptor, "tenant", reversed))
                .endsWith("[\"custom\",{\"a\":\"value\",\"z\":[1,2]}]]")
                .isEqualTo(evaluateKey(descriptor, "tenant", Map.of("a", "value", "z", List.of(1, 2))));
        assertThat(evaluateKey(descriptor, "tenant", new SortedPayload("last", "first")))
                .endsWith("[\"custom\",{\"a\":\"first\",\"z\":\"last\"}]]");
        descriptor.setKey("{#p0,#p1}");
        assertThat(evaluateKey(descriptor, "tenant", "a", "b")).endsWith("[\"custom\",[\"a\",\"b\"]]]");
    }

    @Test
    void shouldShareReadAndKeyedEvictionExpressionsWithoutAddingSupportForAllEntries() {
        CacheAnnotationDesc descriptor = cacheForMethod("sample.User#find");
        TopLevelClass controller = new TopLevelClass("sample.Controller");
        assertThat(descriptor.toCacheEvictAnnotation(true, controller)).contains("allEntries = true").doesNotContain("key =");
        assertThat(controller.getFields()).isEmpty();
        assertThat(controller.getMethods()).isEmpty();
        assertThat(keyPart(descriptor.toCacheEvictAnnotation(false, controller)))
                .isEqualTo(keyPart(descriptor.toCacheableAnnotation(controller)));
        assertThat(controller.getFields()).hasSize(1);
        assertThat(controller.getMethods()).hasSize(1);
    }

    private String evaluateKey(CacheAnnotationDesc descriptor, String tenant, Object... parameters) {
        return evaluateKeyWithMapper(descriptor, new ObjectMapper(), tenant, parameters);
    }

    private String evaluateKeyWithMapper(CacheAnnotationDesc descriptor, ObjectMapper applicationMapper,
                                         String tenant, Object... parameters) {
        String annotation = descriptor.toCacheableAnnotation();
        String expression = StaticJavaParser.parse("class Fixture { " + annotation + " void query() {} }")
                .findFirst(MethodDeclaration.class).orElseThrow()
                .getAnnotationByName("Cacheable").orElseThrow().asNormalAnnotationExpr().getPairs()
                .stream().filter(pair -> pair.getNameAsString().equals("key"))
                .findFirst().orElseThrow().getValue().asStringLiteralExpr().asString();
        StandardEvaluationContext context = new StandardEvaluationContext(new CacheRoot(keyTarget));
        for (int i = 0; i < parameters.length; i++) {
            context.setVariable("p" + i, parameters[i]);
        }
        ApplicationContext previousContext = SpringContextHolder.getApplicationContext();
        SpringContextHolder holder = new SpringContextHolder();
        try (GenericApplicationContext applicationContext = new GenericApplicationContext()) {
            CurrentUserProvider provider = (CurrentUserProvider) Proxy.newProxyInstance(
                    CurrentUserProvider.class.getClassLoader(), new Class<?>[]{CurrentUserProvider.class},
                    (proxy, method, arguments) -> {
                        if ("getCurrentTenantId".equals(method.getName())) {
                            return tenant;
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
            applicationContext.getBeanFactory().registerSingleton("currentUserProvider", provider);
            if (applicationMapper != null) {
                applicationContext.getBeanFactory().registerSingleton("objectMapper", applicationMapper);
            }
            applicationContext.refresh();
            holder.setApplicationContext(applicationContext);
            return new SpelExpressionParser().parseExpression(expression).getValue(context, String.class);
        } finally {
            holder.setApplicationContext(previousContext);
        }
    }

    public record CacheRoot(Object target) {
    }

    public record SortedPayload(String z, String a) {
    }

    @Test
    void shouldRejectMalformedOrMissingCacheNames() {
        CacheAnnotationDesc malformed = new CacheAnnotationDesc();
        malformed.setCacheNames(List.of("cache#not-a-ttl"));
        assertThatThrownBy(malformed::toCacheableAnnotation)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid cache name/TTL");

        assertThatThrownBy(() -> new CacheAnnotationDesc().toCacheableAnnotation())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required");
    }

    @Test
    void shouldRejectTtlValuesUnsupportedByTheRedisConsumer() {
        for (String name : List.of("custom#30ms", "custom#8h#72h", "custom#0s",
                "custom#-1h", "custom#1.5h", "custom#2147483648s", "custom#1H")) {
            CacheAnnotationDesc descriptor = new CacheAnnotationDesc();
            descriptor.setCacheNames(List.of(name));
            assertThatThrownBy(descriptor::toCacheableAnnotation)
                    .as(name)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid cache name/TTL");
            assertThatThrownBy(() -> descriptor.toCacheEvictAnnotation(true))
                    .as(name)
                    .isInstanceOf(IllegalArgumentException.class);
        }
        for (String unit : List.of("s", "m", "h", "d")) {
            CacheAnnotationDesc descriptor = new CacheAnnotationDesc();
            descriptor.setCacheNames(List.of("custom#30" + unit));
            assertThat(descriptor.toCacheableAnnotation()).contains("\"custom#30" + unit + "\"");
        }
    }

    private CacheAnnotationDesc cacheForMethod(String methodName) {
        CacheAnnotationDesc descriptor = new CacheAnnotationDesc();
        descriptor.setCacheNames(List.of("sharedCache"));
        descriptor.setOtherKey(methodName);
        descriptor.setParameters(List.of(new Parameter(FullyQualifiedJavaType.getStringInstance(), "value")));
        return descriptor;
    }

    private String keyPart(String annotation) {
        return annotation.substring(annotation.indexOf(",key = "));
    }
}
