package org.mybatis.generator.plugins;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;
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
    public static final String API_OPERATION = "io.swagger.annotations.ApiOperation";

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

    /**
     * 属性注解@ApiModelProperty
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

    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        String apiModelPropertyAnnotation = buildApiModelPropertyAnnotation(field, introspectedTable, "view");
        if (StringUtility.stringHasValue(apiModelPropertyAnnotation)) {
            field.addAnnotation(apiModelPropertyAnnotation);
            topLevelClass.addImportedType(API_MODEL_PROPERTY);
        }
        return true;
    }

    /**
     * controller及方法注解@Api、@ApiOperation
     */
    @Override
    public boolean controllerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!isNoSwaggerAnnotation(introspectedTable)) {
            topLevelClass.addAnnotation(VStringUtil.format("@Api(tags = \"{0}\")", StringUtility.remarkLeft(introspectedTable.getRemarks())));
            for (Method method : topLevelClass.getMethods()) {
                addMethodApiSwaggerAnnotation(method, introspectedTable);
            }
            topLevelClass.addImportedType(API);
            topLevelClass.addImportedType(API_OPERATION);
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
                sb.append(column.getRemarks()).append("\"");
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
        FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        sb.append("@ApiModel(value = \"").append(fullyQualifiedJavaType.getShortName()).append("\"");
        if (introspectedTable.getRemarks() != null) {
            sb.append(", description = \"").append(introspectedTable.getRemarks()).append("\"");
        } else {
            sb.append(", description = \"").append("\"");
        }
        final Optional<FullyQualifiedJavaType> superClass = topLevelClass.getSuperClass();
        if (superClass.isPresent()) {
            final String clazz = superClass.get().getShortName() + ".class";
            sb.append(", parent = ");
            sb.append(clazz);
        }
        sb.append(" )");
        return sb.toString();
    }

    private void addMethodApiSwaggerAnnotation(Method method, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType record = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        if (("view" + record.getShortName()).equals(method.getName())) {
            buildSwaggerApiAnnotation(method, "获得数据并返回页面视图（可用于普通业务在列表中新建接口）",
                    "根据给定id获取单个实体，id为可选参数，当id存在时查询数据，否则直接返回视图");
        }else if (("get" + record.getShortName()).equals(method.getName())) {
            buildSwaggerApiAnnotation(method, "获得单条记录", "根据给定id获取单个实体");
        }else if (("list" + record.getShortName()).equals(method.getName())) {
            buildSwaggerApiAnnotation(method, "获得数据列表",
                    "根据给定条件获取多条或所有数据列表，可以根据需要传入属性同名参数");
        }else if (("create" + record.getShortName()).equals(method.getName())) {
            buildSwaggerApiAnnotation(method, "新增一条记录", "新增一条记录,返回json，包含影响条数及消息");
        }else if (("upload" + record.getShortName()).equals(method.getName())) {
            buildSwaggerApiAnnotation(method, "单个文件上传", "单个文件上传接口");
        }else if (("download" + record.getShortName()).equals(method.getName())) {
            buildSwaggerApiAnnotation(method, "单个文件下载", "单个文件下载接口");
        }else if (("update" + record.getShortName()).equals(method.getName())) {
            buildSwaggerApiAnnotation(method, "更新一条记录", "根据主键更新实体对象");
        }else if (("delete" + record.getShortName()).equals(method.getName())) {
            buildSwaggerApiAnnotation(method, "单条记录删除", "根据给定的id删除一条记录");
        }else if (("deleteBatch" + record.getShortName()).equals(method.getName())) {
            buildSwaggerApiAnnotation(method, "批量记录删除", "根据给定的一组id删除多条记录");
        }else if (("getDefaultViewConfig" + record.getShortName()).equals(method.getName())) {
            buildSwaggerApiAnnotation(method, "默认数据视图配置", "获取默认数据视图配置");
        }else if (("getDefaultView" + record.getShortName()).equals(method.getName())) {
            buildSwaggerApiAnnotation(method, "默认数据视图显示", "显示默认数据视图");
        }else if (VStringUtil.contains(method.getName(), "option")) {
            String property = VStringUtil.replace(method.getName(), "option", "").replace(record.getShortName(), "");
            buildSwaggerApiAnnotation(method, "获取Options-"+ JavaBeansUtil.getFirstCharacterLowercase(property) +"选项列表", "根据给定条件获取Options-"+ JavaBeansUtil.getFirstCharacterLowercase(property) +"选项列表，可以根据需要传入属性同名参数、前段选中的值");
        }
    }

    private void buildSwaggerApiAnnotation(Method method, String value, String notes) {
        StringBuilder sb = new StringBuilder();
        if (StringUtility.stringHasValue(value)) sb.append("value = \"").append(value).append("\"");
        if (StringUtility.stringHasValue(notes)) {
            if (sb.length() > 0) sb.append(",");
            sb.append("notes = \"").append(notes).append("\"");
        }
        if (sb.length() > 0) {
            method.addAnnotation("@ApiOperation(" + sb + ")");
        }
    }

}
