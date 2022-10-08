package org.mybatis.generator.plugins;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

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

    public static final String  API_MODEL = "io.swagger.annotations.ApiModel";
    public static final String API_MODEL_PROPERTY = "io.swagger.annotations.ApiModelProperty";
    public static final String API = "io.swagger.annotations.Api";

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
            String apiModelAnnotation = buildApiModelAnnotation(introspectedTable, topLevelClass);
            topLevelClass.addAnnotation(apiModelAnnotation);
            topLevelClass.addImportedType(API_MODEL);
        }
        return true;
    }


    @Override
    public boolean voModelAbstractClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String apiModelAnnotation = buildApiModelAnnotation(introspectedTable, topLevelClass);
        topLevelClass.addAnnotation(apiModelAnnotation);
        topLevelClass.addImportedType(API_MODEL);
        return true;
    }

    @Override
    public boolean voModelExcelClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String apiModelAnnotation = buildApiModelAnnotation(introspectedTable, topLevelClass);
        topLevelClass.addAnnotation(apiModelAnnotation);
        topLevelClass.addImportedType(API_MODEL);
        return true;
    }

    /**
     * model属性注解@ApiModelProperty
     */
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        //添加日期序列化格式注解
        if (!isNoSwaggerAnnotation(introspectedTable)) {
            String apiModelPropertyAnnotation = buildApiModelPropertyAnnotation(field, introspectedTable, "m");
            if (StringUtility.stringHasValue(apiModelPropertyAnnotation)) {
                field.addAnnotation(apiModelPropertyAnnotation);
                topLevelClass.addImportedType(API_MODEL_PROPERTY);
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
        String apiModelPropertyAnnotation = buildApiModelPropertyAnnotation(field, introspectedTable, "view");
        if (StringUtility.stringHasValue(apiModelPropertyAnnotation)) {
            field.addAnnotation(apiModelPropertyAnnotation);
            topLevelClass.addImportedType(API_MODEL_PROPERTY);
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

    /**
     * controller及方法注解@Api、@ApiOperation
     */
    @Override
    public boolean controllerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!isNoSwaggerAnnotation(introspectedTable)) {
            topLevelClass.addAnnotation(VStringUtil.format("@Api(tags = \"{0}\")", introspectedTable.getRemarks(true)));
            topLevelClass.addImportedType(API);
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
        String apiModelPropertyAnnotation = buildApiModelPropertyAnnotation(field, introspectedTable, "v");
        if (StringUtility.stringHasValue(apiModelPropertyAnnotation)) {
            field.addAnnotation(apiModelPropertyAnnotation);
            topLevelClass.addImportedType(API_MODEL_PROPERTY);
        }
        return true;
    }

    /**
     * 构造注解@ApiModelProperty
     * @param type 类型：v-vo类，m-model类（entity），view-视图的vo类
     */
    private String buildApiModelPropertyAnnotation(Field field, IntrospectedTable introspectedTable, String type) {
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.getJavaProperty().equals(field.getName())) {
                sb.append("@ApiModelProperty(").append("value = \"");
                sb.append(column.getRemarks(true)).append("\"");
                if (type.equals("v")) {
                    switch (field.getType().getShortName().toLowerCase()) {
                        case "string":
                            sb.append(VStringUtil.format(",example = \"{0}\"", field.getName()));
                            break;
                        case "integer":
                            if (column.getJavaProperty().equals("active")) {
                                sb.append(",example = \"1\"");
                            }else if(column.getJavaProperty().equals("sort")){
                                sb.append(",example = \"1000\"");
                            }else{
                                sb.append(",example = \"0\"");
                            }
                            break;
                        case "boolean":
                            sb.append(",example = \"true\"");
                            break;
                        case "double":
                            sb.append(",example = \"0.0\"");
                            break;
                        case "float":
                            sb.append(",example = \"0.0f\"");
                            break;
                    }
                }
                if (!column.isNullable() && type.equals("m")) {
                    sb.append(",required = true");
                }
                sb.append(")");
                return sb.toString();
            }
        }
        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * model类的@apiModel
     */
    private String buildApiModelAnnotation(IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        StringBuilder sb = new StringBuilder();
        FullyQualifiedJavaType fullyQualifiedJavaType =  topLevelClass.getType();
        sb.append("@ApiModel(value = \"").append(fullyQualifiedJavaType.getShortName()).append("\"");
        if (introspectedTable.getRemarks(true) != null) {
            sb.append(", description = \"").append(introspectedTable.getRemarks(true)).append("\"");
        } else {
            sb.append(", description = \"").append("\"");
        }
        final Optional<FullyQualifiedJavaType> superClass = topLevelClass.getSuperClass();
        if (superClass.isPresent()) {
            final String clazz = superClass.get().getShortNameWithoutTypeArguments() + ".class";
            sb.append(", parent = ");
            sb.append(clazz);
        }
        sb.append(" )");
        return sb.toString();
    }

}
