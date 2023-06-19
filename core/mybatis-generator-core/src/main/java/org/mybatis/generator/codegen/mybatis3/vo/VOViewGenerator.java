package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.view.ViewActionColumnEnum;
import com.vgosoft.core.constant.enums.view.ViewIndexColumnEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VOViewGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.ModelClassTypeEnum;
import org.mybatis.generator.custom.annotations.ApiModel;
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
        ApiModel apiModel = addApiModel(voViewGeneratorConfiguration.getFullyQualifiedJavaType().getShortName());
        apiModel.addAnnotationToTopLevelClass(viewVOClass);
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
        voGenService.buildOverrideColumn(overridePropertyConfigurations, viewVOClass, ModelClassTypeEnum.viewVOClass)
                .forEach(field -> {
                    if (plugins.voViewFieldGenerated(field, viewVOClass, null, introspectedTable)) {
                        viewVOClass.addField(field);
                        viewVOClass.addImportedType(field.getType());
                    }
                });
        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voViewGeneratorConfiguration.getAdditionalPropertyConfigurations();
        additionalPropertyConfigurations.addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        viewVOClass.getAddtionalPropertiesFields(additionalPropertyConfigurations)
                .forEach(field -> {
                    if (plugins.voViewFieldGenerated(field, viewVOClass, null, introspectedTable)) {
                        viewVOClass.addField(field);
                        viewVOClass.addImportedType(field.getType());
                    }
                });

        mappingsInterface.addImportedType(new FullyQualifiedJavaType(viewVOType));
        mappingsInterface.addMethod(addMappingMethod(entityType, viewVOClass.getType(), false));
        mappingsInterface.addMethod(addMappingMethod(entityType, viewVOClass.getType(), true));
        return viewVOClass;
    }
}
