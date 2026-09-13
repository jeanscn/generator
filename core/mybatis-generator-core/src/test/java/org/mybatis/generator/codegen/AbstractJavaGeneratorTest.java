package org.mybatis.generator.codegen;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.util.JavaLiteralUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractJavaGeneratorTest {

    @Test
    void shouldAppendLSuffixForLongWrapperIntegerLiteral() {
        String normalized = JavaLiteralUtil.normalizeLiteralForJavaType(
                new FullyQualifiedJavaType("java.lang.Long"), "0");
        assertEquals("0L", normalized);
    }

    @Test
    void shouldKeepLongLiteralWhenAlreadySuffixed() {
        String normalized = JavaLiteralUtil.normalizeLiteralForJavaType(
                new FullyQualifiedJavaType("java.lang.Long"), "1L");
        assertEquals("1L", normalized);
    }

    @Test
    void shouldAppendFSuffixForFloatWrapperIntegerLiteral() {
        String normalized = JavaLiteralUtil.normalizeLiteralForJavaType(
                new FullyQualifiedJavaType("java.lang.Float"), "0");
        assertEquals("0F", normalized);
    }

    @Test
    void shouldAppendDSuffixForDoubleWrapperIntegerLiteral() {
        String normalized = JavaLiteralUtil.normalizeLiteralForJavaType(
                new FullyQualifiedJavaType("java.lang.Double"), "0");
        assertEquals("0D", normalized);
    }

    @Test
    void shouldNormalizePostgreSqlCastLiteralForLong() {
        String normalized = JavaLiteralUtil.normalizeLiteralForJavaType(
                new FullyQualifiedJavaType("java.lang.Long"), "('0'::bigint)");
        assertEquals("0L", normalized);
    }

    @Test
    void shouldNotChangeNonNumericTypeLiteral() {
        String normalized = JavaLiteralUtil.normalizeLiteralForJavaType(
                new FullyQualifiedJavaType("java.lang.String"), "0");
        assertEquals("0", normalized);
    }

    @Test
    void shouldNotChangeNonLongNumericTypeLiteral() {
        String normalized = JavaLiteralUtil.normalizeLiteralForJavaType(
                new FullyQualifiedJavaType("java.lang.Integer"), "0");
        assertEquals("0", normalized);
    }
}
