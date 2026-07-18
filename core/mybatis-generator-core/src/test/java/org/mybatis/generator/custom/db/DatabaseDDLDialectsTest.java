package org.mybatis.generator.custom.db;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseDDLDialectsTest {

    @Test
    void shouldQuoteIdentifierByDialect() {
        assertThat(DatabaseDDLDialects.MYSQL.quoteIdentifier("user")).isEqualTo("`user`");
        assertThat(DatabaseDDLDialects.ORACLE.quoteIdentifier("user")).isEqualTo("\"user\"");
        assertThat(DatabaseDDLDialects.SQL_SERVER.quoteIdentifier("user")).isEqualTo("[user]");
    }

    @Test
    void shouldDefineCommentSupportByDialect() {
        assertThat(DatabaseDDLDialects.MYSQL.supportsInlineColumnComment()).isTrue();
        assertThat(DatabaseDDLDialects.ORACLE.supportsInlineColumnComment()).isFalse();
        assertThat(DatabaseDDLDialects.ORACLE.supportsCommentOnStatement()).isTrue();
        assertThat(DatabaseDDLDialects.SQLITE.supportsCommentOnStatement()).isFalse();
    }

    @Test
    void shouldProvideDefaultDateTimeExpressionsByDialect() {
        assertThat(DatabaseDDLDialects.MYSQL.currentDateTimeExpression()).isEqualTo("CURRENT_TIMESTAMP");
        assertThat(DatabaseDDLDialects.MYSQL.currentDateExpression()).isEqualTo("(CURRENT_DATE)");
        assertThat(DatabaseDDLDialects.MYSQL.currentTimeExpression()).isEqualTo("(CURRENT_TIME)");
        assertThat(DatabaseDDLDialects.SQL_SERVER.currentDateExpression()).isEqualTo("CAST(CURRENT_TIMESTAMP AS DATE)");
        assertThat(DatabaseDDLDialects.SQL_SERVER2005.currentTimeExpression()).isEqualTo("CAST(CURRENT_TIMESTAMP AS TIME)");
        assertThat(DatabaseDDLDialects.ORACLE.currentTimeExpression()).isEqualTo("CURRENT_TIMESTAMP");
    }
}
