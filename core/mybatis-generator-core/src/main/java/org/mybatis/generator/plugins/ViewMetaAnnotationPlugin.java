package org.mybatis.generator.plugins;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.view.ViewIndexColumnEnum;
import com.vgosoft.tool.core.VArrayUtil;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.annotations.*;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.*;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getRootClass;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * 添加ViewMetaAnnotation
 * @author cen_c
 */
public class ViewMetaAnnotationPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean voModelViewClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        if (introspectedTable.getRules().isGenerateViewVo()) {
            //增加ViewTableMetaAnnotation
            VoViewGeneratorConfiguration voViewGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
            addViewTableMeta(voViewGeneratorConfiguration, topLevelClass, introspectedTable);
        }
        return true;
    }

    /**
     * Vo抽象父类的ColumnMetaAnnotation
     */
    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        if (introspectedTable.getRules().isGenerateViewVo()) {
            VoViewGeneratorConfiguration viewConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
            //增加ViewMetaAnnotation
            ViewColumnMetaDesc viewColumnMetaDesc = ViewColumnMetaDesc.create(introspectedColumn, introspectedTable);
            // 列覆盖信息
            generateVoFieldCustom(field, viewConfiguration, viewColumnMetaDesc,introspectedTable);
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
     * viewVo类的ViewMetaAnnotation
     */
    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        if (!introspectedTable.getRules().isGenerateViewVo()) {
            return true;
        }
        VoViewGeneratorConfiguration viewConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
        ViewColumnMetaDesc viewColumnMetaDesc;
        if (introspectedColumn != null) {
            viewColumnMetaDesc = ViewColumnMetaDesc.create(introspectedColumn, introspectedTable);
        } else {
            viewColumnMetaDesc = new ViewColumnMetaDesc(field, field.getRemark(), introspectedTable);
        }
        // 列覆盖信息
        generateVoFieldCustom(field, viewConfiguration, viewColumnMetaDesc,introspectedTable);
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

    private void generateVoFieldCustom(Field field, VoViewGeneratorConfiguration viewConfiguration, ViewColumnMetaDesc viewColumnMetaDesc, AbstractIntrospectedTable introspectedTable) {
        List<ViewFieldOverrideConfiguration> viewFieldOverrides = viewConfiguration.getViewFieldOverrideConfigurations().stream()
                .filter(config -> config.getFields().contains(field.getName()))
                .collect(Collectors.toList());
        if (viewFieldOverrides.isEmpty()) {
            List<ViewFieldOverrideConfiguration> fieldOverrides = context.getReusableElementsConfiguration().getFieldOverrides();
            viewFieldOverrides = fieldOverrides.stream()
                    .filter(config -> config.getFields().contains(field.getName()))
                    .collect(Collectors.toList());
        }
        for (ViewFieldOverrideConfiguration config : viewFieldOverrides) {
            if (stringHasValue(config.getLabel())) {
                viewColumnMetaDesc.setTitle(config.getLabel());
            }
            if (stringHasValue(config.getWidth())) {
                viewColumnMetaDesc.setWidth(config.getWidth());
            }
            if (stringHasValue(config.getMinWidth())) {
                viewColumnMetaDesc.setMinWidth(config.getMinWidth());
            }
            if (stringHasValue(config.getHeaderAlign())) {
                viewColumnMetaDesc.setAlign(config.getHeaderAlign());
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

    private void addViewTableMeta(VoViewGeneratorConfiguration voViewGeneratorConfiguration, TopLevelClass viewVoClass, AbstractIntrospectedTable introspectedTable) {
        ViewTableMetaDesc viewTableMetaDesc = new ViewTableMetaDesc(introspectedTable);
        viewTableMetaDesc.setTableType(voViewGeneratorConfiguration.getTableType());
        viewTableMetaDesc.setTotalRow(voViewGeneratorConfiguration.isTotalRow());
        viewTableMetaDesc.setTotalFields(voViewGeneratorConfiguration.getTotalFields());
        viewTableMetaDesc.setTotalText(voViewGeneratorConfiguration.getTotalText());
        viewTableMetaDesc.setDetailFormIn(voViewGeneratorConfiguration.getDetailFormIn());
        viewTableMetaDesc.setEditFormIn(voViewGeneratorConfiguration.getEditFormIn());
        viewTableMetaDesc.setShowActionColumn(voViewGeneratorConfiguration.getShowActionColumn());
        viewTableMetaDesc.setDefaultSort(voViewGeneratorConfiguration.getDefaultSort());
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
            String parentMenuId = Mb3GenUtil.getParentMenuId(introspectedTable,voViewGeneratorConfiguration);
            List<String> annotations = Mb3GenUtil.genHtmlButtonAnnotationDescFromKeys(introspectedTable, voViewGeneratorConfiguration.getToolbar(), voViewGeneratorConfiguration.getHtmlButtons(), parentMenuId);
            viewTableMetaDesc.setToolbarActions(String.join(",", annotations));
        }
        //indexColumn
        ViewIndexColumnEnum viewIndexColumnEnum = ViewIndexColumnEnum.ofCode(voViewGeneratorConfiguration.getIndexColumn());
        if (viewIndexColumnEnum != null) {
            viewTableMetaDesc.setIndexColumn(viewIndexColumnEnum);
        }
        //actionColumn
        if (!voViewGeneratorConfiguration.getActionColumn().isEmpty()) {
            String parentMenuId = Mb3GenUtil.getParentMenuId(introspectedTable,voViewGeneratorConfiguration);
            List<String> annotations = Mb3GenUtil.genHtmlButtonAnnotationDescFromKeys(introspectedTable, voViewGeneratorConfiguration.getActionColumn(), voViewGeneratorConfiguration.getHtmlButtons(), parentMenuId);
            viewTableMetaDesc.setColumnActions(String.join(",", annotations));
        }
        //querys
        if (!voViewGeneratorConfiguration.getQueryColumns().isEmpty()) {
            //按列名分组
            Map<String, List<QueryColumnConfiguration>> listMap = voViewGeneratorConfiguration.getQueryColumnConfigurations().stream().collect(Collectors.groupingBy(QueryColumnConfiguration::getColumn));
            //去重,转换为CompositeQueryDesc
            List<CompositeQueryDesc> queryDesc = voViewGeneratorConfiguration.getQueryColumns().stream()
                    .distinct()
                    .map(columnName -> {
                        if (listMap.containsKey(columnName)) {
                            return CompositeQueryDesc.create(listMap.get(columnName).get(0), introspectedTable);
                        } else {
                            // 原来这里使用 orElseThrow，改为列不存在则忽略
                            return introspectedTable.getColumn(columnName)
                                    .map(CompositeQueryDesc::create)
                                    .orElse(null);
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
            //更新顺序号，order
            for (int i = 0; i < queryDesc.size(); i++) {
                queryDesc.get(i).setOrder(i + 1);
            }
            //转换为注解
            String[] array = queryDesc.stream().map(CompositeQueryDesc::toAnnotation).toArray(String[]::new);
            viewTableMetaDesc.setQuerys(array);
            //导入类型
            viewVoClass.addImportedTypes(queryDesc.stream().flatMap(q -> q.getImportedTypes().stream()).collect(Collectors.toSet()));
        }

         //filters
         if (!voViewGeneratorConfiguration.getFilterColumns().isEmpty()) {
             // viewVo中定义的filterColumn配置按列名分组
             Map<String, List<FilterColumnConfiguration>> listMap = voViewGeneratorConfiguration.getFilterColumnsConfigurations().stream().collect(Collectors.groupingBy(FilterColumnConfiguration::getColumn));
             //去重,转换为CompositeQueryDesc
             List<CompositeQueryDesc> queryDesc = voViewGeneratorConfiguration.getFilterColumns().stream()
                     .distinct()
                     .map(columnName -> {
                         if (listMap.containsKey(columnName)) {
                             CompositeQueryDesc compositeQueryDesc = CompositeQueryDesc.create(listMap.get(columnName).get(0), introspectedTable);
                             compositeQueryDesc.setRepeat(listMap.get(columnName).get(0).isRepeat());
                             compositeQueryDesc.setOperators(listMap.get(columnName).get(0).getOperators().stream().distinct().collect(Collectors.joining(",")));
                             return compositeQueryDesc;
                         } else {
                             IntrospectedColumn col = introspectedTable.getColumn(columnName)
                                     .orElseThrow(() -> new IllegalStateException("配置的 filter 列在表 "
                                             + introspectedTable.getTableConfiguration().getTableName()
                                             + " 中未找到: " + columnName));
                             return CompositeQueryDesc.create(col);
                         }
                     }).toList();
             //更新顺序号，order
             for (int i = 0; i < queryDesc.size(); i++) {
                 CompositeQueryDesc compositeQueryDesc = queryDesc.get(i);
                 compositeQueryDesc.setOrder(i + 1);
                 voViewGeneratorConfiguration.getFilterColumnsConfigurations().stream().filter(f -> f.getColumn().equals(compositeQueryDesc.getColumn())).findFirst().ifPresent(f -> {
                     compositeQueryDesc.setOperators(f.getOperators().stream().distinct().collect(Collectors.joining(",")));
                     compositeQueryDesc.setRepeat(f.isRepeat());
                 });
             }
             //转换为注解
             String[] array = queryDesc.stream().map(CompositeQueryDesc::toAnnotation).toArray(String[]::new);
             viewTableMetaDesc.setFilters(array);
             //导入类型
             viewVoClass.addImportedTypes(queryDesc.stream().flatMap(q -> q.getImportedTypes().stream()).collect(Collectors.toSet()));
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
        viewTableMetaDesc.setClassName(viewVoClass.getType().getFullyQualifiedName());

        //restBasePath
        viewTableMetaDesc.setRestBasePath(Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));

        //构造ViewTableMeta
        viewVoClass.addAnnotation(viewTableMetaDesc.toAnnotation());
        viewVoClass.addImportedTypes(viewTableMetaDesc.getImportedTypes());
    }

    private void updateOrder(Field field, AbstractIntrospectedTable abstractIntrospectedTable, ViewColumnMetaDesc viewColumnMetaDesc) {
        //更新order
        if (abstractIntrospectedTable.getRules().isGenerateViewVo()) {
            VoViewGeneratorConfiguration configuration = abstractIntrospectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
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
