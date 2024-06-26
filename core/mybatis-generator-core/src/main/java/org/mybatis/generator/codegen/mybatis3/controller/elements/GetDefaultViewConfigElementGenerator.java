package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

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
        parentElement.addImportedType(VIEW_DT_TABLE_VO);
        parentElement.addImportedType(new FullyQualifiedJavaType(API_CODE_ENUM));
        parentElement.addImportedType("com.vgosoft.web.plugins.datatables.DataTablesMappings");
        final String methodPrefix = "getDefaultViewConfig";
        Method method = createMethod(methodPrefix);

        responseResult.addTypeArgument(new FullyQualifiedJavaType(DATATABLES_CONFIG));
        method.setReturnType(responseResult);
        method.setReturnRemark("视图配置数据对象");

        method.addAnnotation(new SystemLogDesc("查看表默认视图配置",introspectedTable),parentElement);
        method.addAnnotation(new RequestMappingDesc("dt/view-default", RequestMethodEnum.POST),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"默认视图配置");
        method.addAnnotation(new ApiOperationDesc("默认数据视图配置", "获取默认数据视图配置"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "查看表默认视图配置");

        method.addBodyLine("Optional<ViewDtTableVO> result = DataTablesUtil");
        method.addBodyLine("        .getSingleViewDtTableByAnnotation(DataTablesUtil.getDefaultViewId(\"{0}\"), true);"
                ,introspectedTable.getControllerBeanName());
        method.addBodyLine("return result.map(viewDtTableVO -> success(DataTablesMappings.INSTANCE.fromViewDtTableVO(viewDtTableVO)))\n" +
                "                .orElseGet(() -> failure(ApiCodeEnum.FAIL_NOT_FOUND,\"默认列表配置\"));");
        parentElement.addMethod(method);
        parentElement.addImportedType("java.util.Optional");
    }
}
