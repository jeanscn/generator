package org.mybatis.generator.runtime.kotlin.elements;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.kotlin.FullyQualifiedKotlinType;
import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;

public class InsertSelectiveMethodGenerator extends AbstractKotlinFunctionGenerator {
    private final FullyQualifiedKotlinType recordType;
    private final String mapperName;
    private final String supportObjectImport;

    private InsertSelectiveMethodGenerator(Builder builder) {
        super(builder);
        recordType = builder.recordType;
        mapperName = builder.mapperName;
        supportObjectImport = builder.supportObjectImport;
    }

    @Override
    public KotlinFunctionAndImports generateMethodAndImports() {
        KotlinFunctionAndImports functionAndImports = KotlinFunctionAndImports.withFunction(
                KotlinFunction.newOneLineFunction(mapperName + ".insertSelective")
                .withArgument(KotlinArg.newArg("row")
                        .withDataType(recordType.getShortNameWithTypeArguments())
                        .build())
                .build())
                .withImport("org.mybatis.dynamic.sql.util.kotlin.mybatis3.insert")
                .withImports(recordType.getImportList())
                .build();

        addFunctionComment(functionAndImports);

        KotlinFunction function = functionAndImports.getFunction();

        function.addCodeLine("insert(this::insert, row, " + tableFieldName
                + ") {");

        List<IntrospectedColumn> columns =
                ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        for (IntrospectedColumn column : columns) {
            AbstractKotlinFunctionGenerator.FieldNameAndImport fieldNameAndImport =
                    AbstractKotlinFunctionGenerator.calculateFieldNameAndImport(tableFieldName,
                            supportObjectImport, column);
            functionAndImports.getImports().add(fieldNameAndImport.importString());

            if (column.isSequenceColumn()) {
                function.addCodeLine("    map(" + fieldNameAndImport.fieldName()
                        + ").toProperty(\"" + column.getJavaProperty()
                        + "\")");
            } else {
                function.addCodeLine("    map(" + fieldNameAndImport.fieldName()
                        + ").toPropertyWhenPresent(\"" + column.getJavaProperty()
                        + "\", row::"
                        + column.getJavaProperty() + ")");
            }
        }

        function.addCodeLine("}");

        return functionAndImports;
    }

    @Override
    public boolean callPlugins(KotlinFunction kotlinFunction, KotlinFile kotlinFile) {
        return context.getPlugins().clientInsertSelectiveMethodGenerated(kotlinFunction, kotlinFile, introspectedTable);
    }

    public static class Builder extends BaseBuilder<Builder> {
        private FullyQualifiedKotlinType recordType;
        private String mapperName;
        private String supportObjectImport;

        public Builder withRecordType(FullyQualifiedKotlinType recordType) {
            this.recordType = recordType;
            return this;
        }

        public Builder withMapperName(String mapperName) {
            this.mapperName = mapperName;
            return this;
        }

        public Builder withSupportObjectImport(String supportObjectImport) {
            this.supportObjectImport = supportObjectImport;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public InsertSelectiveMethodGenerator build() {
            return new InsertSelectiveMethodGenerator(this);
        }
    }
}
