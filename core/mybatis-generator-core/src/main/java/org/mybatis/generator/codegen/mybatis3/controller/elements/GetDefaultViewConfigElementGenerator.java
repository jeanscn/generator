package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.RequestMethod;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

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

        responseResult.addTypeArgument(new FullyQualifiedJavaType(DATATABLES_VIEW_CONFIG));
        method.setReturnType(responseResult);
        method.setReturnRemark("视图配置数据对象");

        method.addAnnotation(new SystemLog("查看表默认视图配置",introspectedTable),parentElement);
        method.addAnnotation(new RequestMapping(this.serviceBeanName + "/dt/viewDefault", RequestMethod.POST),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"默认视图配置");
        method.addAnnotation(new ApiOperation("默认数据视图配置", "获取默认数据视图配置"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "查看表默认视图配置");

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
