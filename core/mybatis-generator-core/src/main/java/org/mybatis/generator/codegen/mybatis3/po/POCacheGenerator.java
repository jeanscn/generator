package org.mybatis.generator.codegen.mybatis3.po;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.vo.AbstractVOGenerator;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;
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
public class POCacheGenerator extends AbstractVOGenerator {

    public POCacheGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings,Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings,mappingsInterface);
    }


    @Override
    public TopLevelClass generate() {
        final VOCacheGeneratorConfiguration config = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();
        String cachePoType = config.getFullyQualifiedJavaType().getFullyQualifiedName();
        TopLevelClass cachePoClass = createTopLevelClass(cachePoType, "");
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
                .collect(Collectors.toList());

        List<IntrospectedColumn> includeColumns = introspectedTable.getBaseColumns().stream()
                .filter(c -> config.getIncludeColumns().contains(c.getActualColumnName()))
                .collect(Collectors.toList());
        List<IntrospectedColumn> allColumns = Stream.of(pkColumns.stream(), oColumns.stream(), includeColumns.stream())
                .flatMap(Function.identity())
                .distinct()
                .collect(Collectors.toList());
        for (IntrospectedColumn column : allColumns) {
            Field field = getJavaBeansField(column, context, introspectedTable);
            cachePoClass.addField(field);
            cachePoClass.addImportedType(field.getType());
        }

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
        //追加compare方法
        if (cachePoClass.getFields().stream().anyMatch(f -> f.getName().equalsIgnoreCase("sort"))) {
            Method compareMethod = new Method("compareTo");
            compareMethod.setVisibility(JavaVisibility.PUBLIC);
            compareMethod.setReturnType(FullyQualifiedJavaType.getIntInstance());
            compareMethod.addParameter(new Parameter(cachePoClass.getType(), "o"));
            compareMethod.addBodyLine("this.sort = this.sort == null ? 0 : this.sort;");
            compareMethod.addBodyLine("o.sort = o.sort == null ? 0 : o.sort;");
            compareMethod.addBodyLine("return this.sort - o.sort;");
            cachePoClass.addMethod(compareMethod);
        }
        mappingsInterface.addImportedType(new FullyQualifiedJavaType(cachePoType));
        Method method = addMappingMethod(entityType, cachePoClass.getType(), false);
        String valueColumnM = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration().getValueColumn();
        if (valueColumnM != null) {
            IntrospectedColumn column = introspectedTable.getColumn(valueColumnM).orElse(null);
            if (column != null) {
                String a = VStringUtil.format("@Mapping(source = \"{0}\",target = \""+ GlobalConstant.CACHE_PO_DEFAULT_VALUE_TEXT +"\")"
                        , column.getJavaProperty());
                method.addAnnotation(a);
                mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.Mapping"));
            }
        }
        mappingsInterface.addMethod(method);
        mappingsInterface.addMethod(addMappingMethod(entityType, cachePoClass.getType(), true));

        return cachePoClass;
    }
}
