package org.mybatis.generator.api.dom.kotlin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

class JavaToKotlinTypeConverterTest {

    @Test
    void testPrimitiveByte() {
        FullyQualifiedJavaType jt = new FullyQualifiedJavaType("byte");
        FullyQualifiedKotlinType kt = JavaToKotlinTypeConverter.convert(jt);

        assertThat(kt.getShortNameWithTypeArguments()).isEqualTo("Byte");
        assertThat(kt.getImportList()).isEmpty();
    }

    @Test
    void testPrimitiveByteArray() {
        FullyQualifiedJavaType jt = new FullyQualifiedJavaType("byte[]");
        FullyQualifiedKotlinType kt = JavaToKotlinTypeConverter.convert(jt);

        assertThat(kt.getShortNameWithTypeArguments()).isEqualTo("ByteArray");
        assertThat(kt.getImportList()).isEmpty();
    }

    @Test
    void testByteWrapper() {
        FullyQualifiedJavaType jt = new FullyQualifiedJavaType("java.lang.Byte");
        FullyQualifiedKotlinType kt = JavaToKotlinTypeConverter.convert(jt);

        assertThat(kt.getShortNameWithTypeArguments()).isEqualTo("Byte");
        assertThat(kt.getImportList()).isEmpty();
    }

    @Test
    void testByteWrapperArray() {
        FullyQualifiedJavaType jt = new FullyQualifiedJavaType("java.lang.Byte[]");
        FullyQualifiedKotlinType kt = JavaToKotlinTypeConverter.convert(jt);

        assertThat(kt.getShortNameWithTypeArguments()).isEqualTo("Array<Byte>");
        assertThat(kt.getImportList()).isEmpty();
    }

    @Test
    void testUnmappedType() {
        FullyQualifiedJavaType jt = new FullyQualifiedJavaType("java.math.BigDecimal");
        FullyQualifiedKotlinType kt = JavaToKotlinTypeConverter.convert(jt);

        assertThat(kt.getShortNameWithTypeArguments()).isEqualTo("BigDecimal");
        assertThat(kt.getImportList()).hasSize(1);
        assertThat(kt.getImportList()).contains("java.math.BigDecimal");
    }

    @Test
    void testGenericType() {
        FullyQualifiedJavaType jt = new FullyQualifiedJavaType("java.util.List<java.math.BigDecimal>");
        FullyQualifiedKotlinType kt = JavaToKotlinTypeConverter.convert(jt);

        assertThat(kt.getShortNameWithTypeArguments()).isEqualTo("List<BigDecimal>");
        assertThat(kt.getImportList()).hasSize(2);
        assertThat(kt.getImportList()).contains("java.math.BigDecimal", "java.util.List");
    }
}
