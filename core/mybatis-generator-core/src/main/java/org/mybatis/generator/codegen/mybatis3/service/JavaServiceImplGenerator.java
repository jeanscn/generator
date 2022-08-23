package org.mybatis.generator.codegen.mybatis3.service;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaServiceImplGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.custom.pojo.CustomMethodGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.RelationGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaServiceImplGenerator extends AbstractServiceGenerator {


    public JavaServiceImplGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration();
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.38", table.toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();
        Plugin plugins = context.getPlugins();

        String targetPackage = introspectedTable.getTableConfiguration().getJavaClientGeneratorConfiguration().getTargetPackage();
        FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(targetPackage + "." + introspectedTable.getTableConfiguration().getDomainObjectName()+"Mapper");
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());

        FullyQualifiedJavaType importAnnotation = new FullyQualifiedJavaType(ANNOTATION_SERVICE);
        FullyQualifiedJavaType implSuperType = getServiceSupperType(mapperType,entityType, exampleType, introspectedTable);
        String interfaceClassShortName = getGenInterfaceClassShortName(introspectedTable.getTableConfiguration().getJavaServiceGeneratorConfiguration().getTargetPackageGen(), entityType.getShortName());

        Interface bizINF = new Interface(interfaceClassShortName);
        String implGenClazzName = JavaBeansUtil.getFirstCharacterUppercase("Gen"+introspectedTable.getTableConfiguration().getDomainObjectName()+"Impl");
        FullyQualifiedJavaType bizGenClazzImplType = new FullyQualifiedJavaType(javaServiceImplGeneratorConfiguration.getTargetPackageGen() + "." + implGenClazzName);
        TopLevelClass bizGenClazzImpl = new TopLevelClass(bizGenClazzImplType);
        bizGenClazzImpl.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(bizGenClazzImpl);
        bizGenClazzImpl.addImportedType(entityType);
        bizGenClazzImpl.addImportedType(exampleType);
        bizGenClazzImpl.addImportedType(mapperType);
        bizGenClazzImpl.addImportedType(implSuperType);
        bizGenClazzImpl.setSuperClass(implSuperType);
        bizGenClazzImpl.addImportedType(bizINF.getType());
        bizGenClazzImpl.addSuperInterface(bizINF.getType());
        //增加构造器
        Method constructor = new Method(bizGenClazzImpl.getType().getShortName());
        constructor.setVisibility(JavaVisibility.PUBLIC);
        constructor.setConstructor(true);
        constructor.addParameter(new Parameter(mapperType, "mapper"));
        constructor.addBodyLine("super(mapper);");
        constructor.addBodyLine("this.mapper = mapper;");
        bizGenClazzImpl.addMethod(constructor);

        StringBuilder sb = new StringBuilder();
        //增加selectByExampleWithRelation接口实现方法
        if (introspectedTable.getRelationGeneratorConfigurations().size() > 0) {
            long count = introspectedTable.getRelationGeneratorConfigurations().stream().filter(RelationGeneratorConfiguration::isSubSelected).count();
            if (count>0) {
                Method example = getMethodByType(introspectedTable.getSelectByExampleWithRelationStatementId(),
                        entityType, exampleType, "example", false, "查询条件example对象");
                example.addAnnotation("@Override");
                sb.setLength(0);
                sb.append("return mapper.");
                sb.append(introspectedTable.getSelectByExampleWithRelationStatementId());
                sb.append("(example);");
                example.addBodyLine(sb.toString());
                bizGenClazzImpl.addMethod(example);
                bizGenClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                addJavaMapper(introspectedTable, bizGenClazzImpl);
            }
        }
        if (introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().size() > 0) {
            for (SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration : introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations()) {
                IntrospectedColumn foreignKeyColumn = selectByColumnGeneratorConfiguration.getColumn();
                Method methodByColumn;
                if (selectByColumnGeneratorConfiguration.isReturnPrimaryKey()) {
                    methodByColumn = getMethodByColumn(FullyQualifiedJavaType.getStringInstance(), foreignKeyColumn, selectByColumnGeneratorConfiguration.getMethodName(), false);
                    bizGenClazzImpl.addImportedType(FullyQualifiedJavaType.getStringInstance());
                }else{
                    methodByColumn = getMethodByColumn(entityType, foreignKeyColumn, selectByColumnGeneratorConfiguration.getMethodName(), false);
                }
                methodByColumn.addAnnotation("@Override");
                addJavaMapper(introspectedTable, bizGenClazzImpl);
                if (JavaBeansUtil.isSelectBaseByPrimaryKeyMethod(selectByColumnGeneratorConfiguration.getMethodName())) {
                    bizGenClazzImpl.addImportedType(SERVICE_RESULT);
                    bizGenClazzImpl.addImportedType(SERVICE_CODE_ENUM);
                    String entityVar = JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName());
                    methodByColumn.addBodyLine("try{");
                    String format = VStringUtil.format("{0} {1} = mapper.selectBaseByPrimaryKey({2});",entityType.getShortName(), entityVar, foreignKeyColumn.getJavaProperty());
                    methodByColumn.addBodyLine(format);
                    sb.setLength(0);
                    sb.append("if (").append(entityVar).append("!=null) {");
                    methodByColumn.addBodyLine(sb.toString());
                    methodByColumn.addBodyLine(VStringUtil.format("return ServiceResult.success({0});",entityVar));
                    methodByColumn.addBodyLine("}");
                    methodByColumn.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
                    methodByColumn.addBodyLine("} catch (Exception e) {");
                    methodByColumn.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.RUNTIME_ERROR,e);");
                    methodByColumn.addBodyLine("}");
                }else{
                    sb.setLength(0);
                    sb.append("return mapper.");
                    sb.append(selectByColumnGeneratorConfiguration.getMethodName());
                    sb.append("(");
                    sb.append(foreignKeyColumn.getJavaProperty());
                    sb.append(");");
                    methodByColumn.addBodyLine(sb.toString());
                    bizGenClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                }
                bizGenClazzImpl.addMethod(methodByColumn);
                bizGenClazzImpl.addImportedType(foreignKeyColumn.getFullyQualifiedJavaType());
            }
        }

        //增加selectTreeByParentId
        if (introspectedTable.getCustomAddtionalSelectMethods().size() > 0
                && introspectedTable.getCustomAddtionalSelectMethods().containsKey(introspectedTable.getSelectTreeByParentIdStatementId())) {
            CustomMethodGeneratorConfiguration customMethodGeneratorConfiguration = introspectedTable.getCustomAddtionalSelectMethods().get(introspectedTable.getSelectTreeByParentIdStatementId());
            Method methodByColumn = getMethodByColumn(entityType, customMethodGeneratorConfiguration.getParentIdColumn(),
                    customMethodGeneratorConfiguration.getMethodName(), false);
            methodByColumn.addAnnotation("@Override");
            addJavaMapper(introspectedTable, bizGenClazzImpl);
            sb.setLength(0);
            sb.append("return mapper.");
            sb.append(customMethodGeneratorConfiguration.getMethodName());
            sb.append("(");
            sb.append(customMethodGeneratorConfiguration.getParentIdColumn().getJavaProperty());
            sb.append(");");
            methodByColumn.addBodyLine(sb.toString());
            bizGenClazzImpl.addMethod(methodByColumn);
            bizGenClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            bizGenClazzImpl.addImportedType(customMethodGeneratorConfiguration.getParentIdColumn().getFullyQualifiedJavaType());
        }

        //增加selectByTable方法
        for (SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration : introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration()) {
            Method selectByTable;
            if (selectByTableGeneratorConfiguration.isReturnPrimaryKey()) {
                selectByTable = getMethodByType(selectByTableGeneratorConfiguration.getMethodName(), FullyQualifiedJavaType.getStringInstance(),
                        FullyQualifiedJavaType.getStringInstance(), selectByTableGeneratorConfiguration.getParameterName(), false,
                        "中间表中来自其他表的查询键值");
            }else{
                selectByTable = getMethodByType(selectByTableGeneratorConfiguration.getMethodName(), entityType,
                        FullyQualifiedJavaType.getStringInstance(), selectByTableGeneratorConfiguration.getParameterName(), false,
                        "中间表中来自其他表的查询键值");
            }
            selectByTable.setVisibility(JavaVisibility.PUBLIC);
            selectByTable.addAnnotation("@Override");
            addJavaMapper(introspectedTable, bizGenClazzImpl);
            sb.setLength(0);
            sb.append("return mapper.");
            sb.append(selectByTableGeneratorConfiguration.getMethodName());
            sb.append("(");
            sb.append(selectByTableGeneratorConfiguration.getParameterName());
            sb.append(");");
            selectByTable.addBodyLine(sb.toString());
            bizGenClazzImpl.addMethod(selectByTable);
            bizGenClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            bizGenClazzImpl.addImportedType(FullyQualifiedJavaType.getStringInstance());
        }

        List<CompilationUnit> answer = new ArrayList<>();
        if (plugins.serviceImplGenerated(bizGenClazzImpl, introspectedTable)) {
            answer.add(bizGenClazzImpl);
        }

        //生成子类
        String interfaceSubShortName = getInterfaceClassShortName(introspectedTable.getTableConfiguration().getJavaServiceGeneratorConfiguration().getTargetPackage(), entityType.getShortName());
        Interface superINF = new Interface(interfaceSubShortName);
        String implClazzName = JavaBeansUtil.getFirstCharacterUppercase(introspectedTable.getControllerBeanName());
        FullyQualifiedJavaType bizClazzImplType = new FullyQualifiedJavaType(javaServiceImplGeneratorConfiguration.getTargetPackage() + "." + implClazzName);
        TopLevelClass bizClazzImpl = new TopLevelClass(bizClazzImplType);
        commentGenerator.addJavaFileComment(bizClazzImpl);
        bizClazzImpl.addImportedType(bizGenClazzImpl.getType());
        bizClazzImpl.addImportedType(superINF.getType());
        bizClazzImpl.addImportedType(mapperType);
        bizClazzImpl.setVisibility(JavaVisibility.PUBLIC);
        bizClazzImpl.setSuperClass(bizGenClazzImpl.getType());
        bizClazzImpl.addSuperInterface(superINF.getType());

        /*是否添加@Service注解*/
        boolean noServiceAnnotation = introspectedTable.getRules().isNoServiceAnnotation();
        if (!noServiceAnnotation) {
            bizClazzImpl.addImportedType(importAnnotation);
            sb.setLength(0);
            sb = new StringBuilder("@Service(\"").append(getTableBeanName(introspectedTable)).append("\")");
            bizClazzImpl.addAnnotation(sb.toString());
            bizClazzImpl.addImportedType("org.springframework.context.annotation.Primary");
            bizClazzImpl.addAnnotation("@Primary");
        }
        //构造器
        Method conMethod = new Method(implClazzName);
        conMethod.addParameter(new Parameter(mapperType, "mapper"));
        bizClazzImpl.addImportedType(mapperType);
        conMethod.setConstructor(true);
        conMethod.setVisibility(JavaVisibility.PUBLIC);
        conMethod.addBodyLine("super(mapper);");
        bizClazzImpl.addMethod(conMethod);

        boolean forceGenerateScalableElement = introspectedTable.getRules().isForceGenerateScalableElement();
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(javaServiceImplGeneratorConfiguration.getTargetProject(), javaServiceImplGeneratorConfiguration.getTargetPackage(), implClazzName);
        if (forceGenerateScalableElement || fileNotExist) {
            if (plugins.subServiceImplGenerated(bizGenClazzImpl, introspectedTable)){
                answer.add(bizClazzImpl);
            }
        }

        return answer;
    }

    /**
     * 内部类
     * 获得Service抽象类父类
     */
    private FullyQualifiedJavaType getServiceSupperType(FullyQualifiedJavaType mapperType,FullyQualifiedJavaType entityType, FullyQualifiedJavaType exampleType, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType supperType = new FullyQualifiedJavaType(getAbstractService(introspectedTable));
        supperType.addTypeArgument(mapperType);
        supperType.addTypeArgument(entityType);
        supperType.addTypeArgument(exampleType);
        return supperType;
    }

    private void addJavaMapper(IntrospectedTable introspectedTable, TopLevelClass bizClazzImpl) {
        long mapper = bizClazzImpl.getFields().stream().filter(f -> f.getName().equalsIgnoreCase("mapper")).count();
        if (mapper == 0) {
            Field mapperProperty = getMapperProperty(introspectedTable);
            bizClazzImpl.addField(mapperProperty);
            bizClazzImpl.addImportedType(mapperProperty.getType());
        }
    }

    private Field getMapperProperty(IntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        String targetPackage = tc.getJavaClientGeneratorConfiguration().getTargetPackage();
        FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(String.join(".", targetPackage,tc.getDomainObjectName()+"Mapper"));
        Field mapper = new Field("mapper", mapperType);
        mapper.setFinal(true);
        mapper.setVisibility(JavaVisibility.PRIVATE);
        return mapper;
    }

    /**
     * 获得对应的操作Bean的名称
     */
    private String getTableBeanName(IntrospectedTable introspectedTable) {
        String implClazzName = introspectedTable.getControllerBeanName();
        return JavaBeansUtil.getFirstCharacterLowercase(implClazzName);
    }
}
