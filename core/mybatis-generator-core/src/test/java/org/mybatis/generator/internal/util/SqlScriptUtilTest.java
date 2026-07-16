package org.mybatis.generator.internal.util;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;

import java.sql.JDBCType;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlScriptUtilTest {

    @Test
    void shouldBuildDatetimeDefaultsForAllDialects() {
        IntrospectedColumn column = column(JDBCType.TIMESTAMP, false, "'now'");

        for (DatabaseDDLDialects dialect : DatabaseDDLDialects.values()) {
            String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, dialect);
            assertTrue(fragment.startsWith("NOT NULL "));
            assertTrue(fragment.contains("DEFAULT "));
            assertTrue(fragment.endsWith(" "));
        }
    }

    @Test
    void shouldMapCurrentDateAndCurrentTimeForSqlServer() {
        IntrospectedColumn dateColumn = column(JDBCType.DATE, false, "'curdate'");
        IntrospectedColumn timeColumn = column(JDBCType.TIME, false, "'curtime'");

        String dateFragment = SqlScriptUtil.getSqlFragmentNotNull(dateColumn, DatabaseDDLDialects.SQL_SERVER);
        String timeFragment = SqlScriptUtil.getSqlFragmentNotNull(timeColumn, DatabaseDDLDialects.SQL_SERVER2005);

        assertTrue(dateFragment.startsWith("NOT NULL "));
        assertTrue(dateFragment.contains("DEFAULT CAST(CURRENT_TIMESTAMP AS DATE) "));
        assertTrue(timeFragment.startsWith("NOT NULL "));
        assertTrue(timeFragment.contains("DEFAULT CAST(CURRENT_TIMESTAMP AS TIME) "));
    }

    @Test
    void shouldAvoidParenthesizedCurrentExpressionForDatetime() {
        IntrospectedColumn column = column(JDBCType.TIMESTAMP, false, "CURRENT_TIMESTAMP");

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.DM);
        assertTrue(fragment.startsWith("NOT NULL "));
        assertTrue(fragment.contains("DEFAULT CURRENT_TIMESTAMP "));
        assertTrue(!fragment.contains("DEFAULT (CURRENT_TIMESTAMP)"));
    }

    @Test
    void shouldEscapeCharacterDefaultValue() {
        IntrospectedColumn column = column(JDBCType.VARCHAR, false, "O'Reilly");

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.POSTGRE_SQL);
        assertTrue(fragment.startsWith("NOT NULL "));
        assertTrue(fragment.contains("DEFAULT 'O''Reilly' "));
    }

    @Test
    void shouldMapMysqlStyleBitDefaultForDm() {
        IntrospectedColumn column = column(JDBCType.BIT, false, "b(1)");

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.DM);
        assertTrue(fragment.startsWith("NOT NULL "));
        assertTrue(fragment.contains("DEFAULT 1 "));
    }

    @Test
    void shouldKeepMysqlStyleBitDefaultForMysql() {
        IntrospectedColumn column = column(JDBCType.BIT, false, "b(0)");

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.MYSQL);
        assertTrue(fragment.startsWith("NOT NULL "));
        assertTrue(fragment.contains("DEFAULT b(0) "));
    }

    private IntrospectedColumn column(JDBCType jdbcType, boolean nullable, String defaultValue) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setJdbcType(jdbcType.getVendorTypeNumber());
        column.setNullable(nullable);
        column.setDefaultValue(defaultValue);
        return column;
    }
}
