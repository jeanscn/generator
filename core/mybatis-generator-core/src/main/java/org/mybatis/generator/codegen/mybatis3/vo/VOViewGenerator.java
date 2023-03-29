package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.core.constant.enums.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.ViewActionColumnEnum;
import com.vgosoft.core.constant.enums.ViewIndexColumnEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VOViewGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.ModelClassTypeEnum;
import org.mybatis.generator.custom.annotations.CompositeQuery;
import org.mybatis.generator.custom.annotations.ViewColumnMeta;
import org.mybatis.generator.custom.annotations.ViewTableMeta;

import java.util.List;
import java.util.Objects;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 16:03
 * @version 3.0
 */
public class VOViewGenerator extends AbstractVOGenerator{

    public VOViewGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings,mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VOViewGeneratorConfiguration voViewGeneratorConfiguration = voGeneratorConfiguration.getVoViewConfiguration();
        String viewVOType = voViewGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass viewVOClass = createTopLevelClass(viewVOType, getAbstractVOType().getFullyQualifiedName());
        viewVOClass.addMultipleImports("lombok");
        getApiModel(voViewGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()).addAnnotationToTopLevelClass(viewVOClass);
        addViewTableMeta(voViewGeneratorConfiguration,viewVOClass);
        viewVOClass.addImportedType(getAbstractVOType().getFullyQualifiedName());
        viewVOClass.addSerialVersionUID();

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
        overridePropertyConfigurations.addAll(voGeneratorConfiguration.getOverridePropertyConfigurations());
        voGenService.buildOverrideColumn(overridePropertyConfigurations, viewVOClass, ModelClassTypeEnum.viewVOClass).forEach(field -> plugins.voViewFieldGenerated(field, viewVOClass, null, introspectedTable)
        );

        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voViewGeneratorConfiguration.getAdditionalPropertyConfigurations();
        additionalPropertyConfigurations.addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        viewVOClass.getAddtionalPropertiesFields(additionalPropertyConfigurations).forEach(field -> {
                    if (plugins.voViewFieldGenerated(field, viewVOClass, null, introspectedTable)) {
                        viewVOClass.addField(field);
                        viewVOClass.addImportedType(field.getType());
                    }
                }
        );

        mappingsInterface.addImportedType(new FullyQualifiedJavaType(viewVOType));
        mappingsInterface.addMethod(addMappingMethod(entityType, viewVOClass.getType(), false));
        mappingsInterface.addMethod(addMappingMethod(entityType, viewVOClass.getType(), true));

        return viewVOClass;
    }

    private void addViewTableMeta(VOViewGeneratorConfiguration voViewGeneratorConfiguration,TopLevelClass viewVOClass) {
        ViewTableMeta viewTableMeta = new ViewTableMeta(introspectedTable);
        //createUrl
        String createUrl = "";
        FullyQualifiedJavaType rootType = new FullyQualifiedJavaType(getRootClass());
        if (stringHasValue(rootType.getShortName())) {
            if (EntityAbstractParentEnum.ofCode(rootType.getShortName()) == null
                    || (EntityAbstractParentEnum.ofCode(rootType.getShortName()) != null
                    && EntityAbstractParentEnum.ofCode(rootType.getShortName()).scope() != 1)) {
                createUrl = String.join("/"
                        , introspectedTable.getControllerSimplePackage()
                        , introspectedTable.getControllerBeanName()
                        , "view");
            }
        }
        if (stringHasValue(createUrl)) {
            viewTableMeta.setCreateUrl(createUrl);
        }
        //dataUrl
        viewTableMeta.setDataUrl(String.join("/"
                , introspectedTable.getControllerSimplePackage()
                , introspectedTable.getControllerBeanName()
                , "getdtdata"));

        //indexColumn
        ViewIndexColumnEnum viewIndexColumnEnum = ViewIndexColumnEnum.ofCode(voViewGeneratorConfiguration.getIndexColumn());
        if (viewIndexColumnEnum != null) {
            viewTableMeta.setIndexColumn(viewIndexColumnEnum);
        }
        //actionColumn
        if (voViewGeneratorConfiguration.getActionColumn().size() > 0) {
            ViewActionColumnEnum[] viewActionColumnEnums = voViewGeneratorConfiguration.getActionColumn().stream()
                    .map(ViewActionColumnEnum::ofCode)
                    .filter(Objects::nonNull)
                    .distinct().toArray(ViewActionColumnEnum[]::new);
            viewTableMeta.setActionColumn(viewActionColumnEnums);
        }
        //querys
        if (voViewGeneratorConfiguration.getQueryColumns().size() > 0) {
            String[] strings = voViewGeneratorConfiguration.getQueryColumns().stream()
                    .distinct()
                    .map(f -> introspectedTable.getColumn(f).orElse(null))
                    .filter(Objects::nonNull)
                    .map(c -> CompositeQuery.create(c).toAnnotation())
                    .toArray(String[]::new);
            viewTableMeta.setQuerys(strings);
        }
        //columns
        if (voViewGeneratorConfiguration.getIncludeColumns().size() > 0) {
            String[] strings = voViewGeneratorConfiguration.getIncludeColumns().stream()
                    .distinct()
                    .map(f -> introspectedTable.getColumn(f).orElse(null))
                    .filter(Objects::nonNull)
                    .map(c -> ViewColumnMeta.create(c, introspectedTable).toAnnotation())
                    .toArray(String[]::new);
            viewTableMeta.setColumns(strings);
        }
        //ignoreFields
        if (voViewGeneratorConfiguration.getExcludeColumns().size() > 0) {
            String[] columns2 = voViewGeneratorConfiguration.getExcludeColumns().stream()
                    .distinct()
                    .map(f -> introspectedTable.getColumn(f).orElse(null))
                    .filter(Objects::nonNull)
                    .map(IntrospectedColumn::getJavaProperty)
                    .toArray(String[]::new);
            viewTableMeta.setIgnoreFields(columns2);
        }
        //className
        viewTableMeta.setClassName(viewVOClass.getType().getFullyQualifiedName());

        //构造ViewTableMeta
        viewVOClass.addAnnotation(viewTableMeta.toAnnotation());
        viewVOClass.addMultipleImports(viewTableMeta.multipleImports());
    }
}
