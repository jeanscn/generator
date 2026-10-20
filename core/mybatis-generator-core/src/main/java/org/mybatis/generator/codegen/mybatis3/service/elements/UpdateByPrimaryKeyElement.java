package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;

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

        /* updateByPrimaryKeySelective */
        Method updateByPrimaryKeySelective = serviceMethods.getUpdateByPrimaryKey(parentElement, false, true, true);
        updateByPrimaryKeySelective.addAnnotation("@Override");
        Mb3GenUtil.addTransactionalAnnotation(parentElement,updateByPrimaryKeySelective,"READ_COMMITTED");
        addMutationCacheEviction(updateByPrimaryKeySelective, parentElement);
        if (introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isEnableUpdate)) {
            List<RelationGeneratorConfiguration> configs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableUpdate)
                    .collect(Collectors.toList());
            outSubBatchMethodBody(updateByPrimaryKeySelective, "UPDATE", "record", parentElement, configs, false);
            if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name()) || this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name())) {
                overwriteParentUpdate(updateByPrimaryKeySelective, introspectedTable.getUpdateByPrimaryKeySelectiveStatementId(), parentElement);
            }else{
                updateByPrimaryKeySelective.addBodyLine("ServiceResult<{0}> result = super.{1}(record);"
                        , entityType.getShortName()
                        , introspectedTable.getUpdateByPrimaryKeySelectiveStatementId());
                updateByPrimaryKeySelective.addBodyLine("if (result.hasResult()) {\n" +
                        "            return result;\n" +
                        "        } else {\n" +
                        "            return ServiceResult.failure(ServiceCodeEnum.WARN, result.getMessage());\n" +
                        "        }");
            }
            parentElement.addImportedType(SERVICE_CODE_ENUM);
        } else {
            overwriteParentUpdate(updateByPrimaryKeySelective, introspectedTable.getUpdateByPrimaryKeySelectiveStatementId(), parentElement);
        }
        parentElement.addMethod(updateByPrimaryKeySelective);

        /* updateByPrimaryKey */
        Method updateByPrimaryKey = serviceMethods.getUpdateByPrimaryKey(parentElement, false, false, true);
        updateByPrimaryKey.addAnnotation("@Override");
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        addMutationCacheEviction(updateByPrimaryKey, parentElement);
        if (introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isEnableUpdate)) {
            List<RelationGeneratorConfiguration> configs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                    .filter(RelationGeneratorConfiguration::isEnableUpdate)
                    .collect(Collectors.toList());
            Mb3GenUtil.addTransactionalAnnotation(parentElement,updateByPrimaryKey,"READ_COMMITTED");
            outSubBatchMethodBody(updateByPrimaryKey, "UPDATE", "record", parentElement, configs, false);
            if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name()) || this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name())) {
                overwriteParentUpdate(updateByPrimaryKey, introspectedTable.getUpdateByPrimaryKeyStatementId(), parentElement);
            }else{
                updateByPrimaryKey.addBodyLine("ServiceResult<{0}> result = super.{1}(record);"
                        , entityType.getShortName()
                        , introspectedTable.getUpdateByPrimaryKeyStatementId());
                updateByPrimaryKey.addBodyLine("if (result.hasResult()) {\n" +
                        "            return result;\n" +
                        "        } else {\n" +
                        "            return ServiceResult.failure(ServiceCodeEnum.WARN, result.getMessage());\n" +
                        "        }");
            }
            parentElement.addImportedType(SERVICE_CODE_ENUM);
        } else {
            overwriteParentUpdate(updateByPrimaryKey, introspectedTable.getUpdateByPrimaryKeyStatementId(), parentElement);
        }
        parentElement.addMethod(updateByPrimaryKey);
    }

    /**
     * 重写父类如下的方法。插入事件发布
     */
    private void  overwriteParentUpdate(Method method ,String methodName, TopLevelClass parentElement){
        method.addBodyLine("try {");
        if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name())) {
            method.addBodyLine("if (publisher != null) {");
            method.addBodyLine("    publisher.publishEvent(record, EntityEventEnum.{0});", EntityEventEnum.PRE_UPDATE.name());
            method.addBodyLine("}");
        }
        method.addBodyLine("int i = mapper.{0}(record);",methodName);
        if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name())) {
            method.addBodyLine("if (publisher != null) {");
            method.addBodyLine("    publisher.publishEvent(record, EntityEventEnum.{0});", EntityEventEnum.UPDATED.name());
            method.addBodyLine("}");
        }
        method.addBodyLine("return ServiceResult.success(record, i);");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("throw new VgoException(ServiceCodeEnum.FAIL.code(), e.getMessage());");
        method.addBodyLine("}");
        parentElement.addImportedType("com.vgosoft.core.exception.VgoException");
    }
}
