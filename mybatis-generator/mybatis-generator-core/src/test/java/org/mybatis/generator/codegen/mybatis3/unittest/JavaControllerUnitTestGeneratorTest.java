package org.mybatis.generator.codegen.mybatis3.unittest;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.sql.JDBCType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaControllerUnitTestGeneratorTest {

    @Test
    void shouldFormatLocalDateTimeUsingDatetimeFormatter() {
        IntrospectedColumn column = createColumn("updatedAt", "java.time.LocalDateTime", JDBCType.TIMESTAMP, JDBCType.TIMESTAMP.getName());

        assertEquals("entity.getUpdatedAt().format(DATETIME_FORMATTER)",
                JavaControllerUnitTestGenerator.getRequestBodyDateValueExpression(column, "entity"));
    }

    @Test
    void shouldFormatLocalDateUsingDateFormatter() {
        IntrospectedColumn column = createColumn("birthday", "java.time.LocalDate", JDBCType.DATE, JDBCType.DATE.getName());

        assertEquals("entity.getBirthday().format(DATE_FORMATTER)",
                JavaControllerUnitTestGenerator.getRequestBodyDateValueExpression(column, "entity"));
    }

    @Test
    void shouldFormatLocalTimeUsingTimeFormatter() {
        IntrospectedColumn column = createColumn("startTime", "java.time.LocalTime", JDBCType.TIME, JDBCType.TIME.getName());

        assertEquals("entity.getStartTime().format(TIME_FORMATTER)",
                JavaControllerUnitTestGenerator.getRequestBodyDateValueExpression(column, "entity"));
    }

    @Test
    void shouldFormatTimestampWithSimpleDateFormatForLegacyDateTypes() {
        IntrospectedColumn column = createColumn("modifiedAt", "java.util.Date", JDBCType.TIMESTAMP, JDBCType.TIMESTAMP.getName());

        assertEquals("new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\").format(entity.getModifiedAt())",
                JavaControllerUnitTestGenerator.getRequestBodyDateValueExpression(column, "entity"));
    }

    @Test
    void shouldFormatDateWithSimpleDateFormatForLegacyDateTypes() {
        IntrospectedColumn column = createColumn("createdOn", "java.sql.Date", JDBCType.DATE, JDBCType.DATE.getName());

        assertEquals("new java.text.SimpleDateFormat(\"yyyy-MM-dd\").format(entity.getCreatedOn())",
                JavaControllerUnitTestGenerator.getRequestBodyDateValueExpression(column, "entity"));
    }

    @Test
    void shouldFormatTimeWithSimpleDateFormatForLegacyDateTypes() {
        IntrospectedColumn column = createColumn("createdTime", "java.sql.Time", JDBCType.TIME, JDBCType.TIME.getName());

        assertEquals("new java.text.SimpleDateFormat(\"HH:mm:ss\").format(entity.getCreatedTime())",
                JavaControllerUnitTestGenerator.getRequestBodyDateValueExpression(column, "entity"));
    }

    private static IntrospectedColumn createColumn(String javaProperty,
                                                   String javaType,
                                                   JDBCType jdbcType,
                                                   String jdbcTypeName) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setJavaProperty(javaProperty);
        column.setFullyQualifiedJavaType(new FullyQualifiedJavaType(javaType));
        column.setJdbcType(jdbcType.getVendorTypeNumber());
        column.setJdbcTypeName(jdbcTypeName);
        return column;
    }
}
