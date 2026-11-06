package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.tool.core.VCollectionUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.VoCreateGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;

/**
 * CreateVo生成器
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 15:40
 * @version 3.0
 */
public class VoCreateGenerator extends AbstractVoGenerator {

    public VoCreateGenerator(AbstractIntrospectedTable abstractIntrospectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(abstractIntrospectedTable, project,progressCallback, warnings,mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VoCreateGeneratorConfiguration voCreateGeneratorConfiguration = voGeneratorConfiguration.getVoCreateConfiguration();
        String createVoType = voCreateGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass createVoClass = createTopLevelClass(createVoType, getAbstractVoType().getFullyQualifiedName());
        commentGenerator.addModelClassComment(createVoClass, introspectedTable);
        createVoClass.addMultipleImports("lombok");
        addSchemaDesc(voCreateGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()).addAnnotationToTopLevelClass(createVoClass);
        voGenService.addConfigurationSuperInterface(createVoClass, voCreateGeneratorConfiguration);
        createVoClass.addImportedType(getAbstractVoType().getFullyQualifiedName());
        createVoClass.addSerialVersionUID(introspectedTable.getContext().getJdkVersion());
        //添加id属性
        List<String> fields = new ArrayList<>(Collections.singletonList("id"));
        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(fields, voCreateGeneratorConfiguration.getIncludeColumns(), voCreateGeneratorConfiguration.getExcludeColumns());
        Set<String> excludeColumns = voCreateGeneratorConfiguration.getExcludeColumns();
        introspectedColumns.removeIf(introspectedColumn -> excludeColumns.contains(introspectedColumn.getActualColumnName()));
        for (IntrospectedColumn voColumn : introspectedColumns) {
            if (!isAbstractVoColumn(voColumn)) {
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
            selectiveUpdate.setRemark("插入时选择性更新");
            commentGenerator.addFieldComment(selectiveUpdate, "插入时是否检查记录是否存在，进行选择性更新");
            addProperty(createVoClass, selectiveUpdate,"true", introspectedTable);
        }

        Field modelTempId = new Field("modelTempId", FullyQualifiedJavaType.getStringInstance());
        modelTempId.setRemark("临时数据标识");
        modelTempId.setInitializationString("UuidUtil.nextUuid()");
        createVoClass.addImportedType("com.vgosoft.core.util.UuidUtil");
        commentGenerator.addFieldComment(modelTempId, "临时数据标识,用于创建新记录时就指定id的场景");
        addProperty(createVoClass, modelTempId, "''", introspectedTable);

        //重写父类getter方法
        for (IntrospectedColumn introspectedColumn : voGenService.getAbstractVoColumns()) {
            if (!(introspectedColumn.isNullable() || introspectedTable.getTableConfiguration().getValidateIgnoreColumns().contains(introspectedColumn.getActualColumnName()))) {
                this.getOverrideGetter(introspectedColumn).ifPresent(javaBeansGetter -> {
                    if (plugins.voCreateGetterMethodGenerated(javaBeansGetter, createVoClass, introspectedColumn, introspectedTable)) {
                        javaBeansGetter.addAnnotation("@Override");
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
        List<IntrospectedColumn> columns = VCollectionUtil.addAllIfNotContains(voGenService.getAbstractVoColumns(), introspectedColumns);
        InitializationBlock initializationBlock = new InitializationBlock(false);
        //在静态代码块中添加默认值（A 类审计字段与约定默认值字段已由 addInitialization 跳过，不再附加 createdId/modifiedId 空串）
        addInitialization(columns, initializationBlock, createVoClass);
        createVoClass.addInitializationBlock(initializationBlock);

        /*
          生成映射方法
         */
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(createVoType));
        // VO→实体方向忽略服务端强制字段，避免客户端审计值进入实体
        mappingsInterface.addMethod(addMappingMethod(createVoClass.getType(), entityType, false, VoGeneratorUtil.auditIgnoreFields()));
        mappingsInterface.addMethod(addMappingMethod(createVoClass.getType(), entityType, true, VoGeneratorUtil.auditIgnoreFields()));

        return createVoClass;
    }
}
