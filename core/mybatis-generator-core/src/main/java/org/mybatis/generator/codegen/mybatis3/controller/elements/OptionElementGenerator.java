package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
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
        if (introspectedTable.getRules().isGenerateRequestVO()) {
            method.addParameter(new Parameter(entityRequestVoType, entityRequestVoType.getShortNameFirstLowCase()));
            parentElement.addImportedType(entityRequestVoType);
        } else {
            method.addParameter(buildMethodParameter(false, false, parentElement));
        }
        Parameter selected = new Parameter(FullyQualifiedJavaType.getStringInstance(), "selected");
        selected.addAnnotation("@RequestParam(required = false)");
        method.addParameter(selected);
        Parameter actionType = new Parameter(FullyQualifiedJavaType.getStringInstance(), "actionType");
        actionType.addAnnotation("@RequestParam(required = false)");
        method.addParameter(actionType);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        FullyQualifiedJavaType typeResult = FullyQualifiedJavaType.getNewListInstance();
        typeResult.addTypeArgument(optionDataType);
        response.addTypeArgument(typeResult);
        method.setReturnType(response);

        addControllerMapping(method, "option/" + JavaBeansUtil.getFirstCharacterLowercase(column.getJavaProperty()), "get");

        String listEntityVar = entityType.getShortNameFirstLowCase() + "s";
        selectByExampleWithPagehelper(parentElement, method);
        Method pMethod = null;

        method.addBodyLine("List<{0}> options = {1}.stream()", optionDataType.getShortName(),listEntityVar);
        if (introspectedTable.getColumn("sort_").isPresent()) {
            method.addBodyLine("        .sorted(Comparator.comparing({0}::getSort))", entityType.getShortName());
        }
        String getterMethodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(), FullyQualifiedJavaType.getStringInstance());
        if (formOptionGeneratorConfiguration.getDataType() == 0) {
            method.addBodyLine("        .map(t -> new FormSelectOption(t.getId(), t.{0}(), selected))", getterMethodName);
        } else {
            method.addBodyLine("         .map(c -> {0}(c, selected))",pMethodName);
            //添加一个内部递归方法
            pMethod = new Method( pMethodName);
            pMethod.setVisibility(JavaVisibility.PRIVATE);
            pMethod.addParameter(new Parameter(entityType, entityType.getShortNameFirstLowCase()));
            pMethod.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "selected"));
            pMethod.setReturnType(optionTreeType);
            pMethod.addBodyLine("FormSelectTreeOption formSelectTreeOption = new FormSelectTreeOption({0}.getId(), {0}.{1}(), selected);",
                    entityType.getShortNameFirstLowCase(),getterMethodName);
            commentGenerator.addMethodJavaDocLine(pMethod, true, "内部方法：递归处理用");
            pMethod.addBodyLine("if ({0}.getChildren().size()>0) '{'\n" +
                    "            formSelectTreeOption.setParent(true);\n" +
                    "            List<FormSelectTreeOption> collect = {0}.getChildren().stream()\n" +
                    "                    .sorted(Comparator.comparing({1}::getSort))\n" +
                    "                    .map(c -> {2}(c, selected))\n" +
                    "                    .collect(Collectors.toList());\n" +
                    "            formSelectTreeOption.setChildren(collect);\n" +
                    "        '}'", entityType.getShortNameFirstLowCase(),entityType.getShortName(),pMethodName);
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
