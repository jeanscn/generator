package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.RequestMethod;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class InsertByTableGenerator extends AbstractControllerElementGenerator {

    public InsertByTableGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                .filter(SelectByTableGeneratorConfiguration::isEnableUnion)
                .forEach(c -> {
                    Method method = new Method(c.getUnionMethodName());
                    method.setVisibility(JavaVisibility.PROTECTED);

                    Parameter p1 = new Parameter(c.getThisColumn().getFullyQualifiedJavaType(), c.getThisColumn().getJavaProperty());
                    p1.addAnnotation("@RequestParam");
                    p1.setRemark(c.getThisColumn().getRemarks(false));
                    method.addParameter(p1);
                    FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
                    listInstance.addTypeArgument(c.getOtherColumn().getFullyQualifiedJavaType());
                    Parameter p2 = new Parameter(listInstance, c.getOtherColumn().getJavaProperty() + "s");
                    p2.addAnnotation("@RequestParam");
                    p2.setRemark(c.getOtherColumn().getRemarks(false));
                    method.addParameter(p2);
                    FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
                    response.addTypeArgument(new FullyQualifiedJavaType("java.lang.Long"));
                    method.setReturnType(response);
                    method.setReturnRemark("成功添加的记录数");

                    method.addAnnotation(new SystemLog("添加数据关联",introspectedTable),parentElement);
                    method.addAnnotation(new RequestMapping(this.serviceBeanName + "/union/"+ JavaBeansUtil.getFirstCharacterLowercase(c.getMethodSuffix())
                            , RequestMethod.POST),parentElement);
                    addSecurityPreAuthorize(method,c.getUnionMethodName(),"添加关系");
                    method.addAnnotation(new ApiOperation("添加数据关联关系", "添加中间表数据"),parentElement);

                    commentGenerator.addMethodJavaDocLine(method, "添加中间关系表数据（添加数据关联）");

                    method.addBodyLine("int rows =  {0}.{1}({2});", serviceBeanName
                            , c.getUnionMethodName()
                            , Stream.of(p1, p2).map(Parameter::getName).collect(Collectors.joining(",")));
                    method.addBodyLine("if (rows > 0) return success((long) rows,rows);");
                    method.addBodyLine("else return failure(ApiCodeEnum.FAIL_OPERATION);");
                    parentElement.addMethod(method);
                });


    }
}
