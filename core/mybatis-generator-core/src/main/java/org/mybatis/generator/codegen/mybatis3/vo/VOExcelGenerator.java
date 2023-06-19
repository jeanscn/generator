package org.mybatis.generator.codegen.mybatis3.vo;

import cn.hutool.core.collection.CollectionUtil;
import com.vgosoft.core.annotation.*;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VOExcelGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.ModelClassTypeEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 16:45
 * @version 3.0
 */
public class VOExcelGenerator extends AbstractVOGenerator {

    public VOExcelGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings, mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VOExcelGeneratorConfiguration voExcelGeneratorConfiguration = voGeneratorConfiguration.getVoExcelConfiguration();
        String excelVoType = voExcelGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass excelVoClass = createTopLevelClass(excelVoType, null);
        FullyQualifiedJavaType superInterface = new FullyQualifiedJavaType(ConstantsUtil.I_BASE_DTO);
        excelVoClass.addSuperInterface(superInterface);
        excelVoClass.addImportedType(superInterface);
        excelVoClass.addImportedType("lombok.*");
        excelVoClass.addAnnotation("@Data");
        excelVoClass.addAnnotation("@NoArgsConstructor");
        excelVoClass.addSerialVersionUID();
        //添加属性
        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(null, voExcelGeneratorConfiguration.getIncludeColumns(), voExcelGeneratorConfiguration.getExcludeColumns());
        CollectionUtil.addAllIfNotContains(introspectedColumns, voGenService.getAbstractVOColumns());
        if (introspectedColumns.size() > 0) {
            excelVoClass.addAnnotation("@AllArgsConstructor");
        }
        for (IntrospectedColumn voColumn : introspectedColumns) {
            Field field = new Field(voColumn.getJavaProperty(), voColumn.getFullyQualifiedJavaType());
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setRemark(voColumn.getRemarks(true));
            if (plugins.voExcelFieldGenerated(field, excelVoClass, voColumn, introspectedTable)) {
                excelVoClass.addField(field);
                excelVoClass.addImportedType(voColumn.getFullyQualifiedJavaType());
            }
        }

        //增加映射
        List<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations = voExcelGeneratorConfiguration.getOverridePropertyConfigurations();
         List<Field> fields = voGenService.buildOverrideColumn(overridePropertyConfigurations, excelVoClass, ModelClassTypeEnum.excelVoClass);
        fields.forEach(field -> {
            if (plugins.voExcelFieldGenerated(field, excelVoClass, null, introspectedTable)) {
                if (!excelVoClass.isContainField(field.getName())) {
                    excelVoClass.addField(field);
                }
            }
        });
        //重写get方法，为转换字段赋值
        /*overridePropertyConfigurations.stream()
                .filter(Configuration -> Configuration.getAnnotationType().contains("Dict"))
                .forEach(overridePropertyConfiguration -> {
                    introspectedTable.getColumn(overridePropertyConfiguration.getSourceColumnName()).ifPresent(column -> {
                        String fieldName = overridePropertyConfiguration.getTargetPropertyName();
                        FullyQualifiedJavaType javaType = new FullyQualifiedJavaType(overridePropertyConfiguration.getTargetPropertyType());
                        String getterMethodName = JavaBeansUtil.getGetterMethodName(fieldName, javaType);
                        Method method = new Method(getterMethodName);
                        method.setVisibility(JavaVisibility.PUBLIC);
                        method.setReturnType(javaType);
                        StringBuilder sb = new StringBuilder("return ");
                        if (javaType.getShortName().equals("String") && !column.isStringColumn()) {
                            sb.append("String.valueOf(").append(column.getJavaProperty()).append(")");
                        } else {
                            sb.append(column.getJavaProperty());
                        }
                        method.addBodyLine(sb + ";");
                        excelVoClass.addMethod(method);
                    });
                });*/

        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voExcelGeneratorConfiguration.getAdditionalPropertyConfigurations();
        additionalPropertyConfigurations.addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        excelVoClass.getAddtionalPropertiesFields(additionalPropertyConfigurations).forEach(field -> {
                    if (plugins.voExcelFieldGenerated(field, excelVoClass, null, introspectedTable)) {
                        excelVoClass.addField(field);
                        excelVoClass.addImportedType(field.getType());
                    }
                }
        );

        /*
         * 添加映射方法
         */
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(excelVoType));
        mappingsInterface.addMethod(addMappingMethod(entityType, excelVoClass.getType(), false));
        mappingsInterface.addMethod(addMappingMethod(entityType, excelVoClass.getType(), true));
        mappingsInterface.addMethod(addMappingMethod(excelVoClass.getType(), entityType, false));
        mappingsInterface.addMethod(addMappingMethod(excelVoClass.getType(), entityType, true));

        return excelVoClass;
    }
}
