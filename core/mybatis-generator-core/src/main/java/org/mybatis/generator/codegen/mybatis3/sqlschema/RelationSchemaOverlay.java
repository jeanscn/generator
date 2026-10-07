package org.mybatis.generator.codegen.mybatis3.sqlschema;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IndexInfo;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;

import java.util.List;

public final class RelationSchemaOverlay {
    private RelationSchemaOverlay() {
    }

    public static void apply(AbstractIntrospectedTable table) {
        List<String> keys = table.getTableConfiguration().getRelationKeyColumns();
        if (keys.isEmpty()) {
            return;
        }
        if (keys.size() != 2 || keys.get(0).equals(keys.get(1))
                || table.getPrimaryKeyColumns().size() != 1
                || !DefaultColumnNameEnum.ID.columnName().equals(table.getPrimaryKeyColumns().get(0).getActualColumnName())
                || !FullyQualifiedJavaType.getStringInstance().equals(table.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType())) {
            throw new IllegalArgumentException("relationKeyColumns requires two distinct columns and a single String surrogate primary key");
        }
        for (String key : keys) {
            IntrospectedColumn column = table.getColumn(key).orElseThrow(() ->
                    new IllegalArgumentException("Unknown relation key column: " + key));
            if (!FullyQualifiedJavaType.getStringInstance().equals(column.getFullyQualifiedJavaType())
                    || table.getPrimaryKeyColumns().contains(column)) {
                throw new IllegalArgumentException("Relation keys must be String columns distinct from the surrogate primary key: " + key);
            }
            column.setNullable(false);
        }
        addIndex(table, "uk_" + table.getTableConfiguration().getTableName(), keys, true);
        addIndex(table, "idx_" + table.getTableConfiguration().getTableName() + "_reverse",
                List.of(keys.get(1), keys.get(0)), false);
    }

    private static void addIndex(AbstractIntrospectedTable table, String name, List<String> keys, boolean unique) {
        if (table.getIndexes().stream().anyMatch(index -> index.isUnique() == unique && index.getColumnNames().equals(keys))) {
            return;
        }
        if (table.getIndexes().stream().anyMatch(index -> index.getName().equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("Relation index name conflicts with existing index: " + name);
        }
        IndexInfo index = new IndexInfo(name, unique);
        for (short i = 0; i < keys.size(); i++) {
            index.addColumn(keys.get(i), (short) (i + 1), "ASC");
        }
        table.addIndex(index);
    }
}
