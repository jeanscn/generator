package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;

public class DeleteByPrimaryKeyMethodGenerator extends AbstractJavaMapperMethodGenerator {

    public DeleteByPrimaryKeyMethodGenerator(boolean isSimple) {
        super();
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        Method method = serviceMethods.getDeleteByPrimaryKeyMethod(interfaze,true);
        addMapperAnnotations(method);
        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable)) {
            addExtraImports(interfaze);
            interfaze.addMethod(method);
        }
    }

    public void addMapperAnnotations(Method method) {
        // extension point for subclasses
    }

    public void addExtraImports(Interface interfaze) {
        // extension point for subclasses
    }
}
