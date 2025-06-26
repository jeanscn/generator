package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.IntrospectedTable.TargetRuntime;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.List;
import java.util.Properties;

import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinType;
import org.mybatis.generator.internal.util.JavaBeansUtil;

/**
 * This plugin adds the java.io.Serializable marker interface to all generated
 * model objects.
 *
 * <p>This plugin demonstrates adding capabilities to generated Java artifacts, and
 * shows the proper way to add imports to a compilation unit.
 *
 * <p>Important: This is a simplistic implementation of serializable and does not
 * attempt to do any versioning of classes.
 *
 * @author Jeff Butler
 *
 */
public class SerializablePlugin extends PluginAdapter {

    private final FullyQualifiedJavaType serializable;
    private final FullyQualifiedJavaType gwtSerializable;
    private boolean addGWTInterface;
    private boolean suppressJavaInterface;

    public SerializablePlugin() {
        super();
        serializable = new FullyQualifiedJavaType("java.io.Serializable"); //$NON-NLS-1$
        gwtSerializable = new FullyQualifiedJavaType("com.google.gwt.user.client.rpc.IsSerializable"); //$NON-NLS-1$
    }

    @Override
    public boolean validate(List<String> warnings) {
        // this plugin is always valid
        return true;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        addGWTInterface = Boolean.parseBoolean(properties.getProperty("addGWTInterface")); //$NON-NLS-1$
        suppressJavaInterface = Boolean.parseBoolean(properties.getProperty("suppressJavaInterface")); //$NON-NLS-1$
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        makeSerializable(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        makeSerializable(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        makeSerializable(topLevelClass, introspectedTable);
        return true;
    }

    protected void makeSerializable(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (addGWTInterface) {
            topLevelClass.addImportedType(gwtSerializable);
            topLevelClass.addSuperInterface(gwtSerializable);
        }

        if (!suppressJavaInterface) {
            if (!JavaBeansUtil.isAssignableCurrent(serializable.getFullyQualifiedName(),topLevelClass,introspectedTable)) {
                topLevelClass.addSuperInterface(serializable);
                topLevelClass.addImportedType(serializable);
            }
            Field field = new Field("serialVersionUID",new FullyQualifiedJavaType("long"));
            field.setFinal(true);
            field.setInitializationString("1L"); //$NON-NLS-1$
            field.setStatic(true);
            field.setVisibility(JavaVisibility.PRIVATE);
            if (introspectedTable.getRules().isIntegrateMybatisPlus()) {
                field.addAnnotation("@TableField(exist = false)");
            }
            if (introspectedTable.getContext().getJdkVersion()>8) {
                field.addAnnotation("@Serial");
            }

            if (introspectedTable.getTargetRuntime() == TargetRuntime.MYBATIS3_DSQL) {
                context.getCommentGenerator().addFieldAnnotation(field, introspectedTable,topLevelClass.getImportedTypes());
            }
            boolean exist = false;
            for (Field topLevelClassField : topLevelClass.getFields()) {
                if (topLevelClassField.getName().equals("serialVersionUID")) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                topLevelClass.getFields().add(0,field);
                if (introspectedTable.getContext().getJdkVersion()>8) {
                    topLevelClass.addImportedType(new FullyQualifiedJavaType("java.io.Serial"));
                }
            }
        }
    }

    @Override
    public boolean kotlinDataClassGenerated(KotlinFile kotlinFile, KotlinType dataClass,
            IntrospectedTable introspectedTable) {
        kotlinFile.addImport("java.io.Serializable"); //$NON-NLS-1$
        dataClass.addSuperType("Serializable"); //$NON-NLS-1$
        return true;
    }
}
