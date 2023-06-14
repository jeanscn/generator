package org.mybatis.generator.plugins;

import com.vgosoft.core.constant.enums.db.DefultColumnNameEnum;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.HtmlConstant;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.HtmlDocumentGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.LayuiDocumentGenerated;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.ZuiDocumentGenerated;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.List;
import java.util.Set;

import static org.mybatis.generator.custom.ConstantsUtil.ANNOTATION_REPOSITORY;
import static org.mybatis.generator.custom.ConstantsUtil.I_SIMPLE_KVP;

/**
 * dao生成插件
 *
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * 2020-07-14 05:23
 * @version 3.0
 */
public class JavaClientGeneratePlugins extends PluginAdapter implements Plugin, HtmlConstant {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * model类生成后，进行符合性调整。
     * 添加@TableMeta和@ColumnMeta注解
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //添加@Repository注解
        topLevelClass.addImportedType(new FullyQualifiedJavaType(ANNOTATION_REPOSITORY));
        JavaBeansUtil.addAnnotation(topLevelClass, "@Repository");

        //添加@Setter,@Getter
        topLevelClass.addImportedType("lombok.*");
        topLevelClass.addAnnotation("@Data");
        if (!introspectedTable.isConstructorBased()) {
            topLevelClass.addAnnotation("@NoArgsConstructor");
        }
        if (topLevelClass.getSuperClass().isPresent()) {
            topLevelClass.addAnnotation("@EqualsAndHashCode(callSuper = true)");
            topLevelClass.addAnnotation("@ToString(callSuper = true)");
        }else{
            topLevelClass.addAnnotation("@EqualsAndHashCode");
            topLevelClass.addAnnotation("@ToString");
        }

        //检查是否存在children属性，如果存在则追加实现ISimpleKVP接口
        Set<String> fieldNames = introspectedTable.getTableConfiguration().getFieldNames();
        if (fieldNames.contains(DefultColumnNameEnum.ID.fieldName())
                && fieldNames.contains(DefultColumnNameEnum.NAME.fieldName())
                && fieldNames.contains(DefultColumnNameEnum.PARENT_ID.fieldName())
                && topLevelClass.getFields().stream().anyMatch(f->f.getName().equals(DefultColumnNameEnum.CHILDREN.fieldName()))) {
            FullyQualifiedJavaType simpleKvpType = new FullyQualifiedJavaType(I_SIMPLE_KVP);
            topLevelClass.addImportedType(I_SIMPLE_KVP);
            simpleKvpType.addTypeArgument(topLevelClass.getType());
            topLevelClass.addSuperInterface(simpleKvpType);
        }

        return true;
    }

    @Override
    public boolean htmlMapDocumentGenerated(org.mybatis.generator.api.dom.html.Document document, IntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        HtmlDocumentGenerator htmlDocumentGenerated;
        String uiFrame = htmlGeneratorConfiguration.getLayoutDescriptor().getUiFrameType();
        if (HTML_UI_FRAME_LAYUI.equals(uiFrame)) {
            htmlDocumentGenerated = new LayuiDocumentGenerated(document, introspectedTable, htmlGeneratorConfiguration);
        } else if (HTML_UI_FRAME_ZUI.equals(uiFrame)) {
            htmlDocumentGenerated = new ZuiDocumentGenerated(document, introspectedTable, htmlGeneratorConfiguration);
        } else {
            htmlDocumentGenerated = new LayuiDocumentGenerated(document, introspectedTable, htmlGeneratorConfiguration);
        }
        return htmlDocumentGenerated.htmlMapDocumentGenerated();
    }
}
