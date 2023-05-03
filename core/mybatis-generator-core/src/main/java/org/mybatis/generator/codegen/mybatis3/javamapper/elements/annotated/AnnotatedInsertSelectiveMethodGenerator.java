package org.mybatis.generator.codegen.mybatis3.javamapper.elements.annotated;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.InsertSelectiveMethodGenerator;

public class AnnotatedInsertSelectiveMethodGenerator extends InsertSelectiveMethodGenerator {

    public AnnotatedInsertSelectiveMethodGenerator() {
        super();
    }

    @Override
    public void addMapperAnnotations(Method method) {
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(introspectedTable.getMyBatis3SqlProviderType());

        String s = "@InsertProvider(type=" //$NON-NLS-1$
                + fqjt.getShortName()
                + ".class, method=\"" //$NON-NLS-1$
                + introspectedTable.getInsertSelectiveStatementId()
                + "\")"; //$NON-NLS-1$
        method.addAnnotation(s);

        buildGeneratedKeyAnnotation().ifPresent(method::addAnnotation);
    }

    @Override
    public void addExtraImports(Interface interfaze) {
        interfaze.addImportedTypes(buildGeneratedKeyImportsIfRequired());
        interfaze.addImportedType(
                new FullyQualifiedJavaType("org.apache.ibatis.annotations.InsertProvider")); //$NON-NLS-1$
    }
}
