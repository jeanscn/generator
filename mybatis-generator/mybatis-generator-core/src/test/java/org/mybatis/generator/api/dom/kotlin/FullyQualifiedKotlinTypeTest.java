package org.mybatis.generator.api.dom.kotlin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FullyQualifiedKotlinTypeTest {

    @Test
    void testKotlinPrimitive() {
        FullyQualifiedKotlinType fqjt =
            new FullyQualifiedKotlinType("kotlin.String");
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("String");
        assertThat(fqjt.getImportList()).isEmpty();
    }

    @Test
    void testKotlinPrimitive2() {
        FullyQualifiedKotlinType fqjt =
            new FullyQualifiedKotlinType("String");
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("String");
        assertThat(fqjt.getImportList()).isEmpty();
    }

    @Test
    void testSimpleType() {
        FullyQualifiedKotlinType fqjt =
            new FullyQualifiedKotlinType("com.foo.Bar");
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("Bar");
        assertThat(fqjt.getImportList()).hasSize(1);
        assertThat(fqjt.getImportList()).contains("com.foo.Bar");
    }

    @Test
    void testSimpleType2() {
        FullyQualifiedKotlinType fqjt =
            new FullyQualifiedKotlinType("com.foo.bar");
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("bar");
        assertThat(fqjt.getImportList()).hasSize(1);
        assertThat(fqjt.getImportList()).contains("com.foo.bar");
    }

    @Test
    void testGenericType1() {
        FullyQualifiedKotlinType fqjt =
            new FullyQualifiedKotlinType("kotlin.collections.List<kotlin.String>");
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("List<String>");
        assertThat(fqjt.getImportList()).isEmpty();
    }

    @Test
    void testGenericType2() {
        FullyQualifiedKotlinType fqjt =
            new FullyQualifiedKotlinType("List<String>");
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("List<String>");
        assertThat(fqjt.getImportList()).isEmpty();
    }

    @Test
    void testGenericType3() {
        FullyQualifiedKotlinType fqjt =
            new FullyQualifiedKotlinType("kotlin.collections.Map<kotlin.String, kotlin.collections.List<kotlin.String>>");
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("Map<String, List<String>>");
        assertThat(fqjt.getImportList()).isEmpty();
    }

    @Test
    void testGenericType4() {
        FullyQualifiedKotlinType fqjt =
            new FullyQualifiedKotlinType("List<Map<String, String>>");
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("List<Map<String, String>>");
        assertThat(fqjt.getImportList()).isEmpty();
    }

    @Test
    void testUppercasePackage1() {
        FullyQualifiedKotlinType fqjt = new FullyQualifiedKotlinType("org.foo.Bar.Inner");
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("Inner");
        assertThat(fqjt.getImportList()).hasSize(1);
        assertThat(fqjt.getImportList()).contains("org.foo.Bar.Inner");
    }

    @Test
    void testUppercasePackage2() {
        FullyQualifiedKotlinType fqjt = new FullyQualifiedKotlinType("org.foo.Bar.Inner.Inner");
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("Inner");
        assertThat(fqjt.getImportList()).hasSize(1);
        assertThat(fqjt.getImportList()).contains("org.foo.Bar.Inner.Inner");
    }

    @Test
    void testUppercasePackage3() {
        FullyQualifiedKotlinType fqjt = new FullyQualifiedKotlinType("java.util.List<org.foo.Bar.Inner>");
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("List<Inner>");
        assertThat(fqjt.getImportList()).hasSize(2);
        assertThat(fqjt.getImportList()).contains("java.util.List", "org.foo.Bar.Inner");
    }

    @Test
    void testLateInitialization1() {
        FullyQualifiedKotlinType fqjt = new FullyQualifiedKotlinType("List");
        fqjt.addTypeArgument(new FullyQualifiedKotlinType("java.math.BigDecimal"));
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("List<BigDecimal>");
        assertThat(fqjt.getImportList()).hasSize(1);
        assertThat(fqjt.getImportList()).contains("java.math.BigDecimal");
    }

    @Test
    void testLateInitialization2() {
        FullyQualifiedKotlinType inner = new FullyQualifiedKotlinType("some.generic.Thing");
        inner.addTypeArgument(new FullyQualifiedKotlinType("java.math.BigDecimal"));
        FullyQualifiedKotlinType fqjt = new FullyQualifiedKotlinType("kotlin.List");
        fqjt.addTypeArgument(inner);
        assertThat(fqjt.getShortNameWithTypeArguments()).isEqualTo("List<Thing<BigDecimal>>");
        assertThat(fqjt.getImportList()).hasSize(2);
        assertThat(fqjt.getImportList()).contains("java.math.BigDecimal", "some.generic.Thing");
    }
}
