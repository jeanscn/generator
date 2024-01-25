package org.mybatis.generator.codegen.mybatis3.service;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.codegen.mybatis3.service.elements.*;
import org.mybatis.generator.config.JavaServiceImplGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.custom.ScalableElementEnum;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.config.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.config.SelectBySqlMethodGeneratorConfiguration;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.ANNOTATION_SERVICE;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaServiceImplGenerator extends AbstractServiceGenerator {

    public static final String SUFFIX_INSERT_UPDATE_BATCH = "InsertUpdateBatch";
    private static final String APPLICATION_EVENT_PUBLISHER = "com.vgosoft.core.event.entity.EntityEventPublisher";

    TableConfiguration tc;
    FullyQualifiedJavaType entityType;
    FullyQualifiedJavaType exampleType;
    List<RelationGeneratorConfiguration> relationConfigurations;

    private JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration;

    public JavaServiceImplGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        tc = introspectedTable.getTableConfiguration();
        javaServiceImplGeneratorConfiguration = tc.getJavaServiceImplGeneratorConfiguration();
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.38", table.toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();
        Plugin plugins = context.getPlugins();

        relationConfigurations = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations();
        String targetPackage = tc.getJavaClientGeneratorConfiguration().getTargetPackage();
        FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(targetPackage + "." + tc.getDomainObjectName() + "Mapper");
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        FullyQualifiedJavaType importAnnotation = new FullyQualifiedJavaType(ANNOTATION_SERVICE);
        FullyQualifiedJavaType implSuperType = getServiceSupperType(mapperType, entityType, exampleType, introspectedTable);
        String interfaceClassShortName = getGenInterfaceClassShortName(tc.getJavaServiceGeneratorConfiguration().getTargetPackageGen(), entityType.getShortName());

        Interface bizINF = new Interface(interfaceClassShortName);
        String implGenClazzName = JavaBeansUtil.getFirstCharacterUppercase("Gen" + tc.getDomainObjectName() + "Impl");
        FullyQualifiedJavaType bizGenClazzImplType = new FullyQualifiedJavaType(javaServiceImplGeneratorConfiguration.getTargetPackageGen() + "." + implGenClazzName);
        TopLevelClass bizGenClazzImpl = new TopLevelClass(bizGenClazzImplType);
        bizGenClazzImpl.setVisibility(JavaVisibility.PUBLIC);
        bizGenClazzImpl.setAbstract(true);
        commentGenerator.addJavaFileComment(bizGenClazzImpl);
        bizGenClazzImpl.setSuperClass(implSuperType);
        bizGenClazzImpl.addSuperInterface(bizINF.getType());
        bizGenClazzImpl.addImportedType(entityType);
        bizGenClazzImpl.addImportedType(exampleType);
        bizGenClazzImpl.addImportedType(mapperType);
        bizGenClazzImpl.addImportedType(implSuperType);
        bizGenClazzImpl.addImportedType(bizINF.getType());

        if (bizGenClazzImpl.getFields().stream().noneMatch(f -> f.getName().equalsIgnoreCase("mapper"))) {
            Field mapperProperty = getMapperProperty();
            bizGenClazzImpl.addField(mapperProperty);
            bizGenClazzImpl.addImportedType(mapperProperty.getType());
            bizGenClazzImpl.addImportedType(new FullyQualifiedJavaType(EntityEventEnum.class.getCanonicalName()));
        }

        //注入publisher
        if (!javaServiceImplGeneratorConfiguration.getEntityEvent().isEmpty()) {
            Field publisher = new Field(PUBLISHER_FIELD_NAME, new FullyQualifiedJavaType(APPLICATION_EVENT_PUBLISHER));
            publisher.setVisibility(JavaVisibility.PROTECTED);
            publisher.addAnnotation("@Autowired");
            bizGenClazzImpl.addField(publisher);
            bizGenClazzImpl.addImportedType(publisher.getType());
            bizGenClazzImpl.addImportedType(Autowired.class.getCanonicalName());
        }

        //增加构造器
        Method constructor = new Method(bizGenClazzImpl.getType().getShortName());
        constructor.setVisibility(JavaVisibility.PUBLIC);
        constructor.setConstructor(true);
        constructor.addParameter(new Parameter(mapperType, "mapper"));
        constructor.addBodyLine("super(mapper);");
        constructor.addBodyLine("this.mapper = mapper;");
        bizGenClazzImpl.addMethod(constructor);


        /*
         * insertBatch
         * */
        addInsertBatchElement(bizGenClazzImpl);
        /*
         * UpdateBatch
         * */
        addUpdateBatchElement(bizGenClazzImpl);
        /*
         * insertOrUpdate
         * */
        addInsertOrUpdateElement(bizGenClazzImpl);

        //selectByExampleWithRelation
        addSelectByExampleWithRelationElement(bizGenClazzImpl);

        //SelectByColumnXXX
        addSelectByColumnElement(bizGenClazzImpl);

        addDeleteByColumnElement(bizGenClazzImpl);
        //增加selectTreeByParentId
        addSelectBySqlMethodElement(bizGenClazzImpl);

        //增加selectByTable方法
        addSelectByTableElement(bizGenClazzImpl);

        addSelectByKeysDictElement(bizGenClazzImpl);

        addDeleteByTableElement(bizGenClazzImpl);

        addInsertByTableElement(bizGenClazzImpl);

        addSelectByPrimaryKeysElement(bizGenClazzImpl);

        /*
         *  以下是重写的方法
         * */
        //insert
        addInsertElement(bizGenClazzImpl);
        //insertSelective
        addInsertSelectiveElement(bizGenClazzImpl);
        //deleteByExample
        addDeleteByExampleElement(bizGenClazzImpl);
        addDeleteByPrimaryKeyElement(bizGenClazzImpl);
        /* updateByExampleSelective */
        addUpdateByExampleElement(bizGenClazzImpl);
        /* updateByPrimaryKeySelective */
        addUpdateByPrimaryKeyElement(bizGenClazzImpl);
        /* updateBySql */
        addUpdateBySqlElement(bizGenClazzImpl);
        /*
         * 如果javaCollection生成属性中包括支持更新和插入，需要实现内部方法
         * */
        addInnerMethodInsertUpdateElement(bizGenClazzImpl);

        addCleanupInvalidRecordsMethod(bizGenClazzImpl);

        List<CompilationUnit> answer = new ArrayList<>();
        if (plugins.serviceImplGenerated(bizGenClazzImpl, introspectedTable)) {
            answer.add(bizGenClazzImpl);
        }

        //生成子类
        String interfaceSubShortName = getInterfaceClassShortName(tc.getJavaServiceGeneratorConfiguration().getTargetPackage(), entityType.getShortName());
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
        boolean match = introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream().anyMatch(c -> !c.getCacheConfigurationList().isEmpty());
        if (introspectedTable.getRules().isGenerateCachePO() || match) {
            addCacheConfig(bizClazzImpl);
        }
        StringBuilder sb = new StringBuilder();
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

        boolean forceGenerateScalableElement = introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.service.name());
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(javaServiceImplGeneratorConfiguration.getTargetProject(), javaServiceImplGeneratorConfiguration.getTargetPackage(), implClazzName);
        if (forceGenerateScalableElement || fileNotExist) {
            if (plugins.subServiceImplGenerated(bizGenClazzImpl, introspectedTable)) {
                answer.add(bizClazzImpl);
            }
        }

        return answer;
    }

    private void addCleanupInvalidRecordsMethod(TopLevelClass bizGenClazzImpl) {
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            AbstractServiceElementGenerator cleanupInvalidRecordsElement = new CleanupInvalidRecordsElement();
            initializeAndExecuteGenerator(cleanupInvalidRecordsElement, bizGenClazzImpl);
        }
    }

    private void addSelectByPrimaryKeysElement(TopLevelClass bizGenClazzImpl) {
        AbstractServiceElementGenerator elementGenerator = new SelectByPrimaryKeysElement();
        initializeAndExecuteGenerator(elementGenerator, bizGenClazzImpl);
    }

    private void addDeleteByTableElement(TopLevelClass parentElement) {
        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream().anyMatch(SelectByTableGeneratorConfiguration::isEnableSplit)) {
            AbstractServiceElementGenerator elementGenerator = new DeleteByTableElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addInsertByTableElement(TopLevelClass parentElement) {
        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream().anyMatch(SelectByTableGeneratorConfiguration::isEnableUnion)) {
            AbstractServiceElementGenerator elementGenerator = new InsertByTableElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addInsertBatchElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateInsertBatch()) {
            AbstractServiceElementGenerator elementGenerator = new InsertBatchElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addUpdateBatchElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateUpdateBatch()) {
            AbstractServiceElementGenerator elementGenerator = new UpdateBatchElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addInsertOrUpdateElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateInsertOrUpdate()) {
            AbstractServiceElementGenerator elementGenerator = new InsertOrUpdateElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addSelectByExampleWithRelationElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateRelationWithSubSelected()) {
            AbstractServiceElementGenerator elementGenerator = new SelectByExampleWithRelationElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addSelectBySqlMethodElement(TopLevelClass parentElement) {
        for (SelectBySqlMethodGeneratorConfiguration configuration : introspectedTable.getTableConfiguration().getSelectBySqlMethodGeneratorConfigurations()) {
            AbstractServiceElementGenerator elementGenerator = new SelectBySqlMethodElement(configuration);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addSelectByColumnElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateSelectByColumn()) {
            AbstractServiceElementGenerator elementGenerator = new SelectByColumnElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addDeleteByColumnElement(TopLevelClass parentElement) {
        introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().stream()
                .filter(SelectByColumnGeneratorConfiguration::isEnableDelete).forEach(c -> {
                    AbstractServiceElementGenerator elementGenerator = new DeleteByColumnElement(c);
                    initializeAndExecuteGenerator(elementGenerator, parentElement);
                });
    }

    private void addSelectByTableElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateSelectByTable()) {
            AbstractServiceElementGenerator elementGenerator = new SelectByTableElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    /*
     *  getSelectByKeysDictMethod
     * */
    private void addSelectByKeysDictElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().isGenerateCachePO()) {
            parentElement.addImportedType("org.springframework.cache.annotation.Cacheable");
            parentElement.addImportedType("org.springframework.cache.annotation.CacheEvict");
            AbstractServiceElementGenerator elementGenerator = new SelectByKeysDictElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    /**
     * * 以下是重写的方法
     * *
     */
    private void addInsertElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateInsert()
                && (introspectedTable.getRules().isGenerateCachePO()
                || relationConfigurations.stream().anyMatch(RelationGeneratorConfiguration::isEnableInsert)
                || javaServiceImplGeneratorConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name())
                || javaServiceImplGeneratorConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name()))) {
            AbstractServiceElementGenerator elementGenerator = new InsertElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addInsertSelectiveElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateInsert()
                && (introspectedTable.getRules().isGenerateCachePO()
                || relationConfigurations.stream().anyMatch(RelationGeneratorConfiguration::isEnableInsert)
                || javaServiceImplGeneratorConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name())
                || javaServiceImplGeneratorConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name()))) {
            AbstractServiceElementGenerator elementGenerator = new InsertSelectiveElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addDeleteByExampleElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateDeleteByExample()
                && (introspectedTable.getRules().isGenerateCachePO()
                || relationConfigurations.stream().anyMatch(RelationGeneratorConfiguration::isEnableDelete)
                || javaServiceImplGeneratorConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_DELETE.name()))) {
            AbstractServiceElementGenerator elementGenerator = new DeleteByExampleElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addDeleteByPrimaryKeyElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateDeleteByPrimaryKey()
                && (introspectedTable.getRules().isGenerateCachePO()
                || relationConfigurations.stream().anyMatch(RelationGeneratorConfiguration::isEnableDelete)
                || javaServiceImplGeneratorConfiguration.getEntityEvent().contains(EntityEventEnum.DELETED.name())
                || javaServiceImplGeneratorConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_DELETE.name()))) {
            AbstractServiceElementGenerator elementGenerator = new DeleteByPrimaryKeyElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addUpdateByExampleElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateUpdateByExampleSelective()
                && (introspectedTable.getRules().isGenerateCachePO()
                || relationConfigurations.stream().anyMatch(RelationGeneratorConfiguration::isEnableUpdate))) {
            AbstractServiceElementGenerator elementGenerator = new UpdateByExampleElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addUpdateByPrimaryKeyElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateUpdateByPrimaryKeySelective()
                && (introspectedTable.getRules().isGenerateCachePO()
                || relationConfigurations.stream().anyMatch(RelationGeneratorConfiguration::isEnableUpdate)
                || javaServiceImplGeneratorConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name())
                || javaServiceImplGeneratorConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name()))) {
            AbstractServiceElementGenerator elementGenerator = new UpdateByPrimaryKeyElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addInnerMethodInsertUpdateElement(TopLevelClass parentElement) {
        AbstractServiceElementGenerator elementGenerator = new InnerMethodInsertUpdateElement();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addUpdateBySqlElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().isGenerateCachePO()) {
            AbstractServiceElementGenerator elementGenerator = new UpdateBySqlElement();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void initializeAndExecuteGenerator(AbstractServiceElementGenerator elementGenerator, TopLevelClass parentElement) {
        elementGenerator.setContext(context);
        elementGenerator.setIntrospectedTable(introspectedTable);
        elementGenerator.setProgressCallback(progressCallback);
        elementGenerator.setWarnings(warnings);
        elementGenerator.initGenerator();
        elementGenerator.addElements(parentElement);
    }

    /**
     * 内部类
     * 获得Service抽象类父类
     */
    private FullyQualifiedJavaType getServiceSupperType(FullyQualifiedJavaType mapperType, FullyQualifiedJavaType entityType, FullyQualifiedJavaType exampleType, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType supperType = new FullyQualifiedJavaType(getAbstractService(introspectedTable));
        supperType.addTypeArgument(mapperType);
        supperType.addTypeArgument(entityType);
        supperType.addTypeArgument(exampleType);
        return supperType;
    }

    private Field getMapperProperty() {
        String targetPackage = tc.getJavaClientGeneratorConfiguration().getTargetPackage();
        FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(String.join(".", targetPackage, tc.getDomainObjectName() + "Mapper"));
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
