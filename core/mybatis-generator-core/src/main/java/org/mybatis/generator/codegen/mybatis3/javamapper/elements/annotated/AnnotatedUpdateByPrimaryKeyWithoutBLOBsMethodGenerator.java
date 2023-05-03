package org.mybatis.generator.codegen.mybatis3.javamapper.elements.annotated;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.UpdateByPrimaryKeyWithoutBLOBsMethodGenerator;

public class AnnotatedUpdateByPrimaryKeyWithoutBLOBsMethodGenerator
        extends UpdateByPrimaryKeyWithoutBLOBsMethodGenerator {

    private final boolean isSimple;

    public AnnotatedUpdateByPrimaryKeyWithoutBLOBsMethodGenerator(boolean isSimple) {
        super();
        this.isSimple = isSimple;
    }

    @Override
    public void addMapperAnnotations(Method method) {
        if (isSimple) {
            buildUpdateByPrimaryKeyAnnotations(introspectedTable.getNonPrimaryKeyColumns())
                    .forEach(method::addAnnotation);
        } else {
            buildUpdateByPrimaryKeyAnnotations(introspectedTable.getBaseColumns()).forEach(method::addAnnotation);
        }
    }

    @Override
    public void addExtraImports(Interface interfaze) {
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Update")); //$NON-NLS-1$
    }
}
