package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.freeMaker.html.layui.InnerListEditTemplate;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.InnerListViewConfiguration;
import org.mybatis.generator.config.ListColumnConfiguration;
import org.mybatis.generator.config.VOViewGeneratorConfiguration;
import org.mybatis.generator.custom.HtmlElementTagTypeEnum;
import org.mybatis.generator.custom.annotations.LayuiTableColumnMetaDesc;
import org.mybatis.generator.custom.annotations.LayuiTableMetaDesc;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            LayuiTableMetaDesc layuiTableMetaDesc = getLayuiTableMetaDesc(introspectedTable, listViewConfiguration);
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

    //根据配置生成LayuiTableMetaDesc
    private LayuiTableColumnMetaDesc buildLayuiTableColumnDesc(InnerListViewConfiguration listViewConfiguration, Field field, IntrospectedColumn introspectedColumn,IntrospectedTable introspectedTable) {

        Map<String, HtmlElementDescriptor> elementDescriptorMap = listViewConfiguration.getElementDescriptorMap(); //所有的配置标签列表
        Set<String> fieldNames = Stream.of(elementDescriptorMap.values().stream().filter(t->!t.getTagType().equals(HtmlElementTagTypeEnum.INPUT.getCode())).map(t -> t.getColumn().getJavaProperty()),
                        elementDescriptorMap.values().stream().filter(t->!t.getTagType().equals(HtmlElementTagTypeEnum.INPUT.getCode())).map(HtmlElementDescriptor::getOtherFieldName))
                .flatMap(stringStream -> stringStream)
                .collect(Collectors.toSet());
        String fieldName = field.getName();

        Set<String> displayFields = new HashSet<>(listViewConfiguration.getDefaultDisplayFields());
        if (!listViewConfiguration.getDefaultDisplayFields().isEmpty()) {
            listViewConfiguration.getDefaultDisplayFields()
                    .forEach(displayField -> {
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

        LayuiTableColumnMetaDesc layuiTableColumnMetaDesc = getLayuiTableColumnMetaDesc(listViewConfiguration, field, defaultHidden,introspectedTable);
        if (fieldNames.contains(field.getName())) {
            elementDescriptorMap.values().stream()
                    .filter(t -> (t.getColumn().getJavaProperty().equals(field.getName()) || t.getOtherFieldName().equals(field.getName())))
                    .findFirst()
                    .ifPresent(htmlElementDescriptor -> {
                        if (HtmlElementTagTypeEnum.SELECT.getCode().equals(htmlElementDescriptor.getTagType())) {
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
                                }else{
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
        return layuiTableColumnMetaDesc.isHide() ? null : layuiTableColumnMetaDesc;
    }

    private boolean enableEdit(InnerListViewConfiguration listViewConfiguration, String fieldName) {
        return listViewConfiguration.getEnableEditFields().contains(fieldName) || !listViewConfiguration.getReadonlyFields().contains(fieldName);
    }

    private LayuiTableColumnMetaDesc getLayuiTableColumnMetaDesc(InnerListViewConfiguration listViewConfiguration, Field field, boolean defaultHidden, IntrospectedTable introspectedTable) {
        //生成@LayuiTableColumnMeta注解
        LayuiTableColumnMetaDesc layuiTableColumnMetaDesc = new LayuiTableColumnMetaDesc();
        layuiTableColumnMetaDesc.setValue(listViewConfiguration.getListKey());
        layuiTableColumnMetaDesc.setHide(defaultHidden);
        //如果指定了显示范围则更新序号
        if (!listViewConfiguration.getDefaultDisplayFields().isEmpty()) {
            layuiTableColumnMetaDesc.setOrder(getOrder(field, listViewConfiguration,introspectedTable));
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
            HtmlElementTagTypeEnum tagTypeEnum = HtmlElementTagTypeEnum.getEnum(htmlElementDescriptor.getTagType());
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
        introspectedTable.getTableConfiguration()
                .getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations()
                .forEach(listViewConfiguration -> {
                    LayuiTableColumnMetaDesc layuiTableColumnMetaDesc = buildLayuiTableColumnDesc(listViewConfiguration, field, introspectedColumn,introspectedTable);

                    //根据配置设置宽度及其他属性
                    ListColumnConfiguration columnConfiguration = listViewConfiguration.getListColumnConfigurations().stream()
                            .filter(listColumn -> listColumn.getField().equals(field.getName()))
                            .findFirst().orElse(null);
                    if (layuiTableColumnMetaDesc != null) {
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
                                    layuiTableColumnMetaDesc.setWidth("80");
                                    break;
                            }
                        }
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
    private LayuiTableMetaDesc getLayuiTableMetaDesc(IntrospectedTable introspectedTable, InnerListViewConfiguration listViewConfiguration) {
        LayuiTableMetaDesc layuiTableMetaDesc = new LayuiTableMetaDesc();
        layuiTableMetaDesc.setValue(listViewConfiguration.getListKey());
        layuiTableMetaDesc.setDefaultToolbar(listViewConfiguration.getDefaultToolbar());
        layuiTableMetaDesc.setWidth(listViewConfiguration.getWidth());
        layuiTableMetaDesc.setHeight(listViewConfiguration.getHeight());
        layuiTableMetaDesc.setTotalRow(listViewConfiguration.isTotalRow());
        layuiTableMetaDesc.setEnablePage(listViewConfiguration.getEnablePage());
        layuiTableMetaDesc.setTitle(introspectedTable.getRemarks(true));
        layuiTableMetaDesc.setSkin(listViewConfiguration.getSkin());
        layuiTableMetaDesc.setSize(listViewConfiguration.getSize());
        layuiTableMetaDesc.setEven(listViewConfiguration.isEven());
        layuiTableMetaDesc.setToolbar(listViewConfiguration.getToolbar());
        layuiTableMetaDesc.setIndexColumn(listViewConfiguration.getIndexColumn());
        layuiTableMetaDesc.setActionColumn(listViewConfiguration.getActionColumn());
        return layuiTableMetaDesc;
    }

    private int getOrder(Field field, InnerListViewConfiguration listViewConfiguration,IntrospectedTable introspectedTable) {
        final List<String> fieldNames = listViewConfiguration.getDefaultDisplayFields();
        if (fieldNames.isEmpty()){
            fieldNames.addAll(introspectedTable.getAllColumns().stream().map(IntrospectedColumn::getJavaProperty).collect(Collectors.toList()));
        }
        //定义一个原子性int变量
        AtomicInteger orderNo = new AtomicInteger(0);
        if (fieldNames.contains(field.getName())) {
            orderNo.set(fieldNames.indexOf(field.getName()) + 100);
        }else{
            Map<String, HtmlElementDescriptor> elementDescriptorMap = listViewConfiguration.getElementDescriptorMap();
            elementDescriptorMap.values().stream().filter(t->t.getOtherFieldName().equals(field.getName())).findFirst().ifPresent(t->{
                if (fieldNames.contains(t.getColumn().getJavaProperty())) {
                    orderNo.set(fieldNames.indexOf(t.getColumn().getJavaProperty()) + 100);
                }
            });
        }
        return orderNo.get();
    }

}