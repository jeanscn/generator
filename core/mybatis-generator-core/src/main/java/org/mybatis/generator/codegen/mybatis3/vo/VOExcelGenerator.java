package org.mybatis.generator.codegen.mybatis3.vo;

import cn.hutool.core.collection.CollectionUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VOExcelGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.ModelClassTypeEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        List<IntrospectedColumn> introspectedColumns = voGenService.getVOColumns(new ArrayList<>(), voExcelGeneratorConfiguration.getExportIncludeColumns(), voExcelGeneratorConfiguration.getExcludeColumns());
        if (!introspectedColumns.isEmpty()) {
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
        Set<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations = voExcelGeneratorConfiguration.getOverridePropertyConfigurations();
         List<Field> fields = voGenService.buildOverrideColumn(overridePropertyConfigurations, excelVoClass, ModelClassTypeEnum.excelVoClass);
        fields.forEach(field -> {
            if (plugins.voExcelFieldGenerated(field, excelVoClass, null, introspectedTable)) {
                if (!excelVoClass.isContainField(field.getName())) {
                    excelVoClass.addField(field);
                }
            }
        });
        //附加属性
        Set<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voExcelGeneratorConfiguration.getAdditionalPropertyConfigurations();
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
