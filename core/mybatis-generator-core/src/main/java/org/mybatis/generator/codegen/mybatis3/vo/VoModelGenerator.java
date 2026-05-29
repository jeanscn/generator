package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VCollectionUtil;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VoModelGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.FieldItem;
import org.mybatis.generator.custom.annotations.swagger3.SchemaPropertyDesc;
import org.mybatis.generator.custom.enums.ModelClassTypeEnum;
import org.mybatis.generator.custom.enums.RelationTypeEnum;

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

    public VoModelGenerator(AbstractIntrospectedTable abstractIntrospectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(abstractIntrospectedTable, project, progressCallback, warnings, mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VoModelGeneratorConfiguration voModelGeneratorConfiguration = voGeneratorConfiguration.getVoModelConfiguration();
        String voType = voModelGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass voClass = createTopLevelClass(voType, getAbstractVoType().getFullyQualifiedName());
        commentGenerator.addModelClassComment(voClass, introspectedTable);
        voClass.addMultipleImports("lombok");
        voClass.addAnnotation("@NoArgsConstructor");
        addSchemaDesc(voModelGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()).addAnnotationToTopLevelClass(voClass);
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
                introspectedTable.getVoModelFields().add(new FieldItem(field));
            }
        });

        if (!introspectedTable.getRules().isGenerateRequestVo()) {
            //增加任意过滤条件接收
            Field whereConditionResult = getWhereConditionResult(voClass);
            if (plugins.voModelFieldGenerated(whereConditionResult, voClass, null, introspectedTable)) {
                voClass.addField(whereConditionResult);
                voClass.addImportedType(whereConditionResult.getType());
                introspectedTable.getVoModelFields().add(new FieldItem(whereConditionResult));
            }
            //增加前端过滤器属性
            Field filterMap = getFilterMap(voClass);
            if (filterMap != null && plugins.voModelFieldGenerated(filterMap, voClass, null, introspectedTable)) {
                voClass.addField(filterMap);
                voClass.addImportedType(filterMap.getType());
                introspectedTable.getVoModelFields().add(new FieldItem(filterMap));
            }
        }
        voClass.addImportedType(getAbstractVoType().getFullyQualifiedName());

        //persistenceBeanName属性
        Field persistenceBeanName = getPersistenceBeanNameProperty(voClass, introspectedTable);
        if (plugins.voModelFieldGenerated(persistenceBeanName, voClass, null, introspectedTable)) {
            voClass.addField(persistenceBeanName);
            introspectedTable.getVoModelFields().add(new FieldItem(persistenceBeanName));
        }

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


        if (!introspectedTable.getRules().isGenerateRequestVo()) {
            //增加actionType属性
            Field actionType = getActionType(voClass, introspectedTable);
            if (actionType != null && plugins.voModelFieldGenerated(actionType, voClass, null, introspectedTable)) {
                voClass.addField(actionType);
                voClass.addImportedType(actionType.getType());
                introspectedTable.getVoModelFields().add(new FieldItem(actionType));
            }
            //增加ignoreDeleteFlag属性
            Field ignoreDeleteFlag = getIgnoreDeleteFlag(voClass, introspectedTable);
            if (ignoreDeleteFlag != null && plugins.voModelFieldGenerated(ignoreDeleteFlag, voClass, null, introspectedTable)) {
                voClass.addField(ignoreDeleteFlag);
                voClass.addImportedType(ignoreDeleteFlag.getType());
                introspectedTable.getVoModelFields().add(new FieldItem(ignoreDeleteFlag));
            }

            //增加ignorePermissionAnnotation属性
            Field ignorePermissionAnnotation = addIgnorePermissionAnnotation(voClass, introspectedTable);
            if (ignorePermissionAnnotation != null) {
                voClass.addField(ignorePermissionAnnotation);
                voClass.addImportedType(ignorePermissionAnnotation.getType());
                introspectedTable.getVoModelFields().add(new FieldItem(ignorePermissionAnnotation));
            }
            //增加ignoreIdList属性
            Field ignoreIdList = getIgnoreIdList(voClass, introspectedTable);
            if (ignoreIdList != null && plugins.voModelFieldGenerated(ignoreIdList, voClass, null, introspectedTable)) {
                voClass.addField(ignoreIdList);
                voClass.addImportedType(ignoreIdList.getType());
                introspectedTable.getVoModelFields().add(new FieldItem(ignoreIdList));
            }

            //增加isHideIds属性
            Field isHideIds = getIsHideIds(voClass, introspectedTable);
            if (isHideIds != null && plugins.voModelFieldGenerated(isHideIds, voClass, null, introspectedTable)) {
                voClass.addField(isHideIds);
                voClass.addImportedType(isHideIds.getType());
                introspectedTable.getVoModelFields().add(new FieldItem(isHideIds));
            }
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

    private Field getPersistenceBeanNameProperty(TopLevelClass voClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        String initString = abstractIntrospectedTable.getControllerBeanName();
        Field persistenceBeanName = new Field("persistenceBeanName", FullyQualifiedJavaType.getStringInstance());
        persistenceBeanName.setInitializationString("\""+initString+"\"");
        persistenceBeanName.setVisibility(JavaVisibility.PRIVATE);
        persistenceBeanName.setRemark("对象服务java bean名称");
        return persistenceBeanName;
    }
}
