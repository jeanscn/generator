package org.mybatis.generator.codegen.mybatis3.javamapper.elements.annotated;

import static org.mybatis.generator.api.dom.OutputUtilities.javaIndent;
import static org.mybatis.generator.internal.util.StringUtility.escapeStringForJava;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.SelectByPrimaryKeyMethodGenerator;

public class AnnotatedSelectByPrimaryKeyMethodGenerator extends SelectByPrimaryKeyMethodGenerator {

    private final boolean useResultMapIfAvailable;

    public AnnotatedSelectByPrimaryKeyMethodGenerator(boolean useResultMapIfAvailable, boolean isSimple) {
        super(isSimple);
        this.useResultMapIfAvailable = useResultMapIfAvailable;
    }

    @Override
    public void addMapperAnnotations(Interface interfaze, Method method) {
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Select"));

        buildInitialSelectAnnotationStrings().forEach(method::addAnnotation);

        StringBuilder sb = new StringBuilder();
        javaIndent(sb, 1);
        sb.append("\"from ");
        sb.append(escapeStringForJava(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        sb.append("\",");
        method.addAnnotation(sb.toString());

        buildByPrimaryKeyWhereClause().forEach(method::addAnnotation);

        method.addAnnotation("})");

        if (useResultMapIfAvailable) {
            if (introspectedTable.getRules().generateBaseResultMap()
                    || introspectedTable.getRules().generateResultMapWithBLOBs()) {
                addResultMapAnnotation(method);
            } else {
                addAnnotatedResults(interfaze, method, introspectedTable.getNonPrimaryKeyColumns());
            }
        } else {
            addAnnotatedResults(interfaze, method, introspectedTable.getNonPrimaryKeyColumns());
        }
    }

    private void addResultMapAnnotation(Method method) {

        String annotation = String.format("@ResultMap(\"%s.%s\")",
                introspectedTable.getMyBatis3SqlMapNamespace(),
                introspectedTable.getRules().generateResultMapWithBLOBs()
                    ? introspectedTable.getResultMapWithBLOBsId() : introspectedTable.getBaseResultMapId());
        method.addAnnotation(annotation);
    }

    @Override
    public void addExtraImports(Interface interfaze) {
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Select"));

        if (useResultMapIfAvailable) {
            if (introspectedTable.getRules().generateBaseResultMap()
                    || introspectedTable.getRules().generateResultMapWithBLOBs()) {
                interfaze.addImportedType(
                        new FullyQualifiedJavaType("org.apache.ibatis.annotations.ResultMap"));
            } else {
                addAnnotationImports(interfaze);
            }
        } else {
            addAnnotationImports(interfaze);
        }
    }

    private void addAnnotationImports(Interface interfaze) {
        addAnnotatedSelectImports(interfaze);
    }
}
