package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.*;

public class GetDefaultViewElementGenerator extends AbstractControllerElementGenerator {

    public GetDefaultViewElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(MODEL_AND_VIEW);
        parentElement.addImportedType(responseResult);
        parentElement.addImportedType(DATATABLES_VIEW_CONFIG);
        parentElement.addImportedType(VIEW_DT_TABLE);
        parentElement.addImportedType("com.vgosoft.web.plugins.datatables.DataTablesUtil");
        parentElement.addImportedType("com.vgosoft.web.pojo.maps.ViewDtTableMappings");

        final String methodPrefix = "getDefaultView";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);
        method.setReturnType(new FullyQualifiedJavaType(MODEL_AND_VIEW));
        addControllerMapping(method, "dt/viewDefault/show", "get");
        method.addBodyLine("ModelAndView modelAndView = new ModelAndView(\"vgoweb/view/DataTables3\");");
        method.addBodyLine("try {");
        method.addBodyLine("ResponseResult<{0}> defaultView = getDefaultViewConfig{1}();"
                ,new FullyQualifiedJavaType(DATATABLES_VIEW_CONFIG).getShortName()
                ,entityType.getShortName());
        method.addBodyLine("if (defaultView.hasResult()) {");
        method.addBodyLine("ViewDtTable viewDtTable = ViewDtTableMappings.INSTANCE.fromDataTablesParamOptions(defaultView.getData().getParamOptions());");
        method.addBodyLine("viewDtTable.setConfigUrl(\"{0}/{1}/dt/viewDefault\");"
                ,introspectedTable.getControllerSimplePackage(),serviceBeanName);
        method.addBodyLine("modelAndView.addObject(\"config\",viewDtTable);");
        method.addBodyLine("}else{");
        method.addBodyLine("modelAndView.setViewName(\"page/404\");");
        method.addBodyLine("}");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("modelAndView.setViewName(\"page/500\");");
        method.addBodyLine("e.printStackTrace();");
        method.addBodyLine("}");
        method.addBodyLine("return modelAndView;");
        parentElement.addMethod(method);
    }
}