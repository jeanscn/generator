package org.mybatis.generator.custom;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.HtmlConstants;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.custom.controllerGenerator.GenerateJavaController;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.custom.htmlGenerator.HtmlDocumentGenerator;
import org.mybatis.generator.custom.htmlGenerator.LayuiDocumentGenerated;
import org.mybatis.generator.custom.htmlGenerator.ZuiDocumentGenerated;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * dao生成插件
 *
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * 2020-07-14 05:23
 * @version 3.0
 */
public class JavaClientGeneratePlugins extends PluginAdapter implements Plugin {

    private static final Logger logger = LoggerFactory.getLogger(JavaClientGeneratePlugins.class);

    private static final String INTERFACE_SHOW_IN_VIEW = "com.vgosoft.core.entity.IShowInView";
    private static final String tableMeta = "com.vgosoft.core.annotation.TableMeta";
    private static final String apiModel = "io.swagger.annotations.ApiModel";
    private static final String apiModelProperty = "io.swagger.annotations.ApiModelProperty";
    private static final String columnMeta = "com.vgosoft.core.annotation.ColumnMeta";
    private static final String iPersistenceBasic = "com.vgosoft.core.entity.IPersistenceBasic";
    private static final String comSelSqlParameter = "com.vgosoft.core.entity.ComSelSqlParameter";
    //service实现抽象父类
    private static final String abstractMBGServiceInterface = "com.vgosoft.mybatis.abs.AbstractMBGServiceInterface";
    private static final String abstractMBGBlobServiceInterface = "com.vgosoft.mybatis.abs.AbstractMBGBlobServiceInterface";
    private static final String abstractMBGBlobFileService = "com.vgosoft.mybatis.abs.AbstractMBGBlobFileService";
    private static final String abstractMBGBlobBytesService = "com.vgosoft.mybatis.abs.AbstractMBGBlobBytesService";
    private static final String abstractMBGBlobStringService = "com.vgosoft.mybatis.abs.AbstractMBGBlobStringService";
    private static final String abstractServiceBusiness = "com.vgosoft.mybatis.abs.AbstractServiceBusiness";
    private static final String abstractBlobFileServiceBusiness = "com.vgosoft.mybatis.abs.AbstractBlobFileServiceBusiness";
    private static final String abstractBlobBytesServiceBusiness = "com.vgosoft.mybatis.abs.AbstractBlobBytesServiceBusiness";
    private static final String abstractBlobStringServiceBusiness = "com.vgosoft.mybatis.abs.AbstractBlobStringServiceBusiness";
    //service接口父类
    private static final String mBGServiceInterface = "com.vgosoft.mybatis.inf.MBGServiceInterface";
    private static final String mBGBlobServiceInterface = "com.vgosoft.mybatis.inf.MBGBlobServiceInterface";
    private static final String mBGBlobFileService = "com.vgosoft.mybatis.inf.MBGBlobFileService";
    private static final String mBGBlobBytesService = "com.vgosoft.mybatis.inf.MBGBlobBytesService";
    private static final String mBGBlobStringService = "com.vgosoft.mybatis.inf.MBGBlobStringService";

    //mapper接口
    public static final String mBGMapperInterface = "com.vgosoft.mybatis.inf.MBGMapperInterface";
    public static final String mBGMapperBlobInterface = "com.vgosoft.mybatis.inf.MBGMapperBlobInterface";

    private static final String ABSTRACT_BASE_CONTROLLER = "com.vgosoft.web.controller.abs.AbstractBaseController";
    private static final String repositoryAnnotation = "org.springframework.stereotype.Repository";

    private static final String bizSubPackage = "service";
    private static final String implSubPackage = "impl";

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
    }

    /**
     * 生成Service接口，Service实现和Controller文件
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
        Context context = this.getContext();
        CommentGenerator commentGenerator = context.getCommentGenerator();
        String targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        List<GeneratedJavaFile> list = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        for (IntrospectedTable introspectedTable : context.getIntrospectedTables()) {
            /*实体类名*/
            FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
            /*service接口类全名*/
            sb.append(StringUtility.substringBeforeLast(entityType.getPackageName(), "."));
            sb.append(".").append(bizSubPackage);
            String StrBizPackage = sb.toString();
            sb.setLength(0);
            sb.append(StrBizPackage).append(".").append("I").append(entityType.getShortName());
            String infName = sb.toString();
            /*service实现类名*/
            String implClazzName = JavaBeansUtil.getFirstCharacterUppercase(introspectedTable.getControllerBeanName());
            FullyQualifiedJavaType bizClazzImplType = new FullyQualifiedJavaType(StrBizPackage + "." + implSubPackage + "." + implClazzName);
            if (introspectedTable.getRules().generateService()) {
                FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
                /*生成service接口文件*/
                Interface bizINF = new Interface(infName);
                commentGenerator.addJavaFileComment(bizINF);
                FullyQualifiedJavaType infSuperType = new FullyQualifiedJavaType(getServiceInterface(introspectedTable));
                infSuperType.addTypeArgument(entityType);
                infSuperType.addTypeArgument(exampleType);
                bizINF.addImportedType(infSuperType);
                bizINF.addImportedType(entityType);
                bizINF.addImportedType(exampleType);
                bizINF.setVisibility(JavaVisibility.PUBLIC);
                bizINF.addSuperInterface(infSuperType);
                GeneratedJavaFile generatedJavaFile = new GeneratedJavaFile(bizINF, targetProject,
                        context.getProperty("javaFileEncoding"), context.getJavaFormatter());
                list.add(generatedJavaFile);

                /*生成service实现接口*/
                String serviceAnnotation = "org.springframework.stereotype.Service";
                FullyQualifiedJavaType importAnnotation = new FullyQualifiedJavaType(serviceAnnotation);
                FullyQualifiedJavaType implSuperType = getServiceSupperType(entityType, exampleType,introspectedTable);



                TopLevelClass bizClazzImpl = new TopLevelClass(bizClazzImplType);
                commentGenerator.addJavaFileComment(bizClazzImpl);
                bizClazzImpl.addImportedType(implSuperType);
                bizClazzImpl.addImportedType(entityType);
                bizClazzImpl.addImportedType(exampleType);
                bizClazzImpl.addImportedType(bizINF.getType());
                bizClazzImpl.setVisibility(JavaVisibility.PUBLIC);
                bizClazzImpl.setSuperClass(implSuperType);
                bizClazzImpl.addSuperInterface(bizINF.getType());

                /*是否添加@Service注解*/
                boolean noServiceAnnotation = introspectedTable.getRules().isNoServiceAnnotation();
                if (!noServiceAnnotation) {
                    bizClazzImpl.addImportedType(importAnnotation);
                    sb.setLength(0);
                    sb = new StringBuilder("@Service(\"").append(getTableBeanName(introspectedTable)).append("\")");
                    bizClazzImpl.addAnnotation(sb.toString());
                }
                sb.setLength(0);
                GeneratedJavaFile generatedJavaFileBizImpl = new GeneratedJavaFile(bizClazzImpl, targetProject,
                        context.getProperty("javaFileEncoding"), context.getJavaFormatter());
                list.add(generatedJavaFileBizImpl);
            }
            /*生成controller文件*/
            GeneratedJavaFile generatedJavaFileController = generateControllerFile(introspectedTable, bizClazzImplType, infName);
            if (generatedJavaFileController != null) {
                list.add(generatedJavaFileController);
            }
        }
        return list;
    }

    private GeneratedJavaFile generateControllerFile(IntrospectedTable introspectedTable,
                                                     FullyQualifiedJavaType bizClazzImplType,
                                                     String infName) {
        CommentGenerator commentGenerator = this.context.getCommentGenerator();
        //是否生成controller，缺省为true
        StringBuilder sb = new StringBuilder();
        if (introspectedTable.getRules().generateController()) {
            logger.debug("生成Controller");
            String viewpath = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_VIEW_PATH);
            GenerateJavaController gc = new GenerateJavaController(introspectedTable);
            FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
            FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
            String serviceImplShortName = bizClazzImplType.getShortName();
            String entityShortName = entityType.getShortName();
            String lowerCaseEntityName = StringUtility.lowerCase(entityShortName);
            String serviceImplVar = JavaBeansUtil.getFirstCharacterLowercase(serviceImplShortName);

            sb.append(StringUtility.substringBeforeLast(entityType.getPackageName(), "."));
            sb.append(".").append("controller").append(".").append(entityShortName).append("Controller");
            FullyQualifiedJavaType conClazzType = new FullyQualifiedJavaType(sb.toString());
            TopLevelClass conTopClazz = new TopLevelClass(conClazzType);
            commentGenerator.addJavaFileComment(conTopClazz);
            FullyQualifiedJavaType supClazzType = new FullyQualifiedJavaType(ABSTRACT_BASE_CONTROLLER);
            conTopClazz.setSuperClass(supClazzType);
            conTopClazz.addImportedType(infName);
            conTopClazz.addImportedType(supClazzType);
            conTopClazz.addImportedType(entityType);
            conTopClazz.addImportedType(exampleType);
            conClazzAddStaticImportedType(conTopClazz);
            sb.setLength(0);
            sb.append("@Api(value = \"/").append(lowerCaseEntityName).append("\", tags = \"");
            sb.append(introspectedTable.getRemarks()).append("\")");
            conTopClazz.addAnnotation(sb.toString());
            conTopClazz.addAnnotation("@RequestMapping(value = \"/" + introspectedTable.getControllerSimplePackage() + "\")");
            FullyQualifiedJavaType bizInfType = new FullyQualifiedJavaType(infName);
            Field field = new Field(serviceImplVar, bizInfType);
            field.setVisibility(JavaVisibility.PRIVATE);
            conTopClazz.addField(field);
            Method method = new Method(JavaBeansUtil.getSetterMethodName(serviceImplVar));
            method.setVisibility(JavaVisibility.PUBLIC);
            method.addParameter(new Parameter(bizInfType, serviceImplVar));
            method.addAnnotation("@Autowired");
            String sb1 = "this." +
                    serviceImplVar +
                    " = " +
                    serviceImplVar +
                    ';';
            method.addBodyLine(sb1);
            conTopClazz.addMethod(method);
            if (viewpath != null) {
                conTopClazz.addMethod(gc.viewGenerate());
            }
            conTopClazz.addMethod(gc.getGenerate());
            conTopClazz.addMethod(gc.listGenerate());
            conTopClazz.addMethod(gc.createGenerate());
            conTopClazz.addMethod(gc.updateGenerate());
            conTopClazz.addMethod(gc.deleteGenerate());
            conTopClazz.addMethod(gc.deleteBatchGenerate());
            return new GeneratedJavaFile(conTopClazz, context.getJavaModelGeneratorConfiguration().getTargetProject(),
                    context.getProperty("javaFileEncoding"), context.getJavaFormatter());
        }
        return null;
    }


    /**
     * 内部方法
     * 为Controller类添加引入包路径
     */
    private void conClazzAddStaticImportedType(TopLevelClass conTopClazz) {
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseSimple");
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseSimpleImpl");
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseList");
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseSimpleList");
        conTopClazz.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        conTopClazz.addImportedType("org.springframework.web.bind.annotation.*");
        conTopClazz.addImportedType("io.swagger.annotations.Api");
        conTopClazz.addImportedType("io.swagger.annotations.ApiOperation");
        conTopClazz.addJavaDocLine("");
        conTopClazz.addImportedType("java.lang.reflect.Field");
        conTopClazz.addImportedType("java.util.List");
        conTopClazz.addImportedType("org.springframework.web.servlet.ModelAndView");
        conTopClazz.addJavaDocLine("");
        conTopClazz.addAnnotation("@RestController");
    }

    /**
     * dao接口文件生成后，进行符合性调整
     */
    @Override
    public boolean clientGenerated(Interface interFace, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        /*调整引入*/
        FullyQualifiedJavaType mapperAnnotation = new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper");
        interFace.getImportedTypes().clear();
        interFace.addImportedType(mapperAnnotation);
        interFace.addImportedType(entityType);
        interFace.addImportedType(exampleType);
        FullyQualifiedJavaType infSuperType = new FullyQualifiedJavaType(getMapperInterface(introspectedTable));
        infSuperType.addTypeArgument(entityType);
        infSuperType.addTypeArgument(exampleType);
        interFace.addImportedType(infSuperType);
        interFace.addSuperInterface(infSuperType);
        boolean isExist = false;
        for (String annotation : interFace.getAnnotations()) {
            if (annotation.equalsIgnoreCase("@Mapper")) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            interFace.addAnnotation("@Mapper");
        }
        interFace.getMethods().clear();
        return true;
    }

    /**
     * model类生成后，进行符合性调整。
     * 添加@TableMeta和@ColumnMeta注解
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //为属性添加@TableMeta、@ColumnMeta注解
        topLevelClass.addImportedType(new FullyQualifiedJavaType(tableMeta));
        topLevelClass.addImportedType(new FullyQualifiedJavaType(columnMeta));
        //添加@Repository注解
        topLevelClass.addImportedType(new FullyQualifiedJavaType(repositoryAnnotation));
        topLevelClass.addAnnotation("@Repository");
        for (int i = 0; i < topLevelClass.getFields().size(); i++) {
            Field field = topLevelClass.getFields().get(i);
            String columnMetaAnnotation = getColumnMetaAnnotation(field, introspectedTable, topLevelClass, i);
            if (columnMetaAnnotation.length() > 0) {
                field.addAnnotation(columnMetaAnnotation);
            }
        }
        String tableMetaAnnotation = getTableMetaAnnotation(introspectedTable);
        topLevelClass.addAnnotation(tableMetaAnnotation);

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
            String apiModelAnnotation = getApiModelAnnotation(introspectedTable);
            topLevelClass.addAnnotation(apiModelAnnotation);
        }

        //添加序列化标识
        boolean isb = false;
        for (Field field : topLevelClass.getFields()) {
            if (field.getName().equals("serialVersionUID")) {
                isb = true;
                break;
            }
        }
        if (!isb) {
            Field serialVersionUID = new Field("serialVersionUID", new FullyQualifiedJavaType("long"));
            serialVersionUID.setInitializationString("1L");
            serialVersionUID.setVisibility(JavaVisibility.PRIVATE);
            serialVersionUID.setFinal(true);
            serialVersionUID.setStatic(true);
            topLevelClass.getFields().add(0, serialVersionUID);
        }


        //添加@Setter,@Getter
        String aSetter = "lombok.Setter";
        String aGetter = "lombok.Getter";
        topLevelClass.addImportedType(new FullyQualifiedJavaType(aSetter));
        topLevelClass.addImportedType(new FullyQualifiedJavaType(aGetter));
        topLevelClass.addAnnotation("@Setter");
        topLevelClass.addAnnotation("@Getter");

        //更新构造器
        List<Method> methods = topLevelClass.getMethods();
        for (Method method : methods) {
            if (method.isConstructor()) {
                addConstructorBodyLine(method, false, topLevelClass);
            }
        }

        //添加一个参数的构造器
        boolean assignable1 = JavaBeansUtil.isAssignableCurrent(iPersistenceBasic, topLevelClass);
        if (assignable1) {
            Method method = new Method(topLevelClass.getType().getShortName());
            method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "persistenceStatus"));
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setConstructor(true);
            addConstructorBodyLine(method, true, topLevelClass);
            topLevelClass.getMethods().add(1, method);
        }

        /*是否需要增加List集合属性*/
        String javaModelAdditionProperty = introspectedTable.getTableConfigurationProperty(PropertyRegistry.TABLE_JAVA_MODEL_ADDITION_PROPERTY);
        if (javaModelAdditionProperty != null) {
            FullyQualifiedJavaType addPropertyType = new FullyQualifiedJavaType(javaModelAdditionProperty);
            String javaModelName = JavaBeansUtil.getFirstCharacterLowercase(addPropertyType.getShortName());

            String pType = introspectedTable.getTableConfigurationProperty(PropertyRegistry.TABLE_JAVA_MODEL_ADDITION_PROPERTY_TYPE);
            String pName = introspectedTable.getTableConfigurationProperty(PropertyRegistry.TABLE_JAVA_MODEL_ADDITION_PROPERTY_NAME);
            FullyQualifiedJavaType returnType;
            String propertyName;

            if ("list".equalsIgnoreCase(pType)) {
                if (StringUtils.isNotEmpty(pName)) {
                    propertyName = pName;
                } else {
                    propertyName = javaModelName + "s";
                }
                returnType = new FullyQualifiedJavaType("java.util.List<" + addPropertyType.getShortName() + ">");
                topLevelClass.addImportedType("java.util.List");
            } else {
                returnType = addPropertyType;
                if (StringUtils.isNotEmpty(pName)) {
                    propertyName = pName;
                } else {
                    propertyName = javaModelName;
                }
            }
            Field field = new Field(propertyName, returnType);
            field.setVisibility(JavaVisibility.PRIVATE);
            topLevelClass.addField(field);
            topLevelClass.addImportedType(javaModelAdditionProperty);

            //getter
            Method getter = JavaBeansUtil.getBasicJavaBeansGetter(propertyName, returnType);
            topLevelClass.addMethod(getter);
            //setter
            Method setter = JavaBeansUtil.getBasicJavaBeanSetter(propertyName, false, returnType);
            topLevelClass.addMethod(setter);
        }
        String beanName = getTableBeanName(introspectedTable);
        InitializationBlock initializationBlock = new InitializationBlock(false);
        StringBuilder stringBuilder = new StringBuilder();
        if (!StringUtility.isEmpty(beanName) && assignable1) {
            stringBuilder.append("this.persistenceBeanName = ");
            stringBuilder.append("\"").append(getTableBeanName(introspectedTable)).append("\";");
            initializationBlock.addBodyLine(stringBuilder.toString());
        }
        String viewpath = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_VIEW_PATH);
        if (!StringUtility.isEmpty(viewpath)) {
            //添加程序块
            stringBuilder.setLength(0);
            stringBuilder.append("this.viewPath = ");
            stringBuilder.append("\"");
            stringBuilder.append(introspectedTable.getMyBatis3HtmlMapperViewName());
            stringBuilder.append("\";");
            initializationBlock.addBodyLine(stringBuilder.toString());
            //判断是否需要实现ShowInView接口
            boolean assignable = JavaBeansUtil.isAssignableCurrent(INTERFACE_SHOW_IN_VIEW, topLevelClass);
            if (!assignable) {
                //添加ShowInView接口
                FullyQualifiedJavaType showInView = new FullyQualifiedJavaType(INTERFACE_SHOW_IN_VIEW);
                topLevelClass.addImportedType(showInView);
                topLevelClass.addSuperInterface(showInView);
                //添加viewpath的属性及方法
                long lCount = topLevelClass.getFields().stream().filter(t -> t.getName().equalsIgnoreCase("viewPath")).count();
                if (lCount == 0) {
                    Field field = new Field("viewPath", new FullyQualifiedJavaType("String"));
                    field.setVisibility(JavaVisibility.PRIVATE);
                    topLevelClass.addField(field);
                }
            }
        }
        if (initializationBlock.getBodyLines().size() > 0) {
            topLevelClass.addInitializationBlock(initializationBlock);
        }

        // 添加compareTo方法
        final String iSortableEntity = "com.vgosoft.core.entity.ISortableEntity";
        boolean assignable = JavaBeansUtil.isAssignableCurrent(iSortableEntity, topLevelClass);
        if (assignable) {
            Method method = new Method("compareTo");
            FullyQualifiedJavaType type = topLevelClass.getType();
            method.addParameter(new Parameter(type, "o"));
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setReturnType(new FullyQualifiedJavaType("int"));
            method.addBodyLine("return (int) (this.getSort() - o.getSort());");
            method.addAnnotation("@Override");
            topLevelClass.getMethods().add(method);
        }

        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        addSelectBySqlCondition(document, false, introspectedTable);
        addSelectBySqlCondition(document, true, introspectedTable);
        addBaseBySql(document);
        addSelectBySql(document, introspectedTable);
        addSelectMapBySql(document);
        addInsertBySql(document);
        addUpdateBySql(document);
        addCountBySql(document);
        addListBySql(document);
        return true;
    }

    @Override
    public boolean htmlMapDocumentGenerated(org.mybatis.generator.api.dom.html.Document document, IntrospectedTable introspectedTable) {
        HtmlDocumentGenerator htmlDocumentGenerated;
        String uiFrame = getUiFrame(introspectedTable);
        if (HtmlConstants.HTML_UI_FRAME_LAYUI.equals(uiFrame)) {
            htmlDocumentGenerated = new LayuiDocumentGenerated(document, introspectedTable);
        } else if (HtmlConstants.HTML_UI_FRAME_ZUI.equals(uiFrame)) {
            htmlDocumentGenerated = new ZuiDocumentGenerated(document, introspectedTable);
        } else {
            htmlDocumentGenerated = new LayuiDocumentGenerated(document, introspectedTable);
        }
        return htmlDocumentGenerated.htmlMapDocumentGenerated();
    }

    @Override
    public List<GeneratedHtmlFile> contextGenerateAdditionalHtmlFiles(IntrospectedTable introspectedTable) {
        return new ArrayList<>();
    }

    private String getUiFrame(IntrospectedTable introspectedTable) {
        return introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_HTML_UI_FRAME);
    }

    private void addBaseBySql(Document document) {
        XmlElement sqlSqlBuilder = new XmlElement("sql");
        sqlSqlBuilder.addAttribute(new Attribute("id", "Base_By_Sql"));
        context.getCommentGenerator().addComment(sqlSqlBuilder);
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "_parameter != null"));
        ifElement.addElement(new TextElement("${sql}"));
        sqlSqlBuilder.addElement(ifElement);
        document.getRootElement().addElement(sqlSqlBuilder);
    }

    private void addSelectBySql(Document document, IntrospectedTable introspectedTable) {
        XmlElement selectBySqlBuilder = new XmlElement("select");
        selectBySqlBuilder.addAttribute(new Attribute("id", "selectBySql"));
        selectBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        selectBySqlBuilder.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        context.getCommentGenerator().addComment(selectBySqlBuilder);
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "_parameter != null")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("select")); //$NON-NLS-1$
        ifElement.addElement(getBaseColumnListElement(introspectedTable));
        StringBuilder sb = new StringBuilder();
        sb.append("from ");
        sb.append("(${sql}) ");
        String alias = introspectedTable.getFullyQualifiedTable().getAlias();
        if (stringHasValue(alias)) {
            sb.append(' ');
            sb.append(alias);
        }
        ifElement.addElement(new TextElement(sb.toString()));
        selectBySqlBuilder.addElement(ifElement);
        document.getRootElement().addElement(selectBySqlBuilder);
    }

    private void addSelectMapBySql(Document document) {
        XmlElement selectMapBySqlBuilder = new XmlElement("select");
        selectMapBySqlBuilder.addAttribute(new Attribute("id", "selectMapBySql"));
        selectMapBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        selectMapBySqlBuilder.addAttribute(new Attribute("resultType", "java.util.Map"));
        context.getCommentGenerator().addComment(selectMapBySqlBuilder);
        selectMapBySqlBuilder.addElement(getBaseBySqlElement());
        document.getRootElement().addElement(selectMapBySqlBuilder);
    }

    private void addInsertBySql(Document document) {
        XmlElement insertBySqlBuilder = new XmlElement("insert");
        insertBySqlBuilder.addAttribute(new Attribute("id", "insertBySql"));
        insertBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        context.getCommentGenerator().addComment(insertBySqlBuilder);
        insertBySqlBuilder.addElement(getBaseBySqlElement());
        document.getRootElement().addElement(insertBySqlBuilder);
    }

    private void addUpdateBySql(Document document) {
        XmlElement updateBySqlBuilder = new XmlElement("update");
        updateBySqlBuilder.addAttribute(new Attribute("id", "updateBySql"));
        updateBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        context.getCommentGenerator().addComment(updateBySqlBuilder);
        updateBySqlBuilder.addElement(getBaseBySqlElement());
        document.getRootElement().addElement(updateBySqlBuilder);
    }

    private void addCountBySql(Document document) {
        XmlElement countBySqlBuilder = new XmlElement("select");
        countBySqlBuilder.addAttribute(new Attribute("id", "countBySql"));
        countBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        countBySqlBuilder.addAttribute(new Attribute("resultType", "java.lang.Long"));
        context.getCommentGenerator().addComment(countBySqlBuilder);
        countBySqlBuilder.addElement(getBaseBySqlElement());
        document.getRootElement().addElement(countBySqlBuilder);
    }

    private void addListBySql(Document document) {
        XmlElement listBySqlBuilder = new XmlElement("select");
        listBySqlBuilder.addAttribute(new Attribute("id", "listBySql"));
        listBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        listBySqlBuilder.addAttribute(new Attribute("resultType", "java.lang.String"));
        context.getCommentGenerator().addComment(listBySqlBuilder);
        listBySqlBuilder.addElement(getBaseBySqlElement());
        document.getRootElement().addElement(listBySqlBuilder);
    }

    private XmlElement getBaseColumnListElement(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("include"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("refid", //$NON-NLS-1$
                introspectedTable.getBaseColumnListId()));
        return answer;
    }

    private XmlElement getBaseBySqlElement() {
        XmlElement answer = new XmlElement("include"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("refid", "Base_By_Sql"));
        return answer;
    }

    private void addSelectBySqlCondition(Document document, boolean isSub, IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        //追加selectBySqlCondition or selectBySqlConditionSub方法
        FullyQualifiedJavaType sqlParam = new FullyQualifiedJavaType(comSelSqlParameter);
        XmlElement selectBySqlCondition = new XmlElement("select");
        if (isSub) {
            selectBySqlCondition.addAttribute(new Attribute("id", "selectBySqlConditionSub"));
        } else {
            selectBySqlCondition.addAttribute(new Attribute("id", "selectBySqlCondition"));
        }
        selectBySqlCondition.addAttribute(new Attribute("parameterType", sqlParam.getFullyQualifiedName()));
        selectBySqlCondition.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        context.getCommentGenerator().addComment(selectBySqlCondition);
        //select
        selectBySqlCondition.addElement(new TextElement("select")); //$NON-NLS-1$
        //distinct
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "distinct != null")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("distinct")); //$NON-NLS-1$
        selectBySqlCondition.addElement(ifElement);
        //include
        XmlElement xmlElement = new XmlElement("include");
        xmlElement.addAttribute(new Attribute("refid", introspectedTable.getBaseColumnListId()));
        selectBySqlCondition.addElement(xmlElement);
        //from
        sb.setLength(0);
        sb.append("from "); //$NON-NLS-1$
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        selectBySqlCondition.addElement(new TextElement(sb.toString()));
        //where
        if (!isSub) {
            xmlElement = new XmlElement("if");
            xmlElement.addAttribute(new Attribute("test", "condition != null and  condition !=''"));
            xmlElement.addElement(new TextElement("where ${condition}"));
            selectBySqlCondition.addElement(xmlElement);
        } else {
            //<where>
            XmlElement whereElement = new XmlElement("where");
            //<if test="condition != null and  condition !=''">
            //    ${condition}
            //</if>
            xmlElement = new XmlElement("if");
            xmlElement.addAttribute(new Attribute("test", "condition != null and  condition !=''"));
            xmlElement.addElement(new TextElement("${condition}"));
            whereElement.addElement(xmlElement);

            xmlElement = new XmlElement("if");
            xmlElement.addAttribute(new Attribute("test", "principals != null and principals.size &gt;0"));
            sb.setLength(0);

            if (introspectedTable.getPrimaryKeyColumns().size() > 0) {
                IntrospectedColumn introspectedColumn = introspectedTable.getPrimaryKeyColumns().get(0);
                String actualColumnName = introspectedColumn.getActualColumnName();
                if (!StringUtility.isEmpty(actualColumnName)) {
                    sb.append("and ").append(actualColumnName).append(" in(");
                } else {
                    sb.append("and ID_ in(");
                }
            } else {
                sb.append("and ID_ in(");
            }


            sb.append("select distinct BUSINESS_KEY_ from VCORE_RU_AUTHORITY ");
            sb.append("where AUTHORITY_NAME_ in");
            xmlElement.addElement(new TextElement(sb.toString()));
            //<foreach close=")" collection="principals" index="index" item="principal" open="(" separator=",">
            //  #{principal}
            //</foreach>
            XmlElement forEachElement = new XmlElement("foreach");
            forEachElement.addAttribute(new Attribute("close", ")"));
            forEachElement.addAttribute(new Attribute("collection", "principals"));
            forEachElement.addAttribute(new Attribute("index", "index"));
            forEachElement.addAttribute(new Attribute("item", "principal"));
            forEachElement.addAttribute(new Attribute("open", "("));
            forEachElement.addAttribute(new Attribute("separator", ","));
            forEachElement.addElement(new TextElement("#{principal}"));
            xmlElement.addElement(forEachElement);
            xmlElement.addElement(new TextElement(")"));
            whereElement.addElement(xmlElement);
            selectBySqlCondition.addElement(whereElement);
        }
        //orderby
        xmlElement = new XmlElement("if");
        xmlElement.addAttribute(new Attribute("test", "orderby != null and orderby !=''"));
        xmlElement.addElement(new TextElement("order by ${orderby}"));
        selectBySqlCondition.addElement(xmlElement);

        document.getRootElement().addElement(selectBySqlCondition);
    }

    /**
     * 内部类，添加构造器方法体内容
     *
     * @param method          构造器方法
     * @param existParameters 是否有参
     */
    private void addConstructorBodyLine(Method method, boolean existParameters, TopLevelClass topLevelClass) {
        boolean assignable1 = JavaBeansUtil.isAssignableCurrent(iPersistenceBasic, topLevelClass);
        if (existParameters) {
            method.addBodyLine("super(persistenceStatus);");
        }
        if (assignable1) {
            if (!existParameters) {
                method.addBodyLine("this.setPersistenceStatus(this.DEFAULT_PERSISTENCE_STATUS);");
            }
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
            sb.append(remarkLeft(introspectedTable.getRemarks()));
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
                    sb.append(remarkLeft(column.getRemarks()));
                } else {
                    sb.append(column.getActualColumnName());
                }
                sb.append("\"");
                sb.append(",size =");
                sb.append(column.getLength());
                sb.append(",order = ").append((i + 20));

                if (GenerateUtils.isHiddenColumn(column) || introspectedTable.getRules().isNoMetaAnnotation()) {
                    sb.append(",summary = false");
                }

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
     * 内部类
     * 获得Service抽象类父类
     */
    private FullyQualifiedJavaType getServiceSupperType(FullyQualifiedJavaType entityType, FullyQualifiedJavaType exampleType,IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType supperType = new FullyQualifiedJavaType(getAbstractService(introspectedTable));
        supperType.addTypeArgument(entityType);
        supperType.addTypeArgument(exampleType);
        return supperType;
    }

    /**
     * model类的@apiModel
     */
    private String getApiModelAnnotation(IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        sb.append("@ApiModel(value = \"").append(fullyQualifiedJavaType.getShortName()).append("\"");
        if (introspectedTable.getRemarks() != null) {
            sb.append(", description = \"").append(introspectedTable.getRemarks()).append("\"");
        } else {
            sb.append(", description = \"").append("\"");
        }
        sb.append(")");
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
                sb.append(",name = \"");
                sb.append(column.getActualColumnName()).append("\"");
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
     * 截取字符串中括号前的内容
     * 主要用于截取表注释、列注释，用于生成标题
     * @param remark 注释信息
     * @return 字符串
     */
    private String remarkLeft(String remark) {
        String ret = remark;
        if (StringUtils.indexOf(remark, "(") > 0) {
            ret = StringUtils.substringBefore(remark, "(");
        } else if (StringUtils.indexOf(remark, "（") > 0) {
            ret = StringUtils.substringBefore(remark, "（");
        }
        return StringUtils.chomp(ret);
    }

    /**
     * 获得service类的抽象实现类
     * @param introspectedTable 生成基类
     */
    private String getAbstractService(IntrospectedTable introspectedTable){
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            String steamOutType = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_JAVA_MODEL_BYTE_STREAM_OUTPUT_MODE);
            if (GenerateUtils.isBusinessInstance(introspectedTable)) {
                switch (steamOutType){
                    case "bytes":
                        return abstractBlobBytesServiceBusiness;
                    case "file":
                        return abstractBlobFileServiceBusiness;
                    case "string":
                        return abstractBlobStringServiceBusiness;
                }
                return abstractServiceBusiness;
            }else{
                switch (steamOutType) {
                    case "bytes":
                        return abstractMBGBlobBytesService;
                    case "file":
                        return abstractMBGBlobFileService;
                    case "string":
                        return abstractMBGBlobStringService;
                }
                return abstractMBGBlobServiceInterface;
            }
        }
        return abstractMBGServiceInterface;
    }

    /**
     * 获得service类的抽象实现类
     * @param introspectedTable 生成基类
     */
    private String getServiceInterface(IntrospectedTable introspectedTable){
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            String steamOutType = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_JAVA_MODEL_BYTE_STREAM_OUTPUT_MODE);
            switch (steamOutType){
                case "bytes":
                    return mBGBlobBytesService;
                case "file":
                    return mBGBlobFileService;
                case "string":
                    return mBGBlobStringService;
            }
            return mBGBlobServiceInterface;
        }
        return mBGServiceInterface;
    }

    /**
     * 获得mapper接口
     */
    private String getMapperInterface(IntrospectedTable introspectedTable){
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            return mBGMapperBlobInterface;
        }
        return mBGMapperInterface;
    }

}
