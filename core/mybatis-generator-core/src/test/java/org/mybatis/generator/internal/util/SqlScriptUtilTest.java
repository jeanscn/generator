package org.mybatis.generator.internal.util;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;

import java.sql.JDBCType;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    void shouldUseMysqlParenthesizedDateAndTimeDefaults() {
        IntrospectedColumn dateColumn = column(JDBCType.DATE, false, null);
        IntrospectedColumn timeColumn = column(JDBCType.TIME, false, null);

        String dateFragment = SqlScriptUtil.getSqlFragmentNotNull(dateColumn, DatabaseDDLDialects.MYSQL);
        String timeFragment = SqlScriptUtil.getSqlFragmentNotNull(timeColumn, DatabaseDDLDialects.MARIADB);

        assertTrue(dateFragment.contains("DEFAULT (CURRENT_DATE) "));
        assertTrue(timeFragment.contains("DEFAULT (CURRENT_TIME) "));
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

    @Test
    void shouldSkipDefaultForMysqlTextAndJson() {
        IntrospectedColumn textColumn = column(JDBCType.LONGVARCHAR, false, "'abc'", "TEXT");
        IntrospectedColumn jsonColumn = column(JDBCType.VARCHAR, false, "'{}'", "JSON");

        String textFragment = SqlScriptUtil.getSqlFragmentNotNull(textColumn, DatabaseDDLDialects.MYSQL);
        String jsonFragment = SqlScriptUtil.getSqlFragmentNotNull(jsonColumn, DatabaseDDLDialects.MARIADB);

        assertTrue(textFragment.startsWith("NOT NULL "));
        assertFalse(textFragment.contains("DEFAULT "));
        assertTrue(jsonFragment.startsWith("NOT NULL "));
        assertFalse(jsonFragment.contains("DEFAULT "));
    }

    @Test
    void shouldSkipDefaultForSqlServerText() {
        IntrospectedColumn column = column(JDBCType.LONGVARCHAR, false, "'legacy'", "TEXT");

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.SQL_SERVER);

        assertTrue(fragment.startsWith("NOT NULL "));
        assertFalse(fragment.contains("DEFAULT "));
    }

    @Test
    void shouldSkipDefaultForOracleAndDb2Lob() {
        IntrospectedColumn oracleColumn = column(JDBCType.CLOB, false, "'x'", "CLOB");
        IntrospectedColumn db2Column = column(JDBCType.CLOB, false, "'x'", "DBCLOB");

        String oracleFragment = SqlScriptUtil.getSqlFragmentNotNull(oracleColumn, DatabaseDDLDialects.ORACLE);
        String db2Fragment = SqlScriptUtil.getSqlFragmentNotNull(db2Column, DatabaseDDLDialects.DB2);

        assertTrue(oracleFragment.startsWith("NOT NULL "));
        assertFalse(oracleFragment.contains("DEFAULT "));
        assertTrue(db2Fragment.startsWith("NOT NULL "));
        assertFalse(db2Fragment.contains("DEFAULT "));
    }

    @Test
    void shouldKeepDefaultForPostgreSqlText() {
        IntrospectedColumn column = column(JDBCType.LONGVARCHAR, false, "'hello'", "TEXT");

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.POSTGRE_SQL);

        assertTrue(fragment.startsWith("NOT NULL "));
        assertTrue(fragment.contains("DEFAULT 'hello' "));
    }

    private IntrospectedColumn column(JDBCType jdbcType, boolean nullable, String defaultValue) {
        return column(jdbcType, nullable, defaultValue, null);
    }

    private IntrospectedColumn column(JDBCType jdbcType, boolean nullable, String defaultValue, String actualTypeName) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setJdbcType(jdbcType.getVendorTypeNumber());
        column.setNullable(nullable);
        column.setDefaultValue(defaultValue);
        column.setActualTypeName(actualTypeName);
        return column;
    }
}
