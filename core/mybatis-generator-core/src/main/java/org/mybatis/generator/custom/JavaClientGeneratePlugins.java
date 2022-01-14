package org.mybatis.generator.custom;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.HtmlConstants;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.custom.controllerGenerator.GenerateJavaController;
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

    private static final String iShowInView = "com.vgosoft.core.entity.IShowInView";
    private static final String tableMeta = "com.vgosoft.core.annotation.TableMeta";
    private static final String apiModel = "io.swagger.annotations.ApiModel";
    private static final String apiModelProperty = "io.swagger.annotations.ApiModelProperty";
    private static final String columnMeta = "com.vgosoft.core.annotation.ColumnMeta";
    private static final String iPersistenceBasic = "com.vgosoft.core.entity.IPersistenceBasic";
    private static final String comSelSqlParameter = "com.vgosoft.core.entity.ComSelSqlParameter";
    private static final String abstractMBGServiceInterface = "com.vgosoft.mybatis.abs.service.AbstractMBGServiceInterface";
    private static final String absBaseController = "com.vgosoft.web.controller.abs.AbstractBaseController";
    private static final String repositoryAnnotation = "org.springframework.stereotype.Repository";

    public static final String mapperInf = "com.vgosoft.mybatis.inf.MBGMapperInterface";

    private final String bizSubPackage;
    private final String implSubPackage;

    public JavaClientGeneratePlugins() {
        bizSubPackage = "service";
        implSubPackage = "impl";
    }

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
            if (!introspectedTable.getRules().generateService()) {
                continue;
            }
            FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(
                    introspectedTable.getBaseRecordType());
            FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(
                    introspectedTable.getExampleType());
            /*service接口类全名*/
            sb.append(StringUtility.substringBeforeLast(entityType.getPackageName(), ".")).append(".").append(bizSubPackage);
            String StrBizPackage = sb.toString();

            /*生成service接口文件*/
            sb.setLength(0);
            sb.append(StrBizPackage).append(".").append("I").append(entityType.getShortName());
            String infName = sb.toString();
            Interface bizINF = new Interface(infName);
            commentGenerator.addJavaFileComment(bizINF);
            //继承接口
            FullyQualifiedJavaType infSuperType = new FullyQualifiedJavaType("com.vgosoft.mybatis.inf.MBGServiceInterface");
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
            FullyQualifiedJavaType implSuperType = getServiceSupperType(entityType, exampleType);
            String implClazzName = JavaBeansUtil.getFirstCharacterUppercase(introspectedTable.getControllerBeanName());

            FullyQualifiedJavaType bizClazzImplType = new FullyQualifiedJavaType(StrBizPackage + "." + implSubPackage + "." + implClazzName);
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
            GeneratedJavaFile generatedJavaFilebizImpl = new GeneratedJavaFile(bizClazzImpl, targetProject,
                    context.getProperty("javaFileEncoding"), context.getJavaFormatter());
            list.add(generatedJavaFilebizImpl);

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
            GenerateJavaController gc = new GenerateJavaController(introspectedTable);
            FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
            FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
            String seriveImplShortName = bizClazzImplType.getShortName();
            String entityShortName = entityType.getShortName();
            String lowerCaseEntityName = StringUtility.lowerCase(entityShortName);
            String seriveImplVar = JavaBeansUtil.getFirstCharacterLowercase(seriveImplShortName);

            sb.append(StringUtility.substringBeforeLast(entityType.getPackageName(), "."));
            sb.append(".").append("controller").append(".").append(entityShortName).append("Controller");
            FullyQualifiedJavaType conClazzType = new FullyQualifiedJavaType(sb.toString());
            TopLevelClass conTopClazz = new TopLevelClass(conClazzType);
            commentGenerator.addJavaFileComment(conTopClazz);
            FullyQualifiedJavaType supClazzType = new FullyQualifiedJavaType(absBaseController);
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
            Field field = new Field(seriveImplVar, bizInfType);
            field.setVisibility(JavaVisibility.PRIVATE);
            conTopClazz.addField(field);
            Method method = new Method(JavaBeansUtil.getSetterMethodName(seriveImplVar));
            method.setVisibility(JavaVisibility.PUBLIC);
            method.addParameter(new Parameter(bizInfType, seriveImplVar));
            method.addAnnotation("@Autowired");
            String sb1 = "this." +
                    seriveImplVar +
                    " = " +
                    seriveImplVar +
                    ';';
            method.addBodyLine(sb1);
            conTopClazz.addMethod(method);

            conTopClazz.addMethod(gc.viewGenerate());
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
        StringBuilder sb = new StringBuilder();
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseSimple");
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseSimpleImpl");
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseList");
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseSimpleList");
        conTopClazz.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        conTopClazz.addImportedType("org.springframework.web.bind.annotation.*");
        conTopClazz.addImportedType("io.swagger.annotations.Api");
        conTopClazz.addImportedType("io.swagger.annotations.ApiOperation");
        conTopClazz.addJavaDocLine(sb.toString());
        conTopClazz.addImportedType("java.lang.reflect.Field");
        conTopClazz.addImportedType("java.util.List");
        conTopClazz.addImportedType("org.springframework.web.servlet.ModelAndView");
        conTopClazz.addJavaDocLine(sb.toString());
        conTopClazz.addAnnotation("@RestController");
    }

    /**
     * dao接口文件生成后，进行符合性调整
     */
    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        /*调整引入*/
        FullyQualifiedJavaType mapperAnnotation = new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper");
        interfaze.getImportedTypes().clear();
        interfaze.addImportedType(mapperAnnotation);
        interfaze.addImportedType(entityType);
        interfaze.addImportedType(exampleType);
        FullyQualifiedJavaType infSuperType = new FullyQualifiedJavaType(mapperInf);
        infSuperType.addTypeArgument(entityType);
        infSuperType.addTypeArgument(exampleType);
        interfaze.addImportedType(infSuperType);
        interfaze.addSuperInterface(infSuperType);
        boolean isExist = false;
        for (String annotation : interfaze.getAnnotations()) {
            if (annotation.equalsIgnoreCase("@Mapper")) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            interfaze.addAnnotation("@Mapper");
        }
        interfaze.getMethods().clear();
        return true;
    }

    /**
     * model类生成后，进行符合性调整。
     * 添加@TableMeta和@ColumnMeta注解
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //为属性添加@TableMeta、@ColumnMeta注解
        boolean isNoMetaAnnotation = introspectedTable.getRules().isNoMetaAnnotation();
        if (!isNoMetaAnnotation) {
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
        }

        /** 添加@ApiModel、@ApiModelProperty*/
        boolean isNoSwaggerAnnotation = introspectedTable.getRules().isNoSwaggerAnnotation();
        if (!isNoSwaggerAnnotation) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType(apiModel ));
            topLevelClass.addImportedType(new FullyQualifiedJavaType(apiModelProperty ));
            for (int i = 0; i < topLevelClass.getFields().size(); i++) {
                Field field = topLevelClass.getFields().get(i);
                String apiModelPropertyAnnotation = getApiModelPropertyAnnotation(field, introspectedTable, topLevelClass, i);
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
        String javaModelAddtionProperty = introspectedTable.getTableConfigurationProperty("javaModelAddtionProperty");
        if (javaModelAddtionProperty != null) {
            FullyQualifiedJavaType addPropertyType = new FullyQualifiedJavaType(javaModelAddtionProperty);
            String javaModelName = JavaBeansUtil.getFirstCharacterLowercase(addPropertyType.getShortName());

            String ptype = introspectedTable.getTableConfigurationProperty("javaModelAddtionPropertyType");
            FullyQualifiedJavaType returnType;
            String propertyName;
            if (ptype != null && "list".equalsIgnoreCase(ptype)) {
                propertyName = javaModelName + "s";
                returnType = new FullyQualifiedJavaType("java.util.List<" + addPropertyType.getShortName() + ">");
                topLevelClass.addImportedType("java.util.List");
            } else {
                returnType = addPropertyType;
                propertyName = javaModelName;
            }
            Field field = new Field(propertyName, returnType);
            field.setVisibility(JavaVisibility.PRIVATE);
            topLevelClass.addField(field);
            topLevelClass.addImportedType(javaModelAddtionProperty);

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
        if (!StringUtility.isEmpty(beanName)) {
            stringBuilder.append("this.persistenceBeanName = ");
            stringBuilder.append("\"" + getTableBeanName(introspectedTable) + "\";");
            initializationBlock.addBodyLine(stringBuilder.toString());
        }
        String viewpath = null;
        viewpath = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_VIEW_PATH);
        if (!StringUtility.isEmpty(viewpath)) {
            //添加程序块
            stringBuilder.setLength(0);
            stringBuilder.append("this.viewPath = ").append("\"" + introspectedTable.getMyBatis3HtmlMapperViewName() + "\";");
            initializationBlock.addBodyLine(stringBuilder.toString());
            //判断是否需要实现ShowInView接口
            boolean assignable = JavaBeansUtil.isAssignableCurrent(iShowInView, topLevelClass);
            if (!assignable) {
                //添加ShowInView接口
                FullyQualifiedJavaType showinview = new FullyQualifiedJavaType(iShowInView);
                topLevelClass.addImportedType(showinview);
                topLevelClass.addSuperInterface(showinview);
                //添加viewpath的属性及方法
                Field field = new Field("viewPath", new FullyQualifiedJavaType("String"));
                field.setVisibility(JavaVisibility.PRIVATE);
                topLevelClass.addField(field);
            }
        }
        if (initializationBlock.getBodyLines().size()>0) {
            topLevelClass.addInitializationBlock(initializationBlock);
        }

        /** 添加compareTo方法*/
        final String iSortableEntity = "com.vgosoft.core.entity.ISortableEntity";
        boolean assignable = JavaBeansUtil.isAssignableCurrent(iSortableEntity,topLevelClass);
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
        addSelectBySql(document,introspectedTable);
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

    private void addBaseBySql(Document document){
        XmlElement sqlSqlBuilder = new XmlElement("sql");
        sqlSqlBuilder.addAttribute(new Attribute("id", "Base_By_Sql"));
        context.getCommentGenerator().addComment(sqlSqlBuilder);
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "_parameter != null"));
        ifElement.addElement(new TextElement("${sql}"));
        sqlSqlBuilder.addElement(ifElement);
        document.getRootElement().addElement(sqlSqlBuilder);
    }

    private void addSelectBySql(Document document,IntrospectedTable introspectedTable){
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

    private void addSelectMapBySql(Document document){
        XmlElement selectMapBySqlBuilder = new XmlElement("select");
        selectMapBySqlBuilder.addAttribute(new Attribute("id", "selectMapBySql"));
        selectMapBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        selectMapBySqlBuilder.addAttribute(new Attribute("resultType", "java.util.Map"));
        context.getCommentGenerator().addComment(selectMapBySqlBuilder);
        selectMapBySqlBuilder.addElement(getBaseBySqlElement());
        document.getRootElement().addElement(selectMapBySqlBuilder);
    }

    private void addInsertBySql(Document document){
        XmlElement insertBySqlBuilder = new XmlElement("insert");
        insertBySqlBuilder.addAttribute(new Attribute("id", "insertBySql"));
        insertBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        context.getCommentGenerator().addComment(insertBySqlBuilder);
        insertBySqlBuilder.addElement(getBaseBySqlElement());
        document.getRootElement().addElement(insertBySqlBuilder);
    }

    private void addUpdateBySql(Document document){
        XmlElement updateBySqlBuilder = new XmlElement("update");
        updateBySqlBuilder.addAttribute(new Attribute("id", "updateBySql"));
        updateBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        context.getCommentGenerator().addComment(updateBySqlBuilder);
        updateBySqlBuilder.addElement(getBaseBySqlElement());
        document.getRootElement().addElement(updateBySqlBuilder);
    }

    private void addCountBySql(Document document){
        XmlElement countBySqlBuilder = new XmlElement("select");
        countBySqlBuilder.addAttribute(new Attribute("id", "countBySql"));
        countBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        countBySqlBuilder.addAttribute(new Attribute("resultType", "java.lang.Long"));
        context.getCommentGenerator().addComment(countBySqlBuilder);
        countBySqlBuilder.addElement(getBaseBySqlElement());
        document.getRootElement().addElement(countBySqlBuilder);
    }

    private void addListBySql(Document document){
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
        FullyQualifiedJavaType sqlparam = new FullyQualifiedJavaType(comSelSqlParameter);
        XmlElement selectBySqlCondition = new XmlElement("select");
        if (isSub) {
            selectBySqlCondition.addAttribute(new Attribute("id", "selectBySqlConditionSub"));
        } else {
            selectBySqlCondition.addAttribute(new Attribute("id", "selectBySqlCondition"));
        }
        selectBySqlCondition.addAttribute(new Attribute("parameterType", sqlparam.getFullyQualifiedName()));
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
            XmlElement forEachElment = new XmlElement("foreach");
            forEachElment.addAttribute(new Attribute("close", ")"));
            forEachElment.addAttribute(new Attribute("collection", "principals"));
            forEachElment.addAttribute(new Attribute("index", "index"));
            forEachElment.addAttribute(new Attribute("item", "principal"));
            forEachElment.addAttribute(new Attribute("open", "("));
            forEachElment.addAttribute(new Attribute("separator", ","));
            forEachElment.addElement(new TextElement("#{principal}"));
            xmlElement.addElement(forEachElment);
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
        if (introspectedTable.getRemarks() != null) {
            sb.append(", descript = \"").append(introspectedTable.getRemarks()).append("\"");
        } else {
            sb.append(", descript = \"").append("\"");
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
                sb.append(",description = \"").append(column.getRemarks()).append("\"");
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
     * 内部类
     * 获得Service抽象类父类
     */
    private FullyQualifiedJavaType getServiceSupperType(FullyQualifiedJavaType entityType, FullyQualifiedJavaType exampleType) {
        FullyQualifiedJavaType supperType = new FullyQualifiedJavaType(abstractMBGServiceInterface);
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
    private String getApiModelPropertyAnnotation(Field field, IntrospectedTable introspectedTable, TopLevelClass topLevelClass, int i) {
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

}
