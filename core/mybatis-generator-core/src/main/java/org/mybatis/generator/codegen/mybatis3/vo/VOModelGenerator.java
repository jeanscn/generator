package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.core.db.util.JDBCUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VOModelGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.ModelClassTypeEnum;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.custom.annotations.ApiModelProperty;
import org.mybatis.generator.custom.pojo.RelationGeneratorConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;

/**
 * 生成VOModel
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 11:23
 * @version 3.0
 */
public class VOModelGenerator extends AbstractVOGenerator {

    public VOModelGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings, mappingsInterface);
    }

    public TopLevelClass generate() {
        VOModelGeneratorConfiguration voModelGeneratorConfiguration = voGeneratorConfiguration.getVoModelConfiguration();
        String voType = voModelGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass voClass = createTopLevelClass(voType, getAbstractVOType().getFullyQualifiedName());
        voClass.addMultipleImports("lombok");
        voClass.addAnnotation("@NoArgsConstructor");
        getApiModel(voModelGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()).addAnnotationToTopLevelClass(voClass);
        voClass.addSerialVersionUID();
        //添加id、version属性
        List<String> fields = new ArrayList<>(Arrays.asList("id", "version"));
        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(fields, voModelGeneratorConfiguration.getIncludeColumns(), voModelGeneratorConfiguration.getExcludeColumns());
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            if (!isAbstractVOColumn(introspectedColumn)) {
                Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
                if (plugins.voModelFieldGenerated(field, voClass, introspectedColumn, introspectedTable)) {
                    voClass.addField(field);
                    voClass.addImportedType(field.getType());
                }
            }
        }
        for (IntrospectedColumn introspectedColumn : voGenService.getAbstractVOColumns()) {
            if (!(introspectedColumn.isNullable() || introspectedTable.getTableConfiguration().getValidateIgnoreColumns().contains(introspectedColumn.getActualColumnName()))) {
                this.getOverrideGetter(introspectedColumn).ifPresent(m -> {
                    if (plugins.voModelGetterMethodGenerated(m, voClass, introspectedColumn, introspectedTable)) {
                        voClass.addMethod(m);
                    }
                });
            }
        }

        //增加映射
        List<OverridePropertyValueGeneratorConfiguration> overridePropertyVo = voModelGeneratorConfiguration.getOverridePropertyConfigurations();
        overridePropertyVo.addAll(voGeneratorConfiguration.getOverridePropertyConfigurations());
        voGenService.buildOverrideColumn(overridePropertyVo, voClass, ModelClassTypeEnum.voClass)
                .forEach(field -> plugins.voModelFieldGenerated(field, voClass, null, introspectedTable));

        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyVo = voModelGeneratorConfiguration.getAdditionalPropertyConfigurations();
        additionalPropertyVo.addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        voClass.getAddtionalPropertiesFields(additionalPropertyVo).forEach(field -> {
            if (plugins.voModelFieldGenerated(field, voClass, null, introspectedTable)) {
                voClass.addField(field);
                voClass.addImportedType(field.getType());
            }
        });

        voClass.addImportedType(getAbstractVOType().getFullyQualifiedName());
        //persistenceBeanName属性
        Field persistenceBeanName = new Field("persistenceBeanName", FullyQualifiedJavaType.getStringInstance());
        persistenceBeanName.setVisibility(JavaVisibility.PRIVATE);
        persistenceBeanName.setRemark("对象服务java bean名称");
        ApiModelProperty apiModelProperty = new ApiModelProperty(persistenceBeanName.getRemark());
        persistenceBeanName.addAnnotation(apiModelProperty.toAnnotation());
        voClass.addMultipleImports(apiModelProperty.multipleImports());
        voClass.addField(persistenceBeanName);

        //检查是否有定制的新属性
        if (introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().size() > 0) {
            /*
             * 根据联合查询属性配置
             * 增加相应的属性
             */
            if (introspectedTable.getRelationGeneratorConfigurations().size() > 0) {
                for (RelationGeneratorConfiguration relationProperty : introspectedTable.getRelationGeneratorConfigurations()) {
                    FullyQualifiedJavaType returnType;
                    Field field;
                    FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(relationProperty.getVoModelTye());
                    if (relationProperty.getType().equals(RelationTypeEnum.collection)) {
                        voClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                        returnType = FullyQualifiedJavaType.getNewListInstance();
                        returnType.addTypeArgument(fullyQualifiedJavaType);
                    } else {
                        returnType = fullyQualifiedJavaType;
                    }
                    field = new Field(relationProperty.getPropertyName(), returnType);
                    field.setVisibility(JavaVisibility.PRIVATE);
                    field.setRemark(relationProperty.getRemark());
                    new ApiModelProperty(field.getRemark(), JDBCUtil.getExampleByClassName(field.getType().getFullyQualifiedName()))
                            .addAnnotationToField(field, voClass);
                    voClass.addField(field, null, true);
                    voClass.addImportedType(fullyQualifiedJavaType);
                }
            }
        }

        //增加转换方法
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(voClass.getType().getFullyQualifiedName()));
        mappingsInterface.addMethod(addMappingMethod(voClass.getType(), entityType, false));
        mappingsInterface.addMethod(addMappingMethod(entityType, voClass.getType(), false));
        mappingsInterface.addMethod(addMappingMethod(voClass.getType(), entityType, true));
        mappingsInterface.addMethod(addMappingMethod(entityType, voClass.getType(), true));

        return voClass;
    }
}
