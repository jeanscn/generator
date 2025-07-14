package org.mybatis.generator.internal.util;

import cn.hutool.core.collection.CollectionUtil;
import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.vo.AbstractVoGenerator;
import org.mybatis.generator.codegen.mybatis3.vo.CreateMappingsInterface;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.enums.ModelClassTypeEnum;
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
    private final VoGeneratorConfiguration voGeneratorConfiguration;

    private List<IntrospectedColumn> abstractVoColumns = new ArrayList<>();
    private List<String> abstractVoColumnNames = new ArrayList<>();

    public VoGenService(IntrospectedTable introspectedTable) {
        this.introspectedTable = introspectedTable;
        this.voGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration();
    }

    /**
     * 获取vo类的内省列对象列表，返回不包含abstractVo中的列、Vo全局排除的列和指定排除的列名列表
     *
     * @param abstractVoColumnNames abstractVo对象的数据库列名列表
     * @param includeColumns        要包含的数据库列名列表
     * @param excludeColumns        要排除的数据库列名列表
     * @return 内省列对象列表
     */
    public List<IntrospectedColumn> getVoColumns(List<String> abstractVoColumnNames, List<String> includeColumns, Set<String> excludeColumns) {
        List<IntrospectedColumn> ret = new ArrayList<>();

        //在给定的排除列列表中附加全局Vo标签的排除列
        excludeColumns.addAll(introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getExcludeColumns());
        //将排除列追加到abstractVo列表中，整体排除
        abstractVoColumnNames.addAll(excludeColumns);
        if (includeColumns != null && !includeColumns.isEmpty()) {
            for (String includeColumn : includeColumns) {
                if (!abstractVoColumnNames.contains(includeColumn)) {
                    Optional<IntrospectedColumn> column = introspectedTable.getColumn(includeColumn);
                    column.ifPresent(ret::add);
                }
            }
            return ret;
        } else {
            return getIntrospectedColumns(abstractVoColumnNames, true);
        }
    }

    /**
     * 返回除去父类和所有排除的列+指定的fields属性名
     *
     * @param fields         额外的需要增加的属性名
     * @param includeColumns 要包含的
     * @param excludeColumns 要排除的
     * @return 所有列-Vo抽象父类的列-当前vo配置排除+vo全局排除的
     */
    public List<IntrospectedColumn> getAllVoColumns(List<String> fields, List<String> includeColumns, Set<String> excludeColumns) {

        //排除abstractVo、指定排除、Vo全局排除的列名
        List<IntrospectedColumn> voColumns = getVoColumns(this.getAbstractVoColumnNames(), includeColumns, excludeColumns);
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

    public List<String> getAbstractVoColumnNames() {
        if (this.abstractVoColumnNames.isEmpty()) {
            this.abstractVoColumnNames = this.getAbstractVoColumns().stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.toList());
        }
        return this.abstractVoColumnNames;
    }

    public List<IntrospectedColumn> getAbstractVoColumns() {
        if (abstractVoColumns.isEmpty()) {
            this.abstractVoColumns = getAbsVoColumns();
        }
        return abstractVoColumns;
    }

    private List<IntrospectedColumn> getAbsVoColumns() {
        BaseRules rules = introspectedTable.getRules();
        List<String> include = new ArrayList<>();
        List<String> exclude = new ArrayList<>();
        if (rules.isGenerateVoModel()) {
            VoModelGeneratorConfiguration cfg = voGeneratorConfiguration.getVoModelConfiguration();
            if (!cfg.getIncludeColumns().isEmpty()) {
                include.addAll(cfg.getIncludeColumns());
            }
            if (!cfg.getExcludeColumns().isEmpty()) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }
        if (rules.isGenerateViewVo()) {
            VoViewGeneratorConfiguration cfg = voGeneratorConfiguration.getVoViewConfiguration();
            if (!cfg.getIncludeColumns().isEmpty()) {
                include.retainAll(cfg.getIncludeColumns());
            }
            if (!cfg.getExcludeColumns().isEmpty()) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }
        if (rules.isGenerateRequestVo()) {
            VoRequestGeneratorConfiguration cfg = voGeneratorConfiguration.getVoRequestConfiguration();
            if (!cfg.getExcludeColumns().isEmpty()) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }

        if (rules.isGenerateCreateVo()) {
            VoCreateGeneratorConfiguration cfg = voGeneratorConfiguration.getVoCreateConfiguration();
            if (!cfg.getIncludeColumns().isEmpty()) {
                include.addAll(cfg.getIncludeColumns());
            }
            if (!cfg.getExcludeColumns().isEmpty()) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }

        if (rules.isGenerateUpdateVo()) {
            VoUpdateGeneratorConfiguration cfg = voGeneratorConfiguration.getVoUpdateConfiguration();
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
    public List<Field> buildOverrideColumn(List<OverridePropertyValueGeneratorConfiguration> configurations, TopLevelClass topLevelClass, ModelClassTypeEnum type) {

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
            String sourceColumnName;
            IntrospectedColumn targetColumn = introspectedTable.getColumn(overrideConfiguration.getTargetColumnName()).orElse(null);
            if (targetColumn != null) {
                javaType = targetColumn.getFullyQualifiedJavaType();
                propertyName = targetColumn.getJavaProperty();
                sourceColumnName = targetColumn.getActualColumnName();
            } else {
                propertyName = overrideConfiguration.getTargetPropertyName() == null ? ConfigUtil.getOverrideJavaProperty(sourceColumn.getJavaProperty(), null) : overrideConfiguration.getTargetPropertyName();
                javaType = overrideConfiguration.getTargetPropertyType() == null ? FullyQualifiedJavaType.getStringInstance() : new FullyQualifiedJavaType(overrideConfiguration.getTargetPropertyType());
                sourceColumnName = overrideConfiguration.getSourceColumnName();
            }
            Field field = new Field(propertyName, javaType);
            //如果在topLevelClass以及父类中已经存在，则跳过
            if (topLevelClass.getFields().contains(field)) {
                continue;
            }
            if (introspectedTable.getAllColumns().stream().anyMatch(c -> c.getJavaProperty().equals(propertyName))) {
                continue;
            }
            field.setSourceColumnName(sourceColumnName);
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
            if (overrideConfiguration.getAnnotationType() != null) {
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
                    case "DateRangeFormat":
                        DateRangeFormatDesc dateRangeDesc = getDateRangeFormatDesc(overrideConfiguration, sourceColumn);
                        dateRangeDesc.addAnnotationToField(field, topLevelClass);
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

    private static DateRangeFormatDesc getDateRangeFormatDesc(OverridePropertyValueGeneratorConfiguration overrideConfiguration, IntrospectedColumn sourceColumn) {
        DateRangeFormatDesc dateRangeDesc = new DateRangeFormatDesc();
        dateRangeDesc.setSource(sourceColumn.getJavaProperty());
        HtmlElementDescriptor elementDescriptor = overrideConfiguration.getElementDescriptor();
        if (elementDescriptor != null) {
            if (elementDescriptor.getDataFmt() != null) {
                dateRangeDesc.setPattern(elementDescriptor.getDataFmt());
            }
            if (elementDescriptor.getDateRangeSeparator() != null) {
                dateRangeDesc.setSeparator(elementDescriptor.getDateRangeSeparator());
            }
        }
        return dateRangeDesc;
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
        String superInterface = configuration.getProperty("superInterface");
        if (StringUtility.stringHasValue(superInterface)) {
            Arrays.stream(superInterface.split(","))
                    .map(FullyQualifiedJavaType::new)
                    .forEach(s -> {
                        topLevelClass.addSuperInterface(s);
                        topLevelClass.addImportedType(s);
                    });
        }
    }

    public FullyQualifiedJavaType getMappingType(IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String baseTargetPackage = StringUtility.substringBeforeLast(introspectedTable.getContext().getJavaModelGeneratorConfiguration().getTargetPackage(), ".") + "."+ AbstractVoGenerator.SUB_PACKAGE_POJO;
        String type = String.join(".", baseTargetPackage, CreateMappingsInterface.SUB_PACKAGE_MAPS, entityType.getShortName() + "Mappings");
        return new FullyQualifiedJavaType(type);
    }
}
