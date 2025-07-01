package org.mybatis.generator.codegen.mybatis3.model;

import com.vgosoft.core.db.util.JDBCUtil;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.enums.ModelClassTypeEnum;
import org.mybatis.generator.custom.enums.RelationTypeEnum;
import org.mybatis.generator.custom.annotations.ApiModelPropertyDesc;
import org.mybatis.generator.custom.annotations.mybatisplus.TableField;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.VoGenService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.I_PERSISTENCE_BASIC;
import static org.mybatis.generator.custom.ConstantsUtil.PARAM_NAME_PERSISTENCE_STATUS;
import static org.mybatis.generator.internal.util.JavaBeansUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class BaseRecordGenerator extends AbstractJavaGenerator {

    public BaseRecordGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<>();

        JavaModelGeneratorConfiguration configuration = introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration();
        if (configuration == null || !configuration.isGenerate()) {
            return answer;
        }


        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
        Plugin plugins = context.getPlugins();
        CommentGenerator commentGenerator = context.getCommentGenerator();

        //类名
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        //类文件注释
        commentGenerator.addJavaFileComment(topLevelClass);
        //是否忽略tenantId
        if (context.isIntegrateMybatisPlus() && configuration.isIgnoreTenant()) {
            topLevelClass.addAnnotation("@IgnoreTenant");
            topLevelClass.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.annotation.IgnoreTenant"));
        }
        //父类
        FullyQualifiedJavaType superClass = getSuperClass();
        if (superClass != null) {
            topLevelClass.setSuperClass(superClass);
            topLevelClass.addImportedType(superClass);
            //泛型
            List<String> rootClassTypeArguments = getRootClassTypeArguments(introspectedTable);
            rootClassTypeArguments.forEach(s -> superClass.addTypeArgument(new FullyQualifiedJavaType(s)));
        }
        String superInterface = introspectedTable.getTableConfiguration().getProperty("superInterface");
        if (StringUtility.stringHasValue(superInterface)) {
            Arrays.stream(superInterface.split(","))
                    .map(FullyQualifiedJavaType::new)
                    .forEach(s -> {
                        topLevelClass.addSuperInterface(s);
                        topLevelClass.addImportedType(s);
                    });
        }
        //类注释
        commentGenerator.addModelClassComment(topLevelClass, introspectedTable);

        List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();

        if (introspectedTable.isConstructorBased()) {
            addParameterizedConstructor(topLevelClass, introspectedTable.getNonBLOBColumns());

            if (includeBLOBColumns()) {
                addParameterizedConstructor(topLevelClass, introspectedTable.getAllColumns());
            }

            if (!introspectedTable.isImmutable()) {
                addDefaultConstructor(topLevelClass);
            }
        }

        //生成属性
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            if (RootClassInfo.getInstance(getRootClass(), warnings).containsProperty(introspectedColumn)) {
                continue;
            }

            Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
            if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(field);
                topLevelClass.addImportedType(field.getType());
            }

            // 生成getter和setter方法
            Method method = getJavaBeansGetter(introspectedColumn, context, introspectedTable);
            if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                StringBuilder sb = new StringBuilder(introspectedColumn.getJavaProperty());
                if (sb.length() > 1 && Character.isUpperCase(sb.charAt(1))) {
                    topLevelClass.addMethod(method);
                }
            }
            if (!introspectedTable.isImmutable()) {
                method = getJavaBeansSetter(introspectedColumn, context, introspectedTable);
                if (plugins.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable,
                        Plugin.ModelClassType.BASE_RECORD)) {
                    StringBuilder sb = new StringBuilder(introspectedColumn.getJavaProperty());
                    if (sb.length() > 1 && Character.isUpperCase(sb.charAt(1))) {
                        topLevelClass.addMethod(method);
                    }
                }
            }
        }

        addOrgUserOverrideMethod(topLevelClass, introspectedTable);

        //增加映射
        VoGenService voGenService = new VoGenService(introspectedTable);
        List<OverridePropertyValueGeneratorConfiguration> overrideProperty = configuration.getOverridePropertyConfigurations();
        voGenService.buildOverrideColumn(overrideProperty, topLevelClass, ModelClassTypeEnum.modelClass).forEach(f -> {
            if (plugins.modelFieldGenerated(f, topLevelClass, null, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(f);
                topLevelClass.addImportedType(f.getType());
            }
        });

        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalProperty = configuration.getAdditionalPropertyConfigurations();
        List<Field> additionalPropertiesFields = topLevelClass.getAdditionalPropertiesFields(additionalProperty);
        additionalPropertiesFields.forEach(f -> {
            if (plugins.modelFieldGenerated(f, topLevelClass, null, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(f);
                topLevelClass.addImportedType(f.getType());
            }
        });

        //增加actionType属性
        if (!introspectedTable.getRules().isGenerateVoModel() && !introspectedTable.getRules().isGenerateRequestVO()) {
            addActionType(topLevelClass,introspectedTable);
            addIgnoreDeleteFlag(topLevelClass,introspectedTable);
            addIgnorePermissionAnnotation(topLevelClass,introspectedTable);
            //增加ignoreIdList属性
            addIgnoreIdList(topLevelClass, introspectedTable);
            addIsHideIds(topLevelClass, introspectedTable);
            //增加任意过滤条件接收
            addWhereConditionResult(topLevelClass);
            //增加前端过滤器属性
            addFilterMap(topLevelClass);
        }

        //添加一个参数的构造器
        boolean assignable1 = JavaBeansUtil.isAssignableCurrent(I_PERSISTENCE_BASIC, topLevelClass, introspectedTable);
        if (assignable1) {
            Method method = new Method(topLevelClass.getType().getShortName());
            method.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), PARAM_NAME_PERSISTENCE_STATUS));
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setConstructor(true);
            addConstructorBodyLine(method, true, topLevelClass, introspectedTable);
            if (topLevelClass.getMethods().isEmpty()) {
                topLevelClass.getMethods().add(method);
            } else {
                topLevelClass.getMethods().add(0, method);
            }
        }

        //添加静态代码块
        InitializationBlock initializationBlock = new InitializationBlock(false);
        //在静态代码块中添加默认值
        addInitialization(introspectedColumns, initializationBlock, topLevelClass);

        String beanName = introspectedTable.getControllerBeanName();
        if (!StringUtility.isEmpty(beanName) && assignable1) {
            initializationBlock.addBodyLine(VStringUtil.format("this.persistenceBeanName = \"{0}\";", introspectedTable.getControllerBeanName()));
        }
        if (!initializationBlock.getBodyLines().isEmpty()) {
            topLevelClass.addInitializationBlock(initializationBlock);
        }

        /*
         * 根据联合查询属性配置
         * 在实体对象中增加相应的属性
         */
        introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().forEach(relationProperty -> {
            FullyQualifiedJavaType returnType;
            Field field;
            FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(relationProperty.getModelTye());
            if (topLevelClass.isNotContainField(relationProperty.getPropertyName())) {
                if (relationProperty.getType().equals(RelationTypeEnum.collection)) {
                    FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
                    topLevelClass.addImportedType(listType);
                    returnType = FullyQualifiedJavaType.getNewListInstance();
                    returnType.addTypeArgument(fullyQualifiedJavaType);
                    field = new Field(relationProperty.getPropertyName(), returnType);
                    if (relationProperty.getInitializationString().isPresent()) {
                        field.setInitializationString(relationProperty.getInitializationString().get());
                    } else {
                        field.setInitializationString("new ArrayList<>()");
                    }
                    topLevelClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
                } else {
                    returnType = fullyQualifiedJavaType;
                    field = new Field(relationProperty.getPropertyName(), returnType);
                }
                if (introspectedTable.getRules().isIntegrateMybatisPlus()) {
                    TableField tableField = new TableField();
                    tableField.setExist(false);
                    tableField.addAnnotationToField(field, topLevelClass);
                }
                field.setVisibility(JavaVisibility.PRIVATE);
                if (field.getRemark() == null) {
                    field.setRemark(relationProperty.getRemark());
                }
                ApiModelPropertyDesc apiModelProperty = new ApiModelPropertyDesc(field.getRemark(), JDBCUtil.getExampleByClassName(field.getType().getFullyQualifiedNameWithoutTypeParameters(), field.getName(), 0));
                apiModelProperty.addAnnotationToField(field, topLevelClass);
                topLevelClass.addField(field, null, true);
                topLevelClass.addImportedType(fullyQualifiedJavaType);
            }
        });

        // 重写转换vo对象的方法：VO getViewObject();
        addGetViewObject(topLevelClass,introspectedTable);

        if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
            answer.add(topLevelClass);
        }
        return answer;
    }

    private void addGetViewObject(TopLevelClass topLevelClass,IntrospectedTable introspectedTable){
        boolean assignableCurrent = isAssignableCurrent("com.vgosoft.workflow.adapter.IWorkflowBusinessEntity", topLevelClass, introspectedTable);
        if (!assignableCurrent) {
            return;
        }
        VoGenService voGenService = new VoGenService(introspectedTable);
        Method method = new Method("getViewObject");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addAnnotation("@Override");
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" * 重写返回ViewObject对象");
        method.addJavaDocLine(" * @return ViewObject对象");
        method.addJavaDocLine(" */");
        if (introspectedTable.getRules().isGenerateVoModel()) {
            FullyQualifiedJavaType mappingType = voGenService.getMappingType(introspectedTable);
            TableConfiguration tc = introspectedTable.getTableConfiguration();
            FullyQualifiedJavaType voType = tc.getVoGeneratorConfiguration().getVoModelConfiguration().getFullyQualifiedJavaType();
            method.setReturnType(voType);
            method.addBodyLine("{0} mappings = SpringContextHolder.getBean({0}.class);",mappingType.getShortName());
            method.addBodyLine("return mappings.to{0}(this);",voType.getShortName());
            topLevelClass.addImportedType(voType);
            topLevelClass.addImportedType(mappingType);
            topLevelClass.addImportedType("com.vgosoft.tool.core.SpringContextHolder");
            Mb3GenUtil.injectionMappingsInstance(topLevelClass,mappingType);
        } else {
            method.setReturnType(topLevelClass.getType());
            method.addBodyLine("return this;");
        }
        topLevelClass.addMethod(method);
    }

    /**
     * 内部方法：获得父类
     */
    private FullyQualifiedJavaType getSuperClass() {
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            return new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
        } else {
            String rootClass = JavaBeansUtil.getRootClass(introspectedTable);
            if (rootClass != null) {
                return new FullyQualifiedJavaType(rootClass);
            }
        }

        return null;
    }

    /**
     * 内部方法：增加带参构造器
     */
    private void addParameterizedConstructor(TopLevelClass topLevelClass, List<IntrospectedColumn> constructorColumns) {
        Method method = new Method(topLevelClass.getType().getShortName());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(),
                    introspectedColumn.getJavaProperty()));
            topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
        }

        StringBuilder sb = new StringBuilder();
        List<String> superColumns = new LinkedList<>();
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            boolean comma = false;
            sb.append("super("); //$NON-NLS-1$
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                if (comma) {
                    sb.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }
                sb.append(introspectedColumn.getJavaProperty());
                superColumns.add(introspectedColumn.getActualColumnName());
            }
            sb.append(");"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        }

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            if (!superColumns.contains(introspectedColumn.getActualColumnName())) {
                sb.setLength(0);
                sb.append("this."); //$NON-NLS-1$
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(" = "); //$NON-NLS-1$
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(';');
                method.addBodyLine(sb.toString());
            }
        }

        topLevelClass.addMethod(method);
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

    protected void addWhereConditionResult(TopLevelClass topLevelClass) {
        Field cascade = new Field("anyWhereCondition", FullyQualifiedJavaType.getStringInstance());
        cascade.setVisibility(JavaVisibility.PRIVATE);
        cascade.setRemark("任意过滤条件");
        new ApiModelPropertyDesc(cascade.getRemark(), "field = ‘condition’").addAnnotationToField(cascade, topLevelClass);
        topLevelClass.addField(cascade);
    }

    //增加查询过滤器的filterMap属性
    protected void addFilterMap(TopLevelClass topLevelClass) {
        Field filterMap = new Field("filterParam", new FullyQualifiedJavaType("com.vgosoft.core.adapter.web.FilterParam"));
        filterMap.setVisibility(JavaVisibility.PRIVATE);
        filterMap.setRemark("前端过滤器及类似查询过滤条件");
        new ApiModelPropertyDesc(filterMap.getRemark(), "filterParam = ‘{}’").addAnnotationToField(filterMap, topLevelClass);
        topLevelClass.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.adapter.web.FilterParam"));
        topLevelClass.addField(filterMap);
    }

    private void addOrgUserOverrideMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!"org_user".equalsIgnoreCase(introspectedTable.getTableConfiguration().getTableName())) {
            return;
        }
        Method getContactInfo = new Method("getContactInfo");
        getContactInfo.setVisibility(JavaVisibility.PUBLIC);
        getContactInfo.setReturnType(FullyQualifiedJavaType.getStringInstance());
        getContactInfo.addAnnotation("@Override");
        getContactInfo.addBodyLine("return this.telephone != null ? this.telephone : this.mobilePhone;");
        topLevelClass.addMethod(getContactInfo);

        Method getDepartmentIds = new Method("getDepartmentIds");
        getDepartmentIds.setVisibility(JavaVisibility.PUBLIC);
        getDepartmentIds.setReturnType(new FullyQualifiedJavaType("java.util.List<String>"));
        getDepartmentIds.addAnnotation("@Override");
        getDepartmentIds.addBodyLine("Set<String> ids = new HashSet<>();");
        getDepartmentIds.addBodyLine("ids.add(this.mainDeptId);");
        getDepartmentIds.addBodyLine("if (VStringUtil.stringHasValue(this.otherDeptId)) {");
        getDepartmentIds.addBodyLine("ids.addAll(Arrays.asList(this.otherDeptId.split(\",\")));");
        getDepartmentIds.addBodyLine("}");
        getDepartmentIds.addBodyLine("return new ArrayList<>(ids);");
        topLevelClass.addMethod(getDepartmentIds);
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.List"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Set"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.HashSet"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Arrays"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.ArrayList"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("com.vgosoft.tool.core.VStringUtil"));
    }
}
