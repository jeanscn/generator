package org.mybatis.generator.codegen.mybatis3.javamapper.elements.annotated;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.SelectByExampleWithoutBLOBsMethodGenerator;

public class AnnotatedSelectByExampleWithoutBLOBsMethodGenerator extends SelectByExampleWithoutBLOBsMethodGenerator {

    public AnnotatedSelectByExampleWithoutBLOBsMethodGenerator() {
        super();
    }

    @Override
    public void addMapperAnnotations(Interface interfaze, Method method) {
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(introspectedTable.getMyBatis3SqlProviderType());

        String s = "@SelectProvider(type=" //$NON-NLS-1$
                + fqjt.getShortName()
                + ".class, method=\"" //$NON-NLS-1$
                + introspectedTable.getSelectByExampleStatementId()
                + "\")"; //$NON-NLS-1$
        method.addAnnotation(s);

        addAnnotatedResults(interfaze, method, introspectedTable.getBaseColumns());
    }

    @Override
    public void addExtraImports(Interface interfaze) {
        addAnnotatedSelectImports(interfaze);
        interfaze.addImportedType(
                new FullyQualifiedJavaType("org.apache.ibatis.annotations.SelectProvider")); //$NON-NLS-1$
    }
}
