package org.mybatis.generator.codegen.mybatis3.po;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.vo.AbstractVoGenerator;
import org.mybatis.generator.codegen.mybatis3.DictCacheOrderResolver;
import org.mybatis.generator.config.VoCacheGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 17:08
 * @version 3.0
 */
public class PoCacheGenerator extends AbstractVoGenerator {

    public PoCacheGenerator(AbstractIntrospectedTable abstractIntrospectedTable, String project, ProgressCallback progressCallback, List<String> warnings, Interface mappingsInterface) {
        super(abstractIntrospectedTable, project, progressCallback, warnings,mappingsInterface);
    }


    @Override
    public TopLevelClass generate() {
        final VoCacheGeneratorConfiguration config = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();
        String cachePoType = config.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass cachePoClass = createTopLevelClass(cachePoType, "");
        commentGenerator.addModelClassComment(cachePoClass, introspectedTable);
        cachePoClass.addSuperInterface(new FullyQualifiedJavaType(ConstantsUtil.I_BASE_DTO));
        cachePoClass.addImportedType(ConstantsUtil.I_BASE_DTO);
        cachePoClass.addImportedType("lombok.*");
        cachePoClass.addAnnotation("@Data");
        cachePoClass.addSerialVersionUID(introspectedTable.getContext().getJdkVersion());

        List<IntrospectedColumn> pkColumns = introspectedTable.getPrimaryKeyColumns();
        List<IntrospectedColumn> oColumns = Stream.of(config.getKeyColumn(), config.getTypeColumn(), config.getValueColumn())
                .distinct()
                .map(c -> introspectedTable.getColumn(c).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        List<IntrospectedColumn> includeColumns = introspectedTable.getBaseColumns().stream()
                .filter(c -> config.getIncludeColumns().contains(c.getActualColumnName()))
                .collect(Collectors.toList());
        if (includeColumns.isEmpty()) {
            includeColumns = introspectedTable.getBaseColumns();
        }
        List<IntrospectedColumn> orderColumns = DictCacheOrderResolver.resolve(introspectedTable);
        List<IntrospectedColumn> allColumns = Stream.of(pkColumns.stream(), oColumns.stream(), includeColumns.stream(), orderColumns.stream())
                .flatMap(Function.identity())
                .distinct()
                .toList();
        for (IntrospectedColumn column : allColumns) {
            Field field = getJavaBeansField(column, context, introspectedTable);
            if (isHiddenDictOrderColumn(config, column, orderColumns, pkColumns, oColumns, includeColumns)) {
                field.addAnnotation("@JsonIgnore");
                cachePoClass.addImportedType("com.fasterxml.jackson.annotation.JsonIgnore");
            }
            cachePoClass.addField(field);
            cachePoClass.addImportedType(field.getType());
        }

        addDictValueTextField(config, cachePoClass);
        addCompareMethod(cachePoClass, includeColumns);
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(cachePoType));
        mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.Mapping"));
        Method method = addMappingMethod(entityType, cachePoClass.getType(), false);
        String valueColumnM = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration().getValueColumn();
        if (valueColumnM != null) {
            IntrospectedColumn column = introspectedTable.getColumn(valueColumnM).orElse(null);
            if (column != null) {
                String a = VStringUtil.format("@Mapping(source = \"{0}\",target = \""+ GlobalConstant.CACHE_PO_DEFAULT_VALUE_TEXT +"\")"
                        , column.getJavaProperty());
                method.addAnnotation(a);
            }
        }
        mappingsInterface.addMethod(method);
        mappingsInterface.addMethod(addMappingMethod(entityType, cachePoClass.getType(), true));

        // 增加Po到do的转换方法
        mappingsInterface.addMethod(addMappingMethod(cachePoClass.getType(),entityType,  false));
        mappingsInterface.addMethod(addMappingMethod(cachePoClass.getType(),entityType,  true));

        // 增加Po到vo的转换方法
        if (introspectedTable.getRules().isGenerateVoModel()) {
            FullyQualifiedJavaType voType = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoModelConfiguration().getFullyQualifiedJavaType();
            mappingsInterface.addMethod(addMappingMethod(cachePoClass.getType(),voType,  false));
            mappingsInterface.addMethod(addMappingMethod(cachePoClass.getType(),voType,  true));
            mappingsInterface.addImportedType(voType);
        }
        return cachePoClass;
    }

    private boolean isHiddenDictOrderColumn(VoCacheGeneratorConfiguration config, IntrospectedColumn column,
                                             List<IntrospectedColumn> orderColumns,
                                             List<IntrospectedColumn> pkColumns,
                                             List<IntrospectedColumn> dictionaryColumns,
                                             List<IntrospectedColumn> includeColumns) {
        if (!orderColumns.contains(column) || dictionaryColumns.contains(column)) {
            return false;
        }
        boolean explicitlyOrdered = config.getDictCacheOrderColumns().stream()
                .map(introspectedTable::getColumn)
                .anyMatch(resolved -> resolved.filter(column::equals).isPresent());
        return explicitlyOrdered || (!pkColumns.contains(column) && !includeColumns.contains(column));
    }

    private void addDictValueTextField(VoCacheGeneratorConfiguration config, TopLevelClass cachePoClass) {
        //追加dictValueText属性
        IntrospectedColumn valueColumn = introspectedTable.getColumn(config.getValueColumn()).orElse(null);
        long dictValueCount = cachePoClass.getFields().stream()
                .filter(f -> f.getName().equalsIgnoreCase(GlobalConstant.CACHE_PO_DEFAULT_VALUE_TEXT))
                .count();
        if (dictValueCount == 0 && valueColumn != null) {
            Field field = new Field(GlobalConstant.CACHE_PO_DEFAULT_VALUE_TEXT, FullyQualifiedJavaType.getStringInstance());
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setRemark("字典应用的返回值");
            field.addJavaDocLine("/**");
            field.addJavaDocLine("* 字典应用的返回值");
            field.addJavaDocLine("*/");
            cachePoClass.addField(field);
        }
    }

    static void addCompareMethod(TopLevelClass cachePoClass, List<IntrospectedColumn> includeColumns) {
        //追加compare方法
        if (includeColumns.stream().anyMatch(column ->
                column.getJavaProperty().equalsIgnoreCase(DefaultColumnNameEnum.SORT.fieldName()))) {
            Method compareMethod = new Method("compareTo");
            compareMethod.setVisibility(JavaVisibility.PUBLIC);
            compareMethod.setReturnType(FullyQualifiedJavaType.getIntInstance());
            compareMethod.addParameter(new Parameter(cachePoClass.getType(), "o"));
            compareMethod.addBodyLine("this.sort = this.sort == null ? 0 : this.sort;");
            compareMethod.addBodyLine("o.sort = o.sort == null ? 0 : o.sort;");
            compareMethod.addBodyLine("return this.sort - o.sort;");
            cachePoClass.addMethod(compareMethod);
        }
    }
}
