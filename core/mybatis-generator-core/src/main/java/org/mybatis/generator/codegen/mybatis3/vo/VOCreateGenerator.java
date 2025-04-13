package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VCollectionUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.VOCreateGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.ApiModelPropertyDesc;

import java.util.*;

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
    public TopLevelClass generate() {
        VOCreateGeneratorConfiguration voCreateGeneratorConfiguration = voGeneratorConfiguration.getVoCreateConfiguration();
        String createVoType = voCreateGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass createVoClass = createTopLevelClass(createVoType, getAbstractVOType().getFullyQualifiedName());
        createVoClass.addMultipleImports("lombok");
        addApiModel(voCreateGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()).addAnnotationToTopLevelClass(createVoClass);
        voGenService.addConfigurationSuperInterface(createVoClass, voCreateGeneratorConfiguration);
        createVoClass.addImportedType(getAbstractVOType().getFullyQualifiedName());
        createVoClass.addSerialVersionUID(introspectedTable.getContext().getJdkVersion());
        //添加id属性
        List<String> fields = new ArrayList<>(Collections.singletonList("id"));
        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(fields, voCreateGeneratorConfiguration.getIncludeColumns(), voCreateGeneratorConfiguration.getExcludeColumns());
        Set<String> excludeColumns = voCreateGeneratorConfiguration.getExcludeColumns();
        introspectedColumns.removeIf(introspectedColumn -> excludeColumns.contains(introspectedColumn.getActualColumnName()));
        for (IntrospectedColumn voColumn : introspectedColumns) {
            if (!isAbstractVOColumn(voColumn)) {
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
            ApiModelPropertyDesc apiModelPropertyDesc = new ApiModelPropertyDesc(selectiveUpdate.getRemark());
            apiModelPropertyDesc.setExample("true");
            selectiveUpdate.addAnnotation(apiModelPropertyDesc.toAnnotation());
            createVoClass.addImportedTypes(apiModelPropertyDesc.getImportedTypes());
            createVoClass.addField(selectiveUpdate);
        }

        //重写父类getter方法
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
        VCollectionUtil.addAllIfNotContains(additionalPropertyConfigurations, voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        createVoClass.getAdditionalPropertiesFields(additionalPropertyConfigurations).forEach(field -> {
                    if (plugins.voCreateFieldGenerated(field, createVoClass, null, introspectedTable)) {
                        createVoClass.addField(field);
                        createVoClass.addImportedType(field.getType());
                    }
                }
        );

        //是否有启用insert的JavaCollectionRelation
        addJavaCollectionRelation(createVoClass, "insert");

        //添加静态代码块
        //获取vo中的含父类和子类的所有字段
        List<IntrospectedColumn> columns = VCollectionUtil.addAllIfNotContains(voGenService.getAbstractVOColumns(), introspectedColumns);
        InitializationBlock initializationBlock = new InitializationBlock(false);
        //在静态代码块中添加默认值
        Set<String> initializationColumns = addInitialization(columns, initializationBlock, createVoClass);
        //额外添加createId和modifiedId
        IntrospectedColumn createdId = introspectedTable.getColumn("created_id").orElse(null);
        if (createdId!=null && !initializationColumns.contains(createdId.getActualColumnName())) {
            initializationBlock.addBodyLine("this.createdId = \"\";");
        }
        IntrospectedColumn modifiedId = introspectedTable.getColumn("modified_id").orElse(null);
        if (modifiedId!=null && !initializationColumns.contains(modifiedId.getActualColumnName())) {
            initializationBlock.addBodyLine("this.modifiedId = \"\";");
        }
        createVoClass.addInitializationBlock(initializationBlock);

        /*
          生成映射方法
         */
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(createVoType));
        mappingsInterface.addMethod(addMappingMethod(createVoClass.getType(), entityType, false));
        mappingsInterface.addMethod(addMappingMethod(createVoClass.getType(), entityType, true));

        return createVoClass;
    }
}
