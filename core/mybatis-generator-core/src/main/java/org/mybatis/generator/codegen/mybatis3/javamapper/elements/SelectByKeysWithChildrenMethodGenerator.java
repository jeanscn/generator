package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;

public class SelectByKeysWithChildrenMethodGenerator extends AbstractJavaMapperMethodGenerator {

    public SelectByKeysWithChildrenMethodGenerator() {
        super();
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        Method method = serviceMethods.getSelectByKeysWithChildrenMethod(interfaze,true,false);
        addMapperAnnotations(interfaze, method);
        addExtraImports(interfaze);
        interfaze.addMethod(method);
    }

    public void addMapperAnnotations(Interface interfaze, Method method) {
        // extension point for subclasses
    }

    public void addExtraImports(Interface interfaze) {
        // extension point for subclasses
    }
}
