package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;

public class InsertByTableMethodGenerator extends AbstractJavaMapperMethodGenerator {

    private final boolean isSimple;

    private final SelectByTableGeneratorConfiguration configuration;

    public InsertByTableMethodGenerator(boolean isSimple, SelectByTableGeneratorConfiguration configuration) {
        super();
        this.isSimple = isSimple;
        this.configuration = configuration;
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        if (isSimple) {
            return;
        }
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        Method method = serviceMethods.getSplitUnionByTableMethod(interfaze, configuration, true, true,false);
        addMapperAnnotations(method);
        if (context.getPlugins().clientInsertOrDeleteByTableMethodGenerated(method, interfaze, introspectedTable)) {
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
