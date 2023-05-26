package org.mybatis.generator.codegen.mybatis3.vo;

import cn.hutool.core.collection.CollectionUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VOExcelGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.ModelClassTypeEnum;

import java.util.List;

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
        excelImportVoClass.addImportedType(superInterface);
        excelImportVoClass.addImportedType("lombok.*");
        excelImportVoClass.addAnnotation("@Data");
        excelImportVoClass.addAnnotation("@Builder");
        excelImportVoClass.addAnnotation("@NoArgsConstructor");
        excelImportVoClass.addSerialVersionUID();

        //添加属性
        List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(null, voExcelGeneratorConfiguration.getIncludeColumns(), voExcelGeneratorConfiguration.getExcludeColumns());
        CollectionUtil.addAllIfNotContains(introspectedColumns, voGenService.getAbstractVOColumns());
        if (introspectedColumns.size() > 0) {
            excelImportVoClass.addAnnotation("@AllArgsConstructor");
        }
        for (IntrospectedColumn voColumn : introspectedColumns) {
            Field field = new Field(voColumn.getJavaProperty(), voColumn.getFullyQualifiedJavaType());
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setRemark(voColumn.getRemarks(true));
            if (plugins.voExcelImportFieldGenerated(field, excelImportVoClass, voColumn, introspectedTable)) {
                excelImportVoClass.addField(field);
                excelImportVoClass.addImportedType(voColumn.getFullyQualifiedJavaType());
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
