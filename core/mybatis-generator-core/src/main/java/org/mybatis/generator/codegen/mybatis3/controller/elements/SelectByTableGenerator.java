package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import static com.vgosoft.tool.core.VStringUtil.toHyphenCase;
import static org.mybatis.generator.custom.ConstantsUtil.API_CODE_ENUM;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class SelectByTableGenerator extends AbstractControllerElementGenerator {

    public SelectByTableGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                .filter(c -> c.isEnableUnion() || c.isEnableSplit())
                .forEach(c -> {
                    Method method = new Method(c.getSelectByTableMethodName());
                    method.setVisibility(JavaVisibility.PROTECTED);
                    Parameter p1;
                    if (c.getParameterType().equals("list")) {
                        FullyQualifiedJavaType newListInstance = FullyQualifiedJavaType.getNewListInstance();
                        newListInstance.addTypeArgument(c.getOtherColumn().getFullyQualifiedJavaType());
                        p1 = new Parameter(newListInstance, c.getOtherColumn().getJavaProperty() + "s");
                        if (introspectedTable.getRules().isGenerateVueEnd()) {
                            p1.addAnnotation("@RequestBody");
                        }else{
                            p1.addAnnotation("@RequestParam");
                            p1.addAnnotation("@RequestParamSplit");
                        }
                    } else {
                        p1 = new Parameter(c.getOtherColumn().getFullyQualifiedJavaType(), c.getOtherColumn().getJavaProperty());
                        p1.addAnnotation("@RequestParam");
                    }
                    p1.setRemark(c.getThisColumn().getRemarks(false));
                    method.addParameter(p1);
                    FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
                    if (c.isReturnPrimaryKey()) {
                        listInstance.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
                    } else {
                        listInstance.addTypeArgument(entityType);
                    }
                    FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
                    response.addTypeArgument(listInstance);
                    method.setReturnType(response);
                    method.setReturnRemark(c.isReturnPrimaryKey() ? "关系的当前表的数据标识集合" : "关系的当前表的数据集合");

                    method.addAnnotation(new SystemLogDesc(c.isReturnPrimaryKey() ? "获取中间关系中的当前表的数据标识" : "根据给定的标识获取关系中当前表的数据对象", introspectedTable), parentElement);
                    method.addAnnotation(new RequestMappingDesc("select/" + toHyphenCase(c.getMethodSuffix()), RequestMethodEnum.GET), parentElement);
                    method.addAnnotation(new ApiOperationDesc("获取中间关系表的当前表数据（标识）", "获取中间关系表的当前表数据（标识）（查找存在的数据关系）"), parentElement);
                    commentGenerator.addMethodJavaDocLine(method, "获取中间关系表的数据（标识）");

                    method.addBodyLine("List<{0}> ret = {1}.{2}({3});"
                            , c.isReturnPrimaryKey() ? "String" : entityType.getShortName()
                            , serviceBeanName
                            , c.getSelectByTableMethodName()
                            , p1.getName());
                    method.addBodyLine(" return success(ret,ret.size());");
                    parentElement.addMethod(method);
                    parentElement.addImportedType(new FullyQualifiedJavaType(API_CODE_ENUM));
                    parentElement.addImportedType(new FullyQualifiedJavaType(RESPONSE_RESULT));
                    parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                });

    }
}
