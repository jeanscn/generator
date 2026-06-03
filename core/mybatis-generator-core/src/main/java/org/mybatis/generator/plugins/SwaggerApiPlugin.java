package org.mybatis.generator.plugins;

import com.vgosoft.core.db.util.JdbcUtil;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.custom.annotations.swagger3.SchemaDesc;
import org.mybatis.generator.custom.annotations.swagger3.SchemaPropertyDesc;
import org.mybatis.generator.custom.annotations.swagger3.TagDesc;

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
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        if (isSwaggerAnnotation(introspectedTable)) {
            SchemaDesc schemaDesc = buildSchemaDescAnnotation(introspectedTable, topLevelClass);
            if (schemaDesc != null) {
                schemaDesc.addAnnotationToTopLevelClass(topLevelClass);
            }
        }
        return true;
    }


    @Override
    public boolean voModelAbstractClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        SchemaDesc schemaDesc = buildSchemaDescAnnotation(introspectedTable, topLevelClass);
        if (schemaDesc != null) {
            schemaDesc.addAnnotationToTopLevelClass(topLevelClass);
        }
        return true;
    }

    @Override
    public boolean voModelExcelClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        SchemaDesc schemaDesc = buildSchemaDescAnnotation(introspectedTable, topLevelClass);
        if (schemaDesc != null) {
            schemaDesc.addAnnotationToTopLevelClass(topLevelClass);
        }
        return true;
    }

    @Override
    public boolean voModelExcelImportClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        SchemaDesc schemaDesc = buildSchemaDescAnnotation(introspectedTable, topLevelClass);
        if (schemaDesc != null) {
            schemaDesc.addAnnotationToTopLevelClass(topLevelClass);
        }
        return true;
    }

    /**
     * model属性注解@Schema
     */
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (isSwaggerAnnotation(introspectedTable)) {
            addSchemaProperty(field, topLevelClass, introspectedColumn, introspectedTable, false);
        }
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        SchemaPropertyDesc schemaPropertyDesc = buildSchemaPropertyAnnotation(field, introspectedTable);
        if (schemaPropertyDesc != null) {
            if (!introspectedColumn.isNullable()) {
                schemaPropertyDesc.setRequiredMode("Schema.RequiredMode.REQUIRED");
            }
            schemaPropertyDesc.addAnnotationToField(field, topLevelClass);
        }
        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        if (isSwaggerAnnotation(introspectedTable)) {
            return addSchemaProperty(field, topLevelClass, introspectedColumn, introspectedTable, true);
        }
        return true;
    }

    /**
     * ViewVo属性注解@SchemaProperty
     */
    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        SchemaPropertyDesc schemaPropertyDesc = buildSchemaPropertyAnnotation(field, introspectedTable);
        if (schemaPropertyDesc != null) {
            schemaPropertyDesc.addAnnotationToField(field, topLevelClass);
        }
        return true;
    }

    @Override
    public boolean voRequestFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        if (isSwaggerAnnotation(introspectedTable)) {
            return addSchemaProperty(field, topLevelClass, introspectedColumn, introspectedTable, false);
        }
        return true;
    }

    @Override
    public boolean voExcelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable, int index) {
        if (isSwaggerAnnotation(introspectedTable)) {
            return addSchemaProperty(field, topLevelClass, introspectedColumn, introspectedTable, false);
        }
        return true;
    }

    @Override
    public boolean voExcelImportFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable, int index) {
        if (isSwaggerAnnotation(introspectedTable)) {
            return addSchemaProperty(field, topLevelClass, introspectedColumn, introspectedTable, false);
        }
        return true;
    }

    @Override
    public boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        if (isSwaggerAnnotation(introspectedTable)) {
            return addSchemaProperty(field, topLevelClass, introspectedColumn, introspectedTable, true);
        }
        return true;
    }

    @Override
    public boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        if (isSwaggerAnnotation(introspectedTable)) {
            return addSchemaProperty(field, topLevelClass, introspectedColumn, introspectedTable, false);
        }
        return true;
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
    public void initialized(AbstractIntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
    }

    /**
     * controller及方法注解@Api、@ApiOperation
     */
    @Override
    public boolean controllerGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        if (isSwaggerAnnotation(introspectedTable)) {
            TagDesc.create(introspectedTable.getRemarks(true)).addAnnotationToTopLevelClass(topLevelClass);
        }
        return true;
    }

    private boolean isSwaggerAnnotation(AbstractIntrospectedTable introspectedTable) {
        return !context.getAnyPropertyBoolean(PropertyRegistry.ANY_NO_SWAGGER_ANNOTATION,
                "false",
                introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration(),
                introspectedTable.getTableConfiguration(),
                context);
    }

    /**
     * model属性注解@SchemaProperty
     */
    private boolean addSchemaProperty(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable, boolean required) {
        Optional<String> exist = field.getAnnotations().stream().filter(annotation -> annotation.contains(SchemaPropertyDesc.ANNOTATION_NAME)).findFirst();
        if (exist.isPresent()) {
            return true;
        }
        SchemaPropertyDesc schemaPropertyDesc = buildSchemaPropertyAnnotation(field, introspectedTable);
        if (schemaPropertyDesc != null) {
            if (required) {
                schemaPropertyDesc.setRequiredMode("Schema.RequiredMode.REQUIRED");
            } else {
                schemaPropertyDesc.setRequiredMode(null);
            }
            schemaPropertyDesc.addAnnotationToField(field, topLevelClass);
        }
        return true;
    }

    /**
     * 构造注解@SchemaProperty
     */
    private SchemaPropertyDesc buildSchemaPropertyAnnotation(Field field, AbstractIntrospectedTable introspectedTable) {
        SchemaPropertyDesc schemaPropertyDesc = new SchemaPropertyDesc(field.getRemark());
        schemaPropertyDesc.setExample(JdbcUtil.getExampleByClassName(field.getType().getFullyQualifiedNameWithoutTypeParameters(), field.getName(), 0));
        introspectedTable.getAllColumns().stream()
                .filter(column -> column.getJavaProperty().equals(field.getName()))
                .findFirst()
                .ifPresent(column -> {
                    if (!column.isNullable()) {
                        schemaPropertyDesc.setRequiredMode("Schema.RequiredMode.REQUIRED");
                    }
                });
        if (schemaPropertyDesc.getDescription() == null) {
            return null;
        }
        return schemaPropertyDesc;
    }

    /**
     * 构造注解@Schema
     */
    private SchemaDesc buildSchemaDescAnnotation(AbstractIntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        Optional<String> exist = topLevelClass.getAnnotations().stream().filter(annotation -> annotation.contains(SchemaDesc.ANNOTATION_NAME)).findFirst();
        if (exist.isPresent()) {
            return null;
        }
        FullyQualifiedJavaType fullyQualifiedJavaType = topLevelClass.getType();
        SchemaDesc schemaDesc = SchemaDesc.create(fullyQualifiedJavaType.getShortName());
        schemaDesc.setDescription(introspectedTable.getRemarks(true));
        final Optional<FullyQualifiedJavaType> superClass = topLevelClass.getSuperClass();
        superClass.ifPresent(qualifiedJavaType -> schemaDesc.setImplementation(qualifiedJavaType.getShortNameWithoutTypeArguments() + ".class"));
        return schemaDesc;
    }

}
