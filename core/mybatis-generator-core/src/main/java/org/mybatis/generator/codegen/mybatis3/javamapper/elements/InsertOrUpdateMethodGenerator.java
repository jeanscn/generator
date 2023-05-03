package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;

import java.util.Set;
import java.util.TreeSet;

public class InsertOrUpdateMethodGenerator extends AbstractJavaMapperMethodGenerator {

    private final boolean isSimple;

    public InsertOrUpdateMethodGenerator(boolean isSimple) {
        super();
        this.isSimple = isSimple;
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        Method method = serviceMethods.getInsertOrUpdateMethod(interfaze, true,false);
        addMapperAnnotations(method);
        if (context.getPlugins().clientInsertOrUpdateMethodGenerated(method, interfaze, introspectedTable)) {
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
