package org.mybatis.generator.custom;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.HtmlConstants;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.HtmlMapGeneratorConfiguration;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.custom.htmlGenerator.HtmlDocumentGenerator;
import org.mybatis.generator.custom.htmlGenerator.LayuiDocumentGenerated;
import org.mybatis.generator.custom.htmlGenerator.ZuiDocumentGenerated;
import org.mybatis.generator.custom.pojo.CustomMethodGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.RelationGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
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

    @Override
    public void setContext(Context context) {
        super.setContext(context);
    }


    /*
     * dao接口文件生成后，进行符合性调整
     *
     */
    @Override
    public boolean clientGenerated(Interface interFace, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        /*调整引入*/
        interFace.getImportedTypes().clear();
        interFace.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
        interFace.addImportedType(entityType);
        interFace.addImportedType(exampleType);

        FullyQualifiedJavaType infSuperType = new FullyQualifiedJavaType(getMapperInterface(introspectedTable));
        infSuperType.addTypeArgument(entityType);
        infSuperType.addTypeArgument(exampleType);
        interFace.addImportedType(infSuperType);
        interFace.addSuperInterface(infSuperType);
        JavaBeansUtil.addAnnotation(interFace, "@Mapper");

        interFace.getMethods().clear();
        //增加relation方法
        if (introspectedTable.getRules().generateRelationWithSubSelected()) {
            Method example = getMethodByType(introspectedTable.getSelectByExampleWithRelationStatementId(), entityType,
                    exampleType, "example", true, "查询条件对象");
            interFace.addMethod(example);
            interFace.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            interFace.addImportedType(entityType);
        }
        //增加by外键
        if (introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().size() > 0) {
            for (SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration : introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations()) {
                if (selectByColumnGeneratorConfiguration.isReturnPrimaryKey()) {
                    addAbstractMethodByColumn(interFace, FullyQualifiedJavaType.getStringInstance(), selectByColumnGeneratorConfiguration);
                } else {
                    addAbstractMethodByColumn(interFace, entityType, selectByColumnGeneratorConfiguration);
                }
            }
        }
        //增加
        if (introspectedTable.getCustomAddtionalSelectMethods().size() > 0
                && introspectedTable.getCustomAddtionalSelectMethods().containsKey(introspectedTable.getSelectTreeByParentIdStatementId())) {
            CustomMethodGeneratorConfiguration customMethodGeneratorConfiguration = introspectedTable.getCustomAddtionalSelectMethods().get(introspectedTable.getSelectTreeByParentIdStatementId());
            addAbstractMethodByColumn(interFace, entityType, customMethodGeneratorConfiguration.getParentIdColumn(), introspectedTable.getSelectTreeByParentIdStatementId());
        }

        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().size() > 0) {
            for (SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration : introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration()) {
                Method selectByTable;
                if (selectByTableGeneratorConfiguration.isReturnPrimaryKey()) {
                    selectByTable = getMethodByType(selectByTableGeneratorConfiguration.getMethodName(), FullyQualifiedJavaType.getStringInstance(),
                            FullyQualifiedJavaType.getStringInstance(), selectByTableGeneratorConfiguration.getParameterName(), true,
                            "中间表中来自其他表的查询键值");
                    interFace.addImportedType(FullyQualifiedJavaType.getStringInstance());
                } else {
                    selectByTable = getMethodByType(selectByTableGeneratorConfiguration.getMethodName(), entityType,
                            FullyQualifiedJavaType.getStringInstance(), selectByTableGeneratorConfiguration.getParameterName(), true,
                            "中间表中来自其他表的查询键值");
                }
                interFace.addMethod(selectByTable);
            }
            interFace.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            interFace.addImportedType(entityType);
        }
        return true;
    }

    private void addAbstractMethodByColumn(Interface interFace, FullyQualifiedJavaType entityType, SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration) {
        addAbstractMethodByColumn(interFace, entityType, selectByColumnGeneratorConfiguration.getColumn(), selectByColumnGeneratorConfiguration.getMethodName());
    }


    private void addAbstractMethodByColumn(Interface interFace, FullyQualifiedJavaType entityType, IntrospectedColumn parameterColumn, String methodName) {
        Method method = getMethodByColumn(entityType, parameterColumn, methodName, true);
        interFace.addMethod(method);
        interFace.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        interFace.addImportedType(parameterColumn.getFullyQualifiedJavaType());
    }

    private Method getMethodByColumn(FullyQualifiedJavaType returnType, IntrospectedColumn parameterColumn, String methodName, boolean isAbstract) {
        return getMethodByType(methodName, returnType, parameterColumn.getFullyQualifiedJavaType(),
                parameterColumn.getJavaProperty(), isAbstract, parameterColumn.getRemarks());
    }

    private Method getMethodByType(String methodName, FullyQualifiedJavaType returnType, FullyQualifiedJavaType parameterFullyQualifiedJavaType, String parameterName, boolean isAbstract, String remark) {
        Method method = new Method(methodName);
        if (isAbstract) {
            method.setAbstract(true);
        } else {
            method.setVisibility(JavaVisibility.PUBLIC);
        }
        method.addParameter(new Parameter(parameterFullyQualifiedJavaType, parameterName));
        if (methodName.equals("selectBaseByPrimaryKey")) {
            method.setReturnType(returnType);
        } else {
            FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
            listType.addTypeArgument(returnType);
            method.setReturnType(listType);
        }
        context.getCommentGenerator().addMethodJavaDocLine(method, false, "提示 - @mbg.generated",
                "这个抽象方法通过定制版Mybatis Generator自动生成",
                VStringUtil.format("@param {0} {1}", parameterName, remark));
        return method;
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
        String aSetter = "lombok.Setter";
        String aGetter = "lombok.Getter";
        topLevelClass.addImportedType(new FullyQualifiedJavaType(aSetter));
        topLevelClass.addImportedType(new FullyQualifiedJavaType(aGetter));
        topLevelClass.addAnnotation("@Setter");
        topLevelClass.addAnnotation("@Getter");

        //添加静态代码块
        String beanName = introspectedTable.getControllerBeanName();
        InitializationBlock initializationBlock = new InitializationBlock(false);

        //添加一个参数的构造器
        boolean assignable1 = JavaBeansUtil.isAssignableCurrent(I_PERSISTENCE_BASIC, topLevelClass, introspectedTable);
        if (assignable1) {
            Method method = new Method(topLevelClass.getType().getShortName());
            method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "persistenceStatus"));
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
                if (!isPropertyExist(topLevelClass, relationProperty.getPropertyName())) {
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
                    addField(topLevelClass, field);
                    topLevelClass.addImportedType(fullyQualifiedJavaType);
                }
            }
        }

        //追加respBasePath属性
        Field field = new Field(PROP_NAME_REST_BASE_PATH, FullyQualifiedJavaType.getStringInstance());
        if (addField(topLevelClass, field)) {
            if (!introspectedTable.getRules().isNoSwaggerAnnotation()) {
                field.addAnnotation("@ApiModelProperty(value = \"Restful请求中的跟路径\",hidden = true)");
            }
        }

        /*
         * 静态代码初始化
         * restBasePath、persistenceBeanName、viewPath
         */
        List<HtmlMapGeneratorConfiguration> htmlMapGeneratorConfigurations = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations();
        if (htmlMapGeneratorConfigurations.size() > 0) {
            initializationBlock.addBodyLine(VStringUtil.format("this.{0} = \"{1}\";", PROP_NAME_REST_BASE_PATH, introspectedTable.getControllerSimplePackage()));
        }
        if (!StringUtility.isEmpty(beanName) && assignable1) {
            initializationBlock.addBodyLine(VStringUtil.format("this.persistenceBeanName = \"{0}\";", introspectedTable.getControllerBeanName()));
        }

        introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().stream()
                .findFirst().filter(t -> StringUtility.stringHasValue(t.getViewPath())).ifPresent(htmlConfig -> {
                    initializationBlock.addBodyLine(VStringUtil.format("this.{0} = \"{1}\";", PROP_NAME_VIEW_PATH, htmlConfig.getViewPath()));
                    //判断是否需要实现ShowInView接口
                    boolean assignable = JavaBeansUtil.isAssignableCurrent(ConstantsUtil.I_SHOW_IN_VIEW, topLevelClass, introspectedTable);
                    if (!assignable) {
                        //添加ShowInView接口
                        FullyQualifiedJavaType showInView = new FullyQualifiedJavaType(I_SHOW_IN_VIEW);
                        topLevelClass.addImportedType(showInView);
                        topLevelClass.addSuperInterface(showInView);
                        //添加viewpath的属性及方法
                        Field viewPath = new Field(PROP_NAME_VIEW_PATH, FullyQualifiedJavaType.getStringInstance());
                        if (addField(topLevelClass, viewPath)) {
                            if (!introspectedTable.getRules().isNoSwaggerAnnotation()) {
                                viewPath.addAnnotation("@ApiModelProperty(value = \"视图路径\",hidden = true)");
                            }
                        }
                    }
                });

        if (initializationBlock.getBodyLines().size() > 0) {
            topLevelClass.addInitializationBlock(initializationBlock);
        }
        return true;
    }

    @Override
    public boolean htmlMapDocumentGenerated(org.mybatis.generator.api.dom.html.Document document, IntrospectedTable introspectedTable, HtmlMapGeneratorConfiguration htmlMapGeneratorConfiguration) {
        HtmlDocumentGenerator htmlDocumentGenerated;
        String uiFrame = htmlMapGeneratorConfiguration.getUiFrameType();
        if (HtmlConstants.HTML_UI_FRAME_LAYUI.equals(uiFrame)) {
            htmlDocumentGenerated = new LayuiDocumentGenerated(document, introspectedTable, htmlMapGeneratorConfiguration);
        } else if (HtmlConstants.HTML_UI_FRAME_ZUI.equals(uiFrame)) {
            htmlDocumentGenerated = new ZuiDocumentGenerated(document, introspectedTable, htmlMapGeneratorConfiguration);
        } else {
            htmlDocumentGenerated = new LayuiDocumentGenerated(document, introspectedTable, htmlMapGeneratorConfiguration);
        }
        return htmlDocumentGenerated.htmlMapDocumentGenerated();
    }

    @Override
    public List<GeneratedHtmlFile> contextGenerateAdditionalHtmlFiles(IntrospectedTable introspectedTable) {
        return new ArrayList<>();
    }

    private boolean addField(AbstractJavaType javaType, Field field) {
        return addField(javaType, field, null, JavaVisibility.PRIVATE);
    }

    private boolean addField(AbstractJavaType javaType, Field field, Integer index, JavaVisibility javaVisibility) {
        field.setVisibility(javaVisibility);
        long count = javaType.getFields().stream()
                .filter(t -> t.getName().equalsIgnoreCase(field.getName()))
                .count();
        if (count == 0) {
            if (index != null && javaType.getFields().size() > 0) {
                javaType.getFields().add(index, field);
            } else {
                javaType.addField(field);
            }
            return true;
        }
        return false;
    }

    /**
     * 内部类，添加构造器方法体内容
     *
     * @param method          构造器方法
     * @param existParameters 是否有参
     */
    private void addConstructorBodyLine(Method method, boolean existParameters, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        boolean assignable1 = JavaBeansUtil.isAssignableCurrent(I_PERSISTENCE_BASIC, topLevelClass, introspectedTable);
        if (existParameters) {
            if (assignable1) {
                method.addBodyLine("super(persistenceStatus);");
            } else {
                method.addBodyLine("this.persistenceStatus = persistenceStatus;");
            }
        }
    }

    /**
     * 获得mapper接口
     */
    private String getMapperInterface(IntrospectedTable introspectedTable) {
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            return MBG_MAPPER_BLOB_INTERFACE;
        }
        return MBG_MAPPER_INTERFACE;
    }

    private Boolean isPropertyExist(TopLevelClass topLevelClass,String propertyName){
        boolean found = false;
        for (Field topLevelClassField : topLevelClass.getFields()) {
            if (topLevelClassField.getName().equalsIgnoreCase(propertyName)) {
                found = true;
                break;
            }
        }
        if (topLevelClass.getSuperClass().isPresent()) {
            String superClassName = topLevelClass.getSuperClass().get().getFullyQualifiedNameWithoutTypeParameters();
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(Class.forName(superClassName));
                for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                    if (propertyDescriptor.getName().equalsIgnoreCase(propertyName)) {
                        found = true;
                        break;
                    }
                }

            } catch (ClassNotFoundException | IntrospectionException e) {
                e.printStackTrace();
            }
        }
        return found;
    }

}
