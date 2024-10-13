package org.mybatis.generator.codegen.mybatis3.vo;

import cn.hutool.core.collection.CollectionUtil;
import com.vgosoft.tool.core.VCollectionUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.VOExcelGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 16:45
 * @version 3.0
 */
public class VOExcelImportGenerator extends AbstractVOGenerator{

    public VOExcelImportGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings,mappingsInterface);
    }

    @Override
    public TopLevelClass generate() {
        VOExcelGeneratorConfiguration voExcelGeneratorConfiguration = voGeneratorConfiguration.getVoExcelConfiguration();
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
        excelImportVoClass.addSerialVersionUID();

        //添加属性
        List<IntrospectedColumn> introspectedColumns = voGenService.getVOColumns(new ArrayList<>(), voExcelGeneratorConfiguration.getImportIncludeColumns(), voExcelGeneratorConfiguration.getImportExcludeColumns());
        if (!introspectedColumns.isEmpty()) {
            excelImportVoClass.addAnnotation("@AllArgsConstructor");
        }

        List<Field> importFields = new ArrayList<>();
        for (IntrospectedColumn voColumn : introspectedColumns) {
            Field field = new Field(voColumn.getJavaProperty(), voColumn.getFullyQualifiedJavaType());
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setRemark(voColumn.getRemarks(true));
            importFields.add(field);
        }

        //附加属性
        TreeSet<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = voExcelGeneratorConfiguration.getAdditionalPropertyConfigurations();
        additionalPropertyConfigurations.addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        importFields.addAll(excelImportVoClass.getAdditionalPropertiesFields(additionalPropertyConfigurations));

        for (Field importField : importFields) {
            if (plugins.voExcelImportFieldGenerated(importField, excelImportVoClass, null, introspectedTable)) {
                excelImportVoClass.addField(importField);
                excelImportVoClass.addImportedType(importField.getType());
            }
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
