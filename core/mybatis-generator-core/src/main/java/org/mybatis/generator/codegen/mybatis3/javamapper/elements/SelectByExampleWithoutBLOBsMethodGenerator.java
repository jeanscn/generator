package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.Set;
import java.util.TreeSet;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;

public class SelectByExampleWithoutBLOBsMethodGenerator extends AbstractJavaMapperMethodGenerator {

    public SelectByExampleWithoutBLOBsMethodGenerator() {
        super();
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        importedTypes.add(type);
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());

        Method method = new Method(introspectedTable.getSelectByExampleStatementId());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);

        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType listType;
        if (introspectedTable.getRules().generateBaseRecordClass()) {
            listType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        } else if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            listType = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
        } else {
            throw new RuntimeException(getString("RuntimeError.12","SelectByExampleWithoutBLOBs")); //$NON-NLS-1$
        }

        importedTypes.add(listType);
        returnType.addTypeArgument(listType);
        method.setReturnType(returnType);

        Parameter parameter = new Parameter(type, "example");
        parameter.setRemark("查询条件example对象");
        method.addParameter(parameter); //$NON-NLS-1$

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        addMapperAnnotations(interfaze, method);

        if (context.getPlugins()
                .clientSelectByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable)) {
            addExtraImports(interfaze);
            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }

    public void addMapperAnnotations(Interface interfaze, Method method) {
        // extension point for subclasses
    }

    public void addExtraImports(Interface interfaze) {
        // extension point for subclasses
    }
}
