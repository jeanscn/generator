package org.mybatis.generator.api.dom.java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JavaDomUtilsTest {

    @Test
    void testGenericTypeNothingImported() {
        Interface interfaze = new Interface(new FullyQualifiedJavaType("com.foo.UserMapper"));

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.util.Map<java.math.BigDecimal, java.util.List<com.beeant.dto.User>>");
        assertEquals("java.util.Map<java.math.BigDecimal, java.util.List<com.beeant.dto.User>>",
                JavaDomUtils.calculateTypeName(interfaze, fqjt));
    }

    @Test
    void testGenericTypeBaseTypeImportedImported() {
        Interface interfaze = new Interface(new FullyQualifiedJavaType("com.foo.UserMapper"));

        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.Map"));

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.util.Map<java.math.BigDecimal, java.util.List<com.beeant.dto.User>>");
        assertEquals("Map<java.math.BigDecimal, java.util.List<com.beeant.dto.User>>",
                JavaDomUtils.calculateTypeName(interfaze, fqjt));
    }

    @Test
    void testGenericTypeWithAllTypeParametersImported() {
        Interface interfaze = new Interface(new FullyQualifiedJavaType("com.foo.UserMapper"));

        interfaze.addImportedType(new FullyQualifiedJavaType("com.beeant.dto.User"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.math.BigDecimal"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.List"));

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.util.Map<java.math.BigDecimal, java.util.List<com.beeant.dto.User>>");
        assertEquals("java.util.Map<BigDecimal, List<User>>",
                JavaDomUtils.calculateTypeName(interfaze, fqjt));
    }

    @Test
    void testGenericTypeWithSomeParametersImported() {
        Interface interfaze = new Interface(new FullyQualifiedJavaType("com.foo.UserMapper"));

        interfaze.addImportedType(new FullyQualifiedJavaType("com.beeant.dto.User"));

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.util.Map<java.math.BigDecimal, java.util.List<com.beeant.dto.User>>");
        assertEquals("java.util.Map<java.math.BigDecimal, java.util.List<User>>",
                JavaDomUtils.calculateTypeName(interfaze, fqjt));
    }

    @Test
    void testGenericTypeWithAllImported() {
        Interface interfaze = new Interface(new FullyQualifiedJavaType("com.foo.UserMapper"));

        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.Map"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.List"));
        interfaze.addImportedType(new FullyQualifiedJavaType("com.beeant.dto.User"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.math.BigDecimal"));

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.util.Map<java.math.BigDecimal, java.util.List<com.beeant.dto.User>>");
        assertEquals("Map<BigDecimal, List<User>>",
                JavaDomUtils.calculateTypeName(interfaze, fqjt));
    }

    @Test
    void testGenericTypeWithWildCardAllImported() {
        Interface interfaze = new Interface(new FullyQualifiedJavaType("com.foo.UserMapper"));

        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.Map"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.List"));
        interfaze.addImportedType(new FullyQualifiedJavaType("com.beeant.dto.User"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.math.BigDecimal"));

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.util.Map<java.math.BigDecimal, java.util.List<? extends com.beeant.dto.User>>");
        assertEquals("Map<BigDecimal, List<? extends User>>",
                JavaDomUtils.calculateTypeName(interfaze, fqjt));
    }

    @Test
    void testGenericTypeWithWildCardSomeImported() {
        Interface interfaze = new Interface(new FullyQualifiedJavaType("com.foo.UserMapper"));

        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.Map"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.List"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.math.BigDecimal"));

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.util.Map<java.math.BigDecimal, java.util.List<? super com.beeant.dto.User>>");
        assertEquals("Map<BigDecimal, List<? super com.beeant.dto.User>>",
                JavaDomUtils.calculateTypeName(interfaze, fqjt));
    }

    @Test
    void testGenericTypeWithWildCard() {
        Interface interfaze = new Interface(new FullyQualifiedJavaType("com.foo.UserMapper"));

        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.Map"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.util.List"));
        interfaze.addImportedType(new FullyQualifiedJavaType("java.math.BigDecimal"));

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.util.Map<java.math.BigDecimal, java.util.List<?>>");
        assertEquals("Map<BigDecimal, List<?>>",
                JavaDomUtils.calculateTypeName(interfaze, fqjt));
    }

    @Test
    void testArray() {
        Interface interfaze = new Interface(new FullyQualifiedJavaType("com.foo.UserMapper"));

        interfaze.addImportedType(new FullyQualifiedJavaType("java.math.BigDecimal[]"));

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.math.BigDecimal[]");
        assertEquals("BigDecimal[]",
                JavaDomUtils.calculateTypeName(interfaze, fqjt));
    }
}
