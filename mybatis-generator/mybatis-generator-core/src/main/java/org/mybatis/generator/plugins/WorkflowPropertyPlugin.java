package org.mybatis.generator.plugins;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.custom.annotations.swagger3.SchemaPropertyDesc;

import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-15 19:35
 * @version 3.0
 */
public class WorkflowPropertyPlugin extends PluginAdapter{

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        addWorkflowProperty(topLevelClass, abstractIntrospectedTable);
        return true;
    }

    @Override
    public boolean voModelRecordClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        addWorkflowProperty(topLevelClass, abstractIntrospectedTable);
        return true;
    }

    @Override
    public boolean voModelViewClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        addWorkflowProperty(topLevelClass, abstractIntrospectedTable);
        return true;
    }

    /**
     * 添加工作流属性 并根据是否工作流类型，添加初始值，1-工作流，0-非工作流
     * @param topLevelClass 生成的类
     * @param abstractIntrospectedTable 表信息
     */
    private void addWorkflowProperty(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        Field field = new Field("workflowEnabled", new FullyQualifiedJavaType("Integer"));
        if (GenerateUtils.isWorkflowInstance(abstractIntrospectedTable)) {
            field.setInitializationString("1");
        } else {
            field.setInitializationString("0");
        }
        SchemaPropertyDesc schemaPropertyDesc = new SchemaPropertyDesc("是否工作流应用", "0");
        field.addAnnotation(schemaPropertyDesc.toAnnotation());
        topLevelClass.addField(field);
        topLevelClass.addImportedTypes(schemaPropertyDesc.getImportedTypes());
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}
