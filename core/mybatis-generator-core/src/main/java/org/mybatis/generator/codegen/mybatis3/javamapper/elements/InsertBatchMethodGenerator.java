package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;

import java.util.Set;
import java.util.TreeSet;

public class InsertBatchMethodGenerator extends AbstractJavaMapperMethodGenerator {

    public InsertBatchMethodGenerator(boolean isSimple) {
        super();
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        Method method = serviceMethods.getInsertBatchMethod(interfaze, true,false);
        addMapperAnnotations(method);
        if (context.getPlugins().clientInsertBatchMethodGenerated(method, interfaze, introspectedTable)) {
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
