package org.mybatis.generator.runtime.dynamic.sql.elements;

import java.util.HashSet;
import java.util.Set;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.config.GeneratedKey;

public class BasicMultipleInsertMethodGenerator extends AbstractMethodGenerator {

    private final FullyQualifiedJavaType recordType;

    private BasicMultipleInsertMethodGenerator(Builder builder) {
        super(builder);
        recordType = builder.recordType;
    }

    @Override
    public MethodAndImports generateMethodAndImports() {
        if (!Utils.generateMultipleRowInsert(introspectedTable)) {
            return null;
        }

        return introspectedTable.getGeneratedKey().map(this::generateMethodWithGeneratedKeys).orElse(null);
    }

    private MethodAndImports generateMethodWithGeneratedKeys(GeneratedKey gk) {
        Set<FullyQualifiedJavaType> imports = new HashSet<>();

        imports.add(new FullyQualifiedJavaType("org.mybatis.dynamic.sql.util.SqlProviderAdapter"));
        imports.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.InsertProvider"));
        imports.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        Parameter parm1 = new Parameter(FullyQualifiedJavaType.getStringInstance(), "insertStatement");
        parm1.addAnnotation("@Param(\"insertStatement\")");

        FullyQualifiedJavaType recordListType = FullyQualifiedJavaType.getNewListInstance();
        recordListType.addTypeArgument(recordType);
        imports.add(recordListType);

        Parameter parm2 = new Parameter(recordListType, "records");
        parm2.addAnnotation("@Param(\"records\")");

        Method method = new Method("insertMultiple");
        method.setAbstract(true);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addParameter(parm1);
        method.addParameter(parm2);
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, imports);
        method.addAnnotation("@InsertProvider(type=SqlProviderAdapter.class, "
                + "method=\"insertMultipleWithGeneratedKeys\")");

        MethodAndImports.Builder builder = MethodAndImports.withMethod(method)
                .withImports(imports);

        MethodParts methodParts = getGeneratedKeyAnnotation(gk);
        acceptParts(builder, method, methodParts);

        return builder.build();
    }

    private MethodParts getGeneratedKeyAnnotation(GeneratedKey gk) {
        MethodParts.Builder builder = new MethodParts.Builder();

        StringBuilder sb = new StringBuilder();
        introspectedTable.getColumn(gk.getColumn()).ifPresent(introspectedColumn -> {
            if (gk.isJdbcStandard()) {
                // only jdbc standard keys are supported for multiple insert
                builder.withImport(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Options"));
                sb.append("@Options(useGeneratedKeys=true,keyProperty=\"records.");
                sb.append(introspectedColumn.getJavaProperty());
                sb.append("\")");
                builder.withAnnotation(sb.toString());
            }
        });

        return builder.build();
    }

    @Override
    public boolean callPlugins(Method method, Interface interfaze) {
        return context.getPlugins().clientBasicInsertMultipleMethodGenerated(method, interfaze, introspectedTable);
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

        public BasicMultipleInsertMethodGenerator build() {
            return new BasicMultipleInsertMethodGenerator(this);
        }
    }
}
