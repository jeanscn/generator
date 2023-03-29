package org.mybatis.generator.codegen.mybatis3.vo;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.VOCreateGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.custom.annotations.ApiModelProperty;
import org.mybatis.generator.custom.pojo.RelationGeneratorConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;

/**
 * CreateVO生成器
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 15:40
 * @version 3.0
 */
public class VOCreateGenerator extends AbstractVOGenerator{

    public VOCreateGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(introspectedTable, project,progressCallback, warnings,mappingsInterface);
    }

    @Override
    TopLevelClass generate() {
        VOCreateGeneratorConfiguration voCreateGeneratorConfiguration = voGeneratorConfiguration.getVoCreateConfiguration();
        String createVoType = voCreateGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass createVoClass = createTopLevelClass(createVoType, getAbstractVOType().getFullyQualifiedName());
        createVoClass.addMultipleImports("lombok");
        getApiModel(voCreateGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()).addAnnotationToTopLevelClass(createVoClass);
        createVoClass.addImportedType(getAbstractVOType().getFullyQualifiedName());
        createVoClass.addSerialVersionUID();
        //添加id属性
        List<String> fields = new ArrayList<>(Collections.singletonList("id"));
        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(fields, voCreateGeneratorConfiguration.getIncludeColumns(), voCreateGeneratorConfiguration.getExcludeColumns());

        for (IntrospectedColumn voColumn : introspectedColumns) {
            if (!(isAbstractVOColumn(voColumn) || voCreateGeneratorConfiguration.getExcludeColumns().contains(voColumn.getActualColumnName()))) {
                Field field = getJavaBeansField(voColumn, context, introspectedTable);
                field.setVisibility(JavaVisibility.PRIVATE);
                if (plugins.voCreateFieldGenerated(field, createVoClass, voColumn, introspectedTable)) {
                    createVoClass.addField(field);
                    createVoClass.addImportedType(field.getType());
                }
            }
        }
        //增加是否选择性更新
        if (introspectedTable.getRules().createEnableSelective()) {
            Field selectiveUpdate = new Field("selectiveUpdate", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
            selectiveUpdate.setVisibility(JavaVisibility.PRIVATE);
            selectiveUpdate.setRemark("插入时选择性更新");
            ApiModelProperty apiModelProperty = new ApiModelProperty(selectiveUpdate.getRemark());
            apiModelProperty.setExample("true");
            selectiveUpdate.addAnnotation(apiModelProperty.toAnnotation());
            createVoClass.addMultipleImports(apiModelProperty.multipleImports());
            createVoClass.addField(selectiveUpdate);
        }

        for (IntrospectedColumn introspectedColumn : voGenService.getAbstractVOColumns()) {
            if (!(introspectedColumn.isNullable() || introspectedTable.getTableConfiguration().getValidateIgnoreColumns().contains(introspectedColumn.getActualColumnName()))) {
                this.getOverrideGetter(introspectedColumn).ifPresent(javaBeansGetter -> {
                    if (plugins.voCreateGetterMethodGenerated(javaBeansGetter, createVoClass, introspectedColumn, introspectedTable)) {
                        createVoClass.addMethod(javaBeansGetter);
                    }
                });
            }
        }

        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voCreateGeneratorConfiguration.getAdditionalPropertyConfigurations();
        additionalPropertyConfigurations.addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        createVoClass.getAddtionalPropertiesFields(additionalPropertyConfigurations).forEach(field -> {
                    if (plugins.voCreateFieldGenerated(field, createVoClass, null, introspectedTable)) {
                        createVoClass.addField(field);
                        createVoClass.addImportedType(field.getType());
                    }
                }
        );

        //是否有启用insert的JavaCollectionRelation
        introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableInsert).collect(Collectors.toList())
                .forEach(c -> {
                    if (!createVoClass.isContainField(c.getPropertyName())) {
                        FullyQualifiedJavaType type;
                        if (c.getType().equals(RelationTypeEnum.collection)) {
                            type = FullyQualifiedJavaType.getNewListInstance();
                            type.addTypeArgument(new FullyQualifiedJavaType(c.getVoModelTye()));
                            createVoClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                        } else {
                            type = new FullyQualifiedJavaType(c.getVoModelTye());
                        }
                        Field field = new Field(c.getPropertyName(), type);
                        field.setVisibility(JavaVisibility.PRIVATE);
                        field.setRemark(c.getRemark());
                        createVoClass.addField(field);
                        createVoClass.addImportedType(c.getVoModelTye());
                    }
                });

        /*
          生成映射方法
         */
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(createVoType));
        mappingsInterface.addMethod(addMappingMethod(createVoClass.getType(), entityType, false));
        mappingsInterface.addMethod(addMappingMethod(createVoClass.getType(), entityType, true));

        return createVoClass;
    }
}
