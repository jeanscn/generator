package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaServiceImplGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.pojo.*;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaServiceImplGenerator extends AbstractServiceGenerator {

    public static final String SUFFIX_INSERT_UPDATE_BATCH = "InsertUpdateBatch";

    TableConfiguration tc;
    FullyQualifiedJavaType entityType;
    FullyQualifiedJavaType exampleType;

    public JavaServiceImplGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        tc = introspectedTable.getTableConfiguration();
        JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration = tc.getJavaServiceImplGeneratorConfiguration();
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.38", table.toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();
        Plugin plugins = context.getPlugins();

        String targetPackage = tc.getJavaClientGeneratorConfiguration().getTargetPackage();
        FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(targetPackage + "." + tc.getDomainObjectName() + "Mapper");
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        FullyQualifiedJavaType importAnnotation = new FullyQualifiedJavaType(ANNOTATION_SERVICE);
        FullyQualifiedJavaType implSuperType = getServiceSupperType(mapperType, entityType, exampleType, introspectedTable);
        String interfaceClassShortName = getGenInterfaceClassShortName(tc.getJavaServiceGeneratorConfiguration().getTargetPackageGen(), entityType.getShortName());

        CacheAnnotation cacheAnnotation = new CacheAnnotation(entityType.getShortName());
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
        }
        //增加构造器
        Method constructor = new Method(bizGenClazzImpl.getType().getShortName());
        constructor.setVisibility(JavaVisibility.PUBLIC);
        constructor.setConstructor(true);
        constructor.addParameter(new Parameter(mapperType, "mapper"));
        constructor.addBodyLine("super(mapper);");
        constructor.addBodyLine("this.mapper = mapper;");
        bizGenClazzImpl.addMethod(constructor);
        StringBuilder sb = new StringBuilder();

        /*
         * 增加selectByExampleWithRelation接口实现方法
         * 当生成的方法包括至少有一个selectByTableXXX、selectByColumnXXX方法时
         * 此方法可以使byExample方法支持级联查询
         * */
        if (introspectedTable.getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isSubSelected)) {
            Method method = getSelectWithRelationMethod(entityType, exampleType, bizGenClazzImpl, false);
            method.addAnnotation("@Override");
            method.addBodyLine("return mapper.{0}(example);", introspectedTable.getSelectByExampleWithRelationStatementId());
            bizGenClazzImpl.addMethod(method);
        }
        for (SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration : tc.getSelectByColumnGeneratorConfigurations()) {
            IntrospectedColumn foreignKeyColumn = selectByColumnGeneratorConfiguration.getColumn();
            Method methodByColumn = getSelectByColumnMethod(entityType, bizGenClazzImpl, selectByColumnGeneratorConfiguration, false);
            methodByColumn.addAnnotation("@Override");
            if (JavaBeansUtil.isSelectBaseByPrimaryKeyMethod(selectByColumnGeneratorConfiguration.getMethodName())) {
                methodByColumn.addBodyLine("return mapper.{0}({1});"
                        , introspectedTable.getSelectBaseByPrimaryKeyStatementId()
                        , foreignKeyColumn.getJavaProperty());
            } else {
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

        //增加selectTreeByParentId
        Map<String, CustomMethodGeneratorConfiguration> customAddtionalSelectMethodMap = introspectedTable.getCustomAddtionalSelectMethods();
        if (customAddtionalSelectMethodMap.size() > 0
                && customAddtionalSelectMethodMap.containsKey(introspectedTable.getSelectTreeByParentIdStatementId())) {
            CustomMethodGeneratorConfiguration customMethodConfiguration = customAddtionalSelectMethodMap.get(introspectedTable.getSelectTreeByParentIdStatementId());
            Method selectTreeMethod = getSelectTreeByParentIdMethod(entityType, bizGenClazzImpl, customMethodConfiguration, false);
            selectTreeMethod.addAnnotation("@Override");
            sb.setLength(0);
            sb.append("mapper.");
            sb.append(customMethodConfiguration.getMethodName());
            sb.append("(");
            sb.append(customMethodConfiguration.getParentIdColumn().getJavaProperty());
            sb.append(");");
            selectTreeMethod.addBodyLine(sb.toString());
            bizGenClazzImpl.addMethod(selectTreeMethod);
            bizGenClazzImpl.addImportedType(customMethodConfiguration.getParentIdColumn().getFullyQualifiedJavaType());
        }

        //增加selectByTable方法
        for (SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration : tc.getSelectByTableGeneratorConfiguration()) {
            Method selectByTable = getSelectByTableMethod(entityType, bizGenClazzImpl, selectByTableGeneratorConfiguration, false);
            selectByTable.addAnnotation("@Override");
            sb.setLength(0);
            sb.append("return mapper.");
            sb.append(selectByTableGeneratorConfiguration.getMethodName());
            sb.append("(");
            sb.append(selectByTableGeneratorConfiguration.getParameterName());
            sb.append(");");
            selectByTable.addBodyLine(sb.toString());
            bizGenClazzImpl.addMethod(selectByTable);
        }

        /*
         *  getSelectByKeysDictMethod
         * */
        if (introspectedTable.getRules().isGenerateCachePO()) {
            VOCacheGeneratorConfiguration voCacheGeneratorConfiguration = tc.getVoCacheGeneratorConfiguration();
            Method selectByKeysDictMethod = getSelectByKeysDictMethod(bizGenClazzImpl,
                    voCacheGeneratorConfiguration,
                    false);
            selectByKeysDictMethod.addAnnotation("@Override");
            List<IntrospectedColumn> introspectedColumns = Stream.of(voCacheGeneratorConfiguration.getTypeColumn(), voCacheGeneratorConfiguration.getCodeColumn())
                    .map(n -> introspectedTable.getColumn(n).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            cacheAnnotation.setUnless("#result.hasResult()==false");
            cacheAnnotation.setParameters(introspectedColumns.size() == 0 ? 1 : introspectedColumns.size());
            selectByKeysDictMethod.addAnnotation(cacheAnnotation.toCacheableAnnotation());
            bizGenClazzImpl.addImportedType("org.springframework.cache.annotation.Cacheable");
            bizGenClazzImpl.addImportedType("org.springframework.cache.annotation.CacheEvict");
            selectByKeysDictMethod.addBodyLine("{0}Mappings mappings = {0}Mappings.INSTANCE;", entityType.getShortName());
            if (introspectedTable.getRules().isGenerateCachePOWithMultiKey()) {
                String parameters = introspectedColumns.stream().map(IntrospectedColumn::getJavaProperty).collect(Collectors.joining(","));
                selectByKeysDictMethod.addBodyLine("List<{0}> result = mapper.{2}({1});"
                        , entityType.getShortName()
                        , parameters
                        , introspectedTable.getSelectByKeysDictStatementId());
                selectByKeysDictMethod.addBodyLine("if (result.size()>0) {");
                selectByKeysDictMethod.addBodyLine("return ServiceResult.success(mappings.to{0}CachePO(result.get(0)));"
                        , entityType.getShortName());
            } else {
                String parameters = introspectedTable.getPrimaryKeyColumns().stream()
                        .map(IntrospectedColumn::getJavaProperty)
                        .collect(Collectors.joining(","));
                selectByKeysDictMethod.addBodyLine("{0} result = mapper.selectByPrimaryKey({1});"
                        , entityType.getShortName()
                        , parameters);
                selectByKeysDictMethod.addBodyLine("if (result!=null) {");
                selectByKeysDictMethod.addBodyLine("return ServiceResult.success(mappings.to{0}CachePO(result));"
                        , entityType.getShortName());
            }
            selectByKeysDictMethod.addBodyLine("}else{");
            selectByKeysDictMethod.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
            selectByKeysDictMethod.addBodyLine("}");
            bizGenClazzImpl.addMethod(selectByKeysDictMethod);
            bizGenClazzImpl.addImportedType(voCacheGeneratorConfiguration.getBaseTargetPackage() + ".maps." + entityType.getShortName() + "Mappings");
        }


        /*
         * insertBatch
         * */
        if (introspectedTable.getRules().generateInsertBatch()) {
            Method method = getInsertBatchMethod(entityType, bizGenClazzImpl, false);
            method.addAnnotation("@Override");
            if (introspectedTable.getRules().isGenerateCachePO()) {
                method.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
            }
            method.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            bizGenClazzImpl.addImportedType(ANNOTATION_TRANSACTIONAL);
            method.addBodyLine("int i = mapper.{0}({1});", introspectedTable.getInsertBatchStatementId(), entityType.getShortNameFirstLowCase() + "s");
            method.addBodyLine("if (i > 0) {");
            List<RelationGeneratorConfiguration> configs = introspectedTable.getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableInsert)
                    .collect(Collectors.toList());
            if (configs.size() > 0) {
                method.addBodyLine("for ({0} {1} : {1}s) '{'", entityType.getShortName(), entityType.getShortNameFirstLowCase());
                outSubBatchMethodBody(method, "INSERT", entityType.getShortNameFirstLowCase(), bizGenClazzImpl, configs, false);
                method.addBodyLine("}");
            }
            method.addBodyLine("return ServiceResult.success({0},i);", entityType.getShortNameFirstLowCase() + "s");
            method.addBodyLine("}else{");
            method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
            method.addBodyLine("}");
            bizGenClazzImpl.addMethod(method);
            bizGenClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            bizGenClazzImpl.addImportedType("com.vgosoft.core.constant.enums.ServiceCodeEnum");
        }

        /*
         * updateBatch
         * */
        if (introspectedTable.getRules().generateUpdateBatch()) {
            Method method = getUpdateBatchMethod(entityType, bizGenClazzImpl, false);
            method.addAnnotation("@Override");
            if (introspectedTable.getRules().isGenerateCachePO()) {
                method.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
            }
            method.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            bizGenClazzImpl.addImportedType(ANNOTATION_TRANSACTIONAL);
            method.addBodyLine("int i = mapper.{0}({1});", introspectedTable.getUpdateBatchStatementId(), entityType.getShortNameFirstLowCase() + "s");
            method.addBodyLine("if (i > 0) {");
            List<RelationGeneratorConfiguration> configs = introspectedTable.getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableUpdate)
                    .collect(Collectors.toList());
            if (configs.size() > 0) {
                method.addBodyLine("for ({0} {1} : {1}s) '{'", entityType.getShortName(), entityType.getShortNameFirstLowCase());
                outSubBatchMethodBody(method, "UPDATE", entityType.getShortNameFirstLowCase(), bizGenClazzImpl, configs, false);
                method.addBodyLine("}");
            }
            method.addBodyLine("return ServiceResult.success({0},i);", entityType.getShortNameFirstLowCase() + "s");
            method.addBodyLine("}else{");
            method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
            method.addBodyLine("}");
            bizGenClazzImpl.addMethod(method);
        }

        /*
         * insertOrUpdate
         * */
        if (introspectedTable.getRules().generateInsertOrUpdate()) {
            Method method = getInsertOrUpdateMethod(entityType, bizGenClazzImpl, false);
            if (introspectedTable.getRules().isGenerateCachePO()) {
                method.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
            }
            method.addAnnotation("@Override");
            method.addBodyLine("int i = mapper.{0}({1});", introspectedTable.getInsertOrUpdateStatementId(), entityType.getShortNameFirstLowCase());
            method.addBodyLine("if (i > 0) {");
            List<RelationGeneratorConfiguration> configs = introspectedTable.getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableInsertOrUpdate)
                    .collect(Collectors.toList());
            if (configs.size() > 0) {
                outSubBatchMethodBody(method, "INSERTORUPDATE", entityType.getShortNameFirstLowCase(), bizGenClazzImpl, configs, false);
            }
            method.addBodyLine("return ServiceResult.success({0},i);", entityType.getShortNameFirstLowCase());
            method.addBodyLine("}else{");
            method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
            method.addBodyLine("}");
            bizGenClazzImpl.addMethod(method);
        }

        //如果支持子集合插入则需要重写父类的insert相关方法
        if (introspectedTable.getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isEnableInsert)) {
            //insert
            Method insertMethod = this.getInsertMethod(bizGenClazzImpl, false, false);
            insertMethod.addAnnotation("@Override");
            insertMethod.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            bizGenClazzImpl.addImportedType(ANNOTATION_TRANSACTIONAL);
            insertMethod.addBodyLine("ServiceResult<{0}> result = super.insert(record);", entityType.getShortName());
            insertMethod.addBodyLine("if (result.getAffectedRows()>0) {");
            List<RelationGeneratorConfiguration> configs = introspectedTable.getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableInsert)
                    .collect(Collectors.toList());
            if (configs.size() > 0) {
                outSubBatchMethodBody(insertMethod, "INSERT", "record", bizGenClazzImpl, configs, false);
            }
            insertMethod.addBodyLine("return result;");
            insertMethod.addBodyLine("}else{\n" +
                    "            return  ServiceResult.failure(ServiceCodeEnum.WARN);\n" +
                    "        }");
            bizGenClazzImpl.addMethod(insertMethod);

            //insertSelective
            Method insertSelectiveMethod = this.getInsertMethod(bizGenClazzImpl, false, true);
            insertSelectiveMethod.addAnnotation("@Override");
            insertSelectiveMethod.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            bizGenClazzImpl.addImportedType(ANNOTATION_TRANSACTIONAL);
            insertSelectiveMethod.addBodyLine("ServiceResult<{0}> result = super.insertSelective(record);", entityType.getShortName());
            insertSelectiveMethod.addBodyLine("if (result.getAffectedRows()>0) {");
            List<RelationGeneratorConfiguration> configs1 = introspectedTable.getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableInsert)
                    .collect(Collectors.toList());
            if (configs1.size() > 0) {
                outSubBatchMethodBody(insertSelectiveMethod, "INSERT", "record", bizGenClazzImpl, configs1, false);
            }
            insertSelectiveMethod.addBodyLine("return result;");
            insertSelectiveMethod.addBodyLine("}else{\n" +
                    "            return  ServiceResult.failure(ServiceCodeEnum.WARN);\n" +
                    "        }");
            bizGenClazzImpl.addMethod(insertSelectiveMethod);
        }

        //如果需要缓存，需要重写几个父类的更新和删除的方法.
        // 需要考虑子集合更新的问题
        boolean b = introspectedTable.getRelationGeneratorConfigurations().stream().anyMatch(c -> c.isEnableUpdate() || c.isEnableInsertOrUpdate() || c.isEnableInsert() || c.isEnableDelete());
        if (introspectedTable.getRules().isGenerateCachePO() || b) {
            //selectByPrimaryKey
            if (introspectedTable.getRules().isGenerateCachePO()) {
                CacheAnnotation cacheAnnotation1 = new CacheAnnotation(entityType.getShortName());
                List<String> parameters = introspectedTable.getPrimaryKeyColumns().stream()
                        .map(IntrospectedColumn::getJavaProperty)
                        .collect(Collectors.toList());
                Method selectByPrimaryKeyMethod = this.getSelectByPrimaryKeyMethod(bizGenClazzImpl, false);
                selectByPrimaryKeyMethod.addAnnotation("@Override");
                cacheAnnotation1.setUnless("#result.hasResult()==false");
                cacheAnnotation1.setParameters(parameters.size());
                selectByPrimaryKeyMethod.addAnnotation(cacheAnnotation1.toCacheableAnnotation());

                selectByPrimaryKeyMethod.addBodyLine("return super.{0}({1});", introspectedTable.getSelectByPrimaryKeyStatementId(), String.join(",", parameters));
                bizGenClazzImpl.addMethod(selectByPrimaryKeyMethod);
            }

            //deleteByExample
            Method deleteByExampleMethod = this.getDeleteByExampleMethod(bizGenClazzImpl, false);
            deleteByExampleMethod.addAnnotation("@Override");
            deleteByExampleMethod.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            bizGenClazzImpl.addImportedType(ANNOTATION_TRANSACTIONAL);
            if (introspectedTable.getRules().isGenerateCachePO()) {
                deleteByExampleMethod.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
            }
            List<RelationGeneratorConfiguration> collect = introspectedTable.getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableDelete)
                    .collect(Collectors.toList());
            if (collect.size() > 0) {
                deleteByExampleMethod.addBodyLine("List<{0}> {1}s = this.selectByExample(example);", entityType.getShortName(), entityType.getShortNameFirstLowCase());
                deleteByExampleMethod.addBodyLine("int affectedRows = super.deleteByExample(example);");
                deleteByExampleMethod.addBodyLine("if (affectedRows > 0) {");
                deleteByExampleMethod.addBodyLine("for ({0} {1} : {1}s) '{'", entityType.getShortName(), entityType.getShortNameFirstLowCase());
                outSubBatchMethodBody(deleteByExampleMethod, "DELETE", entityType.getShortNameFirstLowCase(), bizGenClazzImpl, collect, true);
                deleteByExampleMethod.addBodyLine("}");
                deleteByExampleMethod.addBodyLine("}");
                deleteByExampleMethod.addBodyLine("return 0;");
            } else {
                deleteByExampleMethod.addBodyLine("return super.{0}(example);", introspectedTable.getDeleteByExampleStatementId());
            }
            bizGenClazzImpl.addMethod(deleteByExampleMethod);
            //deleteByPrimaryKey
            Method deleteByPrimaryKey = this.getDeleteByPrimaryKeyMethod(bizGenClazzImpl, false);
            deleteByPrimaryKey.addAnnotation("@Override");
            deleteByPrimaryKey.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            bizGenClazzImpl.addImportedType(ANNOTATION_TRANSACTIONAL);
            if (introspectedTable.getRules().isGenerateCachePO()) {
                deleteByPrimaryKey.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
            }
            String pks = introspectedTable.getPrimaryKeyColumns().stream().map(IntrospectedColumn::getJavaProperty).collect(Collectors.joining(","));
            List<RelationGeneratorConfiguration> deleteConfigs = introspectedTable.getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableDelete)
                    .collect(Collectors.toList());
            if (deleteConfigs.size() > 0) {
                deleteByPrimaryKey.addBodyLine("ServiceResult<{0}> result = this.selectByPrimaryKey({1});", entityType.getShortName(), pks);
                deleteByPrimaryKey.addBodyLine("if (result.hasResult()) {");
                deleteByPrimaryKey.addBodyLine("{0} {1} = result.getResult();", entityType.getShortName(), entityType.getShortNameFirstLowCase());
                deleteByPrimaryKey.addBodyLine("int affectedRows = super.deleteByPrimaryKey({0});", pks);
                deleteByPrimaryKey.addBodyLine("if (affectedRows > 0) {");
                outSubBatchMethodBody(deleteByPrimaryKey, "DELETE", entityType.getShortNameFirstLowCase(), bizGenClazzImpl, deleteConfigs, true);
                deleteByPrimaryKey.addBodyLine("}}");
                deleteByPrimaryKey.addBodyLine("return 0;");
            } else {
                deleteByPrimaryKey.addBodyLine("return super.{0}({1});", introspectedTable.getDeleteByPrimaryKeyStatementId()
                        , introspectedTable.getPrimaryKeyColumns().stream()
                                .map(IntrospectedColumn::getJavaProperty)
                                .collect(Collectors.joining(",")));
            }
            bizGenClazzImpl.addMethod(deleteByPrimaryKey);

            /* updateByExampleSelective */
            Method updateByExampleSelective = this.getUpdateByExample(bizGenClazzImpl, false, true);
            updateByExampleSelective.addAnnotation("@Override");
            updateByExampleSelective.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            bizGenClazzImpl.addImportedType(ANNOTATION_TRANSACTIONAL);
            if (introspectedTable.getRules().isGenerateCachePO()) {
                updateByExampleSelective.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
            }
            updateByExampleSelective.addBodyLine("return super.{0}(record, example);", introspectedTable.getUpdateByExampleSelectiveStatementId());
            bizGenClazzImpl.addMethod(updateByExampleSelective);
            /*updateByExample*/
            Method updateByExample = this.getUpdateByExample(bizGenClazzImpl, false, false);
            updateByExample.addAnnotation("@Override");
            if (introspectedTable.getRules().isGenerateCachePO()) {
                updateByExample.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
            }
            updateByExample.addBodyLine("return super.{0}(record, example);", introspectedTable.getUpdateByExampleStatementId());
            bizGenClazzImpl.addMethod(updateByExample);

            /* updateByPrimaryKeySelective */
            Method updateByPrimaryKeySelective = this.getUpdateByPrimaryKey(bizGenClazzImpl, false, true);
            updateByPrimaryKeySelective.addAnnotation("@Override");
            updateByPrimaryKeySelective.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            bizGenClazzImpl.addImportedType(ANNOTATION_TRANSACTIONAL);
            if (introspectedTable.getRules().isGenerateCachePO()) {
                updateByPrimaryKeySelective.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
            }
            if (introspectedTable.getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isEnableUpdate)) {
                updateByPrimaryKeySelective.addBodyLine("ServiceResult<{0}> result = super.{1}(record);"
                        , entityType.getShortName()
                        , introspectedTable.getUpdateByPrimaryKeySelectiveStatementId());
                updateByPrimaryKeySelective.addBodyLine("if (result.isSuccess()) {");
                List<RelationGeneratorConfiguration> configs = introspectedTable.getRelationGeneratorConfigurations().stream()
                        .filter(RelationGeneratorConfiguration::isEnableUpdate)
                        .collect(Collectors.toList());
                outSubBatchMethodBody(updateByPrimaryKeySelective, "UPDATE", "record", bizGenClazzImpl, configs, false);
                updateByPrimaryKeySelective.addBodyLine(" return result;\n" +
                        "        }else{\n" +
                        "            return  ServiceResult.failure(ServiceCodeEnum.WARN);\n" +
                        "        }");
            } else {
                updateByPrimaryKeySelective.addBodyLine("return super.{0}(record);", introspectedTable.getUpdateByPrimaryKeySelectiveStatementId());
            }
            bizGenClazzImpl.addMethod(updateByPrimaryKeySelective);

            /* updateByPrimaryKey */
            Method updateByPrimaryKey = this.getUpdateByPrimaryKey(bizGenClazzImpl, false, false);
            updateByPrimaryKey.addAnnotation("@Override");
            updateByPrimaryKey.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            bizGenClazzImpl.addImportedType(ANNOTATION_TRANSACTIONAL);
            if (introspectedTable.getRules().isGenerateCachePO()) {
                updateByPrimaryKey.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
            }
            if (introspectedTable.getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isEnableUpdate)) {
                updateByPrimaryKey.addBodyLine("ServiceResult<{0}> result = super.{1}(record);"
                        , entityType.getShortName()
                        , introspectedTable.getUpdateByPrimaryKeyStatementId());
                updateByPrimaryKey.addBodyLine("if (result.isSuccess()) {");
                List<RelationGeneratorConfiguration> configs = introspectedTable.getRelationGeneratorConfigurations().stream()
                        .filter(RelationGeneratorConfiguration::isEnableUpdate)
                        .collect(Collectors.toList());
                outSubBatchMethodBody(updateByPrimaryKey, "UPDATE", "record", bizGenClazzImpl, configs, false);
                updateByPrimaryKey.addBodyLine(" return result;\n" +
                        "        }else{\n" +
                        "            return  ServiceResult.failure(ServiceCodeEnum.WARN);\n" +
                        "        }");
            } else {
                updateByPrimaryKey.addBodyLine("return super.{0}(record);", introspectedTable.getUpdateByPrimaryKeyStatementId());
            }
            bizGenClazzImpl.addMethod(updateByPrimaryKey);

            /* updateBySql */
            Method updateBySql = this.getUpdateBySql(bizGenClazzImpl, false);
            updateBySql.addAnnotation("@Override");
            if (introspectedTable.getRules().isGenerateCachePO()) {
                updateBySql.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
            }
            updateBySql.addBodyLine("return super.{0}(updateSqlBuilder);", introspectedTable.getUpdateBySqlStatementId());
            bizGenClazzImpl.addMethod(updateBySql);
        }

        /*
         * 如果javaCollection生成属性中包括支持更新和插入，需要实现内部方法
         * */
        introspectedTable.getRelationGeneratorConfigurations().stream()
                .filter(c -> c.isEnableInsert() || c.isEnableUpdate())
                .forEach(config -> {
                            List<Parameter> parameters = new ArrayList<>();
                            String methodName = config.getPropertyName() + SUFFIX_INSERT_UPDATE_BATCH;
                            FullyQualifiedJavaType modelType = new FullyQualifiedJavaType(config.getModelTye());
                            introspectedTable.getColumn(config.getColumn()).ifPresent(column -> {
                                Parameter parameter = new Parameter(column.getFullyQualifiedJavaType(), column.getJavaProperty());
                                parameter.setRemark("主记录的标识");
                                parameters.add(parameter);
                            });
                            boolean isCollection = config.getType().equals(RelationTypeEnum.collection);
                            if (isCollection) {
                                FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
                                listInstance.addTypeArgument(modelType);
                                Parameter parameter = new Parameter(listInstance, "items");
                                parameter.setRemark("待处理的子记录集合");
                                parameters.add(parameter);
                            } else {
                                Parameter parameter = new Parameter(modelType, "item");
                                parameter.setRemark("待处理的子记录");
                                parameters.add(parameter);
                            }
                            Parameter parameter = new Parameter(new FullyQualifiedJavaType(ACTION_CATE_ENUM), "actionCate");
                            parameter.setRemark("数据库操作类型的枚举");
                            parameters.add(parameter);

                            FullyQualifiedJavaType serviceResult = new FullyQualifiedJavaType(SERVICE_RESULT);
                            serviceResult.addTypeArgument(modelType);
                            Method innerInsertUpdateMethod = getMethodByType(methodName
                                    , isCollection ? ReturnTypeEnum.LIST : ReturnTypeEnum.MODEL
                                    , serviceResult
                                    , parameters
                                    , false
                                    , bizGenClazzImpl);
                            innerInsertUpdateMethod.setVisibility(JavaVisibility.PROTECTED);
                            bizGenClazzImpl.addImportedType(ACTION_CATE_ENUM);

                            if (config.getBeanClassFullName() != null) {
                                FullyQualifiedJavaType beanClass = new FullyQualifiedJavaType(config.getBeanClassFullName());
                                innerInsertUpdateMethod.addBodyLine("{0} bean = SpringContextHolder.getBean({0}.class);", beanClass.getShortName());
                                if (isCollection) {
                                    innerInsertUpdateMethod.addBodyLine("switch (actionCate.code()) '{'\n" +
                                            "            case \"INSERT\":\n" +
                                            "                return items.stream().map(bean::insertSelective).collect(Collectors.toList());\n" +
                                            "            case \"UPDATE\":\n" +
                                            "                return items.stream().map(bean::updateByPrimaryKeySelective).collect(Collectors.toList());\n" +
                                            "            case \"INSERTORUPDATE\":\n" +
                                            "                return items.stream().map(bean::insertOrUpdate).collect(Collectors.toList());\n" +
                                            "            case \"DELETE\":\n" +
                                            "                return items.stream()\n" +
                                            "                        .map(i -> '{'\n" +
                                            "                            int ret = bean.deleteByPrimaryKey(i.getId());\n" +
                                            "                            ServiceResult<{0}> result = ret > 0 ? ServiceResult.success(i) : ServiceResult.failure(ServiceCodeEnum.FAIL);\n" +
                                            "                            return result;\n" +
                                            "                        '}').collect(Collectors.toList());\n" +
                                            "            default:\n" +
                                            "                return Collections.singletonList(ServiceResult.failure(ServiceCodeEnum.FAIL));\n" +
                                            "        '}'", modelType.getShortName());
                                    bizGenClazzImpl.addImportedType("java.util.stream.Collectors");
                                } else {
                                    innerInsertUpdateMethod.addBodyLine("switch (actionCate.code()) {\n" +
                                            "            case \"INSERT\":\n" +
                                            "                return bean.insertSelective(item);\n" +
                                            "            case \"UPDATE\":\n" +
                                            "                return bean.updateByPrimaryKey(item);\n" +
                                            "            case \"INSERTORUPDATE\":\n" +
                                            "                return bean.insertOrUpdate(item);\n" +
                                            "            case \"DELETE\":\n" +
                                            "                int ret = bean.deleteByPrimaryKey(item.getId());\n" +
                                            "                return ret > 0 ? ServiceResult.success(item) : ServiceResult.failure(ServiceCodeEnum.FAIL);\n" +
                                            "            default:\n" +
                                            "                return ServiceResult.failure(ServiceCodeEnum.FAIL);\n" +
                                            "        }");
                                }
                                bizGenClazzImpl.addImportedType("java.util.Collections");
                                bizGenClazzImpl.addImportedType(SPRING_CONTEXT_HOLDER);
                                bizGenClazzImpl.addImportedType(beanClass);
                            } else {
                                if (isCollection) {
                                    innerInsertUpdateMethod.addBodyLine("Collections.singletonList(ServiceResult.success(null));");
                                    bizGenClazzImpl.addImportedType("java.util.Collections");
                                } else {
                                    innerInsertUpdateMethod.addBodyLine("ServiceResult.success(null);");
                                }
                            }
                            bizGenClazzImpl.addMethod(innerInsertUpdateMethod);
                        }
                );

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
        if (introspectedTable.getRules().isGenerateCachePO()) {
            addCacheConfig(bizClazzImpl);
        }

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
            if (plugins.subServiceImplGenerated(bizGenClazzImpl, introspectedTable)) {
                answer.add(bizClazzImpl);
            }
        }

        return answer;
    }

    private void outSubBatchMethodBody(Method method, String actionType, String entityVar, TopLevelClass parent, List<RelationGeneratorConfiguration> configs, boolean resultInt) {
        for (RelationGeneratorConfiguration config : configs) {
            boolean isCollection = config.getType().equals(RelationTypeEnum.collection);
            if (isCollection) {
                method.addBodyLine("if ({0}({1}.getId(), {1}.{2}(), ActionCateEnum.{3})"
                        , config.getPropertyName() + SUFFIX_INSERT_UPDATE_BATCH
                        , entityVar
                        , JavaBeansUtil.getGetterMethodName(config.getPropertyName(), FullyQualifiedJavaType.getStringInstance())
                        , actionType);
                method.addBodyLine("        .stream()");
                method.addBodyLine("        .anyMatch(r -> !r.isSuccess())) {");
            } else {
                method.addBodyLine("if (!{0}({1}.getId(), {1}.{2}(), ActionCateEnum.{3})"
                        , config.getPropertyName() + SUFFIX_INSERT_UPDATE_BATCH
                        , entityVar
                        , JavaBeansUtil.getGetterMethodName(config.getPropertyName(), FullyQualifiedJavaType.getStringInstance())
                        , actionType);
                method.addBodyLine(".isSuccess()) {");
            }
            method.addBodyLine("TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();");
            if (resultInt) {
                method.addBodyLine("return 0;");
            } else {
                method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
            }
            method.addBodyLine("}");
        }
        parent.addImportedType("org.springframework.transaction.interceptor.TransactionAspectSupport");
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
