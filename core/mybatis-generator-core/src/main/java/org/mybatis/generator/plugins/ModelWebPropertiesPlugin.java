package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
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
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addWebProperties(topLevelClass, introspectedTable,"model");
        return true;
    }

    @Override
    public boolean voModelRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addWebProperties(topLevelClass, introspectedTable,"voModel");
        return true;
    }

    @Override
    public boolean voModelViewClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addWebProperties(topLevelClass, introspectedTable, "view");
        return true;
    }

    private void addWebProperties(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String type) {
        //追加respBasePath属性
        addRespBasePath(topLevelClass, introspectedTable, type);
        //追加viewPath
        addViewPath(topLevelClass, introspectedTable, type);
    }

    private void addViewPath(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String type) {
        HtmlGeneratorConfiguration htmlGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations()
                .stream()
                .filter(t -> stringHasValue(t.getViewPath()))
                .findFirst().orElse(null);
        if (htmlGeneratorConfiguration != null && stringHasValue(htmlGeneratorConfiguration.getViewPath())) {
            Field viewPath = new Field(PROP_NAME_VIEW_PATH, FullyQualifiedJavaType.getStringInstance());
            viewPath.setVisibility(JavaVisibility.PRIVATE);
            viewPath.setInitializationString("\""+htmlGeneratorConfiguration.getViewPath()+"\"");
            if (topLevelClass.addField(viewPath,null,true)) {
                if (!introspectedTable.getRules().isNoSwaggerAnnotation()) {
                    ApiModelPropertyDesc apiModelPropertyDesc = new ApiModelPropertyDesc("视图路径",htmlGeneratorConfiguration.getViewPath());
                    apiModelPropertyDesc.setHidden("true");
                    apiModelPropertyDesc.addAnnotationToField(viewPath, topLevelClass);
                }
                if (type.equals("model") && introspectedTable.getRules().isIntegrateMybatisPlus()) {
                    viewPath.addAnnotation("@TableField(exist = false)");
                    topLevelClass.addMultipleImports("TableField");
                }
            }
            boolean assignable = JavaBeansUtil.isAssignableCurrent(ConstantsUtil.I_SHOW_IN_VIEW, topLevelClass, introspectedTable);
            if (!assignable) {
                FullyQualifiedJavaType showInView = new FullyQualifiedJavaType(I_SHOW_IN_VIEW);
                topLevelClass.addImportedType(showInView);
                topLevelClass.addSuperInterface(showInView);
            }
        }
    }

    private void addRespBasePath(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String type) {
        if (stringHasValue(introspectedTable.getControllerSimplePackage())) {
            Field field = new Field(PROP_NAME_REST_BASE_PATH, FullyQualifiedJavaType.getStringInstance());
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setInitializationString("\""+ Mb3GenUtil.getControllerBaseMappingPath(introspectedTable) +"\"");
            if(topLevelClass.addField(field, null, true)){
                if (!introspectedTable.getRules().isNoSwaggerAnnotation()) {
                    ApiModelPropertyDesc apiModelPropertyDesc = new ApiModelPropertyDesc("Restful请求中的跟路径", "api/serv");
                    apiModelPropertyDesc.setHidden("true");
                    apiModelPropertyDesc.addAnnotationToField(field, topLevelClass);
                }
                if (type.equals("model") && introspectedTable.getRules().isIntegrateMybatisPlus()) {
                    field.addAnnotation("@TableField(exist = false)");
                    topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableField");
                }
            }
        }
    }


}
