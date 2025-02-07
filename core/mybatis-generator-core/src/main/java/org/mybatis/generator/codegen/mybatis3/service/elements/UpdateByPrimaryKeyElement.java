package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
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
        CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());

        /* updateByPrimaryKeySelective */
        Method updateByPrimaryKeySelective = serviceMethods.getUpdateByPrimaryKey(parentElement, false, true, true);
        updateByPrimaryKeySelective.addAnnotation("@Override");
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            updateByPrimaryKeySelective.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        if (introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isEnableUpdate)) {
            List<RelationGeneratorConfiguration> configs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableUpdate)
                    .collect(Collectors.toList());
            updateByPrimaryKeySelective.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            outSubBatchMethodBody(updateByPrimaryKeySelective, "UPDATE", "record", parentElement, configs, false);
            if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name()) || this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name())) {
                overwriteParentUpdate(updateByPrimaryKeySelective, introspectedTable.getUpdateByPrimaryKeySelectiveStatementId());
            }else{
                updateByPrimaryKeySelective.addBodyLine("ServiceResult<{0}> result = super.{1}(record);"
                        , entityType.getShortName()
                        , introspectedTable.getUpdateByPrimaryKeySelectiveStatementId());
                updateByPrimaryKeySelective.addBodyLine("if (result.hasResult()) {\n" +
                        "            return result;\n" +
                        "        } else {\n" +
                        "            return ServiceResult.failure(ServiceCodeEnum.WARN, e.getMessage());\n" +
                        "        }");
            }
            parentElement.addImportedType(SERVICE_CODE_ENUM);
        } else {
            overwriteParentUpdate(updateByPrimaryKeySelective, introspectedTable.getUpdateByPrimaryKeySelectiveStatementId());
        }
        parentElement.addMethod(updateByPrimaryKeySelective);

        /* updateByPrimaryKey */
        Method updateByPrimaryKey = serviceMethods.getUpdateByPrimaryKey(parentElement, false, false, true);
        updateByPrimaryKey.addAnnotation("@Override");
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            cacheAnnotationDesc.setKey("#record.id");
            updateByPrimaryKey.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        if (introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isEnableUpdate)) {
            List<RelationGeneratorConfiguration> configs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableUpdate)
                    .collect(Collectors.toList());
            updateByPrimaryKey.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            outSubBatchMethodBody(updateByPrimaryKey, "UPDATE", "record", parentElement, configs, false);
            if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name()) || this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name())) {
                overwriteParentUpdate(updateByPrimaryKey, introspectedTable.getUpdateByPrimaryKeyStatementId());
            }else{
                updateByPrimaryKey.addBodyLine("ServiceResult<{0}> result = super.{1}(record);"
                        , entityType.getShortName()
                        , introspectedTable.getUpdateByPrimaryKeyStatementId());
                updateByPrimaryKey.addBodyLine("if (result.hasResult()) {\n" +
                        "            return result;\n" +
                        "        } else {\n" +
                        "            return ServiceResult.failure(ServiceCodeEnum.WARN, e.getMessage());\n" +
                        "        }");
            }
            parentElement.addImportedType(SERVICE_CODE_ENUM);
        } else {
            overwriteParentUpdate(updateByPrimaryKey, introspectedTable.getUpdateByPrimaryKeyStatementId());
        }
        parentElement.addMethod(updateByPrimaryKey);
    }

    /**
     * 重写父类如下的方法。插入事件发布
     */
    private void  overwriteParentUpdate(Method method ,String methodName){
        method.addBodyLine("try {");
        if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name())) {
            method.addBodyLine("publisher.publishEvent(record, EntityEventEnum.{0});", EntityEventEnum.PRE_UPDATE.name());
        }
        method.addBodyLine("int i = mapper.{0}(record);",methodName);
        method.addBodyLine("if (i > 0) {");
        if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name())) {
            method.addBodyLine("publisher.publishEvent(record, EntityEventEnum.{0});", EntityEventEnum.UPDATED.name());
        }
        method.addBodyLine("return ServiceResult.success(record,i);");
        method.addBodyLine("} else {");
        method.addBodyLine(" return ServiceResult.failure(ServiceCodeEnum.WARN);");
        method.addBodyLine("}");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.RUNTIME_ERROR, e.getMessage());");
        method.addBodyLine("}");
    }
}
