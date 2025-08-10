package org.mybatis.generator.runtime.dynamic.sql.elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;

public class InsertMethodGenerator extends AbstractMethodGenerator {
    private final FullyQualifiedJavaType recordType;

    private InsertMethodGenerator(Builder builder) {
        super(builder);
        recordType = builder.recordType;
    }

    @Override
    public MethodAndImports generateMethodAndImports() {
        Set<FullyQualifiedJavaType> imports = new HashSet<>();

        imports.add(new FullyQualifiedJavaType("org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils"));
        imports.add(recordType);

        Method method = new Method("insert");
        method.setDefault(true);
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, imports);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addParameter(new Parameter(recordType, "row"));

        method.addBodyLine("return MyBatis3Utils.insert(this::insert, row, " + tableFieldName
                + ", c ->");

        List<IntrospectedColumn> columns =
                ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        boolean first = true;
        for (IntrospectedColumn column : columns) {
            String fieldName = calculateFieldName(column);

            if (first) {
                method.addBodyLine("    c.map(" + fieldName
                        + ").toProperty(\"" + column.getJavaProperty()
                        + "\")");
                first = false;
            } else {
                method.addBodyLine("    .map(" + fieldName
                        + ").toProperty(\"" + column.getJavaProperty()
                        + "\")");
            }
        }

        method.addBodyLine(");");

        return MethodAndImports.withMethod(method)
                .withImports(imports)
                .build();
    }

    @Override
    public boolean callPlugins(Method method, Interface interfaze) {
        return context.getPlugins().clientInsertMethodGenerated(method, interfaze, introspectedTable);
    }

    public static class Builder extends BaseBuilder<Builder> {
        private FullyQualifiedJavaType recordType;

        public Builder withRecordType(FullyQualifiedJavaType recordType) {
            this.recordType = recordType;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public InsertMethodGenerator build() {
            return new InsertMethodGenerator(this);
        }
    }
}
