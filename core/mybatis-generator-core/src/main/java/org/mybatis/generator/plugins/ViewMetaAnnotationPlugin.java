package org.mybatis.generator.plugins;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.view.ViewActionColumnEnum;
import com.vgosoft.core.constant.enums.view.ViewIndexColumnEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.freeMaker.html.layui.InnerListEditTemplate;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.InnerListViewConfiguration;
import org.mybatis.generator.config.VOViewGeneratorConfiguration;
import org.mybatis.generator.custom.HtmlElementTagTypeEnum;
import org.mybatis.generator.custom.annotations.*;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getRootClass;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * 添加ViewMetaAnnotation
 */
public class ViewMetaAnnotationPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean voModelViewClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (introspectedTable.getRules().isGenerateViewVO()) {
            //增加ViewTableMetaAnnotation
            VOViewGeneratorConfiguration voViewGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
            addViewTableMeta(voViewGeneratorConfiguration, topLevelClass, introspectedTable);
        }
        if (introspectedTable.getRules().isGenerateInnerTable()) {
            //增加InnerTableMetaAnnotation
            InnerListViewConfiguration listViewConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration()
                    .getVoViewConfiguration().getInnerListViewConfigurations().get(0);
            LayuiTableMetaDesc layuiTableMetaDesc = new LayuiTableMetaDesc();
            layuiTableMetaDesc.setEven(listViewConfiguration.isEven());
            layuiTableMetaDesc.setEnablePage(listViewConfiguration.getEnablePage());
            layuiTableMetaDesc.setTotalRow(listViewConfiguration.isTotalRow());
            layuiTableMetaDesc.setHeight(listViewConfiguration.getHeight());
            layuiTableMetaDesc.setDefaultToolbar(listViewConfiguration.getDefaultToolbar());
            topLevelClass.addAnnotation(layuiTableMetaDesc.toAnnotation());
            topLevelClass.addImportedTypes(layuiTableMetaDesc.getImportedTypes());
        }
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (introspectedTable.getRules().isGenerateViewVO()) {
            //增加ViewMetaAnnotation
            ViewColumnMetaDesc viewColumnMetaDesc = ViewColumnMetaDesc.create(introspectedColumn, introspectedTable);
            updateOrder(field, introspectedTable, viewColumnMetaDesc);
            field.addAnnotation(viewColumnMetaDesc.toAnnotation());
            topLevelClass.addImportedTypes(viewColumnMetaDesc.getImportedTypes());
        }
        addLayuiTableColumnMeta(field, topLevelClass, introspectedColumn, introspectedTable);
        return true;
    }

    /**
     * viewVO类的ViewMetaAnnotation
     */
    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (!introspectedTable.getRules().isGenerateViewVO()) {
            return true;
        }
        ViewColumnMetaDesc viewColumnMetaDesc;
        if (introspectedColumn != null) {
            viewColumnMetaDesc = ViewColumnMetaDesc.create(introspectedColumn, introspectedTable);
        } else {
            viewColumnMetaDesc = new ViewColumnMetaDesc(field, field.getRemark(), introspectedTable);
        }
        updateOrder(field, introspectedTable, viewColumnMetaDesc);
        field.addAnnotation(viewColumnMetaDesc.toAnnotation());
        topLevelClass.addImportedTypes(viewColumnMetaDesc.getImportedTypes());

        addLayuiTableColumnMeta(field, topLevelClass, introspectedColumn, introspectedTable);
        return true;
    }

    private void updateOrder(Field field, IntrospectedTable introspectedTable, ViewColumnMetaDesc viewColumnMetaDesc) {
        //更新order
        if (introspectedTable.getRules().isGenerateViewVO()) {
            VOViewGeneratorConfiguration configuration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
            List<String> displayFields = configuration.getDefaultDisplayFields();
            viewColumnMetaDesc.setOrder(getOrder(field, displayFields));
        }
    }

    private int getOrder(Field field, List<String> fieldNames) {
        if (fieldNames.size() > 0) {
            if (fieldNames.contains(field.getName())) {
                return fieldNames.indexOf(field.getName()) + 100;
            }
        }
        return 0;
    }

    private void addViewTableMeta(VOViewGeneratorConfiguration voViewGeneratorConfiguration, TopLevelClass viewVOClass, IntrospectedTable introspectedTable) {
        ViewTableMetaDesc viewTableMetaDesc = new ViewTableMetaDesc(introspectedTable);
        //createUrl
        String createUrl = "";
        FullyQualifiedJavaType rootType = new FullyQualifiedJavaType(getRootClass(introspectedTable));
        if (stringHasValue(rootType.getShortName())) {
            if (EntityAbstractParentEnum.ofCode(rootType.getShortName()) == null
                    || (EntityAbstractParentEnum.ofCode(rootType.getShortName()) != null
                    && EntityAbstractParentEnum.ofCode(rootType.getShortName()).scope() != 1)) {
                createUrl = String.join("/"
                        , Mb3GenUtil.getControllerBaseMappingPath(introspectedTable)
                        , "view");
            }
        }
        if (stringHasValue(createUrl)) {
            viewTableMetaDesc.setCreateUrl(createUrl);
        }
        //dataUrl
        viewTableMetaDesc.setDataUrl(String.join("/"
                , Mb3GenUtil.getControllerBaseMappingPath(introspectedTable)
                , "getdtdata"));

        //indexColumn
        ViewIndexColumnEnum viewIndexColumnEnum = ViewIndexColumnEnum.ofCode(voViewGeneratorConfiguration.getIndexColumn());
        if (viewIndexColumnEnum != null) {
            viewTableMetaDesc.setIndexColumn(viewIndexColumnEnum);
        }
        //actionColumn
        if (voViewGeneratorConfiguration.getActionColumn().size() > 0) {
            ViewActionColumnEnum[] viewActionColumnEnums = voViewGeneratorConfiguration.getActionColumn().stream()
                    .map(ViewActionColumnEnum::ofCode)
                    .filter(Objects::nonNull)
                    .distinct().toArray(ViewActionColumnEnum[]::new);
            viewTableMetaDesc.setActionColumn(viewActionColumnEnums);
        }
        //querys
        if (voViewGeneratorConfiguration.getQueryColumns().size() > 0) {
            String[] strings = voViewGeneratorConfiguration.getQueryColumns().stream()
                    .distinct()
                    .map(f -> introspectedTable.getColumn(f).orElse(null))
                    .filter(Objects::nonNull)
                    .map(c -> CompositeQueryDesc.create(c).toAnnotation())
                    .toArray(String[]::new);
            viewTableMetaDesc.setQuerys(strings);
        }
        //columns
        if (voViewGeneratorConfiguration.getIncludeColumns().size() > 0) {
            String[] strings = voViewGeneratorConfiguration.getIncludeColumns().stream()
                    .distinct()
                    .map(f -> introspectedTable.getColumn(f).orElse(null))
                    .filter(Objects::nonNull)
                    .map(c -> ViewColumnMetaDesc.create(c, introspectedTable).toAnnotation())
                    .toArray(String[]::new);
            viewTableMetaDesc.setColumns(strings);
        }
        if (stringHasValue(voViewGeneratorConfiguration.getCategoryTreeUrl())) {
            viewTableMetaDesc.setCategoryTreeUrl(voViewGeneratorConfiguration.getCategoryTreeUrl());
        }
        //ignoreFields
        if (voViewGeneratorConfiguration.getExcludeColumns().size() > 0) {
            String[] columns2 = voViewGeneratorConfiguration.getExcludeColumns().stream()
                    .distinct()
                    .map(f -> introspectedTable.getColumn(f).orElse(null))
                    .filter(Objects::nonNull)
                    .map(IntrospectedColumn::getJavaProperty)
                    .toArray(String[]::new);
            viewTableMetaDesc.setIgnoreFields(columns2);
        }
        //className
        viewTableMetaDesc.setClassName(viewVOClass.getType().getFullyQualifiedName());
        //restBasePath
        viewTableMetaDesc.setRestBasePath(Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));

        //构造ViewTableMeta
        viewVOClass.addAnnotation(viewTableMetaDesc.toAnnotation());
        viewVOClass.addImportedTypes(viewTableMetaDesc.getImportedTypes());
    }

    private void addLayuiTableColumnMeta(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (!introspectedTable.getRules().isGenerateInnerTable()) {
            return;
        }
        InnerListViewConfiguration listViewConfiguration = introspectedTable.getTableConfiguration()
                .getVoGeneratorConfiguration().getVoViewConfiguration()
                .getInnerListViewConfigurations().get(0);
        //如果有默认显示字段且字段不在默认显示字段中，字段不生成layuiTableColumnMeta
        if (!(listViewConfiguration.getDefaultDisplayFields().isEmpty() || listViewConfiguration.getDefaultDisplayFields().contains(field.getName()))) {
            return;
        }
        //如果有描述器且描述器为select类型，map的key属性字段不生成layuiTableColumnMeta
        Map<String, HtmlElementDescriptor> elementDescriptorMap = listViewConfiguration.getElementDescriptorMap();
        Set<String> mapFields = new HashSet<>();
        elementDescriptorMap.forEach((key, value) -> {
            if (!HtmlElementTagTypeEnum.INPUT.getCode().equals(value.getTagType())) {
                mapFields.add(key);
                mapFields.add(value.getOtherFieldName());
            }
        });
        if (elementDescriptorMap.containsKey(field.getName()) && HtmlElementTagTypeEnum.SELECT.getCode().equals(elementDescriptorMap.get(field.getName()).getTagType())) {
            return;
        }
        //如果是隐藏字段，不生成layuiTableColumnMeta
        Set<String> htmlHiddenFieldNames = new HashSet<>();
        if (listViewConfiguration.getDefaultDisplayFields().isEmpty()) {
            HtmlGeneratorConfiguration htmlGeneratorConfiguration = listViewConfiguration.getHtmlGeneratorConfiguration();
            htmlHiddenFieldNames = htmlGeneratorConfiguration != null ? htmlGeneratorConfiguration.getHiddenColumns()
                    .stream()
                    .map(IntrospectedColumn::getJavaProperty)
                    .collect(Collectors.toSet()) : listViewConfiguration.getDefaultHiddenFields();
        }
        if (htmlHiddenFieldNames.contains(field.getName())) {
            return;
        }

        LayuiTableColumnMetaDesc layuiTableColumnMetaDesc = new LayuiTableColumnMetaDesc();
        InnerListEditTemplate innerListEditTemplate = new InnerListEditTemplate();
        innerListEditTemplate.setFieldName(field.getName());
        innerListEditTemplate.setFieldType(field.getType().getShortNameWithoutTypeArguments());
        //如果指定了显示范围则更新序号
        if (!listViewConfiguration.getDefaultDisplayFields().isEmpty()) {
            layuiTableColumnMetaDesc.setOrder(getOrder(field, new ArrayList<>(listViewConfiguration.getDefaultDisplayFields())));
        }

        //判断是否允许编辑
        boolean enableEdit;
        List<String> enableEditFields = listViewConfiguration.getEnableEditFields();
        if (!enableEditFields.isEmpty()) {
            enableEdit = enableEditFields.contains(field.getName()) && !listViewConfiguration.getReadonlyColumnNames().contains(field.getName());
        } else {
            enableEdit = !listViewConfiguration.getReadonlyColumnNames().contains(field.getName());
        }

        if (mapFields.contains(field.getName())) {
            elementDescriptorMap.forEach((fieldName, htmlElementDescriptor) -> {
                if (field.getName().equals(htmlElementDescriptor.getOtherFieldName())) {
                    if (HtmlElementTagTypeEnum.SELECT.getCode().equals(htmlElementDescriptor.getTagType()) && enableEdit) {
                        layuiTableColumnMetaDesc.setEditor(htmlElementDescriptor.getTagType());
                        layuiTableColumnMetaDesc.setTemplet("#TPL-inner-" + htmlElementDescriptor.getOtherFieldName());
                        layuiTableColumnMetaDesc.setEdit(true);
                        innerListEditTemplate.setThisFieldName(htmlElementDescriptor.getColumn().getJavaProperty());
                        innerListEditTemplate.setOtherFieldName(htmlElementDescriptor.getOtherFieldName());
                        innerListEditTemplate.setType(htmlElementDescriptor.getTagType());
                        innerListEditTemplate.setTemplate("TPL-inner-" + htmlElementDescriptor.getOtherFieldName());
                        innerListEditTemplate.setDataType(htmlElementDescriptor.getDataSource());
                        String dataUrl = parseDataUrl(htmlElementDescriptor, introspectedTable);
                        innerListEditTemplate.setDataUrl(dataUrl);
                        innerListEditTemplate.setTitle(field.getRemark());
                    } else {
                        layuiTableColumnMetaDesc.setEdit(false);
                        layuiTableColumnMetaDesc.setScope("readonly");
                    }
                } else if (field.getName().equals(fieldName) && enableEdit) {
                    layuiTableColumnMetaDesc.setEditor(htmlElementDescriptor.getTagType());
                    layuiTableColumnMetaDesc.setTemplet("#TPL-inner-" + fieldName);
                    layuiTableColumnMetaDesc.setEdit(true);
                    layuiTableColumnMetaDesc.setScope("edit");
                    innerListEditTemplate.setThisFieldName(htmlElementDescriptor.getColumn().getJavaProperty());
                    innerListEditTemplate.setOtherFieldName(htmlElementDescriptor.getOtherFieldName());
                    innerListEditTemplate.setType(htmlElementDescriptor.getTagType());
                    innerListEditTemplate.setTemplate("TPL-inner-" + fieldName);
                    String dataUrl = parseDataUrl(htmlElementDescriptor, introspectedTable);
                    innerListEditTemplate.setDataUrl(dataUrl);
                    innerListEditTemplate.setTitle(field.getRemark());
                }
            });
        } else {
            if (enableEdit) {
                if (introspectedColumn != null
                        && (introspectedColumn.isJdbcCharacterColumn() && introspectedColumn.getLength() > 255
                        || introspectedColumn.isBLOBColumn())) {
                    layuiTableColumnMetaDesc.setEditor("textarea");
                    layuiTableColumnMetaDesc.setEdit(true);
                } else if (isDateType(field.getType())) {
                    layuiTableColumnMetaDesc.setEdit(false);
                    layuiTableColumnMetaDesc.setEditor("date");
                    layuiTableColumnMetaDesc.setTemplet("#TPL-inner-" + field.getName());
                    innerListEditTemplate.setThisFieldName(field.getName());
                    innerListEditTemplate.setType("date");
                    innerListEditTemplate.setTemplate("TPL-inner-" + field.getName());
                    innerListEditTemplate.setFieldType(getDateformat(field.getType().getShortNameWithoutTypeArguments()));
                } else {
                    layuiTableColumnMetaDesc.setEdit(true);
                    layuiTableColumnMetaDesc.setEditor("text");
                }
            } else {
                layuiTableColumnMetaDesc.setEdit(false);
            }
        }

        //根据编辑器类型指定默认宽度
        if (layuiTableColumnMetaDesc.getEditor() != null) {
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
        if (stringHasValue(innerListEditTemplate.getType())) {
            listViewConfiguration.getInnerListEditTemplate().add(innerListEditTemplate);
        }
        field.addAnnotation(layuiTableColumnMetaDesc.toAnnotation());
        topLevelClass.addImportedTypes(layuiTableColumnMetaDesc.getImportedTypes());
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

}
