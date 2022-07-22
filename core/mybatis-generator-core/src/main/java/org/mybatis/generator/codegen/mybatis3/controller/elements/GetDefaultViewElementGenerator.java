package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_VIEW_CONFIG;

public class GetDefaultViewElementGenerator extends AbstractControllerElementGenerator {

    public GetDefaultViewElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(RESPONSE_VIEW_CONFIG);
        final String methodPrefix = "getDefaultView";
        Method method = createMethod(methodPrefix);
        method.setReturnType(responseSimple);
        addControllerMapping(method, "DefaultView", "get");
        method.addBodyLine("ResponseViewConfig response = new ResponseViewConfig();");

        StringBuilder sb = new StringBuilder();

        method.addBodyLine("try {");
        sb.setLength(0);

        method.addBodyLine(sb.toString());
        addExceptionAndReturn(method);
        parentElement.addMethod(method);
    }
}
