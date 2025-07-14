package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VCollectionUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VoViewGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.enums.ModelClassTypeEnum;
import org.mybatis.generator.custom.annotations.ApiModelDesc;

import java.util.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 16:03
 * @version 3.0
 */
public class VoViewGenerator extends AbstractVoGenerator {

    public VoViewGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings, mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VoViewGeneratorConfiguration voViewGeneratorConfiguration = voGeneratorConfiguration.getVoViewConfiguration();
        String viewVoType = voViewGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass viewVoClass = createTopLevelClass(viewVoType, getAbstractVoType().getFullyQualifiedName());
        viewVoClass.addMultipleImports("lombok");
        voGenService.addConfigurationSuperInterface(viewVoClass, voViewGeneratorConfiguration);
        ApiModelDesc apiModelDesc = addApiModel(voViewGeneratorConfiguration.getFullyQualifiedJavaType().getShortName());
        apiModelDesc.addAnnotationToTopLevelClass(viewVoClass);
        viewVoClass.addImportedType(getAbstractVoType().getFullyQualifiedName());
        viewVoClass.addSerialVersionUID(introspectedTable.getContext().getJdkVersion());
        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(null, voViewGeneratorConfiguration.getIncludeColumns(), voViewGeneratorConfiguration.getExcludeColumns());
        for (IntrospectedColumn voColumn : introspectedColumns) {
            if (!(isAbstractVoColumn(voColumn) || voViewGeneratorConfiguration.getExcludeColumns().contains(voColumn.getActualColumnName()))) {
                Field field = new Field(voColumn.getJavaProperty(), voColumn.getFullyQualifiedJavaType());
                field.setVisibility(JavaVisibility.PRIVATE);
                field.setRemark(voColumn.getRemarks(true));
                if (plugins.voViewFieldGenerated(field, viewVoClass, voColumn, introspectedTable)) {
                    viewVoClass.addField(field);
                    viewVoClass.addImportedType(voColumn.getFullyQualifiedJavaType());
                }
            }
        }
        //增加映射
        List<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations = voViewGeneratorConfiguration.getOverridePropertyConfigurations();
        voGenService.buildOverrideColumn(overridePropertyConfigurations, viewVoClass, ModelClassTypeEnum.viewVoClass)
                .forEach(field -> {
                    if (plugins.voViewFieldGenerated(field, viewVoClass, null, introspectedTable)) {
                        viewVoClass.addField(field);
                        viewVoClass.addImportedType(field.getType());
                    }
                });
        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voViewGeneratorConfiguration.getAdditionalPropertyConfigurations();
        VCollectionUtil.addAllIfNotContains(additionalPropertyConfigurations, voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        viewVoClass.getAdditionalPropertiesFields(additionalPropertyConfigurations)
                .forEach(field -> {
                    if (plugins.voViewFieldGenerated(field, viewVoClass, null, introspectedTable)) {
                        viewVoClass.addField(field);
                        viewVoClass.addImportedType(field.getType());
                    }
                });

        mappingsInterface.addImportedType(new FullyQualifiedJavaType(viewVoType));
        mappingsInterface.addMethod(addMappingMethod(entityType, viewVoClass.getType(), false));
        mappingsInterface.addMethod(addMappingMethod(entityType, viewVoClass.getType(), true));

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

        return viewVoClass;
    }
}
