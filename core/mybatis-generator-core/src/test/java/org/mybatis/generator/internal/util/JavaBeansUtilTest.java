package org.mybatis.generator.internal.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getSetterMethodName;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getValidPropertyName;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

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
}
