package org.mybatis.generator.config;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.FieldItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 可复用元素配置
 * @author cen_c
 */
@Getter
@Setter
@NoArgsConstructor
public class ReusableElementsConfiguration  extends TypedPropertyHolder{
    /**
     * 排除的表名
     */
    private Set<String> excludedTables = new HashSet<>();

    /**
     * HTML元素描述符列表
     */
    private final List<HtmlElementDescriptor> htmlElementDescriptors = new ArrayList<>();

    /**
     * 重写属性值生成器配置列表
     */
    private final List<OverridePropertyValueGeneratorConfiguration> overridePropertyValues = new ArrayList<>();

    /**
     * 列表列配置列表
     */
    private final List<ListColumnConfiguration> listColumnDescriptors = new ArrayList<>();

    /**
     * 主列表字段重写配置列表
     */
    private final List<ViewFieldOverrideConfiguration> fieldOverrides = new ArrayList<>();

    /**
     * 过滤列配置列表
     */
    private final List<FilterColumnConfiguration> filterColumnDescriptors = new ArrayList<>();

    /**
     * 获取指定表的过滤列配置列表
     * @param introspectedTable 表
     * @return 过滤列配置列表
     */
    public List<FilterColumnConfiguration> getFilterColumnConfigurations( AbstractIntrospectedTable introspectedTable) {
        if (filterColumnDescriptors.isEmpty() || excludedTables.contains(introspectedTable.getTableConfiguration().getTableName())) {
            return new ArrayList<>();
        }
        List<FilterColumnConfiguration> result = new ArrayList<>();
        for (FilterColumnConfiguration filterColumnDescriptor : filterColumnDescriptors) {
            IntrospectedColumn column = introspectedTable.getColumn(filterColumnDescriptor.getColumn()).orElse(null);
            if (column != null) {
                filterColumnDescriptor.setIntrospectedColumn(column);
                filterColumnDescriptor.setField(column.getJavaProperty());
                filterColumnDescriptor.setTableName(introspectedTable.getTableConfiguration().getTableName());
                filterColumnDescriptor.setTableAlias(introspectedTable.getTableConfiguration().getAlias());
                if (!VStringUtil.stringHasValue(filterColumnDescriptor.getRemark())) {
                    filterColumnDescriptor.setRemark(column.getRemarks(true));
                }
                result.add(filterColumnDescriptor);
            }
        }
        return result;
    }

    /**
     * 获取指定表的HTML元素描述符列表
     * @param introspectedTable 表
     * @return HTML元素描述符列表
     */
    public List<HtmlElementDescriptor> getHtmlElementDescriptors( AbstractIntrospectedTable introspectedTable) {
        if (htmlElementDescriptors.isEmpty() || excludedTables.contains(introspectedTable.getTableConfiguration().getTableName())) {
            return new ArrayList<>();
        }
        List<HtmlElementDescriptor> result = new ArrayList<>();
        for (HtmlElementDescriptor descriptor : htmlElementDescriptors) {
            if (introspectedTable.getColumn(descriptor.getName()).isPresent()) {
                result.add(descriptor);
            }
        }
        return result;
    }

    /**
     * 获取指定表的列表列配置列表
     * @param innerListViewConfiguration 列表视图配置
     * @param introspectedTable 表
     * @return 列表列配置列表
     */
    public List<ListColumnConfiguration> getListColumnConfigurations(InnerListViewConfiguration innerListViewConfiguration,AbstractIntrospectedTable introspectedTable) {
        if (listColumnDescriptors.isEmpty() || innerListViewConfiguration == null) {
            return new ArrayList<>();
        }
        final List<String> defaultDisplayFields = innerListViewConfiguration.getDefaultDisplayFields();
        if (defaultDisplayFields.isEmpty()) {
            List<String> filedNames = introspectedTable.getVoModelFields().stream().map(FieldItem::getName).collect(Collectors.toList());
            defaultDisplayFields.addAll(filedNames);
        }
        return listColumnDescriptors.stream().filter(descriptor->defaultDisplayFields.contains(descriptor.getField())).collect(Collectors.toList());
    }

    /**
     * 获取指定表的重写属性值生成器配置列表
     * @param introspectedTable 表
     * @return 重写属性值生成器配置列表
     */
    public List<OverridePropertyValueGeneratorConfiguration> getOverridePropertyValueGeneratorConfigurations(AbstractIntrospectedTable introspectedTable) {
        if (overridePropertyValues.isEmpty() || excludedTables.contains(introspectedTable.getTableConfiguration().getTableName())) {
            return new ArrayList<>();
        }
        List<OverridePropertyValueGeneratorConfiguration> result = new ArrayList<>();
        for (OverridePropertyValueGeneratorConfiguration overrideConfig : overridePropertyValues) {
            if (introspectedTable.getColumn(overrideConfig.getSourceColumnName()).isPresent()) {
                result.add(overrideConfig);
            }
        }
        return result;
    }

    /**
     * 获取指定表的列表列配置列表
     * @param columName 列名
     * @param tableName 表名
     * @return HTML元素描述符
     */
    public HtmlElementDescriptor getHtmlElementDescriptor(String columName,String tableName) {
        if (htmlElementDescriptors.isEmpty() || columName == null) {
            return null;
        }
        if (tableName != null && excludedTables.contains(tableName)) {
            return null;
        }
        for (HtmlElementDescriptor descriptor : htmlElementDescriptors) {
            if (columName.equals(descriptor.getName())) {
                return descriptor;
            }
        }
        return null;
    }

    /**
     * 获取指定表的列表列配置
     * @param fieldName 属性名
     * @param tableName 表名
     * @return 列表列配置
     */
    public ListColumnConfiguration getListColumnConfiguration(String fieldName,String tableName) {
        if (listColumnDescriptors.isEmpty() || fieldName == null) {
            return null;
        }
        if (tableName != null && excludedTables.contains(tableName)) {
            return null;
        }
        for (ListColumnConfiguration descriptor : listColumnDescriptors) {
            if (fieldName.equals(descriptor.getField())) {
                return descriptor;
            }
        }
        return null;
    }

    /**
     * 获取指定表的重写属性值生成器配置
     * @param sourceColumnName 源列名
     * @param tableName 表名
     * @return 重写属性值生成器配置
     */
    public OverridePropertyValueGeneratorConfiguration getOverridePropertyValueGeneratorConfiguration(String sourceColumnName,String tableName) {
        if (overridePropertyValues.isEmpty() || sourceColumnName == null) {
            return null;
        }
        if (tableName != null && excludedTables.contains(tableName)) {
            return null;
        }
        for (OverridePropertyValueGeneratorConfiguration descriptor : overridePropertyValues) {
            if (sourceColumnName.equals(descriptor.getSourceColumnName())) {
                return descriptor;
            }
        }
        return null;
    }
}
