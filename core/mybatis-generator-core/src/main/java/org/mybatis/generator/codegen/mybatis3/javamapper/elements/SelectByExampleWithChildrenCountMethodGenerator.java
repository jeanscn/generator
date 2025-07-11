package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import java.util.Set;
import java.util.TreeSet;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;

public class SelectByExampleWithChildrenCountMethodGenerator extends AbstractJavaMapperMethodGenerator {

    public SelectByExampleWithChildrenCountMethodGenerator() {
        super();
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        Method method = serviceMethods.getSelectWithChildrenCountMethod(interfaze,true,false);
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        importedTypes.add(type);
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());

        //Method method = new Method(introspectedTable.getSelectByExampleWithChildrenCountStatementId());
        //method.setVisibility(JavaVisibility.PUBLIC);
        //method.setAbstract(true);

//        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
//        FullyQualifiedJavaType listType;
//        if (introspectedTable.getRules().generateBaseRecordClass()) {
//            listType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
//        } else if (introspectedTable.getRules().generatePrimaryKeyClass()) {
//            listType = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
//        } else {
//            throw new RuntimeException(getString("RuntimeError.12")); //$NON-NLS-1$
//        }
//
//        importedTypes.add(listType);
//        returnType.addTypeArgument(listType);
//        method.setReturnType(returnType);
//
//        Parameter parameter = new Parameter(type, "example");
//        parameter.setRemark("查询条件example对象");
//        method.addParameter(parameter); //$NON-NLS-1$

        context.getCommentGenerator().addGeneralMethodComment(method,introspectedTable);
        addMapperAnnotations(interfaze, method);
        addExtraImports(interfaze);
        interfaze.addImportedTypes(importedTypes);
        interfaze.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        interfaze.addMethod(method);
    }

    public void addMapperAnnotations(Interface interfaze, Method method) {
        // extension point for subclasses
    }

    public void addExtraImports(Interface interfaze) {
        // extension point for subclasses
    }
}
