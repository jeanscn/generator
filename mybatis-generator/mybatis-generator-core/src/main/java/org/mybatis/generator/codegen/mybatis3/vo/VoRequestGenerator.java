package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VCollectionUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.AbstractModelGeneratorConfiguration;
import org.mybatis.generator.config.VoRequestGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.swagger3.SchemaPropertyDesc;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 16:50
 * @version 3.0
 */
public class VoRequestGenerator extends AbstractVoGenerator {

    public VoRequestGenerator(AbstractIntrospectedTable abstractIntrospectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(abstractIntrospectedTable, project, progressCallback, warnings, mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VoRequestGeneratorConfiguration voRequestGeneratorConfiguration = voGeneratorConfiguration.getVoRequestConfiguration();
        String requestVoType = voRequestGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass requestVoClass = createTopLevelClass(requestVoType, getAbstractVoType().getFullyQualifiedName());
        commentGenerator.addModelClassComment(requestVoClass, introspectedTable);
        requestVoClass.addMultipleImports("lombok");
        voGenService.addConfigurationSuperInterface(requestVoClass, voRequestGeneratorConfiguration);
        addSchemaDesc(voRequestGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()).addAnnotationToTopLevelClass(requestVoClass);
        requestVoClass.addSerialVersionUID(introspectedTable.getContext().getJdkVersion());

        Set<String> excludeColumns = voGenService.getDefaultExcludeColumnNames(voRequestGeneratorConfiguration.getExcludeColumns());
        for (IntrospectedColumn introspectedColumn : voGenService.getAbstractVoColumns()) {
            if (!introspectedColumn.isNullable() && !excludeColumns.contains(introspectedColumn.getActualColumnName())) {
                addRequestField(requestVoClass, introspectedColumn);
            }
        }

        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(null, null, voRequestGeneratorConfiguration.getExcludeColumns());
        introspectedColumns.stream()
                .filter(introspectedColumn -> !isAbstractVoColumn(introspectedColumn) && !excludeColumns.contains(introspectedColumn.getActualColumnName()))
                .forEach(introspectedColumn -> addRequestField(requestVoClass, introspectedColumn));

        //分页属性
        addPageProperty(voRequestGeneratorConfiguration, requestVoClass);
        //排序属性
        addOrderByClause(requestVoClass);
        //增加cascade开关
        addCascadeResult(requestVoClass);
        //增加countChildren开关
        addCountChildren(requestVoClass);

        //增加ignoreDeleteFlag开关
        Field ignoreDeleteFlag = getIgnoreDeleteFlag(requestVoClass, introspectedTable);
        if (ignoreDeleteFlag != null && plugins.voRequestFieldGenerated(ignoreDeleteFlag, requestVoClass, null, introspectedTable)) {
            requestVoClass.addField(ignoreDeleteFlag);
        }

        //增加是否权限过滤注解
        Field ignorePermissionAnnotation = addIgnorePermissionAnnotation(requestVoClass, introspectedTable);
        if (ignorePermissionAnnotation != null && plugins.voRequestFieldGenerated(ignorePermissionAnnotation, requestVoClass, null, introspectedTable)) {
            requestVoClass.addField(ignorePermissionAnnotation);
        }

        //增加ignoreIdList属性
        Field ignoreIdList = getIgnoreIdList(requestVoClass, introspectedTable);
        if (ignoreIdList != null && plugins.voRequestFieldGenerated(ignoreIdList, requestVoClass, null, introspectedTable)) {
            requestVoClass.addField(ignoreIdList);
            requestVoClass.addImportedType(ignoreIdList.getType());
        }

        //增加isHideIds属性
        Field isHideIds = getIsHideIds(requestVoClass, introspectedTable);
        if (isHideIds != null && plugins.voRequestFieldGenerated(isHideIds, requestVoClass, null, introspectedTable)) {
            requestVoClass.addField(isHideIds);
        }
        //附加属性
        additionalProperties(voRequestGeneratorConfiguration, requestVoClass);

        //增加任意过滤条件接收
        Field whereConditionResult = getWhereConditionResult(requestVoClass);
        if (whereConditionResult != null && plugins.voRequestFieldGenerated(whereConditionResult, requestVoClass, null, introspectedTable)) {
            requestVoClass.addField(whereConditionResult);
            requestVoClass.addImportedType(whereConditionResult.getType());
        }

        //增加前端过滤器属性
        Field filterMap = getFilterMap(requestVoClass);
        if (filterMap != null && plugins.voRequestFieldGenerated(filterMap, requestVoClass, null, introspectedTable)) {
            requestVoClass.addField(filterMap);
            requestVoClass.addImportedType(filterMap.getType());
        }
        //增加actionType属性
        Field actionType = getActionType(requestVoClass, introspectedTable);
        if (actionType != null && plugins.voRequestFieldGenerated(actionType, requestVoClass, null, introspectedTable)) {
            requestVoClass.addField(actionType);
            requestVoClass.addImportedType(actionType.getType());
        }
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(requestVoType));
        mappingsInterface.addMethod(addMappingMethod(requestVoClass.getType(), entityType, false));
        return requestVoClass;
    }

    private void addCountChildren(TopLevelClass requestVoClass) {
        if (introspectedTable.getRules().isModelEnableChildren()) {
            Field countChildren = new Field("countChildren", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
            countChildren.setVisibility(JavaVisibility.PRIVATE);
            countChildren.setRemark("是否统计子级数量");
            new SchemaPropertyDesc(countChildren.getRemark(), "false").addAnnotationToField(countChildren, requestVoClass);
            if (plugins.voRequestFieldGenerated(countChildren, requestVoClass, null, introspectedTable)) {
                requestVoClass.addField(countChildren);
            }
        }
    }

    private void addRequestField(TopLevelClass requestVoClass, IntrospectedColumn introspectedColumn) {
        Field field = new Field(introspectedColumn.getJavaProperty(), introspectedColumn.getFullyQualifiedJavaType());
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setRemark(introspectedColumn.getRemarks(true));
        if (plugins.voRequestFieldGenerated(field, requestVoClass, introspectedColumn, introspectedTable)) {
            requestVoClass.addField(field);
            requestVoClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
        }
    }

    private void addCascadeResult(TopLevelClass requestVoClass) {
        if (introspectedTable.getRules().generateRelationWithSubSelected()) {
            Field cascade = new Field("cascadeResult", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
            cascade.setVisibility(JavaVisibility.PRIVATE);
            cascade.setRemark("结果是否包含子级");
            new SchemaPropertyDesc(cascade.getRemark(), "false").addAnnotationToField(cascade, requestVoClass);
            if (plugins.voRequestFieldGenerated(cascade, requestVoClass, null, introspectedTable)) {
                requestVoClass.addField(cascade);
            }
        }
    }

    private void addOrderByClause(TopLevelClass requestVoClass) {
        Field orderByClause = new Field("orderByClause", FullyQualifiedJavaType.getStringInstance());
        orderByClause.setVisibility(JavaVisibility.PRIVATE);
        orderByClause.setRemark("排序语句");
        new SchemaPropertyDesc(orderByClause.getRemark(), "SORT_").addAnnotationToField(orderByClause, requestVoClass);
        if (plugins.voRequestFieldGenerated(orderByClause, requestVoClass, null, introspectedTable)) {
            requestVoClass.addField(orderByClause);
        }
    }

    private void addPageProperty(VoRequestGeneratorConfiguration voRequestGeneratorConfiguration, TopLevelClass topLevelClass) {
        if (voRequestGeneratorConfiguration.isIncludePageParam()) {
            FullyQualifiedJavaType pageType = new FullyQualifiedJavaType("com.vgosoft.core.pojo.IPage");
            topLevelClass.addSuperInterface(pageType);
            topLevelClass.addImportedType(pageType);
            Field pNo = new Field("pageNo", FullyQualifiedJavaType.getIntInstance());
            pNo.setVisibility(JavaVisibility.PRIVATE);
            pNo.setRemark("页码");
            pNo.setInitializationString("DEFAULT_FIRST_PAGE_NO");
            new SchemaPropertyDesc(pNo.getRemark(), "1").addAnnotationToField(pNo, topLevelClass);
            if (plugins.voRequestFieldGenerated(pNo, topLevelClass, null, introspectedTable)) {
                topLevelClass.addField(pNo);
            }

            Field pSize = new Field("pageSize", FullyQualifiedJavaType.getIntInstance());
            pSize.setRemark("每页数据数量");
            new SchemaPropertyDesc(pSize.getRemark(), "10").addAnnotationToField(pSize, topLevelClass);
            pSize.setInitializationString("DEFAULT_PAGE_SIZE");
            pSize.setVisibility(JavaVisibility.PRIVATE);
            if (plugins.voRequestFieldGenerated(pSize, topLevelClass, null, introspectedTable)) {
                topLevelClass.addField(pSize);
            }
        }
    }

    /**
     * 附件属性
     * @param configuration 生成配置
     * @param topLevelClass 生成类
     */
    protected void additionalProperties(AbstractModelGeneratorConfiguration configuration, TopLevelClass topLevelClass) {
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = configuration.getAdditionalPropertyConfigurations();
        VCollectionUtil.addAllIfNotContains(additionalPropertyConfigurations, voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        topLevelClass.getAdditionalPropertiesFields(additionalPropertyConfigurations).forEach(field -> {
                    if (plugins.voRequestFieldGenerated(field, topLevelClass, null, introspectedTable)) {
                        topLevelClass.addField(field);
                        topLevelClass.addImportedType(field.getType());
                    }
                }
        );
    }
}
