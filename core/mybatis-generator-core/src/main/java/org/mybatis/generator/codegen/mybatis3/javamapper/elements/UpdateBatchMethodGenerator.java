package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;

public class UpdateBatchMethodGenerator extends AbstractJavaMapperMethodGenerator {

    private final boolean isSimple;

    public UpdateBatchMethodGenerator(boolean isSimple) {
        super();
        this.isSimple = isSimple;
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        Method method = serviceMethods.getUpdateBatchMethod(interfaze,true,false);
        addMapperAnnotations(method);
        if (context.getPlugins().clientUpdateBatchMethodGenerated(method, interfaze, introspectedTable)) {
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
