package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.EntityAbstractParentEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.pojo.FormOptionGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import static org.mybatis.generator.custom.ConstantsUtil.PAGE_RESULT;
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
        if (introspectedTable.getRules().isGenerateRequestVO()) {
            method.addParameter(new Parameter(entityRequestVoType, entityRequestVoType.getShortNameFirstLowCase()));
            parentElement.addImportedType(entityRequestVoType);
        } else{
            method.addParameter(buildMethodParameter(false, false,parentElement));
        }
        Parameter selected = new Parameter(FullyQualifiedJavaType.getStringInstance(), "selected");
        selected.addAnnotation("@RequestParam(required = false)");
        method.addParameter(selected);
        Parameter actionType = new Parameter(FullyQualifiedJavaType.getStringInstance(), "actionType");
        actionType.addAnnotation("@RequestParam(required = false)");
        method.addParameter(actionType);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        FullyQualifiedJavaType typeResult = FullyQualifiedJavaType.getNewListInstance();
        typeResult.addTypeArgument(optionType);
        response.addTypeArgument(typeResult);
        method.setReturnType(response);
        addSecurityPreAuthorize(method,methodPrefix);

        addControllerMapping(method, "option/"+JavaBeansUtil.getFirstCharacterLowercase(column.getJavaProperty()), "get");

        String listEntityVar = entityType.getShortNameFirstLowCase()+"s";
        selectByExampleWithPagehelper(parentElement, method);
        method.addBodyLine("List<FormSelectOption> options = {0}.stream()", listEntityVar);
        if (introspectedTable.getColumn("sort_").isPresent()) {
            method.addBodyLine("        .sorted(Comparator.comparing({0}::getSort))",entityType.getShortName());
        }
        String getterMethodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(), FullyQualifiedJavaType.getStringInstance());
        method.addBodyLine("        .map(t -> new FormSelectOption(t.getId(), t.{0}(), selected))",getterMethodName);
        method.addBodyLine("        .distinct().collect(Collectors.toList());");
        method.addBodyLine("return ResponsePagehelperResult.success(options,page);");
        parentElement.addMethod(method);
    }
}
