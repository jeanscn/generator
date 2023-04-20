package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.annotations.CacheAnnotation;
import org.mybatis.generator.config.RelationGeneratorConfiguration;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.ANNOTATION_TRANSACTIONAL;
import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_CODE_ENUM;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class UpdateByPrimaryKeyElement extends AbstractServiceElementGenerator {

    public UpdateByPrimaryKeyElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        CacheAnnotation cacheAnnotation = new CacheAnnotation(entityType.getShortName());

        /* updateByPrimaryKeySelective */
        Method updateByPrimaryKeySelective = serviceMethods.getUpdateByPrimaryKey(parentElement, false, true, true);
        updateByPrimaryKeySelective.addAnnotation("@Override");
        updateByPrimaryKeySelective.addAnnotation("@Transactional(rollbackFor = Exception.class)");
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            updateByPrimaryKeySelective.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
        }
        if (introspectedTable.getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isEnableUpdate)) {
            List<RelationGeneratorConfiguration> configs = introspectedTable.getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableUpdate)
                    .collect(Collectors.toList());
            outSubBatchMethodBody(updateByPrimaryKeySelective, "UPDATE", "record", parentElement, configs, false);
            updateByPrimaryKeySelective.addBodyLine("ServiceResult<{0}> result = super.{1}(record);"
                    , entityType.getShortName()
                    , introspectedTable.getUpdateByPrimaryKeySelectiveStatementId());
            updateByPrimaryKeySelective.addBodyLine("if (result.hasResult()) {\n" +
                    "            return result;\n" +
                    "        } else {\n" +
                    "            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();\n" +
                    "            return ServiceResult.failure(ServiceCodeEnum.WARN);\n" +
                    "        }");
            parentElement.addImportedType(SERVICE_CODE_ENUM);
        } else {
            updateByPrimaryKeySelective.addBodyLine("return super.{0}(record);", introspectedTable.getUpdateByPrimaryKeySelectiveStatementId());
        }
        parentElement.addMethod(updateByPrimaryKeySelective);

        /* updateByPrimaryKey */
        Method updateByPrimaryKey = serviceMethods.getUpdateByPrimaryKey(parentElement, false, false, true);
        updateByPrimaryKey.addAnnotation("@Override");
        updateByPrimaryKey.addAnnotation("@Transactional(rollbackFor = Exception.class)");
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            updateByPrimaryKey.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
        }
        if (introspectedTable.getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isEnableUpdate)) {
            List<RelationGeneratorConfiguration> configs = introspectedTable.getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableUpdate)
                    .collect(Collectors.toList());
            outSubBatchMethodBody(updateByPrimaryKey, "UPDATE", "record", parentElement, configs, false);
            updateByPrimaryKey.addBodyLine("ServiceResult<{0}> result = super.{1}(record);"
                    , entityType.getShortName()
                    , introspectedTable.getUpdateByPrimaryKeyStatementId());
            updateByPrimaryKey.addBodyLine("if (result.hasResult()) {\n" +
                    "            return result;\n" +
                    "        } else {\n" +
                    "            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();\n" +
                    "            return ServiceResult.failure(ServiceCodeEnum.WARN);\n" +
                    "        }");
            parentElement.addImportedType(SERVICE_CODE_ENUM);
        } else {
            updateByPrimaryKey.addBodyLine("return super.{0}(record);", introspectedTable.getUpdateByPrimaryKeyStatementId());
        }
        parentElement.addMethod(updateByPrimaryKey);
    }
}
