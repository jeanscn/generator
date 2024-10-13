package org.mybatis.generator.codegen.mybatis3.vo;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.VOUpdateGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.ApiModelPropertyDesc;

import java.util.*;

/**
 * updateVO生成器
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 15:57
 * @version 3.0
 */
public class VOUpdateGenerator extends AbstractVOGenerator{

    public VOUpdateGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings,mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VOUpdateGeneratorConfiguration voUpdateGeneratorConfiguration = voGeneratorConfiguration.getVoUpdateConfiguration();
        String updateVoType = voUpdateGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass updateVoClass = createTopLevelClass(updateVoType, getAbstractVOType().getFullyQualifiedName());
        voGenService.addConfigurationSuperInterface(updateVoClass, voUpdateGeneratorConfiguration);
        updateVoClass.addMultipleImports("lombok");
        addApiModel(voUpdateGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()).addAnnotationToTopLevelClass(updateVoClass);
        updateVoClass.addImportedType(getAbstractVOType().getFullyQualifiedName());
        updateVoClass.addSerialVersionUID();

        //添加id属性
        List<String> fields = new ArrayList<>(Collections.singletonList("id"));
        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(fields, voUpdateGeneratorConfiguration.getIncludeColumns(), voUpdateGeneratorConfiguration.getExcludeColumns());

        for (IntrospectedColumn voColumn : introspectedColumns) {
            if (!(isAbstractVOColumn(voColumn) || voUpdateGeneratorConfiguration.getExcludeColumns().contains(voColumn.getActualColumnName()))) {
                Field field = new Field(voColumn.getJavaProperty(), voColumn.getFullyQualifiedJavaType());
                field.setVisibility(JavaVisibility.PRIVATE);
                field.setRemark(voColumn.getRemarks(true));
                if (plugins.voUpdateFieldGenerated(field, updateVoClass, voColumn, introspectedTable)) {
                    updateVoClass.addField(field);
                    updateVoClass.addImportedType(voColumn.getFullyQualifiedJavaType());
                }
            }
        }
        for (IntrospectedColumn introspectedColumn : voGenService.getAbstractVOColumns()) {
            if (!(introspectedColumn.isNullable() || introspectedTable.getTableConfiguration().getValidateIgnoreColumns().contains(introspectedColumn.getActualColumnName()))) {
                this.getOverrideGetter(introspectedColumn).ifPresent(javaBeansGetter -> {
                    if (plugins.voUpdateGetterMethodGenerated(javaBeansGetter, updateVoClass, introspectedColumn, introspectedTable)) {
                        updateVoClass.addMethod(javaBeansGetter);
                    }
                });
            }
        }

        //附加属性
        TreeSet<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voUpdateGeneratorConfiguration.getAdditionalPropertyConfigurations();
        additionalPropertyConfigurations.addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        updateVoClass.getAdditionalPropertiesFields(additionalPropertyConfigurations).forEach(field -> {
                    if (plugins.voUpdateFieldGenerated(field, updateVoClass, null, introspectedTable)) {
                        updateVoClass.addField(field);
                        updateVoClass.addImportedType(field.getType());
                    }
                }
        );

        //是否增加是否选择性更新的属性
        if (voUpdateGeneratorConfiguration.isEnableSelective()) {
            Field selectiveUpdate = new Field("selectiveUpdate", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
            selectiveUpdate.setVisibility(JavaVisibility.PRIVATE);
            selectiveUpdate.setRemark("更新时选择性插入");
            ApiModelPropertyDesc apiModelProperty = new ApiModelPropertyDesc(selectiveUpdate.getRemark());
            apiModelProperty.setExample("true");
            selectiveUpdate.addAnnotation(apiModelProperty.toAnnotation());
            updateVoClass.addImportedTypes(apiModelProperty.getImportedTypes());
            updateVoClass.addField(selectiveUpdate);
        }

        //是否有启用update的JavaCollectionRelation
        addJavaCollectionRelation(updateVoClass,"update");

        mappingsInterface.addImportedType(new FullyQualifiedJavaType(updateVoType));
        mappingsInterface.addMethod(addMappingMethod(updateVoClass.getType(), entityType, false));
        mappingsInterface.addMethod(addMappingMethod(updateVoClass.getType(), entityType, true));

        return updateVoClass;
    }
}
