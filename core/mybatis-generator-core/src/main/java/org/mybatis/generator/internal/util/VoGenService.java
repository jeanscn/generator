package org.mybatis.generator.internal.util;

import cn.hutool.core.collection.CollectionUtil;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ModelClassTypeEnum;
import org.mybatis.generator.custom.annotations.*;
import org.mybatis.generator.internal.rules.BaseRules;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-01-08 13:32
 * @version 3.0
 */
public class VoGenService {

    private final IntrospectedTable introspectedTable;
    private final VOGeneratorConfiguration voGeneratorConfiguration;

    private List<IntrospectedColumn> abstractVOColumns = new ArrayList<>();
    private List<String> abstractVOColumnNames = new ArrayList<>();

    public VoGenService(IntrospectedTable introspectedTable) {
        this.introspectedTable = introspectedTable;
        this.voGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration();
    }

    /**
     * 获取vo类的内省列对象列表，返回不包含abstractVO中的列、VO全局排除的列和指定排除的列名列表
     *
     * @param abstractVOColumnNames abstractVO对象的数据库列名列表
     * @param includeColumns        要包含的数据库列名列表
     * @param excludeColumns        要排除的数据库列名列表
     * @return 内省列对象列表
     */
    public List<IntrospectedColumn> getVOColumns(List<String> abstractVOColumnNames, Set<String> includeColumns, Set<String> excludeColumns) {
        //在给定的排除列列表中附加全局VO标签的排除列
        excludeColumns.addAll(introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getExcludeColumns());
        //将排除列追加到abstractVO列表中，整体排除
        abstractVOColumnNames.addAll(excludeColumns);
        if (includeColumns != null && !includeColumns.isEmpty()) {
            List<String> includes = CollectionUtil.subtractToList(includeColumns, abstractVOColumnNames);
            return getIntrospectedColumns(includes, false);
        } else {
            return getIntrospectedColumns(abstractVOColumnNames, true);
        }
    }

    /**
     * 返回除去父类和所有排除的列+指定的fields属性名
     *
     * @param fields         额外的需要增加的属性名
     * @param includeColumns 要包含的
     * @param excludeColumns 要排除的
     * @return 所有列-VO抽象父类的列-当前vo配置排除+vo全局排除的
     */
    public List<IntrospectedColumn> getAllVoColumns(List<String> fields, Set<String> includeColumns, Set<String> excludeColumns) {
        List<IntrospectedColumn> voColumns = getVOColumns(this.getAbstractVOColumnNames(), includeColumns, excludeColumns); //排除abstractVO、指定排除、VO全局排除的列名
        if (fields == null || fields.isEmpty()) {
            return voColumns;
        } else {
            List<IntrospectedColumn> fieldColumns = introspectedTable.getAllColumns()
                    .stream()
                    .filter(c -> fields.contains(c.getJavaProperty()))
                    .collect(Collectors.toList());
            return CollectionUtil.addAllIfNotContains(voColumns, fieldColumns);
        }
    }

    public Set<String> getDefaultExcludeColumnNames(Set<String> excludeNames) {
        Set<String> exclude = new HashSet<>();
        if (excludeNames != null && !excludeNames.isEmpty()) {
            exclude.addAll(excludeNames);
        }
        EntityAbstractParentEnum entityAbstractParentEnum = EntityAbstractParentEnum.ofCode("AbstractPersistenceLockEntity");
        if (entityAbstractParentEnum != null) {
            exclude.addAll(entityAbstractParentEnum.columnNames());
        }
        exclude.add("bytes_");
        return exclude;
    }

    public List<String> getAbstractVOColumnNames() {
        if (this.abstractVOColumnNames.isEmpty()) {
            this.abstractVOColumnNames = this.getAbstractVOColumns().stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.toList());
        }
        return this.abstractVOColumnNames;
    }

    public List<IntrospectedColumn> getAbstractVOColumns() {
        if (abstractVOColumns.isEmpty()) {
            this.abstractVOColumns = getAbsVOColumns();
        }
        return abstractVOColumns;
    }

    private List<IntrospectedColumn> getAbsVOColumns() {
        BaseRules rules = introspectedTable.getRules();
        List<String> include = new ArrayList<>();
        List<String> exclude = new ArrayList<>();
        if (rules.isGenerateVoModel()) {
            VOModelGeneratorConfiguration cfg = voGeneratorConfiguration.getVoModelConfiguration();
            if (!cfg.getIncludeColumns().isEmpty()) {
                include.addAll(cfg.getIncludeColumns());
            }
            if (!cfg.getExcludeColumns().isEmpty()) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }
        if (rules.isGenerateViewVO()) {
            VOViewGeneratorConfiguration cfg = voGeneratorConfiguration.getVoViewConfiguration();
            if (!cfg.getIncludeColumns().isEmpty()) {
                include.retainAll(cfg.getIncludeColumns());
            }
            if (!cfg.getExcludeColumns().isEmpty()) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }
        if (rules.isGenerateRequestVO()) {
            VORequestGeneratorConfiguration cfg = voGeneratorConfiguration.getVoRequestConfiguration();
            if (!cfg.getExcludeColumns().isEmpty()) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }

        if (rules.isGenerateCreateVO()) {
            VOCreateGeneratorConfiguration cfg = voGeneratorConfiguration.getVoCreateConfiguration();
            if (!cfg.getIncludeColumns().isEmpty()) {
                include.addAll(cfg.getIncludeColumns());
            }
            if (!cfg.getExcludeColumns().isEmpty()) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }

        if (rules.isGenerateUpdateVO()) {
            VOUpdateGeneratorConfiguration cfg = voGeneratorConfiguration.getVoUpdateConfiguration();
            if (!cfg.getIncludeColumns().isEmpty()) {
                include.addAll(cfg.getIncludeColumns());
            }
            if (!cfg.getExcludeColumns().isEmpty()) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }

        if (!voGeneratorConfiguration.getExcludeColumns().isEmpty()) {
            exclude.addAll(voGeneratorConfiguration.getExcludeColumns());
        } else {
            exclude.addAll(getDefaultExcludeColumnNames(null));
        }
        if (!include.isEmpty()) {
            return getIntrospectedColumns(include, false);
        } else {
            return getIntrospectedColumns(exclude, true);
        }
    }

    private List<IntrospectedColumn> getIntrospectedColumns(List<String> columnNames, boolean isExclude) {
        List<String> collect = columnNames.stream().distinct().collect(Collectors.toList());
        return this.introspectedTable.getAllColumns().stream()
                .filter(c -> isExclude != collect.contains(c.getActualColumnName()))
                .collect(Collectors.toList());
    }

    /**
     * 构造OverrideColumn
     *
     * @param type ModelClassTypeEnum
     */
    public List<Field> buildOverrideColumn(Set<OverridePropertyValueGeneratorConfiguration> configurations, TopLevelClass topLevelClass, ModelClassTypeEnum type) {

        List<Field> answer = new ArrayList<>();
        for (OverridePropertyValueGeneratorConfiguration overrideConfiguration : configurations) {
            //如果指定的列不存在，则跳过
            IntrospectedColumn sourceColumn = introspectedTable.getColumn(overrideConfiguration.getSourceColumnName()).orElse(null);
            if (sourceColumn == null) {
                continue;
            }

            //创建转换后的属性对象
            FullyQualifiedJavaType javaType;
            String propertyName;
            IntrospectedColumn targetColumn = introspectedTable.getColumn(overrideConfiguration.getTargetColumnName()).orElse(null);
            if (targetColumn != null) {
                javaType = targetColumn.getFullyQualifiedJavaType();
                propertyName = targetColumn.getJavaProperty();
            } else {
                propertyName = overrideConfiguration.getTargetPropertyName() == null ? ConfigUtil.getOverrideJavaProperty(sourceColumn.getJavaProperty()) : overrideConfiguration.getTargetPropertyName();
                javaType = overrideConfiguration.getTargetPropertyType() == null ? FullyQualifiedJavaType.getStringInstance() : new FullyQualifiedJavaType(overrideConfiguration.getTargetPropertyType());
            }
            Field field = new Field(propertyName, javaType);
            //如果在topLevelClass以及父类中已经存在，则跳过
            if (topLevelClass.getFields().contains(field)) {
                continue;
            }
            if (introspectedTable.getAllColumns().stream().anyMatch(c -> c.getJavaProperty().equals(propertyName))) {
                continue;
            }
            //设置属性的注释
            field.setRemark(overrideConfiguration.getRemark() != null ? overrideConfiguration.getRemark() : sourceColumn.getRemarks(true));
            field.setVisibility(JavaVisibility.PRIVATE);

            if (!overrideConfiguration.getInitializationString().isPresent()) {
                if (field.getType().equals(FullyQualifiedJavaType.getStringInstance())) {
                    field.setInitializationString("\"-\"");
                } else if (field.getType().equals(FullyQualifiedJavaType.getIntegerInstance()) || field.getType().equals(FullyQualifiedJavaType.getIntInstance())) {
                    field.setInitializationString("0");
                }
            } else {
                field.setInitializationString(overrideConfiguration.getInitializationString().get());
                overrideConfiguration.getImportTypes().forEach(topLevelClass::addImportedType);
            }
            if (overrideConfiguration.getAnnotationType() == null) {
                //do nothing
            } else {
                switch (overrideConfiguration.getAnnotationType()) {
                    case "DictUser":
                        DictUserDesc dictUserDesc = new DictUserDesc();
                        dictUserDesc.setSource(sourceColumn.getJavaProperty());
                        dictUserDesc.addAnnotationToField(field, topLevelClass);
                        break;
                    case "DictDepartment":
                        DictDepartmentDesc dictDepartmentDesc = new DictDepartmentDesc();
                        dictDepartmentDesc.setSource(sourceColumn.getJavaProperty());
                        dictDepartmentDesc.addAnnotationToField(field, topLevelClass);
                        break;
                    case "DictModule":
                        DictModuleDesc dictModuleDesc = new DictModuleDesc();
                        dictModuleDesc.setSource(sourceColumn.getJavaProperty());
                        dictModuleDesc.addAnnotationToField(field, topLevelClass);
                        break;
                    case "DictSys":
                        DictSysDesc dictSysDesc = new DictSysDesc();
                        dictSysDesc.setValue(overrideConfiguration.getTypeValue());
                        dictSysDesc.setSource(sourceColumn.getJavaProperty());
                        dictSysDesc.addAnnotationToField(field, topLevelClass);
                        break;
                    case "DictData":
                        DictDataDesc dictDataDesc = new DictDataDesc();
                        dictDataDesc.setSource(sourceColumn.getJavaProperty());
                        dictDataDesc.setValue(overrideConfiguration.getTypeValue());
                        dictDataDesc.addAnnotationToField(field, topLevelClass);
                        break;
                    case "Dict":
                        if (overrideConfiguration.getBeanName() != null) {
                            DictDesc dictDesc = new DictDesc(overrideConfiguration.getBeanName());
                            if (overrideConfiguration.getTypeValue() != null) {
                                dictDesc.setValue(overrideConfiguration.getTypeValue());
                            }
                            if (overrideConfiguration.getApplyProperty() != null) {
                                dictDesc.setApplyProperty(overrideConfiguration.getApplyProperty());
                            }
                            dictDesc.setSource(sourceColumn.getJavaProperty());
                            dictDesc.addAnnotationToField(field, topLevelClass);
                        }
                        break;
                    case "DictEnum":
                        if (overrideConfiguration.getEnumClassName() != null) {
                            DictEnumDesc dictEnumDesc = new DictEnumDesc(overrideConfiguration.getEnumClassName());
                            dictEnumDesc.setSource(sourceColumn.getJavaProperty());
                            dictEnumDesc.addAnnotationToField(field, topLevelClass);
                        }
                        break;
                    default:
                        break;
                }
                if (ModelClassTypeEnum.modelClass.equals(type) && overrideConfiguration.getAnnotationType().contains("Dict")) {
                    if (!topLevelClass.getAnnotations().contains("@EnableDictionary")) {
                        topLevelClass.addAnnotation("@EnableDictionary");
                        topLevelClass.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.annotation.EnableDictionary"));
                    }
                }
            }
            answer.add(field);
        }
        return answer;
    }

    public OverridePropertyValueGeneratorConfiguration getOverridePropertyValueConfiguration(IntrospectedColumn introspectedColumn) {
        final String actualColumnName = introspectedColumn.getActualColumnName();
        if (introspectedTable.getRules().isGenerateVoModel()) {
            return this.introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoModelConfiguration().getOverridePropertyConfigurations()
                    .stream()
                    .filter(overridePropertyConfiguration -> overridePropertyConfiguration.getSourceColumnName().equals(actualColumnName))
                    .findFirst().orElse(null);
        } else {
            return this.introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().getOverridePropertyConfigurations()
                    .stream()
                    .filter(overridePropertyConfiguration -> overridePropertyConfiguration.getSourceColumnName().equals(actualColumnName))
                    .findFirst().orElse(null);
        }
    }

    public void addConfigurationSuperInterface(TopLevelClass topLevelClass, PropertyHolder configuration) {
        String superInterface =configuration.getProperty("superInterface");
        if (StringUtility.stringHasValue(superInterface)) {
            Arrays.stream(superInterface.split(","))
                    .map(FullyQualifiedJavaType::new)
                    .forEach(s -> {
                        topLevelClass.addSuperInterface(s);
                        topLevelClass.addImportedType(s);
                    });
        }
    }
}
