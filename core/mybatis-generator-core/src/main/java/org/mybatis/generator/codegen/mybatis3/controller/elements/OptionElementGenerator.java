package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.FormOptionGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import static com.vgosoft.tool.core.VStringUtil.toHyphenCase;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class OptionElementGenerator extends AbstractControllerElementGenerator {

    private final FullyQualifiedJavaType optionType = new FullyQualifiedJavaType("com.vgosoft.system.web.FormSelectOption");
    private final FullyQualifiedJavaType optionTreeType = new FullyQualifiedJavaType("com.vgosoft.system.web.FormSelectTreeOption");

    private final FormOptionGeneratorConfiguration formOptionGeneratorConfiguration;

    public OptionElementGenerator(FormOptionGeneratorConfiguration formOptionGeneratorConfiguration) {
        super();
        this.formOptionGeneratorConfiguration = formOptionGeneratorConfiguration;
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        IntrospectedColumn column = introspectedTable.getColumn(formOptionGeneratorConfiguration.getNameColumn()).orElse(null);
        if (column == null) {
            return;
        }
        FullyQualifiedJavaType optionDataType = formOptionGeneratorConfiguration.getDataType() == 1?optionTreeType:optionType;
        String methodKey = JavaBeansUtil.getFirstCharacterUppercase(column.getJavaProperty());
        String pMethodName = "transfer"+entityType.getShortName()+methodKey+"ToFormSelectTreeOption";
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(responseResult);
        parentElement.addImportedType(optionDataType);
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(exampleType);
        parentElement.addImportedType("java.util.Comparator");
        parentElement.addImportedType("java.util.stream.Collectors");

        final String methodPrefix = "option" + methodKey;
        Method method = createMethod(methodPrefix);
        MethodParameterDescriptor mpd = new MethodParameterDescriptor(parentElement,"get");
        Parameter parameter = buildMethodParameter(mpd);
        parameter.setRemark("用于接收属性同名参数");
        method.addParameter(parameter);
        Parameter selected = new Parameter(FullyQualifiedJavaType.getStringInstance(), "selected");
        selected.addAnnotation("@RequestParam(required = false)");
        selected.setRemark("选中的值，用于前端选中数据的回显");
        method.addParameter(selected);
        Parameter actionType = new Parameter(FullyQualifiedJavaType.getStringInstance(), "actionType");
        actionType.addAnnotation("@RequestParam(required = false)");
        actionType.setRemark("可选参数，查询场景标识");
        method.addParameter(actionType);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        FullyQualifiedJavaType typeResult = FullyQualifiedJavaType.getNewListInstance();
        typeResult.addTypeArgument(optionDataType);
        response.addTypeArgument(typeResult);
        method.setReturnType(response);
        method.setReturnRemark("FormSelectTreeOption对象列表");

        method.addAnnotation(new SystemLogDesc("调用数据选项接口",introspectedTable),parentElement);
        method.addAnnotation(new RequestMappingDesc("option/"+ toHyphenCase(column.getJavaProperty())
                , RequestMethodEnum.GET),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"数据选项");
        method.addAnnotation(new ApiOperationDesc("获取Options-XX选项列表","根据给定条件获取Options-XXX选项列表，可以根据需要传入属性同名参数、消费端选中的值"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "获取Options-XX选项列表");

        selectByExampleWithPagehelper(parentElement, method);
        Method pMethod = null;

        method.addBodyLine("List<{0}> options = result.getResult().stream()", optionDataType.getShortName());
        if (introspectedTable.getColumn("sort_").isPresent()) {
            method.addBodyLine("        .sorted(Comparator.comparing({0}::getSort))", entityType.getShortName());
        }
        String getterMethodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(), FullyQualifiedJavaType.getStringInstance());
        IntrospectedColumn idColumn = introspectedTable.getColumn(formOptionGeneratorConfiguration.getIdColumn()).orElse(null);
        String idGetterName = idColumn!=null?JavaBeansUtil.getGetterMethodName(idColumn.getJavaProperty(), idColumn.getFullyQualifiedJavaType()):"getId";
        if (formOptionGeneratorConfiguration.getDataType() == 0) {  //0-flat，1-tree
            String idGetter;
            if (idColumn != null && idColumn.isStringColumn()) {
                idGetter = "t."+idGetterName+"()";
            }else if (idColumn != null && idColumn.isBigDecimalColumn()) {
                idGetter = "t."+idGetterName+"().toPlainString()";
            }else{
                idGetter = "String.valueOf(t."+idGetterName+"())";
            }
            method.addBodyLine("        .map(t -> new FormSelectOption({1}, t.{0}(), selected))", getterMethodName,idGetter);
        } else {
            method.addBodyLine("         .map(c -> {0}(c, selected))",pMethodName);
            //添加一个内部递归方法
            pMethod = new Method( pMethodName);
            pMethod.setVisibility(JavaVisibility.PRIVATE);
            pMethod.addParameter(new Parameter(entityType, entityType.getShortNameFirstLowCase()));
            pMethod.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "selected"));
            pMethod.setReturnType(optionTreeType);
            pMethod.addBodyLine("FormSelectTreeOption formSelectTreeOption = new FormSelectTreeOption({0}.{2}(), {0}.{1}(), selected);",
                    entityType.getShortNameFirstLowCase(),getterMethodName,idGetterName);
            commentGenerator.addMethodJavaDocLine(pMethod, true, "内部方法：递归处理用");
            pMethod.addBodyLine("if ({0}.getChildren().isEmpty()) '{'",entityType.getShortNameFirstLowCase());
            pMethod.addBodyLine("formSelectTreeOption.setParent(true);");
            pMethod.addBodyLine("List<FormSelectTreeOption> collect = {0}.getChildren().stream()",entityType.getShortNameFirstLowCase());
            pMethod.addBodyLine("        .sorted(Comparator.comparing({0}::getSort))",entityType.getShortName());
            pMethod.addBodyLine("        .map(c -> {0}(c, selected))",pMethodName);
            pMethod.addBodyLine("        .collect(Collectors.toList());");
            pMethod.addBodyLine("formSelectTreeOption.setChildren(collect);}");
            pMethod.addBodyLine("return formSelectTreeOption;");
        }
        method.addBodyLine("        .distinct().collect(Collectors.toList());");
        if (introspectedTable.getRules().isGenerateRequestVO()) {
            method.addBodyLine("if (page!=null) {");
            method.addBodyLine("return ResponsePagehelperResult.success(options,page);");
            method.addBodyLine("} else {");
            method.addBodyLine("return ResponseResult.success(options);");
            method.addBodyLine("}");
            parentElement.addImportedType(responsePagehelperResult);
        } else {
            method.addBodyLine("return ResponseResult.success(options);");
        }
        parentElement.addMethod(method);
        if (pMethod != null) {
            parentElement.addMethod(pMethod);
        }
    }
}
