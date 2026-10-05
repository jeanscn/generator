package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CacheEnableAnnotationTest {

    @Test
    void shouldDeduplicateAndWrapMultipleCacheableAnnotations() {
        TestElement element = new TestElement();
        Method method = new Method("select");
        TopLevelClass parent = new TopLevelClass(new FullyQualifiedJavaType("sample.Service"));
        String first = "@Cacheable(cacheNames = {\"one#72h\"}, key = \"key1\")";
        String second = "@Cacheable(cacheNames = {\"two#72h\"}, key = \"key2\")";

        element.addAnnotations(List.of(first, first, second), method, parent);

        assertThat(method.getAnnotations()).hasSize(1);
        assertThat(method.getAnnotations().get(0))
                .contains("@Caching(cacheable")
                .contains(first)
                .contains(second);
        assertThat(parent.getImportedTypes())
                .extracting(FullyQualifiedJavaType::getFullyQualifiedName)
                .contains("org.springframework.cache.annotation.Caching",
                        "org.springframework.cache.annotation.Cacheable");
    }

    private static final class TestElement extends AbstractServiceElementGenerator {
        @Override
        public void addElements(TopLevelClass parentElement) {
        }

        private void addAnnotations(List<String> annotations, Method method, TopLevelClass parent) {
            addCacheEnableAnnotations(annotations, method, parent);
        }
    }
}
