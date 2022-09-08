package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.DATATABLES_VIEW_CONFIG;

public class GetDefaultViewConfigElementGenerator extends AbstractControllerElementGenerator {

    public GetDefaultViewConfigElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(DATATABLES_VIEW_CONFIG);
        parentElement.addImportedType("com.vgosoft.web.plugins.datatables.DataTablesUtil");
        final String methodPrefix = "getDefaultViewConfig";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);
        responseResult.addTypeArgument(new FullyQualifiedJavaType(DATATABLES_VIEW_CONFIG));
        method.setReturnType(responseResult);
        addControllerMapping(method, "dt/viewDefault", "post");
        addSecurityPreAuthorize(method,methodPrefix,"默认视图配置");

        method.addBodyLine("try {");
        method.addBodyLine("DataTablesViewConfig dataTablesViewConfig = DataTablesUtil\n" +
                "                    .getConfigFromVoClass(\"{0}\");",entityViewVoType.getFullyQualifiedName());
        method.addBodyLine("return success(dataTablesViewConfig);");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL,e);");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
