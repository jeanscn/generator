package org.mybatis.generator.plugins;

import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.sql.JDBCType;
import java.text.MessageFormat;
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

    private void addWebProperties(TopLevelClass topLevelClass, IntrospectedTable introspectedTable,String type) {
        InitializationBlock initializationBlock;
        if (topLevelClass.getInitializationBlocks().size()>0) {
            initializationBlock = topLevelClass.getInitializationBlocks().get(0);
        }else{
            initializationBlock = new InitializationBlock();
        }
        //追加respBasePath属性
        addRespBasePath(topLevelClass, introspectedTable, initializationBlock,type);
        //追加viewPath
        addViewPath(topLevelClass, introspectedTable, initializationBlock,type);
        if (topLevelClass.getInitializationBlocks().size()==0) {
            topLevelClass.addInitializationBlock(initializationBlock);
        }
    }

    private void addViewPath(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, InitializationBlock initializationBlock,String type) {
        HtmlGeneratorConfiguration htmlGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations()
                .stream()
                .filter(t -> stringHasValue(t.getViewPath()))
                .findFirst().orElse(null);
        if (htmlGeneratorConfiguration != null) {
            Field viewPath = new Field(PROP_NAME_VIEW_PATH, FullyQualifiedJavaType.getStringInstance());
            viewPath.setVisibility(JavaVisibility.PRIVATE);
            if (topLevelClass.addField(viewPath,null,true)) {
                if (!introspectedTable.getRules().isNoSwaggerAnnotation()) {
                    viewPath.addAnnotation("@ApiModelProperty(value = \"视图路径\",hidden = true)");
                    topLevelClass.addMultipleImports("ApiModelProperty");
                }
                if (type.equals("model") && introspectedTable.getRules().isIntegrateMybatisPlus()) {
                    viewPath.addAnnotation("@TableField(exist = false)");
                    topLevelClass.addMultipleImports("TableField");
                }
            }
            initializationBlock.addBodyLine(VStringUtil.format("this.{0} = \"{1}\";", PROP_NAME_VIEW_PATH, htmlGeneratorConfiguration.getViewPath()));
            boolean assignable = JavaBeansUtil.isAssignableCurrent(ConstantsUtil.I_SHOW_IN_VIEW, topLevelClass, introspectedTable);
            if (!assignable) {
                FullyQualifiedJavaType showInView = new FullyQualifiedJavaType(I_SHOW_IN_VIEW);
                topLevelClass.addImportedType(showInView);
                topLevelClass.addSuperInterface(showInView);
            }
        }
    }

    private void addRespBasePath(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, InitializationBlock initializationBlock,String type) {
        if (stringHasValue(introspectedTable.getControllerSimplePackage())) {
            Field field = new Field(PROP_NAME_REST_BASE_PATH, FullyQualifiedJavaType.getStringInstance());
            field.setVisibility(JavaVisibility.PRIVATE);
            if(topLevelClass.addField(field, null, true)){
                if (!introspectedTable.getRules().isNoSwaggerAnnotation()) {
                    field.addAnnotation("@ApiModelProperty(value = \"Restful请求中的跟路径\",hidden = true)");
                }
                if (type.equals("model") && introspectedTable.getRules().isIntegrateMybatisPlus()) {
                    field.addAnnotation("@TableField(exist = false)");
                    topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableField");
                }
                initializationBlock.addBodyLine(VStringUtil.format("this.{0} = \"{1}\";",PROP_NAME_REST_BASE_PATH, introspectedTable.getControllerSimplePackage()));
            }
        }
    }


}
