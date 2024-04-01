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

import java.util.*;
import java.util.stream.Collectors;

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
        voGenService.addConfigurationSuperInterface(excelVoClass, voExcelGeneratorConfiguration);
        excelVoClass.addImportedType(superInterface);
        excelVoClass.addImportedType("lombok.*");
        excelVoClass.addAnnotation("@Data");
        excelVoClass.addAnnotation("@NoArgsConstructor");
        excelVoClass.addSerialVersionUID();

        //获得所有可能的属性
        List<Field> allFields = new ArrayList<>();
        for (IntrospectedColumn nonBLOBColumn : introspectedTable.getNonBLOBColumns()) {
            Field field = new Field(nonBLOBColumn.getJavaProperty(), nonBLOBColumn.getFullyQualifiedJavaType());
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setRemark(nonBLOBColumn.getRemarks(true));
            allFields.add(field);
        }
        //增加映射
        Set<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations = voExcelGeneratorConfiguration.getOverridePropertyConfigurations();
        List<Field> fields = voGenService.buildOverrideColumn(overridePropertyConfigurations, excelVoClass, ModelClassTypeEnum.excelVoClass);
        allFields.addAll(fields);
        //附加属性
        Set<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voExcelGeneratorConfiguration.getAdditionalPropertyConfigurations();
        additionalPropertyConfigurations.addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        allFields.addAll(excelVoClass.getAddtionalPropertiesFields(additionalPropertyConfigurations));
        //添加属性
        List<Field> exportFields;
        if (!voExcelGeneratorConfiguration.getExportIncludeFields().isEmpty()) {
            exportFields = allFields.stream().filter(f->voExcelGeneratorConfiguration.getExportIncludeFields().contains(f.getName())).collect(Collectors.toList());
        }else{
           Set<String> excludeFields = voExcelGeneratorConfiguration.getExportExcludeFields();
            voGeneratorConfiguration.getExcludeColumns().stream().map(c->introspectedTable.getColumn(c).orElse(null)).filter(Objects::nonNull).forEach(c->excludeFields.add(c.getJavaProperty()));
            exportFields = allFields.stream().filter(f->!excludeFields.contains(f.getName())).collect(Collectors.toList());
        }
        if (!exportFields.isEmpty()) {
            excelVoClass.addAnnotation("@AllArgsConstructor");
        }
        for (Field exportField : exportFields) {
            if (plugins.voExcelFieldGenerated(exportField, excelVoClass, null, introspectedTable)) {
                excelVoClass.addField(exportField);
                excelVoClass.addImportedType(exportField.getType());
            }
        }

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
