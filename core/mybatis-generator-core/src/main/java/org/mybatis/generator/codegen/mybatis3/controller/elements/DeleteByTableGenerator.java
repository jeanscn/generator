package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethod;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vgosoft.tool.core.VStringUtil.toHyphenCase;
import static org.mybatis.generator.custom.ConstantsUtil.*;

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
                    Parameter p1 = new Parameter(c.getThisColumn().getFullyQualifiedJavaType(), c.getThisColumn().getJavaProperty());
                    p1.addAnnotation("@RequestParam");
                    p1.setRemark(c.getThisColumn().getRemarks(false));
                    method.addParameter(p1);
                    FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
                    listInstance.addTypeArgument(c.getOtherColumn().getFullyQualifiedJavaType());
                    Parameter p2 = new Parameter(listInstance, c.getOtherColumn().getJavaProperty()+"s");
                    p2.addAnnotation("@RequestParam");
                    p2.addAnnotation("@RequestParamSplit");
                    p2.setRemark(c.getOtherColumn().getRemarks(false));
                    method.addParameter(p2);
                    FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
                    response.addTypeArgument(new FullyQualifiedJavaType("java.lang.Long"));
                    method.setReturnType(response);
                    method.setReturnRemark("成功删除的记录数");

                    method.addAnnotation(new SystemLog("删除数据关联",introspectedTable),parentElement);
                    method.addAnnotation(new RequestMapping("split/"+ toHyphenCase(c.getMethodSuffix())
                            , RequestMethod.POST),parentElement);
                    addSecurityPreAuthorize(method,c.getSplitMethodName(),"删除关系");
                    method.addAnnotation(new ApiOperation("删除数据关联关系", "删除中间表数据"),parentElement);

                    commentGenerator.addMethodJavaDocLine(method, "删除中间关系表数据（取消数据关联）");

                    method.addBodyLine("int rows =  {0}.{1}({2});",serviceBeanName
                            ,c.getSplitMethodName()
                            ,Stream.of(p1,p2).map(Parameter::getName).collect(Collectors.joining(",")));
                    method.addBodyLine("if (rows > 0) return success((long) rows,rows);");
                    method.addBodyLine("else return success(null,0);");
                    parentElement.addMethod(method);
                    parentElement.addImportedType(new FullyQualifiedJavaType(API_CODE_ENUM));
                    parentElement.addImportedType(new FullyQualifiedJavaType(RESPONSE_RESULT));
                    parentElement.addImportedType(new FullyQualifiedJavaType(ANNOTATION_REQUEST_PARAM_SPLIT));

                });

    }
}
