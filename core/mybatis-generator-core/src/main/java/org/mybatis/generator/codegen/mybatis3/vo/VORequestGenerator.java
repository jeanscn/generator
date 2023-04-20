package org.mybatis.generator.codegen.mybatis3.vo;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.VORequestGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 16:50
 * @version 3.0
 */
public class VORequestGenerator extends AbstractVOGenerator{

    public VORequestGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings,mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VORequestGeneratorConfiguration voRequestGeneratorConfiguration = voGeneratorConfiguration.getVoRequestConfiguration();
        String requestVoType = voRequestGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass requestVoClass = createTopLevelClass(requestVoType, getAbstractVOType().getFullyQualifiedName());
        requestVoClass.addMultipleImports("lombok");
        getApiModel(voRequestGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()).addAnnotationToTopLevelClass(requestVoClass);
        requestVoClass.addSerialVersionUID();

        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(null, null, voRequestGeneratorConfiguration.getExcludeColumns());
        List<String > excludeColumns = voGenService.getDefaultExcludeColumnNames(voRequestGeneratorConfiguration.getExcludeColumns());
        for (IntrospectedColumn voColumn : introspectedColumns) {
            if (!(isAbstractVOColumn(voColumn) || !excludeColumns.contains(voColumn.getActualColumnName()))) {
                Field field = new Field(voColumn.getJavaProperty(), voColumn.getFullyQualifiedJavaType());
                field.setVisibility(JavaVisibility.PRIVATE);
                field.setRemark(voColumn.getRemarks(true));
                if (plugins.voRequestFieldGenerated(field, requestVoClass, voColumn, introspectedTable)) {
                    requestVoClass.addField(field);
                    requestVoClass.addImportedType(voColumn.getFullyQualifiedJavaType());
                }
            }
        }

        //分页属性
        if (voRequestGeneratorConfiguration.isIncludePageParam()) {
            FullyQualifiedJavaType pageType = new FullyQualifiedJavaType("com.vgosoft.core.pojo.IPage");
            requestVoClass.addSuperInterface(pageType);
            requestVoClass.addImportedType(pageType);
            Field pNo = new Field("pageNo", FullyQualifiedJavaType.getIntInstance());
            pNo.setVisibility(JavaVisibility.PRIVATE);
            pNo.setRemark("页码");
            pNo.setInitializationString("DEFAULT_FIRST_PAGE_NO");
            new ApiModelProperty(pNo.getRemark(), "1").addAnnotationToField(pNo, requestVoClass);
            requestVoClass.addField(pNo);
            Field pSize = new Field("pageSize", FullyQualifiedJavaType.getIntInstance());
            pSize.setRemark("每页数据数量");
            new ApiModelProperty(pSize.getRemark(), "10").addAnnotationToField(pSize, requestVoClass);
            pSize.setInitializationString("DEFAULT_PAGE_SIZE");
            pSize.setVisibility(JavaVisibility.PRIVATE);
            requestVoClass.addField(pSize);
        }
        Field orderByClause = new Field("orderByClause", FullyQualifiedJavaType.getStringInstance());
        orderByClause.setVisibility(JavaVisibility.PRIVATE);
        orderByClause.setRemark("排序语句");
        new ApiModelProperty(orderByClause.getRemark(), "SORT_").addAnnotationToField(orderByClause, requestVoClass);
        requestVoClass.addField(orderByClause);
        //增加cascade开关
        if (introspectedTable.getRules().generateRelationWithSubSelected()) {
            Field cascade = new Field("cascadeResult", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
            cascade.setVisibility(JavaVisibility.PRIVATE);
            cascade.setRemark("结果是否包含子级");
            new ApiModelProperty(cascade.getRemark(), "false").addAnnotationToField(cascade, requestVoClass);
            requestVoClass.addField(cascade);
        }


        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voRequestGeneratorConfiguration.getAdditionalPropertyConfigurations();
        additionalPropertyConfigurations.addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        requestVoClass.getAddtionalPropertiesFields(additionalPropertyConfigurations).forEach(field -> {
                    if (plugins.voRequestFieldGenerated(field, requestVoClass, null, introspectedTable)) {
                        requestVoClass.addField(field);
                        requestVoClass.addImportedType(field.getType());
                    }
                }
        );

        mappingsInterface.addImportedType(new FullyQualifiedJavaType(requestVoType));
        mappingsInterface.addMethod(addMappingMethod(requestVoClass.getType(), entityType, false));

        return requestVoClass;
    }
}
