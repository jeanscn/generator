package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.RequestMethod;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

import static org.mybatis.generator.custom.ConstantsUtil.*;

public class GetDefaultViewConfigElementGenerator extends AbstractControllerElementGenerator {

    public GetDefaultViewConfigElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(responseResult);
        parentElement.addImportedType(DATATABLES_CONFIG);
        parentElement.addImportedType(DATATABLES_UTIL);
        parentElement.addImportedType(VIEW_DT_TABLE);
        parentElement.addImportedType(API_CODE_ENUM);
        parentElement.addImportedType("com.vgosoft.web.plugins.datatables.DataTablesMappings");
        final String methodPrefix = "getDefaultViewConfig";
        Method method = createMethod(methodPrefix);

        responseResult.addTypeArgument(new FullyQualifiedJavaType(DATATABLES_CONFIG));
        method.setReturnType(responseResult);
        method.setReturnRemark("视图配置数据对象");

        method.addAnnotation(new SystemLog("查看表默认视图配置",introspectedTable),parentElement);
        method.addAnnotation(new RequestMapping("dt/viewDefault", RequestMethod.POST),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"默认视图配置");
        method.addAnnotation(new ApiOperation("默认数据视图配置", "获取默认数据视图配置"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "查看表默认视图配置");

        method.addBodyLine("Optional<ViewDtTable> result = DataTablesUtil");
        method.addBodyLine("        .getSingleViewDtTableByAnnotation(DataTablesUtil.getDefaultViewId(\"{0}\"), true);"
                ,introspectedTable.getControllerBeanName());
        method.addBodyLine("return result.map(viewDtTable -> success(DataTablesMappings.INSTANCE.fromViewDtTable(viewDtTable)))\n" +
                "                .orElseGet(() -> failure(ApiCodeEnum.FAIL_NOT_FOUND,\"默认列表配置\"));");
        parentElement.addMethod(method);
    }
}
