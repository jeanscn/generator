package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import static org.mybatis.generator.custom.ConstantsUtil.DATATABLES_UTIL;
import static org.mybatis.generator.custom.ConstantsUtil.MODEL_AND_VIEW;

public class GetDefaultViewElementGenerator extends AbstractControllerElementGenerator {

    public GetDefaultViewElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(MODEL_AND_VIEW);
        parentElement.addImportedType(DATATABLES_UTIL);

        final String methodPrefix = "getDefaultView";
        Method method = createMethod(methodPrefix);
        method.setReturnType(new FullyQualifiedJavaType(MODEL_AND_VIEW));

        method.addAnnotation(new SystemLogDesc("查看默认视图",introspectedTable),parentElement);
        method.addAnnotation(new RequestMappingDesc("dt/view-default/show", RequestMethodEnum.GET),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"默认视图");
        method.addAnnotation(new ApiOperationDesc("默认数据视图显示", "显示默认数据视图"),parentElement);
        commentGenerator.addMethodJavaDocLine(method, "查看默认视图");

        method.addBodyLine("ModelAndView modelAndView = new ModelAndView(\"vgoweb/view/DataTables\");");
        method.addBodyLine("modelAndView.addObject(\"viewId\", DataTablesUtil.getDefaultViewId(\"{0}\"));"
                ,introspectedTable.getControllerBeanName());
        method.addBodyLine("modelAndView.addObject(\"listName\", \"{0}\");",introspectedTable.getRemarks(true));
        method.addBodyLine("return modelAndView;");
        parentElement.addMethod(method);
    }
}
