package org.mybatis.generator.api;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;

class IntrospectedColumnTest {

    @Test
    void shouldReturnLengthForVarchar() {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setJdbcType(Types.VARCHAR);
        column.setLength(255);

        assertThat(column.getSqlFragmentLength()).isEqualTo("(255) ");
    }

    @Test
    void shouldReturnPrecisionAndScaleForDecimal() {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setJdbcType(Types.DECIMAL);
        column.setLength(10);
        column.setScale(2);

        assertThat(column.getSqlFragmentLength()).isEqualTo("(10, 2) ");
    }

    @Test
    void shouldReturnPrecisionOnlyForNumericWithoutScale() {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setJdbcType(Types.NUMERIC);
        column.setLength(10);
        column.setScale(0);

        assertThat(column.getSqlFragmentLength()).isEqualTo("(10) ");
    }

    @Test
    void shouldReturnSpaceForTypesWithoutLength() {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setJdbcType(Types.TIMESTAMP);
        column.setLength(6);

        assertThat(column.getSqlFragmentLength()).isEqualTo(" ");
    }

    @Test
    void shouldReturnSpaceForUnsignedTypes() {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setJdbcType(Types.INTEGER);
        column.setLength(11);
        column.setActualTypeName("INT UNSIGNED");

        assertThat(column.getSqlFragmentLength()).isEqualTo(" ");
    }
}
