package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.DictTypeEnum;
import com.vgosoft.core.constant.enums.IBaseEnum;
import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.config.VoExcelGeneratorConfiguration;
import org.mybatis.generator.config.VoExcelImportWorkflowConfiguration;
import org.mybatis.generator.config.VoGeneratorConfiguration;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.VoGenService;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;
import org.mybatis.generator.custom.annotations.swagger3.OperationDesc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cen_c
 */
public class TemplateElementGenerator extends AbstractControllerElementGenerator {

    public TemplateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType("jakarta.servlet.http.HttpServletResponse");
        parentElement.addImportedType("com.vgosoft.plugins.excel.service.VgoFesodSheet");
        parentElement.addImportedType("java.io.IOException");
        parentElement.addImportedType(entityExcelImportVoType);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());

        final String methodPrefix = "template";
        Method method = createMethod(methodPrefix);

        FullyQualifiedJavaType response = new FullyQualifiedJavaType("jakarta.servlet.http.HttpServletResponse");
        Parameter parameter = new Parameter(response, "response");
        parameter.setRemark("http响应");
        method.addParameter(parameter);
        method.addException(new FullyQualifiedJavaType("java.io.IOException"));
        method.setExceptionRemark("IO读写异常");

        method.addAnnotation(new SystemLogDesc("下载数据导入模板", introspectedTable),parentElement);
        RequestMappingDesc requestMappingDesc = new RequestMappingDesc("import/template", RequestMethodEnum.GET);
        requestMappingDesc.addProduces("MediaType.APPLICATION_OCTET_STREAM_VALUE");
        method.addAnnotation(requestMappingDesc,parentElement);
        parentElement.addImportedType("org.springframework.http.MediaType");
        addSecurityPreAuthorize(method,methodPrefix,"导入模板");
        method.addAnnotation(new OperationDesc("Excel导入模板", "下载Excel导入模板接口"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "下载数据导入模板");

        method.addBodyLine("List<{0}> list = buildTemplateSampleData();",entityExcelImportVoType.getShortName());
        method.addBodyLine("VgoFesodSheet.writeTemplate(response, \"{1}导入模板\", \"{1}\", {0}.class, list, {2}, getDefaultColumnWidthStyleStrategy(\"template\"));",
                entityExcelImportVoType.getShortName(), introspectedTable.getRemarks(true), templateColumnsExpression());

        parentElement.addMethod(method);
    }

    private String templateColumnsExpression() {
        VoGeneratorConfiguration voConfiguration = tc.getVoGeneratorConfiguration();
        List<String> includeColumns;
        Set<String> excludeColumns;
        Set<String> ignoreFields;
        List<VoAdditionalPropertyGeneratorConfiguration> additionalProperties;
        if (introspectedTable.getRules().isGenerateExcelImportWorkflow()) {
            VoExcelImportWorkflowConfiguration configuration = voConfiguration.getVoExcelImportWorkflowConfiguration();
            includeColumns = configuration.getImportIncludeColumns();
            excludeColumns = new HashSet<>(configuration.getImportExcludeColumns());
            ignoreFields = configuration.getImportIgnoreFields();
            additionalProperties = configuration.getAdditionalPropertyConfigurations();
        } else {
            VoExcelGeneratorConfiguration configuration = voConfiguration.getVoExcelConfiguration();
            includeColumns = configuration.getImportIncludeColumns();
            excludeColumns = new HashSet<>(configuration.getImportExcludeColumns());
            ignoreFields = configuration.getImportIgnoreFields();
            additionalProperties = configuration.getAdditionalPropertyConfigurations();
        }

        List<IntrospectedColumn> columns = new VoGenService(introspectedTable)
                .getVoColumns(new ArrayList<>(), includeColumns, excludeColumns);
        List<String> metadata = new ArrayList<>();
        Set<String> physicalFields = new HashSet<>();
        for (IntrospectedColumn column : columns) {
            physicalFields.add(column.getJavaProperty());
            if (ignoreFields.contains(column.getJavaProperty())) {
                continue;
            }
            HtmlElementDescriptor descriptor = htmlGeneratorConfiguration == null
                    ? null
                    : htmlGeneratorConfiguration.getHtmlElementDescriptor(column.getActualColumnName());
            String instruction = descriptor != null && hasText(descriptor.getTips())
                    ? descriptor.getTips()
                    : hasText(column.getRemarks()) ? column.getRemarks() : null;
            List<String> options = resolveStaticOptions(descriptor);
            if (instruction == null && options.isEmpty()) {
                continue;
            }
            String optionExpression = options.stream()
                    .map(TemplateElementGenerator::javaStringLiteral)
                    .collect(Collectors.joining(", "));
            metadata.add("new VgoFesodSheet.TemplateColumn("
                    + javaStringLiteral(column.getJavaProperty()) + ", "
                    + (instruction == null ? "null" : javaStringLiteral(instruction))
                    + ", List.of(" + optionExpression + "))");
        }
        for (VoAdditionalPropertyGeneratorConfiguration additionalProperty : additionalProperties) {
            String fieldName = additionalProperty.getName();
            if (physicalFields.contains(fieldName) || ignoreFields.contains(fieldName)) {
                continue;
            }
            HtmlElementDescriptor descriptor = htmlGeneratorConfiguration == null
                    ? null
                    : htmlGeneratorConfiguration.getHtmlElementDescriptor(fieldName);
            String instruction = descriptor != null && hasText(descriptor.getTips())
                    ? descriptor.getTips() : null;
            List<String> options = resolveStaticOptions(descriptor);
            if (instruction == null && options.isEmpty()) {
                continue;
            }
            String optionExpression = options.stream()
                    .map(TemplateElementGenerator::javaStringLiteral)
                    .collect(Collectors.joining(", "));
            metadata.add("new VgoFesodSheet.TemplateColumn("
                    + javaStringLiteral(fieldName) + ", "
                    + (instruction == null ? "null" : javaStringLiteral(instruction))
                    + ", List.of(" + optionExpression + "))");
        }
        return metadata.isEmpty() ? "List.of()" : "List.of(" + String.join(", ", metadata) + ")";
    }

    private List<String> resolveStaticOptions(HtmlElementDescriptor descriptor) {
        if (descriptor == null
                || !DictTypeEnum.DICT_ENUM.getCode().equals(descriptor.getDataSource())
                || !hasText(descriptor.getEnumClassName())) {
            return List.of();
        }

        Class<?> enumType;
        try {
            enumType = ObjectFactory.internalClassForName(descriptor.getEnumClassName());
        } catch (ClassNotFoundException failure) {
            throw new IllegalArgumentException(
                    "Unable to resolve fixed Excel options enum: " + descriptor.getEnumClassName(), failure);
        }
        if (!enumType.isEnum()) {
            throw new IllegalArgumentException(
                    "Fixed Excel options class must be an enum: " + descriptor.getEnumClassName());
        }

        List<String> options = new ArrayList<>();
        Set<String> uniqueValues = new HashSet<>();
        for (Object constant : enumType.getEnumConstants()) {
            String value;
            if (constant instanceof IBaseEnum<?> baseEnum) {
                if (baseEnum.code() == null) {
                    throw new IllegalArgumentException(
                            "Fixed Excel options enum contains a null code: " + descriptor.getEnumClassName());
                }
                value = baseEnum.code().toString();
            } else if (constant instanceof Enum<?> enumConstant) {
                value = enumConstant.name();
            } else {
                throw new IllegalArgumentException(
                        "Fixed Excel options enum constant has an unsupported type: "
                                + descriptor.getEnumClassName());
            }
            if (!uniqueValues.add(value)) {
                throw new IllegalArgumentException(
                        "Fixed Excel options enum contains duplicate values: " + descriptor.getEnumClassName());
            }
            options.add(value);
        }
        return List.copyOf(options);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String javaStringLiteral(String value) {
        StringBuilder literal = new StringBuilder("\"");
        for (char character : value.toCharArray()) {
            switch (character) {
                case '\\' -> literal.append("\\\\");
                case '"' -> literal.append("\\\"");
                case '\b' -> literal.append("\\b");
                case '\t' -> literal.append("\\t");
                case '\n' -> literal.append("\\n");
                case '\f' -> literal.append("\\f");
                case '\r' -> literal.append("\\r");
                default -> {
                    if (character < 0x20 || character == 0x7f) {
                        literal.append(String.format("\\%03o", (int) character));
                    } else {
                        literal.append(character);
                    }
                }
            }
        }
        return literal.append('"').toString();
    }
}
