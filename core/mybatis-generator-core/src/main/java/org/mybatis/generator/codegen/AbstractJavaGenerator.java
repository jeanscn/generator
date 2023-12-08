package org.mybatis.generator.codegen;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;

public abstract class AbstractJavaGenerator extends AbstractGenerator {

    public abstract List<CompilationUnit> getCompilationUnits();

    protected final String project;

    protected AbstractJavaGenerator(String project) {
        super();
        this.project = project;
    }

    public String getProject() {
        return project;
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

    protected void addInitialization(List<IntrospectedColumn> columns, InitializationBlock initializationBlock, TopLevelClass topLevelClass) {
        //在静态代码块中添加默认值
        final List<String> defaultFields = new ArrayList<>();
        topLevelClass.getSuperClass().ifPresent(s -> {
            EntityAbstractParentEnum entityAbstractParentEnum = EntityAbstractParentEnum.ofCode(s.getShortName());
            if (entityAbstractParentEnum != null) {
                defaultFields.addAll(entityAbstractParentEnum.fields());
            }
        });
        columns.forEach(c -> {
            if (c.getDefaultValue() != null  && !c.getDefaultValue().equalsIgnoreCase("null") && !defaultFields.contains(c.getJavaProperty())) {
                if (c.getDefaultValue().equals("CURRENT_TIMESTAMP")) {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = VDateUtils.getCurrentDatetime();", c.getJavaProperty()));
                    topLevelClass.addImportedType(V_DATE_UTILS);
                } else if (c.isJdbcCharacterColumn()) {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = \"{1}\";", c.getJavaProperty(), c.getDefaultValue()));
                } else if (c.getFullyQualifiedJavaType().getShortName().equals("BigDecimal")) {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = new BigDecimal(\"{1}\");", c.getJavaProperty(), c.getDefaultValue()));
                    topLevelClass.addImportedType(new FullyQualifiedJavaType("java.math.BigDecimal"));
                } else {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = {1};", c.getJavaProperty(), c.getDefaultValue()));
                }
            }
        });
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
}
