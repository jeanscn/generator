package org.mybatis.generator.codegen.mybatis3.vo;

import cn.hutool.core.util.ArrayUtil;
import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.ViewActionColumnEnum;
import com.vgosoft.core.constant.enums.ViewIndexColumnEnum;
import com.vgosoft.tool.core.VDateUtils;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VReflectionUtil;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.custom.pojo.RelationGeneratorConfiguration;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;
import static org.mybatis.generator.internal.util.StringUtility.packageToDir;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 生成VO抽象父类
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-07-05 03:29
 * @version 3.0
 */
public class ViewObjectClassGenerator extends AbstractJavaGenerator {

    public static final String subPackageVo = "vo";
    public static final String subPackageMaps = "maps";
    public static final String subPackageAbs = "abs";

    private CommentGenerator commentGenerator;
    private String targetPackage;
    private String baseTargetPackage;
    private String abstractName;
    private String abstractVoType;
    private String voType;
    private TopLevelClass voClass;
    private String createVoType;
    private TopLevelClass createVoClass;
    private String viewVOType;
    private TopLevelClass viewVOClass;
    private String excelVoType;
    private TopLevelClass excelVoClass;
    private String requestVoType;
    private TopLevelClass requestVoClass;
    private boolean generated = false;

    public ViewObjectClassGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        boolean forceGenerateScalableElement = introspectedTable.getRules().isForceGenerateScalableElement();
        List<CompilationUnit> answer = new ArrayList<>();
        commentGenerator = context.getCommentGenerator();
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.78", table.toString()));
        Plugin plugins = context.getPlugins();
        TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        abstractName = "Abstract" + entityType.getShortName() + "VO";

        /**
         * 生成VO类
         * */
        VOModelGeneratorConfiguration voModelGeneratorConfiguration = tableConfiguration.getVoModelGeneratorConfiguration();
        if (voModelGeneratorConfiguration != null && voModelGeneratorConfiguration.isGenerate()) {
            generated = true;
            targetPackage = voModelGeneratorConfiguration.getTargetPackage();
            baseTargetPackage = voModelGeneratorConfiguration.getBaseTargetPackage();
            abstractVoType = String.join(".", baseTargetPackage, subPackageAbs, abstractName);
            voType = voModelGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
            voClass = createTopLevelClass(voType, abstractVoType);
            voClass.addMultipleImports("lombok", "ApiModel");
            voClass.addAnnotation("@SuperBuilder");
            voClass.addAnnotation("@NoArgsConstructor");
            voClass.addImportedType("lombok.experimental.SuperBuilder");
            voClass.addAnnotation(getApiModel(voModelGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()));
            voClass.addField(builderSerialVersionUID());
            //添加id、version属性
            List<String> fields = Arrays.asList("id", "version");
            for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
                if (fields.contains(introspectedColumn.getJavaProperty())) {
                    Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
                    if (plugins.voModelFieldGenerated(field, voClass, introspectedColumn, introspectedTable)) {
                        voClass.addField(field);
                        voClass.addImportedType(field.getType());
                    }
                } else if (!(isAbstractVOColumn(introspectedColumn, introspectedTable) || introspectedColumn.isNullable())) {
                    //重写getter，添加validate
                    Method javaBeansGetter = JavaBeansUtil.getJavaBeansGetter(introspectedColumn, context, introspectedTable);
                    javaBeansGetter.addAnnotation("@Override");
                    if (plugins.voModelGetterMethodGenerated(javaBeansGetter, voClass, introspectedColumn, introspectedTable)) {
                        voClass.addMethod(javaBeansGetter);
                    }
                }
            }
            voClass.addImportedType(abstractVoType);
            //persistenceBeanName属性
            Field persistenceBeanName = new Field("persistenceBeanName", FullyQualifiedJavaType.getStringInstance());
            persistenceBeanName.setVisibility(JavaVisibility.PRIVATE);
            persistenceBeanName.setInitializationString("\""+introspectedTable.getControllerBeanName()+"\"");
            persistenceBeanName.addAnnotation("@ApiModelProperty(value = \"对象服务java bean名称\")");
            voClass.addField(persistenceBeanName);

            //检查是否有定制的新属性
            if (introspectedTable.getTableConfiguration().getRelationPropertyHolders().size()>0) {
                /*
                 * 根据联合查询属性配置
                 * 增加相应的属性
                 */
                if (introspectedTable.getRelationGeneratorConfigurations().size() > 0) {
                    for (RelationGeneratorConfiguration relationProperty : introspectedTable.getRelationGeneratorConfigurations()) {
                        FullyQualifiedJavaType returnType;
                        Field field;
                        FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(relationProperty.getModelTye());
                        if (entityType.getFullyQualifiedName().equalsIgnoreCase(relationProperty.getModelTye())) {
                            fullyQualifiedJavaType = new FullyQualifiedJavaType(voType);
                        }
                        if (relationProperty.getType().equals(RelationTypeEnum.collection)) {
                            FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
                            voClass.addImportedType(listType);
                            returnType = FullyQualifiedJavaType.getNewListInstance();
                            returnType.addTypeArgument(fullyQualifiedJavaType);
                            field = new Field(relationProperty.getPropertyName(), returnType);
                            voClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
                        } else {
                            returnType = fullyQualifiedJavaType;
                            field = new Field(relationProperty.getPropertyName(), returnType);
                        }
                        field.setVisibility(JavaVisibility.PRIVATE);
                        voClass.addField(field,null, true);
                        voClass.addImportedType(fullyQualifiedJavaType);
                    }
                }
            }

            if (forceGenerateScalableElement || fileNotExist(subPackageVo, voModelGeneratorConfiguration.getFullyQualifiedJavaType().getShortName())) {
                if (context.getPlugins().voModelRecordClassGenerated(voClass, introspectedTable)) {
                    answer.add(voClass);
                }
            }
        }

        /**
         *  生成createVo类
         *  */
        VOCreateGeneratorConfiguration voCreateGeneratorConfiguration = tableConfiguration.getVoCreateGeneratorConfiguration();
        if (voCreateGeneratorConfiguration != null && voCreateGeneratorConfiguration.isGenerate()) {
            generated = true;
            targetPackage = voCreateGeneratorConfiguration.getTargetPackage();
            baseTargetPackage = voCreateGeneratorConfiguration.getBaseTargetPackage();
            abstractVoType = String.join(".", baseTargetPackage, subPackageAbs, abstractName);
            createVoType=voCreateGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
            createVoClass = createTopLevelClass(createVoType, abstractVoType);
            createVoClass.addMultipleImports("lombok", "ApiModel","ApiModelProperty");
            createVoClass.addAnnotation(getApiModel(voCreateGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()));
            createVoClass.addImportedType(abstractVoType);
            createVoClass.addField(builderSerialVersionUID());

            List<IntrospectedColumn> voColumns = JavaBeansUtil.getVOColumns(introspectedTable,
                    voCreateGeneratorConfiguration.getIncludeColumns(),
                    voCreateGeneratorConfiguration.getExcludeColumns());
            introspectedTable.getAllColumns().stream()
                    .filter(c -> c.getActualColumnName().equalsIgnoreCase("ID_"))
                    .findFirst().ifPresent(id -> voColumns.add(0, id));
            for (IntrospectedColumn voColumn : voColumns) {
                Field field = new Field(voColumn.getJavaProperty(), voColumn.getFullyQualifiedJavaType());
                field.addAnnotation("@ApiModelProperty(value = \""+voColumn.getRemarks()+"\" ,example = "+JavaBeansUtil.getColumnExampleValue(voColumn)+")");

                field.setVisibility(JavaVisibility.PRIVATE);
                createVoClass.addField(field);
                createVoClass.addImportedType(voColumn.getFullyQualifiedJavaType());
            }

            for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
                if (!(isAbstractVOColumn(introspectedColumn, introspectedTable) || introspectedColumn.isNullable())) {
                    //重写getter，添加validate
                    Method javaBeansGetter = JavaBeansUtil.getJavaBeansGetter(introspectedColumn, context, introspectedTable);
                    javaBeansGetter.addAnnotation("@Override");
                    if (plugins.voCreateGetterMethodGenerated(javaBeansGetter, createVoClass, introspectedColumn, introspectedTable)) {
                        createVoClass.addMethod(javaBeansGetter);
                    }
                }
            }

            if (forceGenerateScalableElement || fileNotExist(subPackageVo, voCreateGeneratorConfiguration.getFullyQualifiedJavaType().getShortName())) {
                if (context.getPlugins().voModelCreateClassGenerated(createVoClass, introspectedTable)) {
                    answer.add(createVoClass);
                }
            }
        }

        /**
         * 生成viewVo类
         * */
        VOViewGeneratorConfiguration voViewGeneratorConfiguration = tableConfiguration.getVoViewGeneratorConfiguration();
        if (voViewGeneratorConfiguration != null && voViewGeneratorConfiguration.isGenerate()) {
            generated = true;
            targetPackage = voViewGeneratorConfiguration.getTargetPackage();
            baseTargetPackage = voViewGeneratorConfiguration.getBaseTargetPackage();
            abstractVoType = String.join(".", baseTargetPackage, subPackageAbs, abstractName);

            viewVOType = voViewGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
            viewVOClass = createTopLevelClass(viewVOType, abstractVoType);
            viewVOClass.addMultipleImports("lombok", "ApiModel", "ViewTableMeta");
            viewVOClass.addAnnotation(getApiModel(voViewGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()));
            String viewMeta = buildViewTableMeta(entityType);
            viewVOClass.addAnnotation(viewMeta);
            viewVOClass.addImportedType(abstractVoType);
            viewVOClass.addField(builderSerialVersionUID());

            if (forceGenerateScalableElement || fileNotExist(subPackageVo, voViewGeneratorConfiguration.getFullyQualifiedJavaType().getShortName())) {
                if (context.getPlugins().voModelViewClassGenerated(viewVOClass, introspectedTable)) {
                    answer.add(viewVOClass);
                }
            }

            //添加菜单项
            String parentMenuId = Optional.ofNullable(voViewGeneratorConfiguration.getParentMenuId())
                    .orElse(Optional.ofNullable(context.getParentMenuId())
                            .orElse(null));
            if (stringHasValue(parentMenuId)) {
                int sort = introspectedTable.getContext().getSysMenuDataScriptLines().size()+1;
                String id = VMD5Util.MD5(introspectedTable.getControllerBeanName() + GlobalConstant.DEFAULT_VIEW_ID_SUFFIX);
                String title = StringUtility.remarkLeft(introspectedTable.getRemarks());
                StringBuilder sb = new StringBuilder("INSERT INTO `SYS_MENU`");
                sb.append(" (ID_,DELETE_FLAG,NAME_,PARENT_ID,SORT_,TITLE_,URL_,ICON_,TYPE_,CONTAIN_TYPE,TARGET_,LEVEL_,STATE_,HIDE_,NOTES_,RIGHT_,VERSION_,CREATED_ID,MODIFIED_ID,TENANT_ID )");
                sb.append(" VALUES (");
                sb.append("'").append(id).append("'");
                sb.append(",").append("0");
                sb.append(",'").append(title).append("'");
                sb.append(",'").append(parentMenuId).append("'");
                sb.append(",").append(sort);
                sb.append(",'").append(title).append("'");
                sb.append(",'").append("viewmgr/").append(id).append("/openview'");
                sb.append(",'pli-list-view'");
                sb.append(",'link'");
                sb.append(",'tab'");
                sb.append(",'iframe',1,1,0");
                sb.append(",'").append(context.getModuleName()).append("->").append(title).append("默认视图");
                sb.append("',NULL,1");
                sb.append(",'").append(GlobalConstant.DEFAULT_SYSTEM_ADMIN_ID).append("'");
                sb.append(",'").append(GlobalConstant.DEFAULT_SYSTEM_ADMIN_ID).append("'");
                sb.append(",'").append(GlobalConstant.DEFAULT_TENANT_ID).append("'");
                sb.append(");");
                introspectedTable.getContext().addSysMenuDataScriptLines(id,sb.toString());
            }
        }

        /**
         *  生成ExcelVO
         *  */
        VOExcelGeneratorConfiguration voExcelGeneratorConfiguration = tableConfiguration.getVoExcelGeneratorConfiguration();
        if (voExcelGeneratorConfiguration != null && voExcelGeneratorConfiguration.isGenerate()) {
            generated = true;
            targetPackage = voExcelGeneratorConfiguration.getTargetPackage();
            baseTargetPackage = voExcelGeneratorConfiguration.getBaseTargetPackage();
            excelVoType = voExcelGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
            excelVoClass = createTopLevelClass(excelVoType, null);
            FullyQualifiedJavaType superInterface = new FullyQualifiedJavaType("com.vgosoft.core.pojo.IBaseDTO");
            excelVoClass.addSuperInterface(superInterface);
            excelVoClass.addImportedType(superInterface);
            excelVoClass.addImportedType("lombok.*");
            excelVoClass.addAnnotation("@Data");
            excelVoClass.addAnnotation("@Builder");
            excelVoClass.addAnnotation("@NoArgsConstructor");
            excelVoClass.addAnnotation("@AllArgsConstructor");
            excelVoClass.addImportedType("com.alibaba.excel.annotation.ExcelProperty");
            excelVoClass.addField(builderSerialVersionUID());
            //添加属性
            List<IntrospectedColumn> columns = JavaBeansUtil.getAllExcelVOColumns(introspectedTable);
            for (IntrospectedColumn column : columns) {
                Field field = getJavaBeansField(column, context, introspectedTable);
                field.setVisibility(JavaVisibility.PRIVATE);
                if (plugins.voExcelFieldGenerated(field, excelVoClass, column, introspectedTable)) {
                    excelVoClass.addField(field);
                    excelVoClass.addImportedType(field.getType());
                }
            }

            if (forceGenerateScalableElement || fileNotExist(subPackageVo, voExcelGeneratorConfiguration.getFullyQualifiedJavaType().getShortName())) {
                if (context.getPlugins().voModelExcelClassGenerated(excelVoClass, introspectedTable)) {
                    answer.add(excelVoClass);
                }
            }
        }

        /**
         * 生成RequestVO
         * */
        VORequestGeneratorConfiguration voRequestGeneratorConfiguration = tableConfiguration.getVoRequestGeneratorConfiguration();
        if (voRequestGeneratorConfiguration != null && voRequestGeneratorConfiguration.isGenerate()) {
            generated = true;
            targetPackage = voRequestGeneratorConfiguration.getTargetPackage();
            baseTargetPackage = voRequestGeneratorConfiguration.getBaseTargetPackage();
            abstractVoType = String.join(".", baseTargetPackage, subPackageAbs, abstractName);
            requestVoType = voRequestGeneratorConfiguration.getFullyQualifiedJavaType().getFullyQualifiedName();
            requestVoClass = createTopLevelClass(requestVoType, abstractVoType);
            requestVoClass.addMultipleImports("lombok", "ApiModel","ApiModelProperty");
            requestVoClass.addAnnotation(getApiModel(voRequestGeneratorConfiguration.getFullyQualifiedJavaType().getShortName()));
            requestVoClass.addField(builderSerialVersionUID());
            //分页属性
            if (voRequestGeneratorConfiguration.isIncludePageParam()) {
                FullyQualifiedJavaType pageType = new FullyQualifiedJavaType("com.vgosoft.core.pojo.IPage");
                requestVoClass.addSuperInterface(pageType);
                requestVoClass.addImportedType(pageType);
                Field pNo = new Field("pageNo", FullyQualifiedJavaType.getIntInstance());
                pNo.addAnnotation("@ApiModelProperty(value = \"页码\")");
                pNo.setVisibility(JavaVisibility.PRIVATE);
                pNo.setInitializationString("DEFAULT_FIRST_PAGE_NO");
                requestVoClass.addField(pNo);
                Field pSize = new Field("pageSize", FullyQualifiedJavaType.getIntInstance());
                pSize.addAnnotation("@ApiModelProperty(value = \"每页数据数量\")");
                pSize.setInitializationString("DEFAULT_PAGE_SIZE");
                pSize.setVisibility(JavaVisibility.PRIVATE);
                requestVoClass.addField(pSize);
            }
            //增加cascade开关
            if (introspectedTable.getRules().generateRelationWithSubSelected()) {
                Field cascade = new Field("cascadeResult", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
                cascade.addAnnotation("@ApiModelProperty(value = \"结果是否包含子级\")");
                cascade.setVisibility(JavaVisibility.PRIVATE);
                requestVoClass.addField(cascade);
            }

            if (forceGenerateScalableElement || fileNotExist(subPackageVo, voRequestGeneratorConfiguration.getFullyQualifiedJavaType().getShortName())) {
                if (context.getPlugins().voModelRequestClassGenerated(requestVoClass, introspectedTable)) {
                    answer.add(requestVoClass);
                }
            }

        }
        /**
         * 生成AbstractVo
         * */
        if (generated) {
            TopLevelClass abstractVo = new TopLevelClass(abstractVoType);
            abstractVo.setAbstract(true);
            abstractVo.setVisibility(JavaVisibility.PUBLIC);
            commentGenerator.addJavaFileComment(abstractVo);
            commentGenerator.addModelClassComment(abstractVo, introspectedTable);
            abstractVo.addAnnotation("@Data");
            abstractVo.addAnnotation("@NoArgsConstructor");
            abstractVo.addAnnotation("@AllArgsConstructor");
            abstractVo.addImportedType("lombok.*");
            abstractVo.addAnnotation("@SuperBuilder");
            abstractVo.addImportedType("lombok.experimental.SuperBuilder");
            //添加属性
            for (IntrospectedColumn introspectedColumn : JavaBeansUtil.getAbstractVOColumns(introspectedTable)) {
                Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
                field.setVisibility(JavaVisibility.PROTECTED);
                if (plugins.voAbstractFieldGenerated(field, abstractVo, introspectedColumn, introspectedTable)) {
                    abstractVo.addField(field);
                    abstractVo.addImportedType(field.getType());
                }
            }

            if (context.getPlugins().voModelAbstractClassGenerated(abstractVo, introspectedTable)) {
                answer.add(abstractVo);
            }
        }


        //生成mapstruct接口
        if (generated) {
            String mappingsName = entityType.getShortName() + "Mappings";

            String mappingsType = String.join(".", baseTargetPackage, subPackageMaps, mappingsName);
            Interface mappingsInterface = new Interface(mappingsType);
            mappingsInterface.setVisibility(JavaVisibility.PUBLIC);
            commentGenerator.addJavaFileComment(mappingsInterface);
            mappingsInterface.addImportedType(entityType);
            mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.Mapper"));
            mappingsInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.factory.Mappers"));
            mappingsInterface.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            mappingsInterface.addAnnotation("@Mapper(componentModel = \"spring\")");
            Field instance = new Field("INSTANCE", new FullyQualifiedJavaType(mappingsType));
            instance.setInitializationString(VStringUtil.format("Mappers.getMapper({0}.class)", mappingsInterface.getType().getShortName()));
            mappingsInterface.addField(instance);
            if (stringHasValue(voType)) {
                mappingsInterface.addImportedType(new FullyQualifiedJavaType(voType));
                mappingsInterface.addMethod(addMappingMethod(voClass.getType(), entityType, false));
                mappingsInterface.addMethod(addMappingMethod(entityType, voClass.getType(), false));
                mappingsInterface.addMethod(addMappingMethod(voClass.getType(), entityType, true));
                mappingsInterface.addMethod(addMappingMethod(entityType, voClass.getType(), true));
                Field children = voClass.getFields().stream().filter(f -> f.getName().equalsIgnoreCase("children")).findFirst().orElse(null);
                if (children!=null) {
                    String rootClass = JavaBeansUtil.getRootClass(introspectedTable);
                    try {
                        Class<?> aClass = ObjectFactory.internalClassForName(rootClass);
                        java.lang.reflect.Field children1 = VReflectionUtil.getField(aClass, "children");
                        if (children1 != null) {
                            if (children1.getDeclaringClass().getSimpleName().equalsIgnoreCase("simpleKVP")) {
                                //<? extents T>.
                                List<Class<?>> superClasses = VReflectionUtil.getInterfaces(aClass);
                                for (Class<?> superClass : superClasses) {
                                    if (ConstantsUtil.childrenGenericClasses.containsKey(superClass.getSimpleName())) {
                                        FullyQualifiedJavaType t = new FullyQualifiedJavaType(ConstantsUtil.childrenGenericClasses.get(superClass.getSimpleName()));
                                        mappingsInterface.addMethod(addMappingMethod(t, voClass.getType(), false));
                                        mappingsInterface.addImportedType(t);
                                    }
                                }
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (stringHasValue(createVoType)) {
                mappingsInterface.addImportedType(new FullyQualifiedJavaType(createVoType));
                mappingsInterface.addMethod(addMappingMethod(createVoClass.getType(), entityType,false));
                mappingsInterface.addMethod(addMappingMethod(createVoClass.getType(), entityType,true));
            }

            if (stringHasValue(excelVoType)) {
                mappingsInterface.addImportedType(new FullyQualifiedJavaType(excelVoType));
                mappingsInterface.addMethod(addMappingMethod(entityType, excelVoClass.getType(), false));
                mappingsInterface.addMethod(addMappingMethod(entityType, excelVoClass.getType(), true));
                mappingsInterface.addMethod(addMappingMethod(excelVoClass.getType(), entityType,false));
                mappingsInterface.addMethod(addMappingMethod(excelVoClass.getType(), entityType,true));
            }

            if (stringHasValue(viewVOType)) {
                mappingsInterface.addImportedType(new FullyQualifiedJavaType(viewVOType));
                mappingsInterface.addMethod(addMappingMethod(entityType, viewVOClass.getType(), false));
                mappingsInterface.addMethod(addMappingMethod(entityType, viewVOClass.getType(), true));
            }
            if (stringHasValue(requestVoType)) {
                mappingsInterface.addImportedType(new FullyQualifiedJavaType(requestVoType));
                mappingsInterface.addMethod(addMappingMethod(requestVoClass.getType(),entityType, false));
            }
            if (forceGenerateScalableElement || fileNotExist(subPackageMaps, mappingsName)) {
                answer.add(mappingsInterface);
            }
        }
        return answer;
    }

    private String buildViewTableMeta(FullyQualifiedJavaType entityType) {
        VOViewGeneratorConfiguration voViewGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoViewGeneratorConfiguration();
        //viewId
        String viewId = "value = \""+ VMD5Util.MD5(introspectedTable.getControllerBeanName() + GlobalConstant.DEFAULT_VIEW_ID_SUFFIX)+"\"";
        String listName = "listName = \""+introspectedTable.getRemarks()+"\"";
        String beanName = "beanName = \""+introspectedTable.getControllerBeanName()+"\"";
        //createUrl
        String createUrl = "";
        FullyQualifiedJavaType rootType = new FullyQualifiedJavaType(getRootClass());
        if (stringHasValue(rootType.getShortName())) {
            if (EntityAbstractParentEnum.ofCode(rootType.getShortName()) != null
                    && EntityAbstractParentEnum.ofCode(rootType.getShortName()).scope() != 2) {
                createUrl = String.join("/"
                        , introspectedTable.getControllerSimplePackage()
                        , introspectedTable.getControllerBeanName()
                        , "view");
            }
        }
        if (stringHasValue(createUrl)) {
            createUrl = "createUrl = \"" + createUrl + "\"";
        }
        //indexColumn
        String indexColumn = "";
        viewVOClass.addImportedType("com.vgosoft.core.constant.enums.ViewIndexColumnEnum");
        ViewIndexColumnEnum viewIndexColumnEnum = ViewIndexColumnEnum.ofCode(voViewGeneratorConfiguration.getIndexColumn());
        if (viewIndexColumnEnum != null) {
            indexColumn = "indexColumn = ViewIndexColumnEnum."+viewIndexColumnEnum.name();
        }
        //actionColumn
        String actionColumn = "";
        if (voViewGeneratorConfiguration.getActionColumn().size()>0) {
            viewVOClass.addImportedType("com.vgosoft.core.constant.enums.ViewActionColumnEnum");
            String actions = voViewGeneratorConfiguration.getActionColumn().stream()
                    .map(ViewActionColumnEnum::ofCode)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(e -> "ViewActionColumnEnum." + e.name())
                    .collect(Collectors.joining(","));
            actionColumn = "actionColumn = {"+actions+"}";
        }
        //querys
        String querys = "";
        if (voViewGeneratorConfiguration.getQueryColumns().size()>0) {
            viewVOClass.addImportedType("com.vgosoft.core.annotation.CompositeQuery");
            String compositeQuery = voViewGeneratorConfiguration.getQueryColumns().stream()
                    .distinct()
                    .map(f -> introspectedTable.getColumn(f).orElse(null))
                    .filter(Objects::nonNull)
                    .map(c -> VStringUtil.format("@CompositeQuery(value = \"{0}\",description = \"{1}\")", c.getActualColumnName(), c.getRemarks()))
                    .collect(Collectors.joining("\n        , "));
            querys = "querys = {"+compositeQuery+"}";
        }
        //columns
        String columns = "";
        if (voViewGeneratorConfiguration.getIncludeColumns().size()>0) {
            viewVOClass.addImportedType("com.vgosoft.core.annotation.ViewColumnMeta");
            String columns1 = voViewGeneratorConfiguration.getIncludeColumns().stream()
                    .distinct()
                    .map(f -> introspectedTable.getColumn(f).orElse(null))
                    .filter(Objects::nonNull)
                    .map(c -> VStringUtil.format("@ViewColumnMeta(value = \"{0}\",title = \"{1}\")", c.getJavaProperty(), c.getRemarks()))
                    .collect(Collectors.joining("\n        , "));
            columns = "columns = {"+columns1+"}";
        }
        //ignoreFields
        String ignoreFields = "";
        if (voViewGeneratorConfiguration.getExcludeColumns().size()>0) {
            String columns2 = voViewGeneratorConfiguration.getExcludeColumns().stream()
                    .distinct()
                    .map(f -> introspectedTable.getColumn(f).orElse(null))
                    .filter(Objects::nonNull)
                    .map(IntrospectedColumn::getJavaProperty)
                    .collect(Collectors.joining(","));
            ignoreFields = "ignoreFields = \""+columns2+"\"";
        }
        //className
        String className = VStringUtil.format("className = \"{0}\"",viewVOType);
        //构造ViewTableMeta
        String[] allItem = {viewId, listName, beanName, createUrl, indexColumn, actionColumn,querys,columns,ignoreFields,className};
        String join = String.join("\n        , ", ArrayUtil.removeBlank(allItem));
        return VStringUtil.format("@ViewTableMeta({0})",join);
    }

    private String getApiModel(String voModelName) {
        return VStringUtil.format("@ApiModel(value = \"{0}\", description = \"{1}\", parent = {2}.class )", voModelName,
                introspectedTable.getRemarks(), abstractName);
    }

    private boolean fileNotExist(String subPackage, String fileName) {
        File project = new File(baseTargetPackage);
        if (!project.isDirectory()) {
            return true;
        }
        File directory = new File(project, packageToDir(String.join(".", targetPackage, subPackage)));
        if (!directory.isDirectory()) {
            return true;
        }
        File file = new File(directory, fileName + ".java");
        return !file.exists();
    }

    private TopLevelClass createTopLevelClass(String type, String superType) {
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass);
        if (!VStringUtil.isBlank(superType)) {
            topLevelClass.addImportedType(superType);
            topLevelClass.setSuperClass(superType);
        }
        return topLevelClass;
    }

    private Method addMappingMethod(FullyQualifiedJavaType fromType, FullyQualifiedJavaType toType, boolean isList) {
        String methodName;
        Method method;
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        if (entityType.getFullyQualifiedName().equalsIgnoreCase(toType.getFullyQualifiedName())) {
            methodName = "from" + fromType.getShortName();
        } else if (entityType.getFullyQualifiedName().equalsIgnoreCase(fromType.getFullyQualifiedName())) {
            methodName = "to" + toType.getShortName();
        } else {
            methodName = "from"+fromType.getShortName() + "To" + toType.getShortName();
        }
        if (isList) {
            methodName = methodName + "s";
            method = new Method(methodName);
            FullyQualifiedJavaType listInstanceFrom = FullyQualifiedJavaType.getNewListInstance();
            listInstanceFrom.addTypeArgument(fromType);
            method.addParameter(new Parameter(listInstanceFrom, JavaBeansUtil.getFirstCharacterLowercase(fromType.getShortName()) + "s"));
            FullyQualifiedJavaType listInstanceTo = FullyQualifiedJavaType.getNewListInstance();
            listInstanceTo.addTypeArgument(toType);
            method.setReturnType(listInstanceTo);
        } else {
            method = new Method(methodName);
            method.addParameter(new Parameter(fromType, JavaBeansUtil.getFirstCharacterLowercase(fromType.getShortName())));
            method.setReturnType(toType);
        }
        method.setAbstract(true);
        return method;
    }

    private boolean isAbstractVOColumn(IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> columnList = JavaBeansUtil.getAbstractVOColumns(introspectedTable);
        List<String> actualColumnNameList = columnList.stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.toList());
        return !actualColumnNameList.contains(introspectedColumn.getActualColumnName());

    }
}
