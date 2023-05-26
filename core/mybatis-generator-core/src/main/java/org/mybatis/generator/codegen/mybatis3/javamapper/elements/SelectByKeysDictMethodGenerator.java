package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;

public class SelectByKeysDictMethodGenerator extends AbstractJavaMapperMethodGenerator {

    public SelectByKeysDictMethodGenerator() {
        super();
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        VOCacheGeneratorConfiguration config = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();
        Method method = serviceMethods.getSelectByKeysDictMethod(interfaze,config,true,false);
        addMapperAnnotations(interfaze, method);
        if (context.getPlugins().clientSelectByKeysDicMethodGenerated(method, interfaze, introspectedTable)) {
            addExtraImports(interfaze);
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
