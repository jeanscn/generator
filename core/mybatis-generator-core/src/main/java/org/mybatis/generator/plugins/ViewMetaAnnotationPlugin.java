package org.mybatis.generator.plugins;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.view.ViewActionColumnEnum;
import com.vgosoft.core.constant.enums.view.ViewIndexColumnEnum;
import com.vgosoft.core.constant.enums.view.ViewToolBarsEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.ConfigUtil;
import org.mybatis.generator.config.VOViewGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.CompositeQueryDesc;
import org.mybatis.generator.custom.annotations.ViewColumnMetaDesc;
import org.mybatis.generator.custom.annotations.ViewTableMetaDesc;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
            //增加ViewMetaAnnotation
            ViewColumnMetaDesc viewColumnMetaDesc = ViewColumnMetaDesc.create(introspectedColumn, introspectedTable);
            updateOrder(field, introspectedTable, viewColumnMetaDesc);
            field.addAnnotation(viewColumnMetaDesc.toAnnotation());
            topLevelClass.addImportedTypes(viewColumnMetaDesc.getImportedTypes());
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
        ViewColumnMetaDesc viewColumnMetaDesc;
        if (introspectedColumn != null) {
            viewColumnMetaDesc = ViewColumnMetaDesc.create(introspectedColumn, introspectedTable);
        } else {
            viewColumnMetaDesc = new ViewColumnMetaDesc(field, field.getRemark(), introspectedTable);
        }
        updateOrder(field, introspectedTable, viewColumnMetaDesc);
        field.addAnnotation(viewColumnMetaDesc.toAnnotation());
        topLevelClass.addImportedTypes(viewColumnMetaDesc.getImportedTypes());
        return true;
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
            ViewToolBarsEnum[] toolBarsEnums = voViewGeneratorConfiguration.getToolbar().stream()
                    .map(ViewToolBarsEnum::ofCode)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(ViewToolBarsEnum::value).reversed())
                    .toArray(ViewToolBarsEnum[]::new);
            viewTableMetaDesc.setToolbarActions(toolBarsEnums);
        }
        //indexColumn
        ViewIndexColumnEnum viewIndexColumnEnum = ViewIndexColumnEnum.ofCode(voViewGeneratorConfiguration.getIndexColumn());
        if (viewIndexColumnEnum != null) {
            viewTableMetaDesc.setIndexColumn(viewIndexColumnEnum);
        }
        //actionColumn
        if (!voViewGeneratorConfiguration.getActionColumn().isEmpty()) {
            ViewActionColumnEnum[] viewActionColumnEnums = voViewGeneratorConfiguration.getActionColumn().stream()
                    .map(ViewActionColumnEnum::ofCode)
                    .filter(Objects::nonNull)
                    .distinct().toArray(ViewActionColumnEnum[]::new);
            viewTableMetaDesc.setActionColumn(viewActionColumnEnums);
        }
        //querys
        if (!voViewGeneratorConfiguration.getQueryColumns().isEmpty()) {
            String[] strings = voViewGeneratorConfiguration.getQueryColumns().stream()
                    .distinct()
                    .map(f -> introspectedTable.getColumn(f).orElse(null))
                    .filter(Objects::nonNull)
                    .map(c -> CompositeQueryDesc.create(c).toAnnotation())
                    .toArray(String[]::new);
            viewTableMetaDesc.setQuerys(strings);
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
