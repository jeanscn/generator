package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.RequestMethod;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;
import org.mybatis.generator.custom.pojo.FormOptionGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class OptionElementGenerator extends AbstractControllerElementGenerator {

    private final FullyQualifiedJavaType optionType = new FullyQualifiedJavaType("com.vgosoft.web.pojo.FormSelectOption");
    private final FullyQualifiedJavaType optionTreeType = new FullyQualifiedJavaType("com.vgosoft.web.pojo.FormSelectTreeOption");

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
        MethodParameterDescript descript = new MethodParameterDescript(parentElement,"get");
        Parameter parameter = buildMethodParameter(descript);
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

        method.addAnnotation(new SystemLog("调用数据选项接口",introspectedTable),parentElement);
        method.addAnnotation(new RequestMapping("option/"+ JavaBeansUtil.getFirstCharacterLowercase(column.getJavaProperty())
                , RequestMethod.GET),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"数据选项");
        method.addAnnotation(new ApiOperation("获取Options-XX选项列表","根据给定条件获取Options-XXX选项列表，可以根据需要传入属性同名参数、消费端选中的值"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "获取Options-XX选项列表");

        String listEntityVar = entityType.getShortNameFirstLowCase() + "s";
        selectByExampleWithPagehelper(parentElement, method);
        Method pMethod = null;

        method.addBodyLine("List<{0}> options = result.getResult().stream()", optionDataType.getShortName());
        if (introspectedTable.getColumn("sort_").isPresent()) {
            method.addBodyLine("        .sorted(Comparator.comparing({0}::getSort))", entityType.getShortName());
        }
        String getterMethodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(), FullyQualifiedJavaType.getStringInstance());
        IntrospectedColumn idColumn = introspectedTable.getColumn(formOptionGeneratorConfiguration.getIdColumn()).orElse(null);
        String idGetterName = idColumn!=null?
                JavaBeansUtil.getGetterMethodName(idColumn.getJavaProperty(), idColumn.getFullyQualifiedJavaType()):"getId";
        if (formOptionGeneratorConfiguration.getDataType() == 0) {
            method.addBodyLine("        .map(t -> new FormSelectOption(t.{1}(), t.{0}(), selected))", getterMethodName,idGetterName);
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
            pMethod.addBodyLine("if ({0}.getChildren().size()>0) '{'",entityType.getShortNameFirstLowCase());
            pMethod.addBodyLine("formSelectTreeOption.setParent(true);");
            pMethod.addBodyLine("List<FormSelectTreeOption> collect = {0}.getChildren().stream()",entityType.getShortNameFirstLowCase());
            pMethod.addBodyLine("        .sorted(Comparator.comparing({0}::getSort))",entityType.getShortName());
            pMethod.addBodyLine("        .map(c -> {0}(c, selected))",pMethodName);
            pMethod.addBodyLine("        .collect(Collectors.toList());");
            pMethod.addBodyLine("formSelectTreeOption.setChildren(collect);}");
            pMethod.addBodyLine("return formSelectTreeOption;");
        }
        method.addBodyLine("        .distinct().collect(Collectors.toList());");
        method.addBodyLine("return ResponsePagehelperResult.success(options,page);");
        parentElement.addMethod(method);
        if (pMethod != null) {
            parentElement.addMethod(pMethod);
        }
    }
}
