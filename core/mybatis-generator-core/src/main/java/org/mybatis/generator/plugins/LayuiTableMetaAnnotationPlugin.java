package org.mybatis.generator.plugins;

import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.freeMaker.html.layui.InnerListEditTemplate;
import org.mybatis.generator.codegen.mybatis3.vue.VueFormGenerateUtil;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.annotations.CompositeQueryDesc;
import org.mybatis.generator.custom.annotations.LayuiTableColumnMetaDesc;
import org.mybatis.generator.custom.annotations.LayuiTableMetaDesc;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * 添加ViewMetaAnnotation
 */
public class LayuiTableMetaAnnotationPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean voModelViewClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!introspectedTable.getRules().isGenerateInnerTable()) return true;

        VOViewGeneratorConfiguration voViewGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
        //增加InnerTableMetaAnnotation
        voViewGeneratorConfiguration.getInnerListViewConfigurations().forEach(listViewConfiguration -> {
            LayuiTableMetaDesc layuiTableMetaDesc = getLayuiTableMetaDesc(introspectedTable, listViewConfiguration, topLevelClass);
            String annotation = layuiTableMetaDesc.toAnnotation();
            if (!topLevelClass.getAnnotations().contains(annotation)) {
                topLevelClass.addAnnotation(annotation);
                topLevelClass.addImportedTypes(layuiTableMetaDesc.getImportedTypes());
            }
        });
        return true;
    }

    /**
     * VO抽象父类的ColumnMetaAnnotation
     */
    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (!introspectedTable.getRules().isGenerateInnerTable()) return true;
        addLayuiTableColumnMeta(field, topLevelClass, introspectedColumn, introspectedTable);
        return true;
    }

    /**
     * viewVO类的ViewMetaAnnotation
     */
    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (!introspectedTable.getRules().isGenerateInnerTable()) return true;
        addLayuiTableColumnMeta(field, topLevelClass, introspectedColumn, introspectedTable);
        return true;
    }

    //构造适用与Vxe的LayTableColumnMetaDesc
    private LayuiTableColumnMetaDesc buildVxeTableColumnDesc(InnerListViewConfiguration listViewConfiguration, Field field, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        String fieldName = field.getName();
        Map<String, HtmlElementDescriptor> elementDescriptorMap = listViewConfiguration.getElementDescriptorMap(); //所有的配置标签列表
        //fieldNames包含所有的fieldName和otherFieldName
        Set<String> fieldNames = Stream.of(
                        elementDescriptorMap.values().stream().map(t -> t.getColumn().getJavaProperty()),
                        elementDescriptorMap.values().stream().map(HtmlElementDescriptor::getOtherFieldName)
                ).flatMap(stringStream -> stringStream)
                .collect(Collectors.toSet());

        Set<String> displayFields = new HashSet<>(listViewConfiguration.getDefaultDisplayFields()); //获取displayFields，包含所有的fieldName和otherFieldName
        Set<String> onlyReadFields = listViewConfiguration.getReadonlyFields(); //只读字段
        Set<String> onlyEditFields = new HashSet<>(); //只编辑字段
        Set<String> hiddenFields = listViewConfiguration.getDefaultHiddenFields(); //隐藏字段
        Set<String> allTableFields = new HashSet<>(displayFields);
        //allTableFields.addAll(hiddenFields);
        elementDescriptorMap.values().forEach(htmlElementDescriptor -> {
            String name = htmlElementDescriptor.getColumn().getJavaProperty();
            String otherFieldName = htmlElementDescriptor.getOtherFieldName();
            if (!name.equals(otherFieldName) && allTableFields.contains(name)) {
                onlyReadFields.add(htmlElementDescriptor.getOtherFieldName());
                onlyEditFields.add(htmlElementDescriptor.getColumn().getJavaProperty());
            }
        });
        if (!(allTableFields.contains(fieldName) || onlyReadFields.contains(fieldName))) {
            return null;
        }
        boolean enableEdit = enableEdit(listViewConfiguration, fieldName);
        LayuiTableColumnMetaDesc layuiTableColumnMetaDesc = getLayuiTableColumnMetaDesc(listViewConfiguration, field, hiddenFields.contains(fieldName), introspectedTable,introspectedColumn);
        if (onlyReadFields.contains(fieldName)) {
            layuiTableColumnMetaDesc.setEdit(false);
            layuiTableColumnMetaDesc.setScope("readonly");
        } else if (fieldNames.contains(fieldName)) {
            elementDescriptorMap.values().stream()
                    .filter(t -> (t.getColumn().getJavaProperty().equals(field.getName()) || t.getOtherFieldName().equals(field.getName())))
                    .findFirst().ifPresent(descriptor -> {
                        if (enableEdit) {
                            layuiTableColumnMetaDesc.setEditor(descriptor.getTagType());
                            layuiTableColumnMetaDesc.setEdit(true);
                            layuiTableColumnMetaDesc.setTemplet("#TPL-inner-" + descriptor.getName());
                            layuiTableColumnMetaDesc.setScope("both");
                        } else {
                            layuiTableColumnMetaDesc.setEdit(false);
                            layuiTableColumnMetaDesc.setScope("both");
                        }
                    });
        } else if (!hiddenFields.contains(fieldName)) {
            if (enableEdit) {
                if (introspectedColumn != null && (introspectedColumn.isJdbcCharacterColumn() && introspectedColumn.getLength() > 1500 || introspectedColumn.isBLOBColumn())) {
                    layuiTableColumnMetaDesc.setEditor("textarea");
                    layuiTableColumnMetaDesc.setEdit(true);
                } else if (introspectedColumn != null && introspectedColumn.isNumericColumn()) {
                    layuiTableColumnMetaDesc.setEdit(true);
                    layuiTableColumnMetaDesc.setEditor("number");
                } else if (isDateType(field.getType())) {
                    layuiTableColumnMetaDesc.setEdit(false);
                    layuiTableColumnMetaDesc.setEditor("date");
                    layuiTableColumnMetaDesc.setTemplet("#TPL-inner-" + field.getName());
                } else {
                    layuiTableColumnMetaDesc.setEdit(true);
                    layuiTableColumnMetaDesc.setEditor("text");
                }
                layuiTableColumnMetaDesc.setScope("both");
            } else {
                layuiTableColumnMetaDesc.setEdit(false);
                layuiTableColumnMetaDesc.setScope("both");
            }
        }

        if (hiddenFields.contains(fieldName)) {
            layuiTableColumnMetaDesc.setHide(true);
        }

        if(onlyEditFields.contains(fieldName)){
            layuiTableColumnMetaDesc.setScope("edit");
        }

        return layuiTableColumnMetaDesc;
    }

    //根据配置生成LayuiTableMetaDesc
    private LayuiTableColumnMetaDesc buildLayuiTableColumnDesc(InnerListViewConfiguration listViewConfiguration, Field field, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        String fieldName = field.getName();
        Map<String, HtmlElementDescriptor> elementDescriptorMap = listViewConfiguration.getElementDescriptorMap(); //所有的配置标签列表
        //fieldNames包含所有的fieldName和otherFieldName
        Set<String> fieldNames = Stream.of(
                        elementDescriptorMap.values().stream().filter(t -> !t.getTagType().equals(HtmlElementTagTypeEnum.INPUT.codeName())).map(t -> t.getColumn().getJavaProperty()),
                        elementDescriptorMap.values().stream().filter(t -> !t.getTagType().equals(HtmlElementTagTypeEnum.INPUT.codeName())).map(HtmlElementDescriptor::getOtherFieldName)
                ).flatMap(stringStream -> stringStream)
                .collect(Collectors.toSet());
        //获取displayFields，包含所有的fieldName和otherFieldName
        Set<String> displayFields = new HashSet<>(listViewConfiguration.getDefaultDisplayFields());
        if (!displayFields.isEmpty()) {
            displayFields.forEach(displayField -> {
                if (elementDescriptorMap.containsKey(displayField)) {
                    HtmlElementDescriptor htmlElementDescriptor = elementDescriptorMap.get(displayField);
                    displayFields.add(htmlElementDescriptor.getColumn().getJavaProperty());
                    displayFields.add(htmlElementDescriptor.getOtherFieldName());
                }
            });
            if (!displayFields.contains(fieldName)) {
                return null;
            }
        }

        boolean enableEdit = enableEdit(listViewConfiguration, fieldName);
        boolean defaultHidden = listViewConfiguration.getDefaultHiddenFields().contains(fieldName);

        LayuiTableColumnMetaDesc layuiTableColumnMetaDesc = getLayuiTableColumnMetaDesc(listViewConfiguration, field, defaultHidden, introspectedTable,introspectedColumn);

        if (fieldNames.contains(field.getName())) {
            elementDescriptorMap.values().stream()
                    .filter(t -> (t.getColumn().getJavaProperty().equals(field.getName()) || t.getOtherFieldName().equals(field.getName())))
                    .findFirst()
                    .ifPresent(htmlElementDescriptor -> {
                        if (HtmlElementTagTypeEnum.SELECT.codeName().equals(htmlElementDescriptor.getTagType())) {
                            if (htmlElementDescriptor.getOtherFieldName().equals(field.getName())) {
                                if (enableEdit) {
                                    layuiTableColumnMetaDesc.setEditor(htmlElementDescriptor.getTagType());
                                    layuiTableColumnMetaDesc.setEdit(true);
                                    layuiTableColumnMetaDesc.setTemplet("#TPL-inner-" + htmlElementDescriptor.getOtherFieldName());
                                    layuiTableColumnMetaDesc.setScope("both");
                                } else {
                                    layuiTableColumnMetaDesc.setEdit(false);
                                    layuiTableColumnMetaDesc.setScope("both");
                                }
                            } else {
                                layuiTableColumnMetaDesc.setHide(true);
                            }
                        } else {
                            if (enableEdit(listViewConfiguration, htmlElementDescriptor.getColumn().getJavaProperty())) {
                                if (htmlElementDescriptor.getColumn().getJavaProperty().equals(field.getName())) {
                                    layuiTableColumnMetaDesc.setEditor(htmlElementDescriptor.getTagType());
                                    layuiTableColumnMetaDesc.setEdit(true);
                                    layuiTableColumnMetaDesc.setTemplet("#TPL-inner-" + htmlElementDescriptor.getColumn().getJavaProperty());
                                    layuiTableColumnMetaDesc.setScope("edit");
                                } else {
                                    layuiTableColumnMetaDesc.setEdit(false);
                                    layuiTableColumnMetaDesc.setScope("readonly");
                                }
                            } else {
                                if (htmlElementDescriptor.getOtherFieldName().equals(field.getName())) {
                                    layuiTableColumnMetaDesc.setEdit(false);
                                    layuiTableColumnMetaDesc.setScope("both");
                                } else {
                                    layuiTableColumnMetaDesc.setHide(true);
                                }
                            }
                        }
                    });
        } else if (enableEdit) {
            if (introspectedColumn != null && (introspectedColumn.isJdbcCharacterColumn() && introspectedColumn.getLength() > 255 || introspectedColumn.isBLOBColumn())) {
                layuiTableColumnMetaDesc.setEditor("textarea");
                layuiTableColumnMetaDesc.setEdit(true);
            } else if (isDateType(field.getType())) {
                layuiTableColumnMetaDesc.setEdit(false);
                layuiTableColumnMetaDesc.setEditor("date");
                layuiTableColumnMetaDesc.setTemplet("#TPL-inner-" + field.getName());
            } else {
                layuiTableColumnMetaDesc.setEdit(true);
                layuiTableColumnMetaDesc.setEditor("text");
            }
            layuiTableColumnMetaDesc.setScope("both");
        } else {
            layuiTableColumnMetaDesc.setEdit(false);
            layuiTableColumnMetaDesc.setScope("both");
        }
        return layuiTableColumnMetaDesc;
    }

    private boolean enableEdit(InnerListViewConfiguration listViewConfiguration, String fieldName) {
        List<String> editFields = listViewConfiguration.getEnableEditFields();
        if (editFields.isEmpty()) {
            return !listViewConfiguration.getReadonlyFields().contains(fieldName);
        } else {
            return editFields.contains(fieldName) && !listViewConfiguration.getReadonlyFields().contains(fieldName);
        }
    }

    private LayuiTableColumnMetaDesc getLayuiTableColumnMetaDesc(InnerListViewConfiguration listViewConfiguration, Field field, boolean defaultHidden, IntrospectedTable introspectedTable,IntrospectedColumn introspectedColumn) {
        //生成@LayuiTableColumnMeta注解
        LayuiTableColumnMetaDesc layuiTableColumnMetaDesc = new LayuiTableColumnMetaDesc();
        layuiTableColumnMetaDesc.setValue(listViewConfiguration.getListKey());
        layuiTableColumnMetaDesc.setHide(defaultHidden);
        //如果指定了显示范围则更新序号
        if (!listViewConfiguration.getDefaultDisplayFields().isEmpty()) {
            layuiTableColumnMetaDesc.setOrder(getOrder(field, listViewConfiguration, introspectedTable));
        }
        listViewConfiguration.getListColumnConfigurations().stream().filter(listColumn -> listColumn.getField().equals(field.getName())).findFirst().ifPresent(listColumn -> {
            if (listColumn.getLabel() != null) {
                layuiTableColumnMetaDesc.setLabel(listColumn.getLabel());
            }
            if (listColumn.getMinWidth() != null) {
                layuiTableColumnMetaDesc.setMinWidth(listColumn.getMinWidth());
            }
            if (listColumn.getAlign() != null) {
                layuiTableColumnMetaDesc.setAlign(listColumn.getAlign());
            }
            if (listColumn.getFixed() != null) {
                layuiTableColumnMetaDesc.setFixed(listColumn.getField());
            }
        });
        //设置rules
        String rules = VueFormGenerateUtil.innerListItemRules(listViewConfiguration, introspectedColumn);
        if (stringHasValue(rules)) {
            layuiTableColumnMetaDesc.addImports("com.vgosoft.core.annotation.VueFormItemRule");
            layuiTableColumnMetaDesc.setRules(rules);
        }
        return layuiTableColumnMetaDesc;
    }

    private InnerListEditTemplate buildInnerListEditTemplate(InnerListViewConfiguration listViewConfiguration, Field field, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {

        Map<String, HtmlElementDescriptor> elementDescriptorMap = listViewConfiguration.getElementDescriptorMap();
        if (!elementDescriptorMap.containsKey(field.getName())) {
            if (isDateType(field.getType())) {
                InnerListEditTemplate innerListEditTemplate = new InnerListEditTemplate();
                innerListEditTemplate.setFieldName(field.getName());
                innerListEditTemplate.setTemplate("TPL-inner-" + field.getName());
                innerListEditTemplate.setThisFieldName(field.getName());
                innerListEditTemplate.setOtherFieldName(field.getName());
                innerListEditTemplate.setDataType(field.getName());
                innerListEditTemplate.setFieldType(getDateformat(field.getType().getShortNameWithoutTypeArguments()));
                innerListEditTemplate.setType("date");
                if (introspectedColumn != null) {
                    String dateType = Mb3GenUtil.getDateType(null, introspectedColumn);
                    innerListEditTemplate.setDateType(dateType);
                    String dateFormat = Mb3GenUtil.getDateFormat(null, introspectedColumn);
                    innerListEditTemplate.setDateFormat(dateFormat);
                } else {
                    innerListEditTemplate.setDateType("date");
                    innerListEditTemplate.setDateFormat("yyyy-MM-dd");
                }
                return innerListEditTemplate;
            }
        } else {
            HtmlElementDescriptor htmlElementDescriptor = elementDescriptorMap.get(field.getName());
            //创建模版
            InnerListEditTemplate innerListEditTemplate = new InnerListEditTemplate();
            innerListEditTemplate.setListKey(htmlElementDescriptor.getListKey());
            innerListEditTemplate.setFieldName(field.getName());
            innerListEditTemplate.setFieldType(field.getType().getShortNameWithoutTypeArguments());
            innerListEditTemplate.setTitle(field.getRemark());
            innerListEditTemplate.setThisFieldName(htmlElementDescriptor.getColumn().getJavaProperty());
            innerListEditTemplate.setOtherFieldName(htmlElementDescriptor.getOtherFieldName());
            innerListEditTemplate.setTemplate("TPL-inner-" + field.getName());
            innerListEditTemplate.setType(htmlElementDescriptor.getTagType());
            innerListEditTemplate.setCallback(htmlElementDescriptor.getCallback());
            innerListEditTemplate.setListViewClass(htmlElementDescriptor.getListViewClass());
            innerListEditTemplate.setDataType(htmlElementDescriptor.getDataSource());
            String dataUrl = parseDataUrl(htmlElementDescriptor, introspectedTable);
            innerListEditTemplate.setDataUrl(dataUrl);
            HtmlElementTagTypeEnum tagTypeEnum = HtmlElementTagTypeEnum.ofCodeName(htmlElementDescriptor.getTagType());
            if (tagTypeEnum != null) {
                switch (tagTypeEnum) {
                    case SELECT:
                        //如果是select指定fileName、模版标识为otherFieldName
                        innerListEditTemplate.setTemplate("TPL-inner-" + htmlElementDescriptor.getOtherFieldName());
                        innerListEditTemplate.setFieldName(htmlElementDescriptor.getOtherFieldName());
                        break;
                    case DATE:
                        String dateType = Mb3GenUtil.getDateType(htmlElementDescriptor, htmlElementDescriptor.getColumn());
                        innerListEditTemplate.setDateType(dateType);
                        String dateFormat = Mb3GenUtil.getDateFormat(htmlElementDescriptor, htmlElementDescriptor.getColumn());
                        innerListEditTemplate.setDateFormat(dateFormat);
                        break;
                }
            }
            return innerListEditTemplate;
        }
        return null;
    }

    private void addLayuiTableColumnMeta(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        List<InnerListViewConfiguration> innerListViewConfigurationList = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations();
        innerListViewConfigurationList.forEach(listViewConfiguration -> {
            LayuiTableColumnMetaDesc layuiTableColumnMetaDesc = buildVxeTableColumnDesc(listViewConfiguration, field, introspectedColumn, introspectedTable);
            if (layuiTableColumnMetaDesc != null) {
                OverrideListColumnProps(field, listViewConfiguration, layuiTableColumnMetaDesc);
                String annotation = layuiTableColumnMetaDesc.toAnnotation();
                if (!field.getAnnotations().contains(annotation)) {
                    field.addAnnotation(annotation);
                    topLevelClass.addImportedTypes(layuiTableColumnMetaDesc.getImportedTypes());
                }
            }
            InnerListEditTemplate innerListEditTemplate = buildInnerListEditTemplate(listViewConfiguration, field, introspectedColumn, introspectedTable);
            if (innerListEditTemplate != null) {
                listViewConfiguration.getInnerListEditTemplate().add(innerListEditTemplate);
            }
        });
    }

    private static void OverrideListColumnProps(Field field, InnerListViewConfiguration listViewConfiguration, LayuiTableColumnMetaDesc layuiTableColumnMetaDesc) {
        //根据配置设置宽度及其他属性
        ListColumnConfiguration columnConfiguration = listViewConfiguration.getListColumnConfigurations().stream()
                .filter(listColumn -> listColumn.getField().equals(field.getName())).findFirst().orElse(null);
        if (columnConfiguration != null) {
            if (columnConfiguration.getWidth() != null) {
                layuiTableColumnMetaDesc.setWidth(columnConfiguration.getWidth());
            }
            if (columnConfiguration.getMinWidth() != null) {
                layuiTableColumnMetaDesc.setMinWidth(columnConfiguration.getMinWidth());
            }
            if (columnConfiguration.getFixed() != null) {
                layuiTableColumnMetaDesc.setFixed(columnConfiguration.getFixed());
            }
            if (columnConfiguration.getAlign() != null) {
                layuiTableColumnMetaDesc.setAlign(columnConfiguration.getAlign());
            }
            if (columnConfiguration.getLabel() != null) {
                layuiTableColumnMetaDesc.setLabel(columnConfiguration.getLabel());
            }
        } else if (layuiTableColumnMetaDesc.getEditor() != null) { //根据指定宽度
            switch (layuiTableColumnMetaDesc.getEditor()) {
                case "date":
                    switch (field.getType().getShortName()) {
                        case "LocalDateTime":
                            layuiTableColumnMetaDesc.setWidth("165");
                            break;
                        case "LocalTime":
                            layuiTableColumnMetaDesc.setWidth("90");
                            break;
                        default:
                            layuiTableColumnMetaDesc.setWidth("105");
                            break;
                    }
                    break;
                case "checkbox":
                case "switch":
                    layuiTableColumnMetaDesc.setWidth("90");
                    break;
                case "dropdownlist":
                case "textarea":
                case "select":
                    layuiTableColumnMetaDesc.setWidth("125");
                    break;
                default:
                    layuiTableColumnMetaDesc.setWidth("");
                    break;
            }
        }
    }

    private boolean isDateType(FullyQualifiedJavaType fieldType) {
        String shortName = fieldType.getShortName();
        return shortName.equalsIgnoreCase("LocalDateTime")
                || shortName.equalsIgnoreCase("LocalDate")
                || shortName.equalsIgnoreCase("LocalTime")
                || shortName.equalsIgnoreCase("Date");
    }

    private String getDateformat(String javaType) {
        switch (javaType) {
            case "LocalDateTime":
                return "datetime";
            case "LocalTime":
                return "time";
            default:
                return "date";
        }
    }

    private String parseDataUrl(HtmlElementDescriptor htmlElementDescriptor, IntrospectedTable introspectedTable) {
        String columnName = htmlElementDescriptor.getName();
        String dataSource = htmlElementDescriptor.getDataSource();
        String dataUrl = htmlElementDescriptor.getDataUrl();
        if (stringHasValue(dataUrl)) {
            return dataUrl;
        }
        AtomicReference<String> url = new AtomicReference<>("");
        if (stringHasValue(dataSource) && stringHasValue(columnName)) {
            introspectedTable.getColumn(columnName).ifPresent(introspectedColumn -> {
                switch (dataSource) {
                    case "DictData":
                        url.set("/system/sys-dict-data-impl/option/" + introspectedColumn.getJavaProperty());
                        break;
                    case "DictEnum":
                        url.set("/system/enum/options/" + htmlElementDescriptor.getEnumClassName());
                        break;
                    case "Department":
                    case "User":
                    case "Oranization":
                    case "Post":
                        url.set("");
                        break;
                    default:
                        url.set(introspectedColumn.getJavaProperty());
                        break;
                }

            });
        }
        return url.get();
    }

    /**
     * 获取LayuiTableMetaDesc
     *
     * @param introspectedTable     表
     * @param listViewConfiguration 列表配置
     * @return LayuiTableMetaDesc layuiTableMetaDesc
     */
    private LayuiTableMetaDesc getLayuiTableMetaDesc(IntrospectedTable introspectedTable, InnerListViewConfiguration listViewConfiguration, TopLevelClass topLevelClass) {
        LayuiTableMetaDesc layuiTableMetaDesc = new LayuiTableMetaDesc();
        layuiTableMetaDesc.setValue(listViewConfiguration.getListKey());
        layuiTableMetaDesc.setTitle(introspectedTable.getRemarks(true));
        layuiTableMetaDesc.setSize(listViewConfiguration.getSize());
        layuiTableMetaDesc.setIndexColumn(listViewConfiguration.getIndexColumn());
        layuiTableMetaDesc.setActionColumn(listViewConfiguration.getActionColumn());
        layuiTableMetaDesc.setIndexColumnFixed(listViewConfiguration.getIndexColumnFixed());
        layuiTableMetaDesc.setActionColumnFixed(listViewConfiguration.getActionColumnFixed());
        layuiTableMetaDesc.setToolbar(listViewConfiguration.getToolbar());
        layuiTableMetaDesc.setTotalRow(listViewConfiguration.isTotalRow());
        layuiTableMetaDesc.setTotalFields(listViewConfiguration.getTotalFields());
        layuiTableMetaDesc.setTotalText(listViewConfiguration.getTotalText());
        layuiTableMetaDesc.setEnablePage(listViewConfiguration.isEnablePager()?"true":"false");
        layuiTableMetaDesc.setDefaultToolbar(listViewConfiguration.getDefaultToolbar());
        layuiTableMetaDesc.setParentMenuId(listViewConfiguration.getParentMenuId());
        layuiTableMetaDesc.setViewMenuElIcon(listViewConfiguration.getViewMenuElIcon());
        layuiTableMetaDesc.setCategoryTreeUrl(listViewConfiguration.getCategoryTreeUrl());
        layuiTableMetaDesc.setCategoryTreeMultiple(listViewConfiguration.isCategoryTreeMultiple());
        layuiTableMetaDesc.setUiFrameType(listViewConfiguration.getUiFrameType());
        layuiTableMetaDesc.setTableType(listViewConfiguration.getTableType());
        layuiTableMetaDesc.setHeight(listViewConfiguration.getHeight());
        layuiTableMetaDesc.setWidth(listViewConfiguration.getWidth());
        layuiTableMetaDesc.setEven(listViewConfiguration.isEven());
        layuiTableMetaDesc.setDefaultFilterExpr(listViewConfiguration.getDefaultFilterExpr());
        layuiTableMetaDesc.setShowRowNumber(listViewConfiguration.isShowRowNumber());
        layuiTableMetaDesc.setShowActionColumn(listViewConfiguration.getShowActionColumn());
        layuiTableMetaDesc.setEditFormIn(listViewConfiguration.getEditFormIn());
        layuiTableMetaDesc.setDetailFormIn(listViewConfiguration.getDetailFormIn());
          layuiTableMetaDesc.setEditableFields(listViewConfiguration.getEnableEditFields());


        //querys
        if (!listViewConfiguration.getQueryColumns().isEmpty()) {
            //按列名分组
            Map<String, List<QueryColumnConfiguration>> listMap = listViewConfiguration.getQueryColumnConfigurations().stream().collect(Collectors.groupingBy(QueryColumnConfiguration::getColumn));
            //去重,转换为CompositeQueryDesc
            List<CompositeQueryDesc> queryDesc = listViewConfiguration.getQueryColumns().stream().distinct().map(columnName -> {
                if (listMap.containsKey(columnName)) {
                    return CompositeQueryDesc.create(listMap.get(columnName).get(0), introspectedTable);
                } else {
                    if (introspectedTable.getColumn(columnName).isPresent()) {
                        return CompositeQueryDesc.create(introspectedTable.getColumn(columnName).get());
                    } else {
                        return null;
                    }
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            //更新顺序号，order
            for (int i = 0; i < queryDesc.size(); i++) {
                CompositeQueryDesc compositeQueryDesc = queryDesc.get(i);
                compositeQueryDesc.setOrder(i + 1);
            }
            //转换为注解
            String[] array = queryDesc.stream().map(CompositeQueryDesc::toAnnotation).toArray(String[]::new);
            layuiTableMetaDesc.setQuerys(array);
            //导入类型
            topLevelClass.addImportedTypes(queryDesc.stream().flatMap(q -> q.getImportedTypes().stream()).collect(Collectors.toSet()));
        }

        //filters
        if (!listViewConfiguration.getFilterColumns().isEmpty()) {
            //按列名分组
            Map<String, List<QueryColumnConfiguration>> listMap = listViewConfiguration.getQueryColumnConfigurations().stream().collect(Collectors.groupingBy(QueryColumnConfiguration::getColumn));
            //去重,转换为CompositeQueryDesc
            List<String> filterColumns = listViewConfiguration.getFilterColumns();
            if (filterColumns.isEmpty()) {
                filterColumns.addAll(introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration().getFilterColumns());
            }
            List<CompositeQueryDesc> queryDesc = listViewConfiguration.getFilterColumns().stream().distinct().map(columnName -> {
                if (listMap.containsKey(columnName)) {
                    return CompositeQueryDesc.create(listMap.get(columnName).get(0), introspectedTable);
                } else {
                    if (introspectedTable.getColumn(columnName).isPresent()) {
                        return CompositeQueryDesc.create(introspectedTable.getColumn(columnName).get());
                    } else {
                        return null;
                    }
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            //更新顺序号，order
            for (int i = 0; i < queryDesc.size(); i++) {
                CompositeQueryDesc compositeQueryDesc = queryDesc.get(i);
                compositeQueryDesc.setOrder(i + 1);
            }
            //转换为注解
            String[] array = queryDesc.stream().map(CompositeQueryDesc::toAnnotation).toArray(String[]::new);
            layuiTableMetaDesc.setFilters(array);
            //导入类型
            topLevelClass.addImportedTypes(queryDesc.stream().flatMap(q -> q.getImportedTypes().stream()).collect(Collectors.toSet()));
        }
        return layuiTableMetaDesc;
    }

    private int getOrder(Field field, InnerListViewConfiguration listViewConfiguration, IntrospectedTable introspectedTable) {
        final List<String> fieldNames = listViewConfiguration.getDefaultDisplayFields();
        if (fieldNames.isEmpty()) {
            fieldNames.addAll(introspectedTable.getAllColumns().stream().map(IntrospectedColumn::getJavaProperty).collect(Collectors.toList()));
        }
        //定义一个原子性int变量
        AtomicInteger orderNo = new AtomicInteger(0);
        if (fieldNames.contains(field.getName())) {
            orderNo.set(fieldNames.indexOf(field.getName()) + 100);
        } else {
            Map<String, HtmlElementDescriptor> elementDescriptorMap = listViewConfiguration.getElementDescriptorMap();
            elementDescriptorMap.values().stream().filter(t -> t.getOtherFieldName().equals(field.getName())).findFirst().ifPresent(t -> {
                if (fieldNames.contains(t.getColumn().getJavaProperty())) {
                    orderNo.set(fieldNames.indexOf(t.getColumn().getJavaProperty()) + 100);
                }
            });
        }
        return orderNo.get();
    }
}
