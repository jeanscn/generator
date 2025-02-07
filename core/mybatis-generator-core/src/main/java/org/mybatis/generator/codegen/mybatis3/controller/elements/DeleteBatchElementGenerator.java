package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class DeleteBatchElementGenerator extends AbstractControllerElementGenerator {

    public DeleteBatchElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(exampleType);
        final String methodPrefix = "deleteBatch";
        Method method = createMethod(methodPrefix);

        Parameter parameter = new Parameter(new FullyQualifiedJavaType("List<String>"), "ids");
        parameter.addAnnotation("@RequestBody");
        parameter.setRemark("待删除数据的唯一标识列表");
        method.addParameter(parameter);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        response.addTypeArgument(new FullyQualifiedJavaType("java.lang.Long"));
        method.setReturnType(response);
        method.setReturnRemark("成功删除的记录数");

        method.addAnnotation(new SystemLogDesc("删除了一条或多条记录",introspectedTable),parentElement);
        method.addAnnotation(new RequestMappingDesc("", RequestMethodEnum.DELETE),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"批量删除");
        method.addAnnotation(new ApiOperationDesc("批量记录删除", "根据给定的一组id删除多条记录"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "根据ids批量删除记录");

        method.addBodyLine("{0} example = new {0}();", exampleType.getShortName());
        method.addBodyLine("example.createCriteria().andIdIn(ids);");
        method.addBodyLine("ServiceResult<Integer> result =  {0}.{1}(example);",serviceBeanName,introspectedTable.getDeleteByExampleStatementId());
        method.addBodyLine("if (result.hasResult() && result.getResult() > 0) {");
        method.addBodyLine("return success((long) result.getResult(),result.getAffectedRows());");
        method.addBodyLine("} else {");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_CUSTOM,result.getMessage());");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
