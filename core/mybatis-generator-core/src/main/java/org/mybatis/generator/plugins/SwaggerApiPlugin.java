package org.mybatis.generator.plugins;

import com.vgosoft.core.db.util.JDBCUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.custom.DictTypeEnum;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;
import org.mybatis.generator.custom.annotations.Api;
import org.mybatis.generator.custom.annotations.ApiModel;
import org.mybatis.generator.custom.annotations.ApiModelProperty;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * 添加日期属性的JsonFormat
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-04-19 14:51
 * @version 3.0
 */
public class SwaggerApiPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * model类的@apiModel
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!isNoSwaggerAnnotation(introspectedTable)) {
            ApiModel apiModelAnnotation = buildApiModelAnnotation(introspectedTable, topLevelClass);
            apiModelAnnotation.addAnnotationToTopLevelClass(topLevelClass);
        }
        return true;
    }


    @Override
    public boolean voModelAbstractClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        ApiModel apiModelAnnotation = buildApiModelAnnotation(introspectedTable, topLevelClass);
        apiModelAnnotation.addAnnotationToTopLevelClass(topLevelClass);
        return true;
    }

    @Override
    public boolean voModelExcelClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        ApiModel apiModelAnnotation = buildApiModelAnnotation(introspectedTable, topLevelClass);
        apiModelAnnotation.addAnnotationToTopLevelClass(topLevelClass);
        return true;
    }

    /**
     * model属性注解@ApiModelProperty
     */
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        //添加日期序列化格式注解
        if (!isNoSwaggerAnnotation(introspectedTable)) {
            ApiModelProperty apiModelPropertyAnnotation = buildApiModelPropertyAnnotation(field, introspectedTable);
            if (apiModelPropertyAnnotation != null) {
                apiModelPropertyAnnotation.addAnnotationToField(field,topLevelClass);
            }
        }
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        return addApiModelProperty(field, topLevelClass, introspectedColumn, introspectedTable);
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        return addApiModelProperty(field, topLevelClass, introspectedColumn, introspectedTable);
    }

    /**
     * ViewVO属性注解@ApiModelProperty
     */
    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        ApiModelProperty apiModelPropertyAnnotation = buildApiModelPropertyAnnotation(field, introspectedTable);
        if (apiModelPropertyAnnotation != null) {
            apiModelPropertyAnnotation.addAnnotationToField(field,topLevelClass);
        }
        return true;
    }

    @Override
    public boolean voRequestFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        return addApiModelProperty(field, topLevelClass, introspectedColumn, introspectedTable);
    }

    @Override
    public boolean voExcelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        return addApiModelProperty(field, topLevelClass, introspectedColumn, introspectedTable);
    }

    @Override
    public boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        return addApiModelProperty(field, topLevelClass, introspectedColumn, introspectedTable);
    }

    @Override
    public boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        return addApiModelProperty(field, topLevelClass, introspectedColumn, introspectedTable);
    }

    /**
     * This method is called just before the getGeneratedXXXFiles methods are called on the introspected table. Plugins
     * can implement this method to override any of the default attributes, or change the results of database
     * introspection, before any code generation activities occur. Attributes are listed as static Strings with the
     * prefix ATTR_ in IntrospectedTable.
     *
     * <p>A good example of overriding an attribute would be the case where a user wanted to change the name of one
     * of the generated classes, change the target package, or change the name of the generated SQL map file.
     *
     * <p><b>Warning:</b> Anything that is listed as an attribute should not be changed by one of the other plugin
     * methods. For example, if you want to change the name of a generated example class, you should not simply change
     * the Type in the <code>modelExampleClassGenerated()</code> method. If you do, the change will not be reflected
     * in other generated artifacts.
     *
     * @param introspectedTable the introspected table
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
    }

    /**
     * controller及方法注解@Api、@ApiOperation
     */
    @Override
    public boolean controllerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!isNoSwaggerAnnotation(introspectedTable)) {
            Api.create(introspectedTable.getRemarks(true)).addAnnotationToTopLevelClass(topLevelClass);
        }
        return true;
    }

    private boolean isNoSwaggerAnnotation(IntrospectedTable introspectedTable) {
        return context.getAnyPropertyBoolean(PropertyRegistry.ANY_NO_SWAGGER_ANNOTATION,
                "false",
                introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration(),
                introspectedTable.getTableConfiguration(),
                context);
    }

    private boolean addApiModelProperty(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        ApiModelProperty apiModelProperty = buildApiModelPropertyAnnotation(field, introspectedTable);
        if (apiModelProperty != null) {
            apiModelProperty.addAnnotationToField(field, topLevelClass);
        }
        return true;
    }

    /**
     * 构造注解@ApiModelProperty
     */
    private ApiModelProperty buildApiModelPropertyAnnotation(Field field, IntrospectedTable introspectedTable) {
        final ApiModelProperty apiModelProperty = new ApiModelProperty(field.getRemark());
        apiModelProperty.setExample(JDBCUtil.getExampleByClassName(field.getType().getFullyQualifiedName()));
        introspectedTable.getAllColumns().stream()
                .filter(column -> column.getJavaProperty().equals(field.getName()))
                .findFirst()
                .ifPresent(column -> {
                    if (!column.isNullable()) {
                        apiModelProperty.setRequired("true");
                    }
        });
        if (apiModelProperty.getValue() == null) {
            return null;
        }
        return apiModelProperty;
    }

    /**
     * model类的@apiModel
     */
    private ApiModel buildApiModelAnnotation(IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        FullyQualifiedJavaType fullyQualifiedJavaType =  topLevelClass.getType();
        ApiModel apiModel = ApiModel.create(fullyQualifiedJavaType.getShortName());
        apiModel.setDescription(introspectedTable.getRemarks(true));
        final Optional<FullyQualifiedJavaType> superClass = topLevelClass.getSuperClass();
        superClass.ifPresent(qualifiedJavaType -> apiModel.setParent(qualifiedJavaType.getShortNameWithoutTypeArguments() + ".class"));
        return apiModel;
    }

}
