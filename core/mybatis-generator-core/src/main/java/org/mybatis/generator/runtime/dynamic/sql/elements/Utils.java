package org.mybatis.generator.runtime.dynamic.sql.elements;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.config.GeneratedKey;

public class Utils {

    public static boolean generateDeleteByPrimaryKey(AbstractIntrospectedTable abstractIntrospectedTable) {
        return abstractIntrospectedTable.hasPrimaryKeyColumns();
    }

    public static boolean generateMultipleRowInsert(AbstractIntrospectedTable abstractIntrospectedTable) {
        // multi row inserts work if we don't expect generated keys, or of the generated keys are
        // JDBC standard.
        return abstractIntrospectedTable.getGeneratedKey().map(GeneratedKey::isJdbcStandard)
                .orElse(true);
    }

    public static boolean canRetrieveMultiRowGeneratedKeys(AbstractIntrospectedTable abstractIntrospectedTable) {
        // if the generated keys are JDBC standard, then we can retrieve them
        // if no generated keys, or not JDBC, then we cannot retrieve them
        return abstractIntrospectedTable.getGeneratedKey().map(GeneratedKey::isJdbcStandard)
                .orElse(false);
    }

    public static boolean generateSelectByPrimaryKey(AbstractIntrospectedTable abstractIntrospectedTable) {
        return abstractIntrospectedTable.hasPrimaryKeyColumns()
                && (abstractIntrospectedTable.hasBaseColumns() || abstractIntrospectedTable
                        .hasBlobColumns());
    }

    public static boolean generateUpdateByPrimaryKey(AbstractIntrospectedTable abstractIntrospectedTable) {
        if (ListUtilities.removeGeneratedAlwaysColumns(abstractIntrospectedTable.getNonPrimaryKeyColumns()).isEmpty()) {
            return false;
        }

        return abstractIntrospectedTable.hasPrimaryKeyColumns()
                && (abstractIntrospectedTable.hasBlobColumns() || abstractIntrospectedTable
                        .hasBaseColumns());
    }
}
