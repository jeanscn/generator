package org.mybatis.generator.plugins;

import com.vgosoft.core.annotation.ViewFuzzyColumnMeta;
import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.view.ViewDefaultToolBarsEnum;
import com.vgosoft.core.constant.enums.view.ViewIndexColumnEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ViewVoUiFrameEnum;
import org.mybatis.generator.custom.annotations.*;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return true;
    }

    /**
     * VO抽象父类的ColumnMetaAnnotation
     */
    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (introspectedTable.getRules().isGenerateViewVO()) {
            VOViewGeneratorConfiguration viewConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
            //增加ViewMetaAnnotation
            ViewColumnMetaDesc viewColumnMetaDesc = ViewColumnMetaDesc.create(introspectedColumn, introspectedTable);
            // 列覆盖信息
            generateVoFieldCustom(field, viewConfiguration, viewColumnMetaDesc);
            updateOrder(field, introspectedTable, viewColumnMetaDesc);
            field.addAnnotation(viewColumnMetaDesc.toAnnotation());
            topLevelClass.addImportedTypes(viewColumnMetaDesc.getImportedTypes());
            //模糊查询列注解
            if (viewConfiguration.getFuzzyColumns().contains(introspectedColumn.getActualColumnName())) {
                ViewFuzzyColumnMetaDesc viewFuzzyColumnMetaDesc = new ViewFuzzyColumnMetaDesc(introspectedColumn.getActualColumnName(),introspectedColumn.getRemarks(true));
                field.addAnnotation(viewFuzzyColumnMetaDesc.toAnnotation());
                topLevelClass.addImportedTypes(viewFuzzyColumnMetaDesc.getImportedTypes());
            }
        }
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
        VOViewGeneratorConfiguration viewConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
        ViewColumnMetaDesc viewColumnMetaDesc;
        if (introspectedColumn != null) {
            viewColumnMetaDesc = ViewColumnMetaDesc.create(introspectedColumn, introspectedTable);
        } else {
            viewColumnMetaDesc = new ViewColumnMetaDesc(field, field.getRemark(), introspectedTable);
        }
        // 列覆盖信息
        generateVoFieldCustom(field, viewConfiguration, viewColumnMetaDesc);
        updateOrder(field, introspectedTable, viewColumnMetaDesc);
        field.addAnnotation(viewColumnMetaDesc.toAnnotation());
        topLevelClass.addImportedTypes(viewColumnMetaDesc.getImportedTypes());

        //模糊查询列注解
        if (introspectedColumn!=null && viewConfiguration.getFuzzyColumns().contains(introspectedColumn.getActualColumnName())) {
            ViewFuzzyColumnMetaDesc viewFuzzyColumnMetaDesc = new ViewFuzzyColumnMetaDesc(introspectedColumn.getActualColumnName(),introspectedColumn.getRemarks(true));
            field.addAnnotation(viewFuzzyColumnMetaDesc.toAnnotation());
            topLevelClass.addImportedTypes(viewFuzzyColumnMetaDesc.getImportedTypes());
        }
        return true;
    }

    private static void generateVoFieldCustom(Field field, VOViewGeneratorConfiguration viewConfiguration, ViewColumnMetaDesc viewColumnMetaDesc) {
        for (ViewFieldOverrideConfiguration config : viewConfiguration.getViewFieldOverrideConfigurations().stream().filter(config -> config.getFields().contains(field.getName())).collect(Collectors.toList())) {
            if (stringHasValue(config.getLabel())) {
                viewColumnMetaDesc.setTitle(config.getLabel());
            }
            if (stringHasValue(config.getWidth())) {
                viewColumnMetaDesc.setWidth(config.getWidth());
            }
            if (stringHasValue(config.getAlign())) {
                viewColumnMetaDesc.setAlign(config.getAlign());
            }
            if (stringHasValue(config.getFixed())) {
                viewColumnMetaDesc.setFixed(config.getFixed());
            }
            viewColumnMetaDesc.setOrderable(config.isSort() ?"true":"false");
            viewColumnMetaDesc.setHide(config.isHide());
            viewColumnMetaDesc.setEdit(config.isEdit());

            if (stringHasValue(config.getHeaderAlign())) {
                viewColumnMetaDesc.setHeaderAlign(config.getHeaderAlign());
            }

        }
    }

    private void addViewTableMeta(VOViewGeneratorConfiguration voViewGeneratorConfiguration, TopLevelClass viewVOClass, IntrospectedTable introspectedTable) {
        ViewTableMetaDesc viewTableMetaDesc = new ViewTableMetaDesc(introspectedTable);
        viewTableMetaDesc.setTableType(voViewGeneratorConfiguration.getTableType());

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
        //数据权限
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        List<String> orgTables = Arrays.asList("org_user", "org_team","org_role","org_organization","org_group","org_department");
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            viewTableMetaDesc.setDataFilterType(1);
        } else if ("wf_per_todo".equals(tableName) || "wf_per_done".equals(tableName)) {
            viewTableMetaDesc.setDataFilterType(2);
        } else if ("sys_per_unread".equals(tableName) || "sys_per_read".equals(tableName)) {
            viewTableMetaDesc.setDataFilterType(3);
        } else if (orgTables.contains(tableName)) {
            viewTableMetaDesc.setDataFilterType(5);
        } else {
            viewTableMetaDesc.setDataFilterType(0);
        }

        //toolbar
        if (!voViewGeneratorConfiguration.getToolbar().isEmpty()) {
            String collect = voViewGeneratorConfiguration.getToolbar().stream().map(s -> {
                //是否默认按钮
                ViewDefaultToolBarsEnum viewDefaultToolBarsEnum = ViewDefaultToolBarsEnum.ofCode(s);
                if (viewDefaultToolBarsEnum != null) {
                    HtmlButtonDesc htmlButtonDesc = new HtmlButtonDesc(viewDefaultToolBarsEnum.id());
                    if (stringHasValue(viewDefaultToolBarsEnum.elIcon())) {
                        htmlButtonDesc.setIcon(viewDefaultToolBarsEnum.elIcon());
                    }else{
                        htmlButtonDesc.setIcon(viewDefaultToolBarsEnum.icon());
                    }
                    return htmlButtonDesc.toAnnotation();
                } else {
                    HtmlButtonGeneratorConfiguration htmlButtonGeneratorConfiguration = voViewGeneratorConfiguration.getHtmlButtons().stream().filter(h -> h.getId().equals(s)).findFirst().orElse(null);
                    if (htmlButtonGeneratorConfiguration != null) {
                        HtmlButtonDesc htmlButtonDesc = HtmlButtonDesc.create(htmlButtonGeneratorConfiguration);
                        return htmlButtonDesc.toAnnotation();
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.joining(","));
            viewTableMetaDesc.setToolbarActions(collect);

        }
        //indexColumn
        ViewIndexColumnEnum viewIndexColumnEnum = ViewIndexColumnEnum.ofCode(voViewGeneratorConfiguration.getIndexColumn());
        if (viewIndexColumnEnum != null) {
            viewTableMetaDesc.setIndexColumn(viewIndexColumnEnum);
        }
        //actionColumn
        if (!voViewGeneratorConfiguration.getActionColumn().isEmpty()) {
            String collect = voViewGeneratorConfiguration.getActionColumn().stream().map(s -> {
                //是否默认按钮
                ViewDefaultToolBarsEnum viewDefaultToolBarsEnum = ViewDefaultToolBarsEnum.ofCode("ROW_"+s);
                if (viewDefaultToolBarsEnum != null) {
                    HtmlButtonDesc htmlButtonDesc = new HtmlButtonDesc(viewDefaultToolBarsEnum.id());
                    if (stringHasValue(viewDefaultToolBarsEnum.elIcon())) {
                        htmlButtonDesc.setIcon(viewDefaultToolBarsEnum.elIcon());
                    }else{
                        htmlButtonDesc.setIcon(viewDefaultToolBarsEnum.icon());
                    }
                    return htmlButtonDesc.toAnnotation();
                } else {
                    HtmlButtonGeneratorConfiguration htmlButtonGeneratorConfiguration = voViewGeneratorConfiguration.getHtmlButtons().stream().filter(h -> h.getId().equals(s)).findFirst().orElse(null);
                    if (htmlButtonGeneratorConfiguration != null) {
                        HtmlButtonDesc htmlButtonDesc = HtmlButtonDesc.create(htmlButtonGeneratorConfiguration);
                        return htmlButtonDesc.toAnnotation();
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.joining(","));
            viewTableMetaDesc.setActionColumn(collect);
        }
        //querys
        if (!voViewGeneratorConfiguration.getQueryColumns().isEmpty()) {
            //按列名分组
            Map<String, List<QueryColumnConfiguration>> listMap = voViewGeneratorConfiguration.getQueryColumnConfigurations().stream().collect(Collectors.groupingBy(QueryColumnConfiguration::getColumn));
            //去重,转换为CompositeQueryDesc
            List<CompositeQueryDesc> queryDesc = voViewGeneratorConfiguration.getQueryColumns().stream().distinct().map(columnName -> {
                if (listMap.containsKey(columnName)) {
                    return CompositeQueryDesc.create(listMap.get(columnName).get(0),introspectedTable);
                } else {
                    if (introspectedTable.getColumn(columnName).isPresent()) {
                        return CompositeQueryDesc.create(introspectedTable.getColumn(columnName).get());
                    }else{
                        return null;
                    }
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            //更新顺序号，order
            for (int i = 0; i < queryDesc.size(); i++) {
                queryDesc.get(i).setOrder(i + 1);
            }
            //转换为注解
            String[] array = queryDesc.stream().map(CompositeQueryDesc::toAnnotation).toArray(String[]::new);
            viewTableMetaDesc.setQuerys(array);
            //导入类型
            viewVOClass.addImportedTypes(queryDesc.stream().flatMap(q -> q.getImportedTypes().stream()).collect(Collectors.toSet()));
        }
        //filters
        if (!voViewGeneratorConfiguration.getFilterColumns().isEmpty()) {
            //按列名分组
            Map<String, List<QueryColumnConfiguration>> listMap = voViewGeneratorConfiguration.getQueryColumnConfigurations().stream().collect(Collectors.groupingBy(QueryColumnConfiguration::getColumn));
            //去重,转换为CompositeQueryDesc
            List<CompositeQueryDesc> queryDesc = voViewGeneratorConfiguration.getFilterColumns().stream().distinct().map(columnName -> {
                if (listMap.containsKey(columnName)) {
                    return CompositeQueryDesc.create(listMap.get(columnName).get(0),introspectedTable);
                } else {
                    if (introspectedTable.getColumn(columnName).isPresent()) {
                        return CompositeQueryDesc.create(introspectedTable.getColumn(columnName).get());
                    }else{
                        return null;
                    }
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            //更新顺序号，order
            for (int i = 0; i < queryDesc.size(); i++) {
                queryDesc.get(i).setOrder(i + 1);
            }
            //转换为注解
            String[] array = queryDesc.stream().map(CompositeQueryDesc::toAnnotation).toArray(String[]::new);
            viewTableMetaDesc.setFilters(array);
            //导入类型
            viewVOClass.addImportedTypes(queryDesc.stream().flatMap(q -> q.getImportedTypes().stream()).collect(Collectors.toSet()));
        }
        //columns
        if (!voViewGeneratorConfiguration.getIncludeColumns().isEmpty()) {
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
        if (!voViewGeneratorConfiguration.getExcludeColumns().isEmpty()) {
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


    private void updateOrder(Field field, IntrospectedTable introspectedTable, ViewColumnMetaDesc viewColumnMetaDesc) {
        //更新order
        if (introspectedTable.getRules().isGenerateViewVO()) {
            VOViewGeneratorConfiguration configuration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
            List<String> displayFields = configuration.getDefaultDisplayFields();
            viewColumnMetaDesc.setOrder(getOrder(field, displayFields));
        }
    }

    private int getOrder(Field field, List<String> fieldNames) {
        if (!fieldNames.isEmpty()) {
            if (fieldNames.contains(field.getName())) {
                return fieldNames.indexOf(field.getName()) + 100;
            }
        }
        return 0;
    }

}
