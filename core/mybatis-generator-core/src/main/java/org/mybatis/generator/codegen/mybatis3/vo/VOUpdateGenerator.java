package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VCollectionUtil;
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
        updateVoClass.addSerialVersionUID(introspectedTable.getContext().getJdkVersion());

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
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voUpdateGeneratorConfiguration.getAdditionalPropertyConfigurations();
        VCollectionUtil.addAllIfNotContains(additionalPropertyConfigurations, voGeneratorConfiguration.getAdditionalPropertyConfigurations());
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
            selectiveUpdate.setRemark("更新时选择性插入");
            commentGenerator.addFieldComment(selectiveUpdate, "插入时是否检查记录是否存在，进行选择性更新");
            addProperty(updateVoClass, selectiveUpdate, "true", introspectedTable);
        }

        //是否有启用update的JavaCollectionRelation
        addJavaCollectionRelation(updateVoClass,"update");

        mappingsInterface.addImportedType(new FullyQualifiedJavaType(updateVoType));
        mappingsInterface.addMethod(addMappingMethod(updateVoClass.getType(), entityType, false));
        mappingsInterface.addMethod(addMappingMethod(updateVoClass.getType(), entityType, true));

        return updateVoClass;
    }
}
