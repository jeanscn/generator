package org.mybatis.generator.plugins;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;
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

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
    }

    /**
     * model类的@apiModel
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        try {
            boolean propertyBoolean = context.getAnyPropertyBoolean(PropertyRegistry.ANY_NO_SWAGGER_ANNOTATION,
                    null,
                    introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration(),
                    introspectedTable.getTableConfiguration(),
                    context);
            if (!propertyBoolean) {
                String apiModelAnnotation = buildApiModelAnnotation(introspectedTable, topLevelClass);
                topLevelClass.addAnnotation(apiModelAnnotation);
                topLevelClass.addImportedType("io.swagger.annotations.ApiModel");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public boolean voModelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String apiModelAnnotation = buildApiModelAnnotation(introspectedTable, topLevelClass);
        topLevelClass.addAnnotation(apiModelAnnotation);
        topLevelClass.addImportedType("io.swagger.annotations.ApiModel");
        return true;
    }

    /**
     * 属性注解@ApiModelProperty
     */
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        //添加日期序列化格式注解
        try {
            boolean propertyBoolean = context.getAnyPropertyBoolean(PropertyRegistry.ANY_NO_SWAGGER_ANNOTATION,
                    null,
                    introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration(),
                    introspectedTable.getTableConfiguration(),
                    context);
            if (!propertyBoolean) {
                String apiModelPropertyAnnotation = buildApiModelPropertyAnnotation(field, introspectedTable);
                if (StringUtility.stringHasValue(apiModelPropertyAnnotation)) {
                    field.addAnnotation(apiModelPropertyAnnotation);
                    topLevelClass.addImportedType("io.swagger.annotations.ApiModelProperty");
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        String apiModelPropertyAnnotation = buildApiModelPropertyAnnotation(field, introspectedTable);
        if (StringUtility.stringHasValue(apiModelPropertyAnnotation)) {
            field.addAnnotation(apiModelPropertyAnnotation);
            topLevelClass.addImportedType("io.swagger.annotations.ApiModelProperty");
        }
        return true;
    }

    /**
     * controller及方法注解@Api、@ApiOperation
     */
    @Override
    public boolean ControllerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        boolean propertyBoolean = context.getAnyPropertyBoolean(PropertyRegistry.ANY_NO_SWAGGER_ANNOTATION,
                null,
                introspectedTable.getTableConfiguration().getJavaControllerGeneratorConfiguration(),
                introspectedTable.getTableConfiguration(),
                context);
        if (!propertyBoolean) {
            topLevelClass.addAnnotation(VStringUtil.format("@Api(tags = \"{0}\")", StringUtility.remarkLeft(introspectedTable.getRemarks())));
            for (Method method : topLevelClass.getMethods()) {
                addMethodApiSwaggerAnnotation(method,introspectedTable);
            }
            topLevelClass.addImportedType("io.swagger.annotations.Api");
            topLevelClass.addImportedType("io.swagger.annotations.ApiOperation");
        }
        return true;
    }

    /**
     * 构造注解@ApiModelProperty
     */
    private String buildApiModelPropertyAnnotation(Field field, IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.getJavaProperty().equals(field.getName())) {
                sb.append("@ApiModelProperty(").append("value = \"");
                sb.append(column.getRemarks()).append("\"");
                switch (field.getType().getShortName().toLowerCase()){
                    case "string":
                        sb.append(VStringUtil.format(",example = \"{0}\"",field.getName()));
                        break;
                    case "integer":
                        sb.append(",example = \"0\"");
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
                if (!column.isNullable()) {
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
        if(("view"+record.getShortName()).equals(method.getName())){
            buildSwaggerApiAnnotation(method,"获得数据并返回页面视图（可用于普通业务在列表中新建接口）",
                    "根据给定id获取单个实体，id为可选参数，当id存在时查询数据，否则直接返回视图");
        }
        if(("get"+record.getShortName()).equals(method.getName())){
            buildSwaggerApiAnnotation(method,"获得单条记录","根据给定id获取单个实体");
        }
        if(("list"+record.getShortName()).equals(method.getName())){
            buildSwaggerApiAnnotation(method,"获得数据列表",
                    "根据给定条件获取多条或所有数据列表，可以根据需要传入属性同名参数");
        }
        if(("create"+record.getShortName()).equals(method.getName())){
            buildSwaggerApiAnnotation(method,"新增一条记录","新增一条记录,返回json，包含影响条数及消息");
        }
        if(("upload"+record.getShortName()).equals(method.getName())){
            buildSwaggerApiAnnotation(method,"单个文件上传","单个文件上传接口");
        }
        if(("download"+record.getShortName()).equals(method.getName())){
            buildSwaggerApiAnnotation(method,"单个文件下载","单个文件下载接口");
        }
        if(("update"+record.getShortName()).equals(method.getName())){
            buildSwaggerApiAnnotation(method,"更新一条记录","根据主键更新实体对象");
        }
        if(("delete"+record.getShortName()).equals(method.getName())){
            buildSwaggerApiAnnotation(method,"单条记录删除","根据给定的id删除一条记录");
        }
        if(("deleteBatch"+record.getShortName()).equals(method.getName())){
            buildSwaggerApiAnnotation(method,"批量记录删除","根据给定的一组id删除多条记录");
        }
    }

    private void buildSwaggerApiAnnotation(Method method, String value, String notes) {
        StringBuilder sb = new StringBuilder();
        if (StringUtility.stringHasValue(value)) sb.append("value = \""+value+"\"");
        if (StringUtility.stringHasValue(notes)) {
            if (sb.length()>0) sb.append(",");
            sb.append("notes = \""+notes+"\"");
        }
        if (sb.length()>0) {
            method.addAnnotation("@ApiOperation("+ sb +")");
        }
    }

}
