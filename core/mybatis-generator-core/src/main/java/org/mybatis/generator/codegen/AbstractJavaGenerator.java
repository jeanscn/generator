package org.mybatis.generator.codegen;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.db.DbFiledDefaultValueEnum;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.custom.FieldItem;
import org.mybatis.generator.custom.annotations.ApiModelPropertyDesc;

import java.util.*;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;

@Getter
public abstract class AbstractJavaGenerator extends AbstractGenerator {

    public abstract List<CompilationUnit> getCompilationUnits();

    protected final String project;

    protected AbstractJavaGenerator(String project) {
        super();
        this.project = project;
    }

    public static Method getGetter(Field field) {
        Method method = new Method(getGetterMethodName(field.getName(), field.getType()));
        method.setReturnType(field.getType());
        method.setVisibility(JavaVisibility.PUBLIC);
        String s = "return " + field.getName() + ';';
        method.addBodyLine(s);
        return method;
    }

    public String getRootClass() {
        String rootClass = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ROOT_CLASS);
        if (rootClass == null) {
            Properties properties = context.getJavaModelGeneratorConfiguration().getProperties();
            rootClass = properties.getProperty(PropertyRegistry.ROOT_CLASS);
        }
        return rootClass;
    }

    protected void addDefaultConstructor(TopLevelClass topLevelClass) {
        topLevelClass.addMethod(getDefaultConstructor(topLevelClass));
    }

    protected void addDefaultConstructorWithGeneratedAnnotation(TopLevelClass topLevelClass) {
        topLevelClass.addMethod(getDefaultConstructorWithGeneratedAnnotation(topLevelClass));
    }

    /**
     * 获得service类的抽象实现类
     *
     * @param introspectedTable 生成基类
     */
    protected String getAbstractService(IntrospectedTable introspectedTable) {
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            String steamOutType = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_JAVA_MODEL_BYTE_STREAM_OUTPUT_MODE);
            if (GenerateUtils.isBusinessInstance(introspectedTable)) {
                switch (steamOutType) {
                    case "bytes":
                        return ABSTRACT_BLOB_BYTES_SERVICE_BUSINESS;
                    case "file":
                        return ABSTRACT_BLOB_FILE_SERVICE_BUSINESS;
                    case "string":
                        return ABSTRACT_BLOB_STRING_SERVICE_BUSINESS;
                }
                return ABSTRACT_SERVICE_BUSINESS;
            } else {
                switch (steamOutType) {
                    case "bytes":
                        return ABSTRACT_MBG_BLOB_BYTES_SERVICE;
                    case "file":
                        return ABSTRACT_MBG_BLOB_FILE_SERVICE;
                    case "string":
                        return ABSTRACT_MBG_BLOB_STRING_SERVICE;
                }
                return ABSTRACT_MBG_BLOB_SERVICE_INTERFACE;
            }
        }
        return ABSTRACT_MBG_SERVICE_INTERFACE;
    }

    private Method getDefaultConstructor(TopLevelClass topLevelClass) {
        Method method = getBasicConstructor(topLevelClass);
        addGeneratedJavaDoc(method);
        return method;
    }

    private Method getDefaultConstructorWithGeneratedAnnotation(TopLevelClass topLevelClass) {
        Method method = getBasicConstructor(topLevelClass);
        addGeneratedAnnotation(method, topLevelClass);
        return method;
    }

    private Method getBasicConstructor(TopLevelClass topLevelClass) {
        Method method = new Method(topLevelClass.getType().getShortName());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        method.addBodyLine("super();"); //$NON-NLS-1$
        return method;
    }

    private void addGeneratedJavaDoc(Method method) {
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
    }

    private void addGeneratedAnnotation(Method method, TopLevelClass topLevelClass) {
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable,
                topLevelClass.getImportedTypes());
    }

    protected void addCacheConfig(TopLevelClass topLevelClass) {
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.cache.annotation.CacheConfig"));
        topLevelClass.addStaticImport("com.vgosoft.core.constant.CacheConstant.CACHE_MANAGER_NAME");
        topLevelClass.addAnnotation("@CacheConfig(cacheManager = CACHE_MANAGER_NAME)");
    }

    protected Set<String> addInitialization(List<IntrospectedColumn> columns, InitializationBlock initializationBlock, TopLevelClass topLevelClass) {
        Set<String> columnNames = new HashSet<>();
        //在静态代码块中添加默认值
        final List<String> defaultFields = new ArrayList<>();
        topLevelClass.getSuperClass().ifPresent(s -> {
            EntityAbstractParentEnum entityAbstractParentEnum = EntityAbstractParentEnum.ofCode(s.getShortName());
            if (entityAbstractParentEnum != null) {
                defaultFields.addAll(entityAbstractParentEnum.fields());
            }
        });
        columns.forEach(c -> {
            if (c.isGeneratedAlways() || c.isAutoIncrement()) {
                // 如果是自动生成的主键或自增列，则不进行初始化
                return;
            }
            String defaultValue = c.getDefaultValue();
            if (defaultValue != null && !defaultValue.equalsIgnoreCase("null")) {
                if (!defaultFields.contains(c.getJavaProperty())) {
                    columnNames.add(c.getActualColumnName());
                }
                if (defaultValue.equals("CURRENT_TIMESTAMP") || defaultValue.startsWith("now(") || defaultValue.startsWith("'now(")) {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = VDateUtils.getCurrentDatetime();", c.getJavaProperty()));
                    topLevelClass.addImportedType(V_DATE_UTILS);
                } else if (defaultValue.startsWith("'curdate(") || defaultValue.startsWith("curdate(")) {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = VDateUtils.getCurrentDate();", c.getJavaProperty()));
                    topLevelClass.addImportedType(V_DATE_UTILS);
                } else if (defaultValue.startsWith("'curtime(") || defaultValue.startsWith("curtime(")) {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = VDateUtils.getCurrentTime();", c.getJavaProperty()));
                    topLevelClass.addImportedType(V_DATE_UTILS);
                } else if (DbFiledDefaultValueEnum.ofCode(defaultValue)!=null) {
                    // 这里需要重写getter，依赖ioc，只能进行懒加载
                    DbFiledDefaultValueEnum defaultValueEnum = DbFiledDefaultValueEnum.ofCode(defaultValue);
                    // defaultValue的getter方法
                    String getterMethodName = getGetterMethodName(c.getJavaProperty(), c.getFullyQualifiedJavaType());
                    Method getterMethod = new Method(getterMethodName);
                    getterMethod.setVisibility(JavaVisibility.PUBLIC);
                    getterMethod.setReturnType(c.getFullyQualifiedJavaType());
                    getterMethod.addBodyLine("if ({0} == null) '{'",c.getJavaProperty());
                    getterMethod.addBodyLine(VStringUtil.format("return {0};", defaultValueEnum.codeName()));
                    getterMethod.addBodyLine("}");
                    getterMethod.addBodyLine(VStringUtil.format("return {0};", c.getJavaProperty()));
                    if (VStringUtil.stringHasValue(defaultValueEnum.imports())) {
                        topLevelClass.addMultipleImports(defaultValueEnum.imports().split(","));
                    }
                    topLevelClass.addMethod(getterMethod);
                } else if (c.isJdbcCharacterColumn()) {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = \"{1}\";", c.getJavaProperty(), defaultValue));
                } else if (c.getFullyQualifiedJavaType().getShortName().equals("BigDecimal")) {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = new BigDecimal(\"{1}\");", c.getJavaProperty(), defaultValue));
                    topLevelClass.addImportedType(new FullyQualifiedJavaType("java.math.BigDecimal"));
                } else if (c.getFullyQualifiedJavaType().getShortName().equals("Boolean")) {
                    switch (defaultValue.toLowerCase()) {
                        case "true":
                        case "1":
                        case "b'1'":
                            initializationBlock.addBodyLine(VStringUtil.format("this.{0} = true;", c.getJavaProperty()));
                            break;
                        case "false":
                        case "0":
                        case "b'0'":
                            initializationBlock.addBodyLine(VStringUtil.format("this.{0} = false;", c.getJavaProperty()));
                            break;
                        default:
                            initializationBlock.addBodyLine(VStringUtil.format("this.{0} = Boolean.valueOf(\"{1}\");", c.getJavaProperty(), defaultValue));
                            break;
                    }
                } else if (c.isJavaLocalDateColumn()) {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = LocalDate.parse(\"{1}\", DateTimeFormatter.ofPattern(\"yyyy-MM-dd\"));", c.getJavaProperty(), defaultValue));
                    topLevelClass.addImportedType(new FullyQualifiedJavaType("java.time.LocalDate"));
                    topLevelClass.addImportedType(new FullyQualifiedJavaType("java.time.format.DateTimeFormatter"));
                } else if (c.isJavaLocalDateTimeColumn()) {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = LocalDateTime.parse(\"{1}\", DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss\"));", c.getJavaProperty(), defaultValue));
                    topLevelClass.addImportedType(new FullyQualifiedJavaType("java.time.LocalDateTime"));
                    topLevelClass.addImportedType(new FullyQualifiedJavaType("java.time.format.DateTimeFormatter"));
                } else if (c.isJavaLocalTimeColumn()) {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = LocalTime.parse(\"{1}\", DateTimeFormatter.ofPattern(\"HH:mm:ss\"));", c.getJavaProperty(), defaultValue));
                    topLevelClass.addImportedType(new FullyQualifiedJavaType("java.time.LocalTime"));
                    topLevelClass.addImportedType(new FullyQualifiedJavaType("java.time.format.DateTimeFormatter"));
                } else {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = {1};", c.getJavaProperty(), defaultValue));
                }
            }
        });
        return columnNames;
    }

    /**
     * 内部方法：获得生成属性的列
     */
    protected List<IntrospectedColumn> getColumnsInThisClass() {
        List<IntrospectedColumn> introspectedColumns;
        if (includePrimaryKeyColumns()) {
            if (includeBLOBColumns()) {
                introspectedColumns = introspectedTable.getAllColumns();
            } else {
                introspectedColumns = introspectedTable.getNonBLOBColumns();
            }
        } else {
            if (includeBLOBColumns()) {
                introspectedColumns = introspectedTable.getNonPrimaryKeyColumns();
            } else {
                introspectedColumns = introspectedTable.getBaseColumns();
            }
        }

        return introspectedColumns;
    }

    /**
     * 内部方法：是否包含主键列
     */
    protected boolean includePrimaryKeyColumns() {
        return !introspectedTable.getRules().generatePrimaryKeyClass() && introspectedTable.hasPrimaryKeyColumns();
    }

    /**
     * 内部方法：是否包含大字段列
     */
    protected boolean includeBLOBColumns() {
        return !introspectedTable.getRules().generateRecordWithBLOBsClass()
                && introspectedTable.hasBLOBColumns();
    }

    /**
     * 增加actionType属性
     *
     * @param topLevelClass     类
     * @param introspectedTable 表对象
     */
    protected void addActionType(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Field addActionType = getField("actionType", FullyQualifiedJavaType.getStringInstance(), "查询应用场景的类型标识", introspectedTable);
        new ApiModelPropertyDesc(addActionType.getRemark(), "selector").addAnnotationToField(addActionType, topLevelClass);
        Optional<Field> actionType = topLevelClass.getFields().stream().filter(f -> f.getName().equals("actionType")).findFirst();
        if (!actionType.isPresent()) {
            topLevelClass.addField(addActionType);
            if (introspectedTable.getRules().isGenerateVoModel()) {
                FieldItem fieldItem = new FieldItem(addActionType);
                introspectedTable.getVoModelFields().add(fieldItem);
            }
        }
    }

    /**
     * 增加ignoreDeleteFlag属性
     *
     * @param topLevelClass     类
     * @param introspectedTable 表对象
     */
    protected void addIgnoreDeleteFlag(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Field ignoreDeleteFlag = getField("ignoreDeleteFlag", FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "是否忽略删除标记", introspectedTable);
        new ApiModelPropertyDesc(ignoreDeleteFlag.getRemark(), "false").addAnnotationToField(ignoreDeleteFlag, topLevelClass);
        Optional<Field> optionalField = topLevelClass.getFields().stream().filter(f -> f.getName().equals("ignoreDeleteFlag")).findFirst();
        if (!optionalField.isPresent()) {
            topLevelClass.addField(ignoreDeleteFlag);
            topLevelClass.addImportedType(new FullyQualifiedJavaType("com.baomidou.mybatisplus.annotation.TableField"));
            if (introspectedTable.getRules().isGenerateVoModel()) {
                FieldItem fieldItem = new FieldItem(ignoreDeleteFlag);
                introspectedTable.getVoModelFields().add(fieldItem);
            }
        }
    }

    protected void addIgnorePermissionAnnotation(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Field ignorePermissionAnnotation = getField("ignorePermissionAnnotation", FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "是否忽略权限注解", introspectedTable);
        new ApiModelPropertyDesc(ignorePermissionAnnotation.getRemark(), "false").addAnnotationToField(ignorePermissionAnnotation, topLevelClass);
        Optional<Field> optionalField = topLevelClass.getFields().stream().filter(f -> f.getName().equals("ignorePermissionAnnotation")).findFirst();
        if (!optionalField.isPresent()) {
            topLevelClass.addField(ignorePermissionAnnotation);
            topLevelClass.addImportedType(new FullyQualifiedJavaType("com.baomidou.mybatisplus.annotation.TableField"));
            if (introspectedTable.getRules().isGenerateVoModel()) {
                FieldItem fieldItem = new FieldItem(ignorePermissionAnnotation);
                introspectedTable.getVoModelFields().add(fieldItem);
            }
        }
    }

    private static Field getField(String ignorePermissionAnnotation, FullyQualifiedJavaType BooleanPrimitiveInstance, String remark, IntrospectedTable introspectedTable) {
        Field field = new Field(ignorePermissionAnnotation, BooleanPrimitiveInstance);
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setRemark(remark);
        if (introspectedTable.getContext().isIntegrateMybatisPlus() && !introspectedTable.getRules().isGenerateVoModel() && !introspectedTable.getRules().isGenerateRequestVO()) {
            field.addAnnotation("@TableField(exist = false)");
        }
        return field;
    }

    /**
     * 增加ignoreIdList属性
     *
     * @param topLevelClass     类
     * @param introspectedTable 表对象
     */
    protected void addIgnoreIdList(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
        Field ignoreIdList = getField("ignoreIdList", listType, "查询忽略id列表", introspectedTable);
        new ApiModelPropertyDesc(ignoreIdList.getRemark(), "[]").addAnnotationToField(ignoreIdList, topLevelClass);
        Optional<Field> optionalField = topLevelClass.getFields().stream().filter(f -> f.getName().equals("ignoreIdList")).findFirst();
        if (!optionalField.isPresent()) {
            topLevelClass.addField(ignoreIdList);
            topLevelClass.addImportedType(new FullyQualifiedJavaType("com.baomidou.mybatisplus.annotation.TableField"));
            topLevelClass.addImportedType(listType);
            if (introspectedTable.getRules().isGenerateVoModel()) {
                FieldItem fieldItem = new FieldItem(ignoreIdList);
                introspectedTable.getVoModelFields().add(fieldItem);
            }
        }
    }

    /**
     * 增加isIgnoreIds属性
     *
     * @param topLevelClass     类
     * @param introspectedTable 表对象
     */
    protected void addIsHideIds(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Field isHideIds = getField("isHideIds", FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "是否忽略隐藏id列表", introspectedTable);
        new ApiModelPropertyDesc(isHideIds.getRemark(), "false").addAnnotationToField(isHideIds, topLevelClass);
        Optional<Field> optionalField = topLevelClass.getFields().stream().filter(f -> f.getName().equals("isHideIds")).findFirst();
        if (!optionalField.isPresent()) {
            topLevelClass.addField(isHideIds);
            topLevelClass.addImportedType(new FullyQualifiedJavaType("com.baomidou.mybatisplus.annotation.TableField"));
            if (introspectedTable.getRules().isGenerateVoModel()) {
                FieldItem fieldItem = new FieldItem(isHideIds);
                introspectedTable.getVoModelFields().add(fieldItem);
            }
        }
    }
}
