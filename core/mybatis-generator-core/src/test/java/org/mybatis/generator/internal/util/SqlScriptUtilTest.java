package org.mybatis.generator.internal.util;

import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;

import java.sql.JDBCType;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertFalse(fragment.contains("DEFAULT (CURRENT_TIMESTAMP)"));
    }

    @Test
    void shouldTreatTimestampWithTimezoneAsDatetime() {
        IntrospectedColumn column = column(JDBCType.TIMESTAMP_WITH_TIMEZONE, false, null);

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.OTHER);

        assertTrue(fragment.startsWith("NOT NULL "));
        assertTrue(fragment.contains("DEFAULT CURRENT_TIMESTAMP "));
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
        IntrospectedColumn column = column(JDBCType.BIT, false, "false");

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.MYSQL);
        assertTrue(fragment.startsWith("NOT NULL "));
        assertTrue(fragment.contains("DEFAULT false "));
    }

    @Test
    void shouldRenderBooleanLiteralDefaultForPostgreSqlBooleanColumn() {
        IntrospectedColumn column = column(JDBCType.BIT, true, "0", "BOOLEAN");

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.POSTGRE_SQL, "BOOLEAN");

        assertTrue(fragment.startsWith("NULL "));
        assertTrue(fragment.contains("DEFAULT FALSE "));
    }

    @Test
    void shouldKeepNumericDefaultForSqlServerBitColumn() {
        IntrospectedColumn column = column(JDBCType.BIT, true, "0", "BIT");

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.SQL_SERVER, "BIT");

        assertTrue(fragment.startsWith("NULL "));
        assertTrue(fragment.contains("DEFAULT 0 "));
    }

    @Test
    void shouldRenderBooleanLiteralsForBooleanTypedDialects() {
        IntrospectedColumn column = column(JDBCType.BIT, true, "1", "BOOLEAN");

        for (DatabaseDDLDialects dialect : List.of(
                DatabaseDDLDialects.POSTGRE_SQL,
                DatabaseDDLDialects.HIGHGO,
                DatabaseDDLDialects.KINGBASE_ES,
                DatabaseDDLDialects.H2,
                DatabaseDDLDialects.HSQL
        )) {
            String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, dialect, "BOOLEAN");
            assertTrue(fragment.contains("DEFAULT TRUE "));
        }
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

    @Test
    void shouldResolveJdbcTypeAliases() {
        assertEquals(JDBCType.INTEGER, JDBCTypeTypeEnum.resolveJdbcType("INT"));
        assertEquals(JDBCType.TIMESTAMP, JDBCTypeTypeEnum.resolveJdbcType("DATETIME"));
        assertEquals(JDBCType.BOOLEAN, JDBCTypeTypeEnum.resolveJdbcType("BOOL"));
        assertEquals(JDBCType.VARCHAR, JDBCTypeTypeEnum.resolveJdbcType("VARCHAR2"));
    }

    @Test
    void shouldRenderSqlNullDefaultForNullableColumn() {
        IntrospectedColumn column = column(JDBCType.VARCHAR, true, "NULL");

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.MYSQL);

        assertTrue(fragment.startsWith("NULL "));
        assertTrue(fragment.contains("DEFAULT NULL "));
        assertFalse(fragment.contains("DEFAULT 'NULL'"));
    }

    @Test
    void shouldSkipSqlNullDefaultForNotNullColumn() {
        IntrospectedColumn column = column(JDBCType.VARCHAR, false, "NULL");

        String fragment = SqlScriptUtil.getSqlFragmentNotNull(column, DatabaseDDLDialects.MYSQL);

        assertTrue(fragment.startsWith("NOT NULL "));
        assertFalse(fragment.contains("DEFAULT "));
    }

    @Test
    void shouldRenderMysqlVarcharLengthInAlterSql() {
        IntrospectedColumn column = column(JDBCType.VARCHAR, false, null, "VARCHAR");
        column.setActualColumnName("name_");
        column.setLength(64);
        column.setRemarks("name");

        String sql = SqlScriptUtil.getColumnSql(List.of(column), "MODIFY", "MySQL");

        assertTrue(sql.contains("VARCHAR(64)"));
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
