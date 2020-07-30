/**
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.custom;


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
import org.mybatis.generator.custom.htmlGenerator.HtmlDocumentGenerator;
import org.mybatis.generator.custom.htmlGenerator.LayuiDocumentGenerated;
import org.mybatis.generator.custom.htmlGenerator.ZuiDocumentGenerated;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @description: dao生成插件
 * @author: <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * @created: 2020-07-14 05:23
 * @version: 3.0
 */
public class JavaClientGeneratePlugins extends PluginAdapter implements Plugin {

    private final String bizSubPackage;
    private final String implSubPackage;
    private final String htmlTargetProject;

    public JavaClientGeneratePlugins() {
        bizSubPackage = "service";
        implSubPackage = "impl";
        htmlTargetProject = "src/main/resources/templates";
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
        String javaFileEncoding = context.getProperty("javaFileEncoding");
        String targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        List<GeneratedJavaFile> list = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        for (IntrospectedTable introspectedTable : context.getIntrospectedTables()) {
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
            String implClazzName = introspectedTable.getControllerBeanName();

            FullyQualifiedJavaType bizClazzImplType = new FullyQualifiedJavaType(StrBizPackage + "." + implSubPackage + "." + implClazzName);
            TopLevelClass bizClazzImpl = new TopLevelClass(bizClazzImplType);
            commentGenerator.addJavaFileComment(bizClazzImpl);
            bizClazzImpl.addImportedType(implSuperType);
            bizClazzImpl.addImportedType(entityType);
            bizClazzImpl.addImportedType(exampleType);
            bizClazzImpl.addImportedType(importAnnotation);
            bizClazzImpl.addImportedType(bizINF.getType());
            bizClazzImpl.setVisibility(JavaVisibility.PUBLIC);
            bizClazzImpl.setSuperClass(implSuperType);
            bizClazzImpl.addSuperInterface(bizINF.getType());
            sb.setLength(0);
            sb = new StringBuilder("@Service(\"").append(getTableBeanName(introspectedTable)).append("\")");
            bizClazzImpl.addAnnotation(sb.toString());
            sb.setLength(0);
            GeneratedJavaFile generatedJavaFilebizImpl = new GeneratedJavaFile(bizClazzImpl, targetProject,
                    context.getProperty("javaFileEncoding"), context.getJavaFormatter());
            list.add(generatedJavaFilebizImpl);

            /*生成controller文件*/
            GeneratedJavaFile generatedJavaFileController = generateControllerFile(introspectedTable,
                    bizClazzImplType,infName);
            if (generatedJavaFileController!=null) {
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
        Optional<String> generateController = Optional.ofNullable(introspectedTable.getTableConfigurationProperty("generateController"));
        generateController.orElse("true");
        StringBuilder sb = new StringBuilder();
        if (Boolean.valueOf(generateController.get())) {
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
            FullyQualifiedJavaType supClazzType = new FullyQualifiedJavaType("com.vgosoft.web.abs.AbsBaseController");
            conTopClazz.setSuperClass(supClazzType);
            conTopClazz.addImportedType(infName);
            conTopClazz.addImportedType(supClazzType);
            conTopClazz.addImportedType(entityType);
            conTopClazz.addImportedType(exampleType);
            conTopClazz.addImportedType(new FullyQualifiedJavaType("org.springframework.web.servlet.ModelAndView"));
            conClazzAddStaticImportedType(conTopClazz);
            sb.setLength(0);
            sb.append("@Api(value = \"/").append(lowerCaseEntityName).append("\", tags = \"");
            sb.append(introspectedTable.getRemarks()).append("\")");
            conTopClazz.addAnnotation(sb.toString());
            conTopClazz.addAnnotation("@RequestMapping(value = \"/" + introspectedTable.getControllerSimplePackage() + "\")");

            FullyQualifiedJavaType bizInfType = new FullyQualifiedJavaType(infName);
            Field field = new Field(seriveImplVar, bizInfType);
            field.addAnnotation("@Autowired");
            conTopClazz.addField(field);

            conTopClazz.addMethod(gc.viewGenerate());
            conTopClazz.addMethod(gc.getGenerate());
            conTopClazz.addMethod(gc.listGenerate());
            conTopClazz.addMethod(gc.createGenerate());
            conTopClazz.addMethod(gc.updateGenerate());
            conTopClazz.addMethod(gc.deleteGenerate());
            return new GeneratedJavaFile(conTopClazz, context.getJavaModelGeneratorConfiguration().getTargetProject(),
                    context.getProperty("javaFileEncoding"), context.getJavaFormatter());
        }
        return null;
    }




    /**
     * 内部方法
     * 为Controller类添加引入包路径
     *
     * @param conTopClazz
     */
    private void conClazzAddStaticImportedType(TopLevelClass conTopClazz) {
        StringBuilder sb = new StringBuilder();
        conTopClazz.addImportedType("com.vgosoft.web.ResponseSimple");
        conTopClazz.addImportedType("com.vgosoft.web.ResponseSimpleImpl");
        conTopClazz.addImportedType("com.vgosoft.web.vo.ResponseSimpleList");
        conTopClazz.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        conTopClazz.addImportedType("org.springframework.web.bind.annotation.*");
        conTopClazz.addImportedType("io.swagger.annotations.Api");
        conTopClazz.addImportedType("io.swagger.annotations.ApiOperation");
        conTopClazz.addJavaDocLine(sb.toString());
        conTopClazz.addImportedType("java.lang.reflect.Field");
        conTopClazz.addImportedType("java.util.List");
        conTopClazz.addJavaDocLine(sb.toString());
        conTopClazz.addAnnotation("@RestController");
    }

    /**
     * dao接口文件生成后，进行符合性调整
     *
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        /*调整引入*/
        interfaze.getImportedTypes().clear();
        String mapperAnnotation = "org.apache.ibatis.annotations.Mapper";
        interfaze.addImportedType(new FullyQualifiedJavaType(mapperAnnotation));
        interfaze.addImportedType(entityType);
        interfaze.addImportedType(exampleType);
        String mapperINF = "com.vgosoft.mybatis.inf.MBGMapperInterface";
        FullyQualifiedJavaType infSuperType = new FullyQualifiedJavaType(mapperINF);
        infSuperType.addTypeArgument(entityType);
        infSuperType.addTypeArgument(exampleType);
        interfaze.addImportedType(infSuperType);
        interfaze.addSuperInterface(infSuperType);
        interfaze.getMethods().clear();
        return true;
    }

    /**
     * model类生成后，进行符合性调整。
     * 添加@TableMeta和@ColumnMeta注解
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //为属性添加@TableMeta、@ColumnMeta注解
        String tableMetaAnnotationName = "com.vgosoft.core.annotation.TableMeta";
        FullyQualifiedJavaType annTable = new FullyQualifiedJavaType(tableMetaAnnotationName);
        String columnMetaAnnotationName = "com.vgosoft.core.annotation.ColumnMeta";
        FullyQualifiedJavaType annColum = new FullyQualifiedJavaType(columnMetaAnnotationName);
        topLevelClass.addImportedType(annTable);
        topLevelClass.addImportedType(annColum);
        for (int i = 0; i < topLevelClass.getFields().size(); i++) {
            Field field = topLevelClass.getFields().get(i);
            String columnMetaAnnotation = getColumnMetaAnnotation(field, introspectedTable, topLevelClass,i);
            if (columnMetaAnnotation.length() > 0) {
                field.addAnnotation(columnMetaAnnotation);
            }
        }
        String tableMetaAnnotation = getTableMetaAnnotation(topLevelClass, introspectedTable);
        topLevelClass.addAnnotation(tableMetaAnnotation);
        //添加序列化标识
        Field serialVersionUID = new Field("serialVersionUID", new FullyQualifiedJavaType("long"));
        serialVersionUID.setInitializationString("1l");
        serialVersionUID.setVisibility(JavaVisibility.PRIVATE);
        serialVersionUID.setFinal(true);
        serialVersionUID.setStatic(true);
        topLevelClass.getFields().add(0, serialVersionUID);
        //添加@Repository注解
        String repositoryAnnotation = "org.springframework.stereotype.Repository";
        topLevelClass.addImportedType(new FullyQualifiedJavaType(repositoryAnnotation));
        topLevelClass.addAnnotation("@Repository");

        String viewpath = introspectedTable.getTableConfigurationProperty("viewPath");

        //更新构造器
        List<Method> methods = topLevelClass.getMethods();
        for (Method method : methods) {
            if (method.isConstructor()) {
                addConstructorBodyLine(method, false, introspectedTable, viewpath);
            }
        }
        //添加一个参数的构造器
        Method method = new Method(topLevelClass.getType().getShortName());
        method.addParameter(new Parameter(new FullyQualifiedJavaType("int"), "persistenceStatus"));
        addConstructorBodyLine(method, true, introspectedTable, viewpath);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        topLevelClass.getMethods().add(1, method);
        topLevelClass.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.inf.PersistenceObject"));

        //判断是否需要实现ShowInWiew接口
        FullyQualifiedJavaType supperType = topLevelClass.getSuperClass().get();
        String s = supperType.getFullyQualifiedName();
        Boolean needViewPath = true;
        try {
            Class<?> pclazz = Class.forName("com.vgosoft.core.inf.ShowInView");
            Class<?> aClass = Class.forName(s);
            if (aClass.isInstance(pclazz)) {
                needViewPath = false;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (viewpath != null && needViewPath) {
            topLevelClass.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.inf.ShowInView"));
            //添加viewpath的属性及方法
            Field field = new Field("viewPath", new FullyQualifiedJavaType("String"));
            field.setVisibility(JavaVisibility.PRIVATE);
            topLevelClass.addField(field);
            method = new Method("getViewPath");
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setReturnType(new FullyQualifiedJavaType("String"));
            method.addBodyLine("return viewPath;");
            topLevelClass.addMethod(method);
            method = new Method("setViewPath");
            method.setVisibility(JavaVisibility.PUBLIC);
            method.addParameter(new Parameter(new FullyQualifiedJavaType("String"), "viewPath"));
            method.addBodyLine("this.viewPath = viewPath;");
            topLevelClass.addMethod(method);
            //为类添加ShowInView接口
            FullyQualifiedJavaType infType = new FullyQualifiedJavaType("com.vgosoft.core.inf.ShowInView");
            topLevelClass.addSuperInterface(infType);
            topLevelClass.addImportedType(infType);
        }
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        addSelectBySqlCondition(document, false, introspectedTable);
        addSelectBySqlCondition(document, true, introspectedTable);
        return true;
    }

    @Override
    public boolean htmlMapDocumentGenerated(org.mybatis.generator.api.dom.html.Document document, IntrospectedTable introspectedTable) {
        HtmlDocumentGenerator htmlDocumentGenerated;
        String uiFrame = getUiFrame(introspectedTable);
        if (HtmlConstants.HTML_UI_FRAME_LAYUI.equals(uiFrame)) {
            htmlDocumentGenerated = (HtmlDocumentGenerator) new LayuiDocumentGenerated(document, introspectedTable);
        }else if(HtmlConstants.HTML_UI_FRAME_ZUI.equals(uiFrame)){
            htmlDocumentGenerated = new ZuiDocumentGenerated(document, introspectedTable);
        }else{
            htmlDocumentGenerated = new LayuiDocumentGenerated(document, introspectedTable);
        }
        return htmlDocumentGenerated.htmlMapDocumentGenerated();
    }

    @Override
    public List<GeneratedHtmlFile> contextGenerateAdditionalHtmlFiles(IntrospectedTable introspectedTable) {
        List<GeneratedHtmlFile> htmlFiles = new ArrayList<>();
        return htmlFiles;
    }

    private String getUiFrame(IntrospectedTable introspectedTable) {
        TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();
        Optional<String> propertyt = Optional.ofNullable(tableConfiguration.getProperty(PropertyRegistry.TABLE_HTML_UI_FRAME));
        if (!propertyt.isPresent()) {
            Optional<String> propertyc = Optional.ofNullable(context.getProperty(PropertyRegistry.TABLE_HTML_UI_FRAME));
            if (!propertyc.isPresent()) {
                return HtmlConstants.HTML_UI_FRAME_LAYUI;
            }else{
                return propertyc.get();
            }
        }else{
            return propertyt.get();
        }
    }

    private void addSelectBySqlCondition(Document document, boolean isSub, IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        //追加selectBySqlCondition or selectBySqlConditionSub方法
        FullyQualifiedJavaType sqlparam = new FullyQualifiedJavaType("com.vgosoft.core.entity.ComSelSqlParameter");
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
            sb.append("and ID in(");
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
     * @param method 构造器方法
     * @param b      是否有参
     */
    private void addConstructorBodyLine(Method method, boolean b, IntrospectedTable introspectedTable, String viewpath) {
        if (b) {
            method.addBodyLine("super(persistenceStatus);");
        } else {
            method.addBodyLine("this.setPersistenceStatus(PersistenceObject.DEFAULT_PERSISTENCE_STATUS);");
        }
        method.addBodyLine("this.setPersistenceBeanName(\"" + getTableBeanName(introspectedTable) + "\");");
        if (viewpath != null) {
            method.addBodyLine("this.setViewPath(\"" + introspectedTable.getMyBatis3HtmlMapperViewName() + "\");");
        }
    }

    /**
     * model类的@TableMeta注解
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private String getTableMetaAnnotation(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();
        sb.append("@TableMeta(value = \"").append(tableConfiguration.getTableName()).append("\"");
        sb.append(", descript = \"").append(introspectedTable.getRemarks()).append("\"");
        sb.append(", beanname = \"").append(getTableBeanName(introspectedTable)).append("\")");
        return sb.toString();
    }

    /**
     * 获得对应的操作Bean的名称
     *
     * @param introspectedTable
     * @return
     */
    private String getTableBeanName(IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String implClazzName = entityType.getShortName() + JavaBeansUtil.getFirstCharacterUppercase(implSubPackage);
        return JavaBeansUtil.getFirstCharacterLowercase(implClazzName);
    }

    /**
     * @param field
     * @param introspectedTable
     * @param topLevelClass
     * @param i
     * @return
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
                sb.append(",order = ").append(String.valueOf(i+10));
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
        return sb.toString();
    }

    /**
     * 内部类
     * 获得Service抽象类父类
     *
     * @param entityType
     * @param exampleType
     * @return
     */
    private FullyQualifiedJavaType getServiceSupperType(FullyQualifiedJavaType entityType, FullyQualifiedJavaType exampleType) {
        String typeName = "com.vgosoft.mybatis.abs.service.AbsMBGServiceInterface";
        FullyQualifiedJavaType supperType = new FullyQualifiedJavaType(typeName);
        supperType.addTypeArgument(entityType);
        supperType.addTypeArgument(exampleType);
        return supperType;
    }

}