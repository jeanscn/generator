package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VCollectionUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.ConfigUtil;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VoExcelGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.enums.ModelClassTypeEnum;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 16:45
 * @version 3.0
 */
public class VoExcelGenerator extends AbstractVoGenerator {

    public VoExcelGenerator(AbstractIntrospectedTable abstractIntrospectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(abstractIntrospectedTable, project, progressCallback, warnings, mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VoExcelGeneratorConfiguration voExcelGeneratorConfiguration = voGeneratorConfiguration.getVoExcelConfiguration();
        String excelVoType = voExcelGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass excelVoClass = createTopLevelClass(excelVoType, null);
        commentGenerator.addModelClassComment(excelVoClass, introspectedTable);
        FullyQualifiedJavaType superInterface = new FullyQualifiedJavaType(ConstantsUtil.I_BASE_DTO);
        excelVoClass.addSuperInterface(superInterface);
        voGenService.addConfigurationSuperInterface(excelVoClass, voExcelGeneratorConfiguration);
        excelVoClass.addImportedType(superInterface);
        excelVoClass.addImportedType("lombok.*");
        excelVoClass.addAnnotation("@Data");
        excelVoClass.addAnnotation("@NoArgsConstructor");
        excelVoClass.addSerialVersionUID(introspectedTable.getContext().getJdkVersion());

        //获得所有可能的属性
        List<Field> allFields = new ArrayList<>();
        for (IntrospectedColumn noBlobColumn : introspectedTable.getNonBlobColumns()) {
            Field field = new Field(noBlobColumn.getJavaProperty(), noBlobColumn.getFullyQualifiedJavaType());
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setRemark(noBlobColumn.getRemarks(true));
            allFields.add(field);
        }
        //增加映射
        List<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations = voExcelGeneratorConfiguration.getOverridePropertyConfigurations();
        // 显式 include 过滤下，仅构建会被导出的转换属性，避免为被过滤字段在登记侧留下孤儿 import（如 @Dict/@DictEnum）。
        if (!voExcelGeneratorConfiguration.getExportIncludeFields().isEmpty()) {
            overridePropertyConfigurations = overridePropertyConfigurations.stream()
                    .filter(configuration -> voExcelGeneratorConfiguration.getExportIncludeFields()
                            .contains(overrideFieldName(configuration)))
                    .collect(Collectors.toList());
        }
        List<Field> fields = voGenService.buildOverrideColumn(overridePropertyConfigurations, excelVoClass, ModelClassTypeEnum.excelVoClass);
        VCollectionUtil.addAllIfNotContains(allFields, fields);
        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voExcelGeneratorConfiguration.getAdditionalPropertyConfigurations();
        VCollectionUtil.addAllIfNotContains(additionalPropertyConfigurations,voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        VCollectionUtil.addAllIfNotContains(allFields,excelVoClass.getAdditionalPropertiesFields(additionalPropertyConfigurations));
        //添加属性
        List<Field> exportFields;
        if (!voExcelGeneratorConfiguration.getExportIncludeFields().isEmpty()) {
            exportFields = orderExportFields(allFields, voExcelGeneratorConfiguration.getExportIncludeFields());
        }else{
           Set<String> excludeFields = voExcelGeneratorConfiguration.getExportExcludeFields();
            voGeneratorConfiguration.getExcludeColumns().stream().map(c-> introspectedTable.getColumn(c).orElse(null)).filter(Objects::nonNull).forEach(c->excludeFields.add(c.getJavaProperty()));
            exportFields = allFields.stream().filter(f->!excludeFields.contains(f.getName())).collect(Collectors.toList());
        }
        if (!exportFields.isEmpty()) {
            excelVoClass.addAnnotation("@AllArgsConstructor");
        }
        int index = 0;
        for (Field exportField : exportFields) {
            index++;
            if (plugins.voExcelFieldGenerated(exportField, excelVoClass, null, introspectedTable,index)) {
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

    private List<Field> orderExportFields(List<Field> allFields, List<String> exportIncludeFields) {
        Map<String, Field> fieldsByName = new LinkedHashMap<>();
        for (Field field : allFields) {
            if (fieldsByName.putIfAbsent(field.getName(), field) != null) {
                throw new IllegalStateException("excelVo 生成属性重复，无法按 includeFields 排序: "
                        + field.getName());
            }
        }

        Set<String> includedNames = new LinkedHashSet<>(exportIncludeFields);
        if (includedNames.size() != exportIncludeFields.size()) {
            throw new IllegalStateException("excelVo includeFields 存在重复字段: " + exportIncludeFields);
        }

        Set<String> unknownNames = new LinkedHashSet<>(includedNames);
        unknownNames.removeAll(fieldsByName.keySet());
        if (!unknownNames.isEmpty()) {
            throw new IllegalStateException("excelVo includeFields 包含不存在的导出属性: " + unknownNames);
        }

        return exportIncludeFields.stream()
                .map(fieldsByName::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 计算转换属性最终落地的字段名，与 {@link VoGenService#buildOverrideColumn} 保持一致：
     * 优先使用 targetPropertyName，缺失时按源列 Java 属性推导（Id 结尾转 Text，否则追加 Text）。
     */
    private String overrideFieldName(OverridePropertyValueGeneratorConfiguration configuration) {
        if (configuration.getTargetPropertyName() != null) {
            return configuration.getTargetPropertyName();
        }
        return introspectedTable.getColumn(configuration.getSourceColumnName())
                .map(column -> ConfigUtil.getOverrideJavaProperty(column.getJavaProperty(), null))
                .orElse(null);
    }
}
