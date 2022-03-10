package org.mybatis.generator.custom;

import com.vgosoft.tool.core.VStringUtil;
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
import org.mybatis.generator.config.PropertyScope;
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
import java.util.Optional;

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
    private static final String abstractEntity = "com.vgosoft.core.entity.AbstractEntity";
    private static final String comSelSqlParameter = "com.vgosoft.core.entity.ComSelSqlParameter";
    public static final String vStringUtil = "com.vgosoft.tool.core.VStringUtil";
    //service实现抽象父类
    private static final String abstractMBGServiceInterface = "com.vgosoft.mybatis.abs.AbstractMybatisBGService";
    private static final String abstractMBGBlobServiceInterface = "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobBaseService";
    private static final String abstractMBGBlobFileService = "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobFileService";
    private static final String abstractMBGBlobBytesService = "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobBytesService";
    private static final String abstractMBGBlobStringService = "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobStringService";
    private static final String abstractServiceBusiness = "com.vgosoft.mybatis.abs.AbstractMybatisServiceBusiness";
    private static final String abstractBlobFileServiceBusiness = "com.vgosoft.mybatis.abs.AbstractBlobFileServiceBusiness";
    private static final String abstractBlobBytesServiceBusiness = "com.vgosoft.mybatis.abs.AbstractBlobBytesServiceBusiness";
    private static final String abstractBlobStringServiceBusiness = "com.vgosoft.mybatis.abs.AbstractBlobStringServiceBusiness";
    //service接口父类
    private static final String mBGServiceInterface = "com.vgosoft.mybatis.inf.IMybatisBGService";
    private static final String mBGBlobServiceInterface = "com.vgosoft.mybatis.inf.IMybatisBGBlobService";
    private static final String mBGBlobFileService = "com.vgosoft.mybatis.inf.IMybatisBGBlobFileService";
    private static final String mBGBlobBytesService = "com.vgosoft.mybatis.inf.IMybatisBGBlobBytesService";
    private static final String mBGBlobStringService = "com.vgosoft.mybatis.inf.IMybatisBGBlobStringService";

    //mapper接口
    public static final String mBGMapperInterface = "com.vgosoft.mybatis.inf.MBGMapperInterface";
    public static final String mBGMapperBlobInterface = "com.vgosoft.mybatis.inf.MBGMapperBlobInterface";

    private static final String ABSTRACT_BASE_CONTROLLER = "com.vgosoft.web.controller.abs.AbstractBaseController";
    private static final String repositoryAnnotation = "org.springframework.stereotype.Repository";
    //Service结果包装类
    private static final String serviceResult = "com.vgosoft.core.adapter.ServiceResult";



    private static final String bizSubPackage = "service";
    private static final String implSubPackage = "impl";
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
                //增加selectByExampleWithRelation接口方法
                if (introspectedTable.getRelationProperties().size() > 0) {
                    Method example = getMethodByType(introspectedTable.getSelectByExampleWithRelationStatementId(), entityType,
                            exampleType, "example", true, "查询条件example对象");
                    bizINF.addMethod(example);
                    bizINF.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                }

                //增加selectTreeByParentId
                if (introspectedTable.getCustomAddtionalSelectMethods().size() > 0
                        && introspectedTable.getCustomAddtionalSelectMethods().containsKey(introspectedTable.getSelectTreeByParentIdStatementId())) {
                    CustomMethodProperties customMethodProperties = introspectedTable.getCustomAddtionalSelectMethods().get(introspectedTable.getSelectTreeByParentIdStatementId());
                    addAbstractMethodByColumn(bizINF, entityType, customMethodProperties.getParentIdColumn(), introspectedTable.getSelectTreeByParentIdStatementId());
                }

                if (introspectedTable.getSelectByColumnProperties().size() > 0) {
                    for (SelectByColumnProperties selectByColumnProperty : introspectedTable.getSelectByColumnProperties()) {
                        addAbstractMethodByColumn(bizINF, entityType, selectByColumnProperty.getColumn());
                    }
                }

                if (introspectedTable.getSelectByTableProperties().size()>0) {
                    for (SelectByTableProperties selectByTableProperty : introspectedTable.getSelectByTableProperties()) {
                        Method selectByTable = getMethodByType(selectByTableProperty.getMethodName(), entityType,
                                new FullyQualifiedJavaType("java.lang.String"), selectByTableProperty.getParameterName(), true,
                                "中间表中来自其他表的查询键值");
                        bizINF.addMethod(selectByTable);
                        bizINF.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                        bizINF.addImportedType(entityType);
                    }
                }

                GeneratedJavaFile generatedJavaFile = new GeneratedJavaFile(bizINF, targetProject,
                        context.getProperty("javaFileEncoding"), context.getJavaFormatter());
                list.add(generatedJavaFile);

                /*生成service实现接口*/
                String serviceAnnotation = "org.springframework.stereotype.Service";
                FullyQualifiedJavaType importAnnotation = new FullyQualifiedJavaType(serviceAnnotation);
                FullyQualifiedJavaType implSuperType = getServiceSupperType(entityType, exampleType, introspectedTable);

                TopLevelClass bizClazzImpl = new TopLevelClass(bizClazzImplType);
                commentGenerator.addJavaFileComment(bizClazzImpl);
                bizClazzImpl.addImportedType(implSuperType);
                bizClazzImpl.addImportedType(entityType);
                bizClazzImpl.addImportedType(exampleType);
                bizClazzImpl.addImportedType(bizINF.getType());
                bizClazzImpl.addImportedType("lombok.RequiredArgsConstructor");
                bizClazzImpl.setVisibility(JavaVisibility.PUBLIC);
                bizClazzImpl.setSuperClass(implSuperType);
                bizClazzImpl.addSuperInterface(bizINF.getType());
                bizClazzImpl.addAnnotation("@RequiredArgsConstructor");

                //增加selectByExampleWithRelation接口实现方法
                if (introspectedTable.getRelationProperties().size() > 0) {
                    Method example = getMethodByType(introspectedTable.getSelectByExampleWithRelationStatementId(),
                            entityType, exampleType, "example", false, "查询条件example对象");
                    example.addAnnotation("@Override");
                    sb.setLength(0);
                    sb.append("return mapper.");
                    sb.append(introspectedTable.getSelectByExampleWithRelationStatementId());
                    sb.append("(example);");
                    example.addBodyLine(sb.toString());
                    bizClazzImpl.addMethod(example);
                    bizClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                    addJavaMapper(introspectedTable, bizClazzImpl);

                }
                if (introspectedTable.getSelectByColumnProperties().size() > 0) {
                    for (SelectByColumnProperties selectByColumnProperty : introspectedTable.getSelectByColumnProperties()) {
                        IntrospectedColumn foreignKeyColumn = selectByColumnProperty.getColumn();
                        Method methodByColumn = getMethodByColumn(entityType, foreignKeyColumn,
                                JavaBeansUtil.byColumnMethodName(foreignKeyColumn), false);
                        methodByColumn.addAnnotation("@Override");
                        addJavaMapper(introspectedTable, bizClazzImpl);
                        sb.setLength(0);
                        sb.append("return mapper.");
                        sb.append(JavaBeansUtil.byColumnMethodName(foreignKeyColumn));
                        sb.append("(");
                        sb.append(foreignKeyColumn.getJavaProperty());
                        sb.append(");");
                        methodByColumn.addBodyLine(sb.toString());
                        bizClazzImpl.addMethod(methodByColumn);
                        bizClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                        bizClazzImpl.addImportedType(foreignKeyColumn.getFullyQualifiedJavaType());
                    }
                }

                //增加selectTreeByParentId
                if (introspectedTable.getCustomAddtionalSelectMethods().size() > 0
                        && introspectedTable.getCustomAddtionalSelectMethods().containsKey(introspectedTable.getSelectTreeByParentIdStatementId())) {
                    CustomMethodProperties customMethodProperties = introspectedTable.getCustomAddtionalSelectMethods().get(introspectedTable.getSelectTreeByParentIdStatementId());
                    Method methodByColumn = getMethodByColumn(entityType, customMethodProperties.getParentIdColumn(),
                            customMethodProperties.getMethodName(), false);
                    methodByColumn.addAnnotation("@Override");
                    addJavaMapper(introspectedTable, bizClazzImpl);
                    sb.setLength(0);
                    sb.append("return mapper.");
                    sb.append(customMethodProperties.getMethodName());
                    sb.append("(");
                    sb.append(customMethodProperties.getParentIdColumn().getJavaProperty());
                    sb.append(");");
                    methodByColumn.addBodyLine(sb.toString());
                    bizClazzImpl.addMethod(methodByColumn);
                    bizClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                    bizClazzImpl.addImportedType(customMethodProperties.getParentIdColumn().getFullyQualifiedJavaType());
                }

                //增加selectByTable方法
                for (SelectByTableProperties selectByTableProperty : introspectedTable.getSelectByTableProperties()) {
                    Method selectByTable = getMethodByType(selectByTableProperty.getMethodName(), entityType,
                            new FullyQualifiedJavaType("java.lang.String"), selectByTableProperty.getParameterName(), false,
                            "中间表中来自其他表的查询键值");
                    selectByTable.setVisibility(JavaVisibility.PUBLIC);
                    selectByTable.addAnnotation("@Override");
                    addJavaMapper(introspectedTable, bizClazzImpl);
                    sb.setLength(0);
                    sb.append("return mapper.");
                    sb.append(selectByTableProperty.getMethodName());
                    sb.append("(");
                    sb.append(selectByTableProperty.getParameterName());
                    sb.append(");");
                    selectByTable.addBodyLine(sb.toString());
                    bizClazzImpl.addMethod(selectByTable);
                    bizClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                    bizClazzImpl.addImportedType(new FullyQualifiedJavaType("java.lang.String"));
                }


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

    private void addJavaMapper(IntrospectedTable introspectedTable, TopLevelClass bizClazzImpl) {
        long mapper1 = bizClazzImpl.getFields().stream().filter(f -> f.getName().equalsIgnoreCase("mapper")).count();
        if (mapper1 == 0) {
            Field mapperProperty = getMapperProperty(introspectedTable);
            bizClazzImpl.addField(mapperProperty);
            bizClazzImpl.addImportedType(mapperProperty.getType());
        }
    }

    private Field getMapperProperty(IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        Field mapper = new Field("mapper", mapperType);
        mapper.setFinal(true);
        mapper.setVisibility(JavaVisibility.PRIVATE);
        return mapper;
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
            String serviceImplVar = JavaBeansUtil.getFirstCharacterLowercase(serviceImplShortName);

            sb.append(StringUtility.substringBeforeLast(entityType.getPackageName(), "."));
            sb.append(".").append("controller").append(".").append(entityShortName).append("Controller");
            FullyQualifiedJavaType conClazzType = new FullyQualifiedJavaType(sb.toString());
            TopLevelClass conTopClazz = new TopLevelClass(conClazzType);
            conTopClazz.setVisibility(JavaVisibility.PUBLIC);
            commentGenerator.addJavaFileComment(conTopClazz);
            FullyQualifiedJavaType supClazzType = new FullyQualifiedJavaType(ABSTRACT_BASE_CONTROLLER);
            conTopClazz.setSuperClass(supClazzType);
            conTopClazz.addImportedType(serviceResult);
            conTopClazz.addImportedType(infName);
            conTopClazz.addImportedType(supClazzType);
            conTopClazz.addImportedType(entityType);
            conTopClazz.addImportedType(exampleType);
            conClazzAddStaticImportedType(conTopClazz);
            //因需添加的静态导入
            //文件上传下载相关
            Boolean blobInstance = GenerateUtils.isBlobInstance(introspectedTable);
            if (blobInstance) {
                conTopClazz.addImportedType("org.apache.commons.lang3.StringUtils");
                conTopClazz.addImportedType("org.springframework.web.multipart.MultipartFile");
                conTopClazz.addImportedType("org.springframework.http.MediaType");
                conTopClazz.addImportedType("org.apache.commons.lang3.StringUtils");
                conTopClazz.addImportedType("javax.servlet.http.HttpServletResponse");
                conTopClazz.addImportedType("org.springframework.util.Assert");
                conTopClazz.addImportedType("org.apache.commons.lang3.BooleanUtils");
            }
            sb.setLength(0);
            sb.append("@Api(tags = \"");
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
            if (blobInstance) {
                conTopClazz.addMethod(gc.uploadGenerate());
                conTopClazz.addMethod(gc.downloadGenerate());
            }
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
        conTopClazz.addImportedType(serviceResult);
        conTopClazz.addImportedType(vStringUtil);
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseSimple");
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseSimpleImpl");
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseList");
        conTopClazz.addImportedType("com.vgosoft.web.respone.ResponseSimpleList");
        conTopClazz.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        conTopClazz.addImportedType("org.springframework.web.bind.annotation.*");
        conTopClazz.addImportedType("io.swagger.annotations.Api");
        conTopClazz.addImportedType("io.swagger.annotations.ApiOperation");
        conTopClazz.addJavaDocLine("");
        conTopClazz.addImportedType("java.util.Objects");
        conTopClazz.addImportedType("java.util.List");
        conTopClazz.addImportedType("java.util.Optional");
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
        if (introspectedTable.getRelationProperties().size() > 0) {
            Method example = getMethodByType(introspectedTable.getSelectByExampleWithRelationStatementId(), entityType,
                    exampleType, "example", true, "查询条件对象");
            interFace.addMethod(example);
            interFace.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            interFace.addImportedType(entityType);
        }
        //增加by外键
        if (introspectedTable.getSelectByColumnProperties().size() > 0) {
            for (SelectByColumnProperties selectByColumnProperty : introspectedTable.getSelectByColumnProperties()) {
                addAbstractMethodByColumn(interFace, entityType, selectByColumnProperty.getColumn());
            }
        }
        //增加
        if (introspectedTable.getCustomAddtionalSelectMethods().size() > 0
                && introspectedTable.getCustomAddtionalSelectMethods().containsKey(introspectedTable.getSelectTreeByParentIdStatementId())) {
            CustomMethodProperties customMethodProperties = introspectedTable.getCustomAddtionalSelectMethods().get(introspectedTable.getSelectTreeByParentIdStatementId());
            addAbstractMethodByColumn(interFace, entityType, customMethodProperties.getParentIdColumn(), introspectedTable.getSelectTreeByParentIdStatementId());
        }

        if (introspectedTable.getSelectByTableProperties().size()>0) {
            for (SelectByTableProperties selectByTableProperty : introspectedTable.getSelectByTableProperties()) {
                Method selectByTable = getMethodByType(selectByTableProperty.getMethodName(), entityType,
                        new FullyQualifiedJavaType("java.lang.String"), selectByTableProperty.getParameterName(), true,
                        "中间表中来自其他表的查询键值");
                interFace.addMethod(selectByTable);
            }
            interFace.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            interFace.addImportedType(entityType);
        }
        return true;
    }

    private void addAbstractMethodByColumn(Interface interFace, FullyQualifiedJavaType entityType, IntrospectedColumn foreignKeyColumn) {
        Method method = new Method(JavaBeansUtil.byColumnMethodName(foreignKeyColumn));
        addAbstractMethodByColumn(interFace, entityType, foreignKeyColumn, JavaBeansUtil.byColumnMethodName(foreignKeyColumn));
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
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(returnType);
        method.setReturnType(listType);
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
        boolean assignable1 = JavaBeansUtil.isAssignableCurrent(abstractEntity, topLevelClass, introspectedTable);
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
                returnType = FullyQualifiedJavaType.getNewListInstance();
                topLevelClass.addImportedType(returnType);
                returnType.addTypeArgument(addPropertyType);
            } else {
                returnType = addPropertyType;
                if (StringUtils.isNotEmpty(pName)) {
                    propertyName = pName;
                } else {
                    propertyName = javaModelName;
                }
            }
            Field field = new Field(propertyName, returnType);
            addField(topLevelClass, field);
            topLevelClass.addImportedType(javaModelAdditionProperty);
        }
        //根据新参数添加
        if (introspectedTable.getRelationProperties().size() > 0) {
            for (RelationPropertyHolder relationProperty : introspectedTable.getRelationProperties()) {
                FullyQualifiedJavaType returnType;
                FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(relationProperty.getModelTye());
                if (relationProperty.getType().equals(RelationTypeEnum.collection)) {
                    FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
                    topLevelClass.addImportedType(listType);
                    returnType = FullyQualifiedJavaType.getNewListInstance();
                    returnType.addTypeArgument(fullyQualifiedJavaType);
                } else {
                    returnType = fullyQualifiedJavaType;
                }
                Field field = new Field(relationProperty.getPropertyName(), returnType);
                addField(topLevelClass, field);
                topLevelClass.addImportedType(fullyQualifiedJavaType);
            }
        }

        //追加respBasePath属性
        Field field = new Field(PROP_NAME_REST_BASE_PATH, new FullyQualifiedJavaType("String"));
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
        String viewpath = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_VIEW_PATH);
        if (!StringUtility.isEmpty(viewpath)) {
            initializationBlock.addBodyLine(VStringUtil.format("this.{0} = \"{1}\";",PROP_NAME_VIEW_PATH, introspectedTable.getMyBatis3HtmlMapperViewName()));
            //判断是否需要实现ShowInView接口
            boolean assignable = JavaBeansUtil.isAssignableCurrent(INTERFACE_SHOW_IN_VIEW, topLevelClass, introspectedTable);
            if (!assignable) {
                //添加ShowInView接口
                FullyQualifiedJavaType showInView = new FullyQualifiedJavaType(INTERFACE_SHOW_IN_VIEW);
                topLevelClass.addImportedType(showInView);
                topLevelClass.addSuperInterface(showInView);
                //添加viewpath的属性及方法
                Field viewPath = new Field(PROP_NAME_VIEW_PATH, new FullyQualifiedJavaType("String"));
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
    private void addConstructorBodyLine(Method method, boolean existParameters, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        boolean assignable1 = JavaBeansUtil.isAssignableCurrent(abstractEntity, topLevelClass, introspectedTable);
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
    private FullyQualifiedJavaType getServiceSupperType(FullyQualifiedJavaType entityType, FullyQualifiedJavaType exampleType, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType supperType = new FullyQualifiedJavaType(getAbstractService(introspectedTable));
        supperType.addTypeArgument(entityType);
        supperType.addTypeArgument(exampleType);
        return supperType;
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
     * 获得service类的抽象实现类
     *
     * @param introspectedTable 生成基类
     */
    private String getAbstractService(IntrospectedTable introspectedTable) {
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            String steamOutType = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_JAVA_MODEL_BYTE_STREAM_OUTPUT_MODE);
            if (GenerateUtils.isBusinessInstance(introspectedTable)) {
                switch (steamOutType) {
                    case "bytes":
                        return abstractBlobBytesServiceBusiness;
                    case "file":
                        return abstractBlobFileServiceBusiness;
                    case "string":
                        return abstractBlobStringServiceBusiness;
                }
                return abstractServiceBusiness;
            } else {
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
     *
     * @param introspectedTable 生成基类
     */
    private String getServiceInterface(IntrospectedTable introspectedTable) {
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            String steamOutType = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_JAVA_MODEL_BYTE_STREAM_OUTPUT_MODE);
            switch (steamOutType) {
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
    private String getMapperInterface(IntrospectedTable introspectedTable) {
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            return mBGMapperBlobInterface;
        }
        return mBGMapperInterface;
    }

}
