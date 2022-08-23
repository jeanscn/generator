package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.pojo.FormOptionGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class OptionElementGenerator extends AbstractControllerElementGenerator {

    private final FullyQualifiedJavaType optionType = new FullyQualifiedJavaType("com.vgosoft.web.pojo.FormSelectOption");

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

        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(responseResult);
        parentElement.addImportedType(optionType);
        parentElement.addImportedType(exampleType);
        parentElement.addImportedType("java.util.Comparator");
        parentElement.addImportedType("java.util.stream.Collectors");
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
        }else{
            parentElement.addImportedType(entityType);
        }
        final String methodPrefix = "option"+JavaBeansUtil.getFirstCharacterUppercase(column.getJavaProperty());
        Method method = createMethod(methodPrefix);
        method.addParameter(buildMethodParameter(false,false));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "selected"));
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(optionType);
        response.addTypeArgument(listInstance);
        method.setReturnType(response);


        addControllerMapping(method, "option/"+JavaBeansUtil.getFirstCharacterLowercase(column.getJavaProperty()), "get");

        String listEntityVar = entityFirstLowerShortName+"s";
        method.addBodyLine("{0} example = new {0}();"
                ,exampleType.getShortName());
        method.addBodyLine("List<{0}> {1} = {2}.selectByExample(example);",
                entityType.getShortName(),listEntityVar,serviceBeanName);
        method.addBodyLine("List<FormSelectOption> options = {0}.stream()", listEntityVar);
        if (introspectedTable.getColumn("sort_").isPresent()) {
            method.addBodyLine("        .sorted(Comparator.comparing(t->t.getSort()))");
        }
        String getterMethodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(), FullyQualifiedJavaType.getStringInstance());
        method.addBodyLine("        .map(t -> new FormSelectOption(t.getId(), t.{0}(), selected))",getterMethodName);
        method.addBodyLine("        .distinct().collect(Collectors.toList());");
        method.addBodyLine("return success(options);");

        parentElement.addMethod(method);
    }
}
