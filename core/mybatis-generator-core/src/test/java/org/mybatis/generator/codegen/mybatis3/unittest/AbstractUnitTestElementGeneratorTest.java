package org.mybatis.generator.codegen.mybatis3.unittest;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractUnitTestElementGeneratorTest {

    @Test
    void testIsDateTimeColumnShouldIgnoreLocalDate() throws Exception {
        AbstractUnitTestElementGenerator generator = new AbstractUnitTestElementGenerator() {
            @Override
            public void addElements(TopLevelClass parentElement) {
            }
        };
        Method method = AbstractUnitTestElementGenerator.class.getDeclaredMethod("isDateTimeColumn", IntrospectedColumn.class);
        method.setAccessible(true);

        IntrospectedColumn localDateColumn = new IntrospectedColumn();
        localDateColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.time.LocalDate"));

        IntrospectedColumn localDateTimeColumn = new IntrospectedColumn();
        localDateTimeColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.time.LocalDateTime"));

        IntrospectedColumn localTimeColumn = new IntrospectedColumn();
        localTimeColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.time.LocalTime"));

        assertFalse((Boolean) method.invoke(generator, localDateColumn));
        assertTrue((Boolean) method.invoke(generator, localDateTimeColumn));
        assertTrue((Boolean) method.invoke(generator, localTimeColumn));
    }
}
