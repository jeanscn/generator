package org.mybatis.generator.codegen.mybatis3.vo;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InitializationBlock;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.VoExcelGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;

import com.vgosoft.tool.core.VCollectionUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 16:45
 * @version 3.0
 */
public class VoExcelImportGenerator extends AbstractVoGenerator {

    public VoExcelImportGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings,mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VoExcelGeneratorConfiguration voExcelGeneratorConfiguration = voGeneratorConfiguration.getVoExcelConfiguration();
        FullyQualifiedJavaType excelImportVoType = voExcelGeneratorConfiguration.getExcelImportType();
        TopLevelClass excelImportVoClass = createTopLevelClass(excelImportVoType.getFullyQualifiedName(), null);
        FullyQualifiedJavaType superInterface = new FullyQualifiedJavaType(ConstantsUtil.I_BASE_DTO);
        excelImportVoClass.addSuperInterface(superInterface);
        voGenService.addConfigurationSuperInterface(excelImportVoClass, voExcelGeneratorConfiguration);
        excelImportVoClass.addImportedType(superInterface);
        excelImportVoClass.addImportedType("lombok.*");
        excelImportVoClass.addAnnotation("@Data");
        excelImportVoClass.addAnnotation("@Builder");
        excelImportVoClass.addAnnotation("@NoArgsConstructor");
        excelImportVoClass.addSerialVersionUID(introspectedTable.getContext().getJdkVersion());

        //添加属性
        List<IntrospectedColumn> introspectedColumns = voGenService.getVoColumns(new ArrayList<>(), voExcelGeneratorConfiguration.getImportIncludeColumns(), voExcelGeneratorConfiguration.getImportExcludeColumns());
        if (!introspectedColumns.isEmpty()) {
            excelImportVoClass.addAnnotation("@AllArgsConstructor");
        }

        List<Field> importFields = new ArrayList<>();
        List<IntrospectedColumn> initColumns = new ArrayList<>();
        for (IntrospectedColumn voColumn : introspectedColumns) {
            Field field = new Field(voColumn.getJavaProperty(), voColumn.getFullyQualifiedJavaType());
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setRemark(voColumn.getRemarks(true));
            if (voColumn.getDefaultValue() != null) {
                initColumns.add(voColumn);
            }
            importFields.add(field);
        }

        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voExcelGeneratorConfiguration.getAdditionalPropertyConfigurations();
        VCollectionUtil.addAllIfNotContains(additionalPropertyConfigurations, voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        importFields.addAll(excelImportVoClass.getAdditionalPropertiesFields(additionalPropertyConfigurations));
        int idx = 1;
        for (Field importField : importFields) {
            if (plugins.voExcelImportFieldGenerated(importField, excelImportVoClass, null, introspectedTable,idx++)) {
                excelImportVoClass.addField(importField);
                excelImportVoClass.addImportedType(importField.getType());
            }
        }

        //静态代码块
        if (!initColumns.isEmpty()) {
            InitializationBlock initializationBlock = new InitializationBlock(false);
            addInitialization(new ArrayList<>(initColumns), initializationBlock,excelImportVoClass);
            excelImportVoClass.addInitializationBlock(initializationBlock);
        }

        /*
         * 添加映射方法
         */
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(excelImportVoType.getFullyQualifiedName()));
        mappingsInterface.addMethod(addMappingMethod(entityType, excelImportVoClass.getType(), false));
        mappingsInterface.addMethod(addMappingMethod(entityType, excelImportVoClass.getType(), true));
        mappingsInterface.addMethod(addMappingMethod(excelImportVoClass.getType(), entityType, false));
        mappingsInterface.addMethod(addMappingMethod(excelImportVoClass.getType(), entityType, true));

        return excelImportVoClass;
    }
}
