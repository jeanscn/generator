package org.mybatis.generator.internal.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getSetterMethodName;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getValidPropertyName;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.sql.JDBCType;

/**
 * @author Jeff Butler
 *
 */
class JavaBeansUtilTest {

    @Test
    void testGetValidPropertyName() {
        assertEquals("eMail", getValidPropertyName("eMail"));
        assertEquals("firstName", getValidPropertyName("firstName"));
        assertEquals("URL", getValidPropertyName("URL"));
        assertEquals("XAxis", getValidPropertyName("XAxis"));
        assertEquals("a", getValidPropertyName("a"));
        assertEquals("b", getValidPropertyName("B"));
        assertEquals("yaxis", getValidPropertyName("Yaxis"));
        assertEquals("i_PARAM_INT_1", getValidPropertyName("I_PARAM_INT_1"));
        assertEquals("_fred", getValidPropertyName("_fred"));
        assertEquals("accountType", getValidPropertyName("AccountType"));
    }

    @Test
    void testGetGetterMethodName() {
        assertEquals("geteMail", getGetterMethodName("eMail", FullyQualifiedJavaType.getStringInstance()));
        assertEquals("getFirstName", getGetterMethodName("firstName", FullyQualifiedJavaType.getStringInstance()));
        assertEquals("getURL", getGetterMethodName("URL", FullyQualifiedJavaType.getStringInstance()));
        assertEquals("getXAxis", getGetterMethodName("XAxis", FullyQualifiedJavaType.getStringInstance()));
        assertEquals("getA", getGetterMethodName("a", FullyQualifiedJavaType.getStringInstance()));
        assertEquals("isActive", getGetterMethodName("active", FullyQualifiedJavaType.getBooleanPrimitiveInstance()));
        assertEquals("getI_PARAM_INT_1", getGetterMethodName("i_PARAM_INT_1", FullyQualifiedJavaType.getStringInstance()));
        assertEquals("get_fred", getGetterMethodName("_fred", FullyQualifiedJavaType.getStringInstance()));
        assertEquals("getAccountType", getGetterMethodName("AccountType", FullyQualifiedJavaType.getStringInstance()));
    }

    @Test
    void testGetSetterMethodName() {
        assertEquals("seteMail", getSetterMethodName("eMail"));
        assertEquals("setFirstName", getSetterMethodName("firstName"));
        assertEquals("setURL", getSetterMethodName("URL"));
        assertEquals("setXAxis", getSetterMethodName("XAxis"));
        assertEquals("setA", getSetterMethodName("a"));
        assertEquals("setI_PARAM_INT_1", getSetterMethodName("i_PARAM_INT_1"));
        assertEquals("set_fred", getSetterMethodName("_fred"));
        assertEquals("setAccountType", getSetterMethodName("AccountType"));
    }

    @Test
    void testGetColumnExampleValueForTinyIntByte() {
        IntrospectedColumn introspectedColumn = new IntrospectedColumn();
        introspectedColumn.setJavaProperty("tinyFlag");
        introspectedColumn.setJdbcType(JDBCType.TINYINT.getVendorTypeNumber());
        introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.lang.Byte"));

        assertEquals("(byte)0", JavaBeansUtil.getColumnExampleValue(introspectedColumn));
    }

    @Test
    void testGetColumnExampleValueForLongWrapper() {
        IntrospectedColumn introspectedColumn = new IntrospectedColumn();
        introspectedColumn.setJavaProperty("fileSize");
        introspectedColumn.setJdbcType(JDBCType.BIGINT.getVendorTypeNumber());
        introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.lang.Long"));

        assertEquals("0L", JavaBeansUtil.getColumnExampleValue(introspectedColumn));
    }

    @Test
    void testGetColumnExampleValueForFloatWrapper() {
        IntrospectedColumn introspectedColumn = new IntrospectedColumn();
        introspectedColumn.setJavaProperty("ratio");
        introspectedColumn.setJdbcType(JDBCType.FLOAT.getVendorTypeNumber());
        introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.lang.Float"));

        assertTrue(JavaBeansUtil.getColumnExampleValue(introspectedColumn).endsWith("F"));
    }

    @Test
    void testGetColumnExampleValueForDoubleWrapper() {
        IntrospectedColumn introspectedColumn = new IntrospectedColumn();
        introspectedColumn.setJavaProperty("price");
        introspectedColumn.setJdbcType(JDBCType.DOUBLE.getVendorTypeNumber());
        introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.lang.Double"));

        assertTrue(JavaBeansUtil.getColumnExampleValue(introspectedColumn).endsWith("D"));
    }

    @Test
    void testGetColumnExampleValueForBigDecimalWrapper() {
        IntrospectedColumn introspectedColumn = new IntrospectedColumn();
        introspectedColumn.setJavaProperty("amount");
        introspectedColumn.setJdbcType(JDBCType.DECIMAL.getVendorTypeNumber());
        introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.math.BigDecimal"));

        assertEquals("BigDecimal.valueOf(0.00)", JavaBeansUtil.getColumnExampleValue(introspectedColumn));
    }

    @Test
    void testGetColumnExampleValueForTimestampShouldKeepCurrentDatetimeExpression() {
        IntrospectedColumn introspectedColumn = new IntrospectedColumn();
        introspectedColumn.setJavaProperty("modified");
        introspectedColumn.setJdbcType(JDBCType.TIMESTAMP.getVendorTypeNumber());
        introspectedColumn.setJdbcTypeName(JDBCType.TIMESTAMP.getName());
        introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.time.LocalDateTime"));

        assertEquals("VDateUtils.getCurrentDatetime()", JavaBeansUtil.getColumnExampleValue(introspectedColumn));
    }
}
