package org.mybatis.generator.plugins;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.custom.FieldItem;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.annotations.ApiModelPropertyDesc;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.StringUtility.*;

/**
 * 添加日期属性的JsonFormat
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-04-19 14:51
 * @version 3.0
 */
public class ModelWebPropertiesPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    //entity实体生成后的调整
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        addWebProperties(topLevelClass, abstractIntrospectedTable,"model");
        return true;
    }

    @Override
    public boolean voModelRecordClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        addWebProperties(topLevelClass, abstractIntrospectedTable,"voModel");
        return true;
    }

    @Override
    public boolean voModelViewClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        addWebProperties(topLevelClass, abstractIntrospectedTable, "view");
        return true;
    }

    private void addWebProperties(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable, String type) {
        //追加respBasePath属性
        addRespBasePath(topLevelClass, abstractIntrospectedTable, type);
        //追加viewPath
        addViewPath(topLevelClass, abstractIntrospectedTable, type);
    }

    private void addViewPath(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable, String type) {
        HtmlGeneratorConfiguration htmlGeneratorConfiguration = abstractIntrospectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations()
                .stream()
                .filter(t -> stringHasValue(t.getViewPath()))
                .findFirst().orElse(null);
        if (htmlGeneratorConfiguration != null && stringHasValue(htmlGeneratorConfiguration.getViewPath())) {
            Field viewPath = new Field(PROP_NAME_VIEW_PATH, FullyQualifiedJavaType.getStringInstance());
            viewPath.setVisibility(JavaVisibility.PRIVATE);
            viewPath.setInitializationString("\""+htmlGeneratorConfiguration.getViewPath()+"\"");
            viewPath.setRemark("视图路径");
            if (topLevelClass.addField(viewPath,null,true)) {
                if (!abstractIntrospectedTable.getRules().isNoSwaggerAnnotation()) {
                    ApiModelPropertyDesc apiModelPropertyDesc = new ApiModelPropertyDesc("视图路径",htmlGeneratorConfiguration.getViewPath());
                    apiModelPropertyDesc.setHidden("true");
                    apiModelPropertyDesc.addAnnotationToField(viewPath, topLevelClass);
                }
                if (type.equals("model") && abstractIntrospectedTable.getRules().isIntegrateMybatisPlus()) {
                    viewPath.addAnnotation("@TableField(exist = false)");
                    topLevelClass.addMultipleImports("TableField");
                }
                abstractIntrospectedTable.getVoModelFields().add(new FieldItem(viewPath));
            }
            boolean assignable = JavaBeansUtil.isAssignableCurrent(ConstantsUtil.I_SHOW_IN_VIEW, topLevelClass, abstractIntrospectedTable);
            if (!assignable) {
                FullyQualifiedJavaType showInView = new FullyQualifiedJavaType(I_SHOW_IN_VIEW);
                topLevelClass.addImportedType(showInView);
                topLevelClass.addSuperInterface(showInView);
            }
        }
    }

    private void addRespBasePath(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable, String type) {
        if (stringHasValue(abstractIntrospectedTable.getControllerSimplePackage())) {
            Field field = new Field(PROP_NAME_REST_BASE_PATH, FullyQualifiedJavaType.getStringInstance());
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setInitializationString("\""+ Mb3GenUtil.getControllerBaseMappingPath(abstractIntrospectedTable) +"\"");
            field.setRemark("Restful请求中的跟路径");
            if(topLevelClass.addField(field, null, true)){
                if (!abstractIntrospectedTable.getRules().isNoSwaggerAnnotation()) {
                    ApiModelPropertyDesc apiModelPropertyDesc = new ApiModelPropertyDesc("Restful请求中的跟路径", "api/serv");
                    apiModelPropertyDesc.setHidden("true");
                    apiModelPropertyDesc.addAnnotationToField(field, topLevelClass);
                }
                if (type.equals("model") && abstractIntrospectedTable.getRules().isIntegrateMybatisPlus()) {
                    field.addAnnotation("@TableField(exist = false)");
                    topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableField");
                }
                abstractIntrospectedTable.getVoModelFields().add(new FieldItem(field));
            }
        }
    }


}
