package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VCollectionUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VoModelGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.FieldItem;
import org.mybatis.generator.custom.enums.ModelClassTypeEnum;
import org.mybatis.generator.custom.enums.RelationTypeEnum;
import org.mybatis.generator.custom.annotations.ApiModelPropertyDesc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;

/**
 * 生成VoModel
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 11:23
 * @version 3.0
 */
public class VoModelGenerator extends AbstractVoGenerator {

    public VoModelGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings, mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VoModelGeneratorConfiguration voModelGeneratorConfiguration = voGeneratorConfiguration.getVoModelConfiguration();
        String voType = voModelGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass voClass = createTopLevelClass(voType, getAbstractVoType().getFullyQualifiedName());
        voClass.addMultipleImports("lombok");
        voClass.addAnnotation("@NoArgsConstructor");
        addApiModel(voModelGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()).addAnnotationToTopLevelClass(voClass);
        voClass.addSerialVersionUID(introspectedTable.getContext().getJdkVersion());
        //添加父类
        voGenService.addConfigurationSuperInterface(voClass, voModelGeneratorConfiguration);
        //添加id、version属性
        List<String> fields = new ArrayList<>(Arrays.asList("id", "version"));
        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(fields, voModelGeneratorConfiguration.getIncludeColumns(), voModelGeneratorConfiguration.getExcludeColumns());
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            if (!isAbstractVoColumn(introspectedColumn)) {
                Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
                if (plugins.voModelFieldGenerated(field, voClass, introspectedColumn, introspectedTable)) {
                    voClass.addField(field);
                    voClass.addImportedType(field.getType());
                }
            }
        }
        for (IntrospectedColumn introspectedColumn : voGenService.getAbstractVoColumns()) {
            if (introspectedColumn.isBeValidated()) {
                this.getOverrideGetter(introspectedColumn).ifPresent(m -> {
                    if (plugins.voModelGetterMethodGenerated(m, voClass, introspectedColumn, introspectedTable)) {
                        voClass.addMethod(m);
                    }
                });
            }
        }

        //增加映射
        List<OverridePropertyValueGeneratorConfiguration> overridePropertyVo = voModelGeneratorConfiguration.getOverridePropertyConfigurations();
        voGenService.buildOverrideColumn(overridePropertyVo, voClass, ModelClassTypeEnum.voClass)
                .forEach(field -> {
                    if (plugins.voModelFieldGenerated(field, voClass, null, introspectedTable)) {
                        voClass.addField(field);
                        voClass.addImportedType(field.getType());
                    }
                });

        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyVo = voModelGeneratorConfiguration.getAdditionalPropertyConfigurations();
        VCollectionUtil.addAllIfNotContains(additionalPropertyVo, voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        voClass.getAdditionalPropertiesFields(additionalPropertyVo).forEach(field -> {
            if (plugins.voModelFieldGenerated(field, voClass, null, introspectedTable)) {
                voClass.addField(field);
                voClass.addImportedType(field.getType());
            }
        });

        if (!introspectedTable.getRules().isGenerateRequestVo()) {
            //增加任意过滤条件接收
            addWhereConditionResult(voClass);
            //增加前端过滤器属性
            addFilterMap(voClass);
        }

        voClass.addImportedType(getAbstractVoType().getFullyQualifiedName());
        //persistenceBeanName属性
        addPersistenceBeanNameProperty(voClass, introspectedTable);

        //检查是否有定制的新属性
        introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().forEach(relationProperty -> {
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
            relationProperty.getInitializationString().ifPresent(field::setInitializationString);
            relationProperty.getImportTypes().forEach(voClass::addImportedType);
            if (plugins.voModelFieldGenerated(field, voClass, null, introspectedTable)) {
                voClass.addField(field, null, true);
                voClass.addImportedType(fullyQualifiedJavaType);
            }
        });

        //增加actionType属性
        if (!introspectedTable.getRules().isGenerateRequestVo()) {
            addActionType(voClass,introspectedTable);
            addIgnoreDeleteFlag(voClass,introspectedTable);
            addIgnorePermissionAnnotation(voClass,introspectedTable);
            //增加ignoreIdList属性
            addIgnoreIdList(voClass, introspectedTable);
            addIsHideIds(voClass, introspectedTable);
        }

        Field modelTempId = new Field("modelTempId", FullyQualifiedJavaType.getStringInstance());
        modelTempId.setRemark("临时数据标识");
        commentGenerator.addFieldComment(modelTempId, "临时数据标识,用于创建新记录时就指定id的场景");
        addProperty(voClass, modelTempId, "''", introspectedTable);

        //增加转换方法
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(voClass.getType().getFullyQualifiedName()));
        mappingsInterface.addMethod(addMappingMethod(voClass.getType(), entityType, false));
        mappingsInterface.addMethod(addMappingMethod(entityType, voClass.getType(), false));
        mappingsInterface.addMethod(addMappingMethod(voClass.getType(), entityType, true));
        mappingsInterface.addMethod(addMappingMethod(entityType, voClass.getType(), true));

        return voClass;
    }

    private void addPersistenceBeanNameProperty(TopLevelClass voClass,IntrospectedTable introspectedTable) {
        String initString = introspectedTable.getControllerBeanName();
        Field persistenceBeanName = new Field("persistenceBeanName", FullyQualifiedJavaType.getStringInstance());
        persistenceBeanName.setInitializationString("\""+initString+"\"");
        persistenceBeanName.setVisibility(JavaVisibility.PRIVATE);
        persistenceBeanName.setRemark("对象服务java bean名称");
        ApiModelPropertyDesc apiModelPropertyDesc = new ApiModelPropertyDesc(persistenceBeanName.getRemark());
        persistenceBeanName.addAnnotation(apiModelPropertyDesc.toAnnotation());
        voClass.addImportedTypes(apiModelPropertyDesc.getImportedTypes());
        voClass.addField(persistenceBeanName);
        FieldItem fieldItem = new FieldItem(persistenceBeanName);
        introspectedTable.getVoModelFields().add(fieldItem);
    }
}
