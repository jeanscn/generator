package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.VOGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.ApiModelProperty;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;

import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-15 19:35
 * @version 3.0
 */
public class WorkflowPropertyPlugin extends PluginAdapter{

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addWorkflowProperty(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean voModelRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addWorkflowProperty(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean voModelViewClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addWorkflowProperty(topLevelClass, introspectedTable);
        return true;
    }

    //添加一个属性，并根据是否工作流类型，添加初始值，1-工作流，0-非工作流
    private void addWorkflowProperty(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Field field = new Field("workflowEnabled", new FullyQualifiedJavaType("Integer"));
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            field.setInitializationString("1");
        } else {
            field.setInitializationString("0");
        }
        ApiModelProperty apiModelProperty = new ApiModelProperty("是否工作流应用", "0");
        field.addAnnotation(apiModelProperty.toAnnotation());
        topLevelClass.addField(field);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}