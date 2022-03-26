package org.mybatis.generator.custom;

import com.vgosoft.tool.core.VStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.codegen.HtmlConstants;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.custom.htmlGenerator.HtmlDocumentGenerator;
import org.mybatis.generator.custom.htmlGenerator.LayuiDocumentGenerated;
import org.mybatis.generator.custom.htmlGenerator.ZuiDocumentGenerated;
import org.mybatis.generator.custom.pojo.CustomMethodGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.RelationPropertyHolder;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * dao生成插件
 *
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * 2020-07-14 05:23
 * @version 3.0
 */
public class JavaClientGeneratePlugins extends PluginAdapter implements Plugin {

    private static final String INTERFACE_SHOW_IN_VIEW = "com.vgosoft.core.entity.IShowInView";
    private static final String tableMeta = "com.vgosoft.core.annotation.TableMeta";
    private static final String apiModel = "io.swagger.annotations.ApiModel";
    private static final String apiModelProperty = "io.swagger.annotations.ApiModelProperty";
    private static final String columnMeta = "com.vgosoft.core.annotation.ColumnMeta";
    private static final String iPersistenceBasic = "com.vgosoft.core.entity.IPersistenceBasic";

    //mapper接口
    public static final String mBGMapperInterface = "com.vgosoft.mybatis.inf.MBGMapperInterface";
    public static final String mBGMapperBlobInterface = "com.vgosoft.mybatis.inf.MBGMapperBlobInterface";

    private static final String repositoryAnnotation = "org.springframework.stereotype.Repository";

    public static final String PROP_NAME_REST_BASE_PATH = "restBasePath";
    public static final String PROP_NAME_VIEW_PATH = "viewPath";

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
    }


    /**
     * dao接口文件生成后，进行符合性调整
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
        if (introspectedTable.getRules().generateRelationMap()) {
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
                }else{
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

        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().size()>0) {
            for (SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration : introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration()) {
                Method selectByTable;
                if (selectByTableGeneratorConfiguration.isReturnPrimaryKey()) {
                    selectByTable = getMethodByType(selectByTableGeneratorConfiguration.getMethodName(), FullyQualifiedJavaType.getStringInstance(),
                            FullyQualifiedJavaType.getStringInstance(), selectByTableGeneratorConfiguration.getParameterName(), true,
                            "中间表中来自其他表的查询键值");
                    interFace.addImportedType(FullyQualifiedJavaType.getStringInstance());
                }else{
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
        }else{
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
        //添加import引入
        topLevelClass.addImportedType(new FullyQualifiedJavaType(tableMeta));
        topLevelClass.addImportedType(new FullyQualifiedJavaType(columnMeta));
        topLevelClass.addImportedType(new FullyQualifiedJavaType(repositoryAnnotation));
        //添加@Repository注解
        JavaBeansUtil.addAnnotation(topLevelClass, "@Repository");

        // 添加@ApiModel、@ApiModelProperty
        boolean isNoSwaggerAnnotation = introspectedTable.getRules().isNoSwaggerAnnotation();
        if (!isNoSwaggerAnnotation) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType(apiModel));
            topLevelClass.addImportedType(new FullyQualifiedJavaType(apiModelProperty));
            for (int i = 0; i < topLevelClass.getFields().size(); i++) {
                Field field = topLevelClass.getFields().get(i);
                String apiModelPropertyAnnotation = getApiModelPropertyAnnotation(field, introspectedTable);
                if (apiModelPropertyAnnotation.length() > 0) {
                    field.addAnnotation(apiModelPropertyAnnotation);
                }
            }
            String apiModelAnnotation = getApiModelAnnotation(introspectedTable, topLevelClass);
            topLevelClass.addAnnotation(apiModelAnnotation);
        }
        //为实体添加@TableMeta注解
        String tableMetaAnnotation = getTableMetaAnnotation(introspectedTable);
        topLevelClass.addAnnotation(tableMetaAnnotation);
        //为属性添加@ColumnMeta注解
        for (int i = 0; i < topLevelClass.getFields().size(); i++) {
            Field field = topLevelClass.getFields().get(i);
            String columnMetaAnnotation = getColumnMetaAnnotation(field, introspectedTable, topLevelClass, i);
            if (columnMetaAnnotation.length() > 0) {
                field.addAnnotation(columnMetaAnnotation);
            }
        }
        //添加序列化标识
        Field serialVersionUID = new Field("serialVersionUID", new FullyQualifiedJavaType("long"));
        serialVersionUID.setInitializationString("1L");
        serialVersionUID.setFinal(true);
        serialVersionUID.setStatic(true);
        addField(topLevelClass, serialVersionUID, 0,JavaVisibility.PRIVATE);

        //添加@Setter,@Getter
        String aSetter = "lombok.Setter";
        String aGetter = "lombok.Getter";
        topLevelClass.addImportedType(new FullyQualifiedJavaType(aSetter));
        topLevelClass.addImportedType(new FullyQualifiedJavaType(aGetter));
        topLevelClass.addAnnotation("@Setter");
        topLevelClass.addAnnotation("@Getter");

        /*
         * 更新构造器
         * */
        List<Method> methods = topLevelClass.getMethods();
        if (methods.size() == 0) {
            //添加一个个无参构造器
            Method method = new Method(topLevelClass.getType().getShortName());
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setConstructor(true);
            topLevelClass.getMethods().add(method);
        }
        for (Method method : methods) {
            if (method.isConstructor()) {
                addConstructorBodyLine(method, false, topLevelClass, introspectedTable);
            }
        }

        //添加一个参数的构造器
        boolean assignable1 = JavaBeansUtil.isAssignableCurrent(iPersistenceBasic, topLevelClass, introspectedTable);
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

        //根据新参数添加
        if (introspectedTable.getRelationProperties().size() > 0) {
            for (RelationPropertyHolder relationProperty : introspectedTable.getRelationProperties()) {
                FullyQualifiedJavaType returnType;
                Field field;
                FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(relationProperty.getModelTye());
                if (relationProperty.getType().equals(RelationTypeEnum.collection)) {
                    FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
                    topLevelClass.addImportedType(listType);
                    returnType = FullyQualifiedJavaType.getNewListInstance();
                    returnType.addTypeArgument(fullyQualifiedJavaType);
                    field = new Field(relationProperty.getPropertyName(), returnType);
                    field.setInitializationString("new ArrayList<>()");
                    topLevelClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
                } else {
                    returnType = fullyQualifiedJavaType;
                    field = new Field(relationProperty.getPropertyName(), returnType);
                }
                addField(topLevelClass, field);
                topLevelClass.addImportedType(fullyQualifiedJavaType);
            }
        }

        //追加respBasePath属性
        Field field = new Field(PROP_NAME_REST_BASE_PATH, FullyQualifiedJavaType.getStringInstance());
        if (addField(topLevelClass,field)) {
            if (!introspectedTable.getRules().isNoSwaggerAnnotation()) {
                field.addAnnotation("@ApiModelProperty(value = \"Restful请求中的跟路径\",hidden = true)");
            }
        }

        //添加静态代码块
        String beanName = getTableBeanName(introspectedTable);
        InitializationBlock initializationBlock = new InitializationBlock(false);
        //计算html包属性
        String propertyValue = introspectedTable.getConfigPropertyValue(PropertyRegistry.CONTEXT_HTML_TARGET_PACKAGE, PropertyScope.any);
        if (StringUtility.stringHasValue(propertyValue)) {
            initializationBlock.addBodyLine(VStringUtil.format("this.{0} = \"{1}\";", PROP_NAME_REST_BASE_PATH,propertyValue));
        }
        if (!StringUtility.isEmpty(beanName) && assignable1) {
            initializationBlock.addBodyLine(VStringUtil.format("this.persistenceBeanName = \"{0}\";", getTableBeanName(introspectedTable)));
        }

        HtmlMapGeneratorConfiguration htmlMapGeneratorConfiguration = null;
        if (introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().size()>0) {
            htmlMapGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0);
        }
        if (htmlMapGeneratorConfiguration!=null && !StringUtility.isEmpty(htmlMapGeneratorConfiguration.getViewPath())) {

            initializationBlock.addBodyLine(VStringUtil.format("this.{0} = \"{1}\";",PROP_NAME_VIEW_PATH, htmlMapGeneratorConfiguration.getTargetPackage()+"/"+htmlMapGeneratorConfiguration.getViewPath()));
            //判断是否需要实现ShowInView接口
            boolean assignable = JavaBeansUtil.isAssignableCurrent(INTERFACE_SHOW_IN_VIEW, topLevelClass, introspectedTable);
            if (!assignable) {
                //添加ShowInView接口
                FullyQualifiedJavaType showInView = new FullyQualifiedJavaType(INTERFACE_SHOW_IN_VIEW);
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
        }
        if (initializationBlock.getBodyLines().size() > 0) {
            topLevelClass.addInitializationBlock(initializationBlock);
        }
        return true;
    }

    private boolean addField(AbstractJavaType javaType, Field field){
        return addField(javaType, field, null, JavaVisibility.PRIVATE);
    }

    private boolean addField(AbstractJavaType javaType, Field field, Integer index,JavaVisibility javaVisibility) {
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

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean htmlMapDocumentGenerated(org.mybatis.generator.api.dom.html.Document document, IntrospectedTable introspectedTable,HtmlMapGeneratorConfiguration htmlMapGeneratorConfiguration) {
        HtmlDocumentGenerator htmlDocumentGenerated;
        String uiFrame = htmlMapGeneratorConfiguration.getUiFrameType();
        if (HtmlConstants.HTML_UI_FRAME_LAYUI.equals(uiFrame)) {
            htmlDocumentGenerated = new LayuiDocumentGenerated(document, introspectedTable,htmlMapGeneratorConfiguration);
        } else if (HtmlConstants.HTML_UI_FRAME_ZUI.equals(uiFrame)) {
            htmlDocumentGenerated = new ZuiDocumentGenerated(document, introspectedTable,htmlMapGeneratorConfiguration);
        } else {
            htmlDocumentGenerated = new LayuiDocumentGenerated(document, introspectedTable,htmlMapGeneratorConfiguration);
        }
        return htmlDocumentGenerated.htmlMapDocumentGenerated();
    }

    @Override
    public List<GeneratedHtmlFile> contextGenerateAdditionalHtmlFiles(IntrospectedTable introspectedTable) {
        return new ArrayList<>();
    }

    /**
     * 内部类，添加构造器方法体内容
     *
     * @param method          构造器方法
     * @param existParameters 是否有参
     */
    private void addConstructorBodyLine(Method method, boolean existParameters, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        boolean assignable1 = JavaBeansUtil.isAssignableCurrent(iPersistenceBasic, topLevelClass, introspectedTable);
        if (existParameters) {
            if (assignable1) {
                method.addBodyLine("super(persistenceStatus);");
            } else {
                method.addBodyLine("this.persistenceStatus = persistenceStatus;");
            }
        }else{
            method.addBodyLine("super();");
        }
    }

    /**
     * model类的@TableMeta注解
     */
    private String getTableMetaAnnotation(IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();
        sb.append("@TableMeta(value = \"").append(tableConfiguration.getTableName()).append("\"");

        final String alias = introspectedTable.getFullyQualifiedTable().getAlias();
        if (StringUtils.isNotBlank(alias)) {
            sb.append(", alias =  \"");
            sb.append(alias);
            sb.append("\"");
        }
        if (introspectedTable.getRemarks() != null) {
            sb.append(", descript = \"");
            sb.append(StringUtility.remarkLeft(introspectedTable.getRemarks()));
            sb.append("\"");
        } else {
            sb.append(", descript = \"").append("\"");
        }
        if (introspectedTable.getRules().isNoMetaAnnotation()) {
            sb.append(", summary = false");
        }
        sb.append(", beanname = \"").append(getTableBeanName(introspectedTable)).append("\")");
        return sb.toString();
    }

    /**
     * 获得对应的操作Bean的名称
     */
    private String getTableBeanName(IntrospectedTable introspectedTable) {
        String implClazzName = introspectedTable.getControllerBeanName();
        return JavaBeansUtil.getFirstCharacterLowercase(implClazzName);
    }

    /**
     * 获得表元数据注解@ColumnMeta
     */
    private String getColumnMetaAnnotation(Field field, IntrospectedTable introspectedTable, TopLevelClass topLevelClass, int i) {
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.getJavaProperty().equals(field.getName())) {
                sb.append("@ColumnMeta(").append("value = \"");
                sb.append(column.getActualColumnName()).append("\"");
                sb.append(",description = \"");
                if (StringUtils.isNotEmpty(column.getRemarks())) {
                    sb.append(StringUtility.remarkLeft(column.getRemarks()));
                } else {
                    sb.append(column.getActualColumnName());
                }
                sb.append("\"");
                sb.append(",size =");
                sb.append(column.getLength());
                sb.append(",order = ").append((i + 20));

                if (!"VARCHAR".equals(column.getJdbcTypeName())) {
                    sb.append(",type = JDBCType.").append(column.getJdbcTypeName());
                    topLevelClass.addImportedType("java.sql.JDBCType");
                }
                if ("DATE".equals(column.getJdbcTypeName())) {
                    sb.append(",dataFormat =\"yyyy-MM-dd\"");
                } else if ("TIME".equals(column.getJdbcTypeName())) {
                    sb.append(",dataFormat =\"HH:mm:ss\"");
                } else if ("TIMESTAMP".equals(column.getJdbcTypeName())) {
                    sb.append(",dataFormat =\"yyyy-MM-dd HH:mm:ss\"");
                }
                sb.append(")");
                return sb.toString();
            }
        }
        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return "";
        }
    }


    /**
     * model类的@apiModel
     */
    private String getApiModelAnnotation(IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        StringBuilder sb = new StringBuilder();
        FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        sb.append("@ApiModel(value = \"").append(fullyQualifiedJavaType.getShortName()).append("\"");
        if (introspectedTable.getRemarks() != null) {
            sb.append(", description = \"").append(introspectedTable.getRemarks()).append("\"");
        } else {
            sb.append(", description = \"").append("\"");
        }
        final Optional<FullyQualifiedJavaType> superClass = topLevelClass.getSuperClass();
        if (superClass.isPresent()) {
            final String clazz = superClass.get().getShortName() + ".class";
            sb.append(", parent = ");
            sb.append(clazz);
        }
        sb.append(" )");
        return sb.toString();
    }

    /**
     * 获得表元数据注解@ApiModelProperty
     */
    private String getApiModelPropertyAnnotation(Field field, IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.getJavaProperty().equals(field.getName())) {
                sb.append("@ApiModelProperty(").append("value = \"");
                sb.append(column.getRemarks()).append("\"");
                sb.append(")");
                return sb.toString();
            }
        }
        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * 获得mapper接口
     */
    private String getMapperInterface(IntrospectedTable introspectedTable) {
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            return mBGMapperBlobInterface;
        }
        return mBGMapperInterface;
    }

}
