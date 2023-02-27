package org.mybatis.generator.custom;

import com.vgosoft.core.db.util.JDBCUtil;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.HtmlConstants;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.ApiModelProperty;
import org.mybatis.generator.custom.annotations.mybatisplus.TableField;
import org.mybatis.generator.custom.htmlGenerator.HtmlDocumentGenerator;
import org.mybatis.generator.custom.htmlGenerator.LayuiDocumentGenerated;
import org.mybatis.generator.custom.htmlGenerator.ZuiDocumentGenerated;
import org.mybatis.generator.custom.pojo.RelationGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.*;

/**
 * dao生成插件
 *
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * 2020-07-14 05:23
 * @version 3.0
 */
public class JavaClientGeneratePlugins extends PluginAdapter implements Plugin {

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

        //添加静态代码块
        String beanName = introspectedTable.getControllerBeanName();
        InitializationBlock initializationBlock = new InitializationBlock(false);

        //添加一个参数的构造器
        boolean assignable1 = JavaBeansUtil.isAssignableCurrent(I_PERSISTENCE_BASIC, topLevelClass, introspectedTable);
        if (assignable1) {
            Method method = new Method(topLevelClass.getType().getShortName());
            method.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), PARAM_NAME_PERSISTENCE_STATUS));
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setConstructor(true);
            addConstructorBodyLine(method, true, topLevelClass, introspectedTable);
            if (topLevelClass.getMethods().size() == 0) {
                topLevelClass.getMethods().add(method);
            } else {
                topLevelClass.getMethods().add(0, method);
            }
        }

        /*
         * 根据联合查询属性配置
         * 在实体对象中增加相应的属性
         */
        if (introspectedTable.getRelationGeneratorConfigurations().size() > 0) {
            for (RelationGeneratorConfiguration relationProperty : introspectedTable.getRelationGeneratorConfigurations()) {
                FullyQualifiedJavaType returnType;
                Field field;
                FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(relationProperty.getModelTye());
                if (!topLevelClass.isContainField(relationProperty.getPropertyName())) {
                    if (relationProperty.getType().equals(RelationTypeEnum.collection)) {
                        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
                        topLevelClass.addImportedType(listType);
                        returnType = FullyQualifiedJavaType.getNewListInstance();
                        returnType.addTypeArgument(fullyQualifiedJavaType);
                        field = new Field(relationProperty.getPropertyName(), returnType);
                        topLevelClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
                        initializationBlock.addBodyLine(VStringUtil.format("this.{0} = new ArrayList<>();", relationProperty.getPropertyName()));
                    } else {
                        returnType = fullyQualifiedJavaType;
                        field = new Field(relationProperty.getPropertyName(), returnType);
                    }
                    if (introspectedTable.getRules().isIntegrateMybatisPlus()) {
                        TableField tableField = new TableField();
                        tableField.setExist(false);
                        tableField.addAnnotationToField(field,topLevelClass);
                    }
                    field.setVisibility(JavaVisibility.PRIVATE);
                    if (field.getRemark() == null) {
                        field.setRemark(relationProperty.getRemark());
                    }
                    ApiModelProperty apiModelProperty = new ApiModelProperty(field.getRemark(), JDBCUtil.getExampleByClassName(field.getType().getFullyQualifiedName()));
                    apiModelProperty.addAnnotationToField(field,topLevelClass);
                    addField(topLevelClass, field);
                    topLevelClass.addImportedType(fullyQualifiedJavaType);
                }
            }
        }

        if (!StringUtility.isEmpty(beanName) && assignable1) {
            initializationBlock.addBodyLine(VStringUtil.format("this.persistenceBeanName = \"{0}\";", introspectedTable.getControllerBeanName()));
        }

        if (initializationBlock.getBodyLines().size() > 0) {
            topLevelClass.addInitializationBlock(initializationBlock);
        }
        return true;
    }

    @Override
    public boolean htmlMapDocumentGenerated(org.mybatis.generator.api.dom.html.Document document, IntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        HtmlDocumentGenerator htmlDocumentGenerated;
        String uiFrame = htmlGeneratorConfiguration.getUiFrameType();
        if (HtmlConstants.HTML_UI_FRAME_LAYUI.equals(uiFrame)) {
            htmlDocumentGenerated = new LayuiDocumentGenerated(document, introspectedTable, htmlGeneratorConfiguration);
        } else if (HtmlConstants.HTML_UI_FRAME_ZUI.equals(uiFrame)) {
            htmlDocumentGenerated = new ZuiDocumentGenerated(document, introspectedTable, htmlGeneratorConfiguration);
        } else {
            htmlDocumentGenerated = new LayuiDocumentGenerated(document, introspectedTable, htmlGeneratorConfiguration);
        }
        return htmlDocumentGenerated.htmlMapDocumentGenerated();
    }

    @Override
    public List<GeneratedHtmlFile> contextGenerateAdditionalHtmlFiles(IntrospectedTable introspectedTable) {
        return new ArrayList<>();
    }

    private boolean addField(TopLevelClass topLevelClass, Field field) {
        return topLevelClass.addField(field, null,true);
    }

    /**
     * 内部类，添加构造器方法体内容
     *
     * @param method          构造器方法
     * @param existParameters 是否有参
     */
    private void addConstructorBodyLine(Method method, boolean existParameters, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        boolean assignable = JavaBeansUtil.isAssignableCurrent(I_PERSISTENCE_BASIC, topLevelClass, introspectedTable);
        if (existParameters) {
            if (assignable) {
                method.addBodyLine("super(persistenceStatus);");
            } else {
                method.addBodyLine("this.persistenceStatus = persistenceStatus;");
            }
        }
    }
}
