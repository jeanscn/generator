package org.mybatis.generator.custom.annotations;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Parameter;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import com.vgosoft.core.adapter.security.CurrentUserProvider;
import com.vgosoft.tool.core.SpringContextHolder;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CacheAnnotationDescTest {

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
                .contains("T(com.vgosoft.tool.core.JsonUtil).toJsonString(T(java.util.Arrays).asList");

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

    private String evaluateKey(CacheAnnotationDesc descriptor, String tenant, Object... parameters) {
        String annotation = descriptor.toCacheableAnnotation();
        String expression = StaticJavaParser.parse("class Fixture { " + annotation + " void query() {} }")
                .findFirst(MethodDeclaration.class).orElseThrow()
                .getAnnotationByName("Cacheable").orElseThrow().asNormalAnnotationExpr().getPairs()
                .stream().filter(pair -> pair.getNameAsString().equals("key"))
                .findFirst().orElseThrow().getValue().asStringLiteralExpr().asString();
        StandardEvaluationContext context = new StandardEvaluationContext();
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
            applicationContext.getBeanFactory().registerSingleton("objectMapper", new ObjectMapper());
            applicationContext.refresh();
            holder.setApplicationContext(applicationContext);
            return new SpelExpressionParser().parseExpression(expression).getValue(context, String.class);
        } finally {
            holder.setApplicationContext(previousContext);
        }
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
