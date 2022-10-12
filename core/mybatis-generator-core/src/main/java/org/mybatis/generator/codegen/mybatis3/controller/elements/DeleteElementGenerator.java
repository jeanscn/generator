package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.RequestMethod;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.*;

public class DeleteElementGenerator extends AbstractControllerElementGenerator {

    public DeleteElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(entityType);
        final String methodPrefix = "delete";
        Method method = createMethod(methodPrefix);
        List<Parameter> parameters = introspectedTable.getPrimaryKeyColumns().stream().map(c -> {
            Parameter parameter = new Parameter(c.getFullyQualifiedJavaType(), c.getJavaProperty());
            parameter.setRemark(c.getRemarks(false));
            parameter.addAnnotation("@PathVariable");
            return parameter;
        }).collect(Collectors.toList());
        String pathVariable = parameters.stream().map(p -> "{" + p.getName() + "}").collect(Collectors.joining("/"));
        parameters.forEach(method::addParameter);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        response.addTypeArgument(new FullyQualifiedJavaType("java.lang.Long"));
        method.setReturnType(response);
        method.setReturnRemark("成功删除的记录数");

        method.addAnnotation(new SystemLog("删除了一条记录",introspectedTable),parentElement);
        method.addAnnotation(new RequestMapping(pathVariable, RequestMethod.DELETE),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"删除");
        method.addAnnotation(new ApiOperation("单条记录删除", "根据给定的id删除一条记录"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "删除一条记录");

        method.addBodyLine("int rows =  {0}.{1}(id);",serviceBeanName,introspectedTable.getDeleteByPrimaryKeyStatementId());
        method.addBodyLine("if (rows > 0) {");
        method.addBodyLine("return success((long) rows);");
        method.addBodyLine("} else {");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
