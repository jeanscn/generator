package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class DeleteByTableGenerator extends AbstractControllerElementGenerator {

    public DeleteByTableGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                .filter(SelectByTableGeneratorConfiguration::isEnableSplit)
                .forEach(c->{
                    Method method = new Method(c.getSplitMethodName());
                    method.setVisibility(JavaVisibility.PROTECTED);
                    addSystemLogAnnotation(method,parentElement);
                    Parameter p1 = new Parameter(c.getThisColumn().getFullyQualifiedJavaType(), c.getThisColumn().getJavaProperty());
                    p1.addAnnotation("@RequestParam");
                    p1.setRemark(c.getThisColumn().getRemarks(false));
                    method.addParameter(p1);
                    FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
                    listInstance.addTypeArgument(c.getOtherColumn().getFullyQualifiedJavaType());
                    Parameter p2 = new Parameter(listInstance, c.getOtherColumn().getJavaProperty()+"s");
                    p2.addAnnotation("@RequestParam");
                    p2.setRemark(c.getOtherColumn().getRemarks(false));
                    method.addParameter(p2);
                    commentGenerator.addGeneralMethodComment(method, introspectedTable);
                    FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
                    response.addTypeArgument(new FullyQualifiedJavaType("java.lang.Long"));
                    method.setReturnType(response);
                    addControllerMapping(method, "split/"+ JavaBeansUtil.getFirstCharacterLowercase(c.getMethodSuffix()), "post");
                    addSecurityPreAuthorize(method,c.getSplitMethodName(),"删除关系");

                    method.addBodyLine("int rows =  {0}.{1}({2});",serviceBeanName
                            ,c.getSplitMethodName()
                            ,Stream.of(p1,p2).map(Parameter::getName).collect(Collectors.joining(",")));
                    method.addBodyLine("if (rows > 0) return success((long) rows,rows);");
                    method.addBodyLine("else return failure(ApiCodeEnum.FAIL_OPERATION);");
                    parentElement.addMethod(method);
                });




    }
}
