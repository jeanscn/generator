package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VCollectionUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VOViewGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.ModelClassTypeEnum;
import org.mybatis.generator.custom.annotations.ApiModelDesc;

import java.util.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 16:03
 * @version 3.0
 */
public class VOViewGenerator extends AbstractVOGenerator {

    public VOViewGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings, mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VOViewGeneratorConfiguration voViewGeneratorConfiguration = voGeneratorConfiguration.getVoViewConfiguration();
        String viewVOType = voViewGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass viewVOClass = createTopLevelClass(viewVOType, getAbstractVOType().getFullyQualifiedName());
        viewVOClass.addMultipleImports("lombok");
        voGenService.addConfigurationSuperInterface(viewVOClass, voViewGeneratorConfiguration);
        ApiModelDesc apiModelDesc = addApiModel(voViewGeneratorConfiguration.getFullyQualifiedJavaType().getShortName());
        apiModelDesc.addAnnotationToTopLevelClass(viewVOClass);
        viewVOClass.addImportedType(getAbstractVOType().getFullyQualifiedName());
        viewVOClass.addSerialVersionUID(introspectedTable.getContext().getJdkVersion());
        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(null, voViewGeneratorConfiguration.getIncludeColumns(), voViewGeneratorConfiguration.getExcludeColumns());
        for (IntrospectedColumn voColumn : introspectedColumns) {
            if (!(isAbstractVOColumn(voColumn) || voViewGeneratorConfiguration.getExcludeColumns().contains(voColumn.getActualColumnName()))) {
                Field field = new Field(voColumn.getJavaProperty(), voColumn.getFullyQualifiedJavaType());
                field.setVisibility(JavaVisibility.PRIVATE);
                field.setRemark(voColumn.getRemarks(true));
                if (plugins.voViewFieldGenerated(field, viewVOClass, voColumn, introspectedTable)) {
                    viewVOClass.addField(field);
                    viewVOClass.addImportedType(voColumn.getFullyQualifiedJavaType());
                }
            }
        }
        //增加映射
        List<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations = voViewGeneratorConfiguration.getOverridePropertyConfigurations();
        voGenService.buildOverrideColumn(overridePropertyConfigurations, viewVOClass, ModelClassTypeEnum.viewVOClass)
                .forEach(field -> {
                    if (plugins.voViewFieldGenerated(field, viewVOClass, null, introspectedTable)) {
                        viewVOClass.addField(field);
                        viewVOClass.addImportedType(field.getType());
                    }
                });
        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voViewGeneratorConfiguration.getAdditionalPropertyConfigurations();
        VCollectionUtil.addAllIfNotContains(additionalPropertyConfigurations, voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        viewVOClass.getAdditionalPropertiesFields(additionalPropertyConfigurations)
                .forEach(field -> {
                    if (plugins.voViewFieldGenerated(field, viewVOClass, null, introspectedTable)) {
                        viewVOClass.addField(field);
                        viewVOClass.addImportedType(field.getType());
                    }
                });

        mappingsInterface.addImportedType(new FullyQualifiedJavaType(viewVOType));
        mappingsInterface.addMethod(addMappingMethod(entityType, viewVOClass.getType(), false));
        mappingsInterface.addMethod(addMappingMethod(entityType, viewVOClass.getType(), true));

        // 如果添加了移入回收站按钮，则添到回收站的映射
        if (introspectedTable.getRules().isGenerateRecycleBin()) {
            FullyQualifiedJavaType sysRecycleBin = new FullyQualifiedJavaType("com.vgosoft.system.entity.SysRecycleBin");
            Method method = addMappingMethod(entityType, sysRecycleBin, false);
            Set<String> ignoreFields = new HashSet<>(Arrays.asList("id", "deleteFlag", "version", "created", "modified", "createdId", "modifiedId"));
            for (String ignoreField : ignoreFields) {
                method.addAnnotation(String.format("@Mapping(target = \"%s\", ignore = true)", ignoreField));
                mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.Mapping"));
            }
            method.addAnnotation(String.format("@Mapping(target = \"%s\", source  = \"%s\")", "recordId", "id"));
            method.addAnnotation(String.format("@Mapping(target = \"%s\", source  = \"%s\")", "businessBeanName", "persistenceBeanName"));
            mappingsInterface.addMethod(method);
            mappingsInterface.addMethod(addMappingMethod(entityType, sysRecycleBin, true));
        }

        // 如果添加了隐藏按钮，则添到隐藏列表的映射
        if (introspectedTable.getRules().isGenerateHideListBin()) {
            FullyQualifiedJavaType sysPerFilterOutBin = new FullyQualifiedJavaType("com.vgosoft.system.entity.SysPerFilterOutBin");
            Method method = addMappingMethod(entityType, sysPerFilterOutBin, false);
            Set<String> ignoreFields = new HashSet<>(Arrays.asList("id", "deleteFlag", "version", "created", "modified", "createdId", "modifiedId"));
            for (String ignoreField : ignoreFields) {
                method.addAnnotation(String.format("@Mapping(target = \"%s\", ignore = true)", ignoreField));
                mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.Mapping"));
            }
            method.addAnnotation(String.format("@Mapping(target = \"%s\", source  = \"%s\")", "recordId", "id"));
            method.addAnnotation(String.format("@Mapping(target = \"%s\", source  = \"%s\")", "businessBeanName", "persistenceBeanName"));
            method.addAnnotation(String.format("@Mapping(target = \"%s\", constant  = \"%s\")", "moduleKey", entityType.getShortName().toLowerCase()));
            mappingsInterface.addMethod(method);
            mappingsInterface.addMethod(addMappingMethod(entityType, sysPerFilterOutBin, true));
        }

        return viewVOClass;
    }
}
