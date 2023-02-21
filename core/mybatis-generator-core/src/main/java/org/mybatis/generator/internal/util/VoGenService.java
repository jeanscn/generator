package org.mybatis.generator.internal.util;

import cn.hutool.core.collection.CollectionUtil;
import com.vgosoft.core.constant.enums.EntityAbstractParentEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ModelClassTypeEnum;
import org.mybatis.generator.custom.annotations.*;
import org.mybatis.generator.internal.rules.BaseRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-01-08 13:32
 * @version 3.0
 */
public class VoGenService {

    private final IntrospectedTable introspectedTable;
    private final VOGeneratorConfiguration voGeneratorConfiguration;

    private List<IntrospectedColumn> abstractVOColumns;
    private List<String> abstractVOColumnNames;

    public VoGenService(IntrospectedTable introspectedTable) {
        this.introspectedTable = introspectedTable;
        this.voGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration();
    }

    /**
     * 获取vo类的内省列对象列表，返回不包含abstractVO中的列、VO全局排除的列和指定排除的列名列表
     * @param abstractVOColumnNames abstractVO对象的数据库列名列表
     * @param includeColumns    要包含的数据库列名列表
     * @param excludeColumns    要排除的数据库列名列表
     * @return 内省列对象列表
     */
    public List<IntrospectedColumn> getVOColumns(List<String> abstractVOColumnNames,List<String> includeColumns, List<String> excludeColumns) {
        //在给定的排除列列表中附加全局VO标签的排除列
        CollectionUtil.addAllIfNotContains(excludeColumns, introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getExcludeColumns());
        //将排除列追加到abstractVO列表中，整体排除
        if (excludeColumns != null && excludeColumns.size() > 0) {
            CollectionUtil.addAllIfNotContains(abstractVOColumnNames,excludeColumns);
        }
        if (includeColumns != null && includeColumns.size() > 0) {
            List<String> includes = CollectionUtil.subtractToList(includeColumns, abstractVOColumnNames);
            return getIntrospectedColumns(includes,false);
        }else{
            return getIntrospectedColumns(abstractVOColumnNames, true);
        }
    }

    /**
     * 返回除去父类和所有排除的列+指定的fields属性名
     * @param fields 额外的需要增加的属性名
     * @param includeColumns 要包含的
     * @param excludeColumns 要排除的
     * @return 所有列-VO抽象父类的列-当前vo配置排除+vo全局排除的
     */
    public List<IntrospectedColumn> getAllVoColumns(List<String> fields, List<String> includeColumns, List<String> excludeColumns) {
        List<IntrospectedColumn> voColumns = getVOColumns(this.getAbstractVOColumnNames(),includeColumns, excludeColumns); //排除abstractVO、指定排除、VO全局排除的列名
        if (fields == null || fields.size()==0) {
            return voColumns;
        }else{
            List<IntrospectedColumn> fieldColumns = introspectedTable.getAllColumns().stream()
                    .filter(c -> fields.contains(c.getJavaProperty())).collect(Collectors.toList());
            return CollectionUtil.addAllIfNotContains(voColumns, fieldColumns);
        }
    }

    public List<String> getDefaultExcludeColumnNames(List<String> excludeNames) {
        List<String> exclude = new ArrayList<>();
        if (excludeNames != null && excludeNames.size()>0) {
            exclude.addAll(excludeNames);
        }
        EntityAbstractParentEnum entityAbstractParentEnum = EntityAbstractParentEnum.ofCode("AbstractPersistenceLockEntity");
        if (entityAbstractParentEnum != null) {
            CollectionUtil.addAllIfNotContains(exclude,entityAbstractParentEnum.columnNames());
        }
        CollectionUtil.addAllIfNotContains(exclude, Arrays.asList("TENANT_ID","BYTES_"));
        return exclude.stream().distinct().collect(Collectors.toList());
    }


    public List<String> getAbstractVOColumnNames() {
        if (this.abstractVOColumnNames == null) {
            return this.getAbstractVOColumns().stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.toList());
        }
        return this.abstractVOColumnNames;
    }

    public List<IntrospectedColumn> getAbstractVOColumns() {
        if (abstractVOColumns == null) {
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
            if (cfg.getIncludeColumns().size() > 0) {
                include.addAll(cfg.getIncludeColumns());
            }
            if (cfg.getExcludeColumns().size() > 0) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }
        if (rules.isGenerateViewVO()) {
            VOViewGeneratorConfiguration cfg = voGeneratorConfiguration.getVoViewConfiguration();
            if (cfg.getIncludeColumns().size() > 0) {
                include.retainAll(cfg.getIncludeColumns());
            }
            if (cfg.getExcludeColumns().size() > 0) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }
        if (rules.isGenerateRequestVO()) {
            VORequestGeneratorConfiguration cfg = voGeneratorConfiguration.getVoRequestConfiguration();
            if (cfg.getExcludeColumns().size() > 0) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }

        if (rules.isGenerateCreateVO()) {
            VOCreateGeneratorConfiguration cfg = voGeneratorConfiguration.getVoCreateConfiguration();
            if (cfg.getIncludeColumns().size()>0) {
                include.addAll(cfg.getIncludeColumns());
            }
            if (cfg.getExcludeColumns().size()>0) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }

        if (rules.isGenerateUpdateVO()) {
            VOUpdateGeneratorConfiguration cfg = voGeneratorConfiguration.getVoUpdateConfiguration();
            if (cfg.getIncludeColumns().size()>0) {
                include.addAll(cfg.getIncludeColumns());
            }
            if (cfg.getExcludeColumns().size()>0) {
                exclude.addAll(cfg.getExcludeColumns());
            }
        }

        if (voGeneratorConfiguration.getExcludeColumns().size() > 0) {
            exclude.addAll(voGeneratorConfiguration.getExcludeColumns());
        } else {
            exclude.addAll(getDefaultExcludeColumnNames(null));
        }
        if (include.size() > 0) {
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
        for (OverridePropertyValueGeneratorConfiguration configuration : configurations) {
            IntrospectedColumn sourceColumn = introspectedTable.getColumn(configuration.getSourceColumnName()).orElse(null);
            if (sourceColumn == null) {
                continue;
            }

            Field field = null;
            IntrospectedColumn targetColumn = introspectedTable.getColumn(configuration.getTargetColumnName()).orElse(null);
            if (targetColumn != null) {
                field = new Field(targetColumn.getJavaProperty(), targetColumn.getFullyQualifiedJavaType());
            } else {
                if (configuration.getTargetPropertyName() != null && configuration.getTargetPropertyType() != null) {
                    field = new Field(configuration.getTargetPropertyName(), new FullyQualifiedJavaType(configuration.getTargetPropertyType()));
                }
            }
            if (field == null) {
                sourceColumn = introspectedTable.getColumn(configuration.getSourceColumnName()).orElse(null);
                if (sourceColumn != null) {
                    field = new Field(sourceColumn.getJavaProperty(), sourceColumn.getFullyQualifiedJavaType());
                }
            }
            if (field != null) {
                field.setVisibility(JavaVisibility.PRIVATE);
                field.setRemark(configuration.getRemark() == null ? sourceColumn.getRemarks(true) : configuration.getRemark());
                String annotation = null;
                if ("DictUser".equals(configuration.getAnnotationType())) {
                    DictUser anno = configuration.getTypeValue() != null ? new DictUser(configuration.getTypeValue()) : new DictUser();
                    anno.setSource(sourceColumn.getJavaProperty());
                    annotation = anno.toAnnotation();
                    topLevelClass.addMultipleImports(anno.multipleImports());
                } else if ("DictSys".equals(configuration.getAnnotationType())) {
                    DictSys anno = configuration.getTypeValue() != null ? new DictSys(configuration.getTypeValue()) : new DictSys();
                    anno.setSource(sourceColumn.getJavaProperty());
                    annotation = anno.toAnnotation();
                    topLevelClass.addMultipleImports(anno.multipleImports());
                } else if ("Dict".equals(configuration.getAnnotationType()) && configuration.getBeanName() != null) {
                    Dict anno = new Dict(configuration.getBeanName());
                    if (configuration.getTypeValue() != null) {
                        anno.setValue(configuration.getTypeValue());
                    }
                    if (configuration.getApplyProperty() != null) {
                        anno.setApplyProperty(configuration.getApplyProperty());
                    }
                    anno.setSource(sourceColumn.getJavaProperty());
                    annotation = anno.toAnnotation();
                    topLevelClass.addMultipleImports(anno.multipleImports());
                }
                if (ModelClassTypeEnum.modelClass.equals(type) && configuration.getAnnotationType().contains("Dict")) {
                    topLevelClass.addAnnotation("@EnableDictionary");
                    topLevelClass.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.annotation.EnableDictionary"));
                    //添加ApiModelProperty注解
                    if (field.getRemark() != null) {
                        ApiModelProperty apiModelProperty = new ApiModelProperty(field.getRemark());
                        apiModelProperty.setExample(field.getName());
                        field.addAnnotation(apiModelProperty.toAnnotation());
                        topLevelClass.addMultipleImports(apiModelProperty.multipleImports());
                    }
                }

                final String fieldName = field.getName();
                List<Field> collect = topLevelClass.getFields()
                        .stream()
                        .filter(f -> f.getName().equals(fieldName))
                        .collect(Collectors.toList());
                if (collect.size() > 0) {
                    addDictAnnotation(topLevelClass, collect.get(0), annotation, type);
                } else {
                    addDictAnnotation(topLevelClass, field, annotation, type);
                    topLevelClass.addField(field);
                    answer.add(field);
                }


                //重写getter
                String getterName;
                FullyQualifiedJavaType getterType;
                if (targetColumn != null) {
                    getterType = targetColumn.getFullyQualifiedJavaType();
                    getterName = getGetterMethodName(targetColumn.getJavaProperty(), getterType);
                } else {
                    getterType = new FullyQualifiedJavaType(configuration.getTargetPropertyType());
                    getterName = getGetterMethodName(configuration.getTargetPropertyName(), getterType);
                }
                List<Method> methods = topLevelClass.getMethods().stream()
                        .filter(m -> m.getName().equals(getterName))
                        .collect(Collectors.toList());
                Method method;
                if (methods.size() > 0) {
                    method = methods.get(0);
                    method.getBodyLines().clear();
                } else {
                    method = new Method(getterName);
                    method.setReturnType(getterType);
                    method.setVisibility(JavaVisibility.PUBLIC);
                    topLevelClass.addMethod(method);
                }
                introspectedTable.getColumn(configuration.getSourceColumnName())
                        .ifPresent(sc -> {
                            String source = sc.getFullyQualifiedJavaType().getShortName();
                            String strType = FullyQualifiedJavaType.getStringInstance().getShortName();
                            String target = getterType.getShortName();

                            if (!source.equals(strType) && target.equals(strType)) {
                                method.addBodyLine("return this.{0} != null ? this.{0}.toString():null;", sc.getJavaProperty());
                            } else {
                                method.addBodyLine("return this.{0};", sc.getJavaProperty());
                            }
                        });
            }
        }
        return answer;
    }

    /**
     * 添加字典注解
     *
     * @param topLevelClass     类
     * @param field     字段
     * @param annotation    字典注解
     * @param type voClass,viewVOClass,excelVoClass,modelClass
     */
    private void addDictAnnotation(final TopLevelClass topLevelClass, Field field, String annotation, ModelClassTypeEnum type) {
        String exportDictConverter = "com.vgosoft.plugins.excel.converter.ExportDictConverter";
        if (annotation != null) {
            if (ModelClassTypeEnum.voClass.equals(type)) {
                field.getAnnotations().forEach(s -> {
                    if (s.contains("@ExcelProperty(") && !s.contains("converter")) {
                        topLevelClass.addImportedType(exportDictConverter);
                    }
                });
                long count = field.getAnnotations().stream().filter(s -> s.contains("@ExcelProperty(")).count();
                if (count == 0) {
                    ExcelProperty excelProperty = new ExcelProperty(field.getRemark());
                    excelProperty.setConverter("ExportDictConverter.class");
                    topLevelClass.addImportedType(exportDictConverter);
                    topLevelClass.addMultipleImports(excelProperty.multipleImports());
                    field.addAnnotation(excelProperty.toAnnotation());
                }
            } else if (ModelClassTypeEnum.viewVOClass.equals(type)) {
                field.addAnnotation(annotation);
            } else if (ModelClassTypeEnum.excelVoClass.equals(type)) {
                field.addAnnotation(annotation);
            } else if(ModelClassTypeEnum.modelClass.equals(type)){
                field.addAnnotation(annotation);
            }
        }
    }

}
