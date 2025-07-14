package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class DeleteByPrimaryKeyElement extends AbstractServiceElementGenerator {

    public DeleteByPrimaryKeyElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        boolean containPreDeleteEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_DELETE.name());
        boolean containDeletedEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.DELETED.name());

        Method method = serviceMethods.getDeleteByPrimaryKeyMethod(parentElement, false);
        method.addAnnotation("@Override");
        if(containPreDeleteEvent || containDeletedEvent) {
            Mb3GenUtil.addTransactionalAnnotation(parentElement,method,"DEFAULT");
            parentElement.addImportedType(new FullyQualifiedJavaType(EntityEventEnum.class.getCanonicalName()));
        }
        if (introspectedTable.getRules().isGenerateCachePo()) {
            CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
            method.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        String pks = introspectedTable.getPrimaryKeyColumns().stream().map(IntrospectedColumn::getJavaProperty).collect(Collectors.joining(","));
        List<RelationGeneratorConfiguration> deleteConfigs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableDelete)
                .collect(Collectors.toList());

        if (!deleteConfigs.isEmpty()) {
            Mb3GenUtil.addTransactionalAnnotation(parentElement,method,"DEFAULT");
            method.addBodyLine("ServiceResult<{0}> result = this.selectByPrimaryKey({1});", entityType.getShortName(), pks);
            method.addBodyLine("if (result.hasResult()) {");
            //增加PRE_DELETE事件发布
            if (containPreDeleteEvent) {
                method.addBodyLine("publisher.publishEvent(result.getResult(),EntityEventEnum.{1});",entityType.getShortName(), EntityEventEnum.PRE_DELETE.name());
            }
            method.addBodyLine("{0} {1} = result.getResult();", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            outSubBatchMethodBody(method, "DELETE", entityType.getShortNameFirstLowCase(), parentElement, deleteConfigs, false);
            method.addBodyLine("int affectedRows = mapper.deleteByPrimaryKey({0});", pks);
            method.addBodyLine("if (affectedRows > 0) {");
            //增加DELETED事件发布
            if (containDeletedEvent) {
                method.addBodyLine("publisher.publishEvent(result.getResult(),EntityEventEnum.{0});",EntityEventEnum.DELETED.name());
            }
            method.addBodyLine("return ServiceResult.success(affectedRows,affectedRows);");
            method.addBodyLine("}}");
            method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.FAIL);");
        } else {
            if (containPreDeleteEvent || containDeletedEvent) {
                overwriteParentDeleteByPrimaryKey(method ,introspectedTable.getDeleteByPrimaryKeyStatementId());
            }else{
                method.addBodyLine("return super.{0}({1});", introspectedTable.getDeleteByPrimaryKeyStatementId()
                        , introspectedTable.getPrimaryKeyColumns().stream()
                                .map(IntrospectedColumn::getJavaProperty)
                                .collect(Collectors.joining(",")));
            }

        }
        parentElement.addMethod(method);
    }

    /**
     * 重写父类如下的方法。插入事件发布
     * public ServiceResult<Integer> deleteByPrimaryKey(String id) {
     *         try {
     *             int i = mapper.deleteByPrimaryKey(id);
     *             return ServiceResult.success(i, i);
     *         } catch (Exception e) {
     *             return ServiceResult.failure(ServiceCodeEnum.RUNTIME_ERROR, e);
     *         }
     *     }
     *
     *
     */
    private void  overwriteParentDeleteByPrimaryKey(Method method ,String methodName){
        String ids = introspectedTable.getPrimaryKeyColumns().stream()
                .map(IntrospectedColumn::getJavaProperty)
                .collect(Collectors.joining(","));
        method.addBodyLine("try {");
        //增加PRE_DELETE事件发布
        if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_DELETE.name())) {
            method.addBodyLine("ServiceResult<{0}> result = selectByPrimaryKey({1});",entityType.getShortName(),ids);
            method.addBodyLine("if (!result.hasResult())  return ServiceResult.failure(ServiceCodeEnum.FAIL, result.getMessage());");
            method.addBodyLine("publisher.publishEvent(result.getResult(),EntityEventEnum.{2});",entityType.getShortName(), ids,EntityEventEnum.PRE_DELETE.name());
        }
        method.addBodyLine("int i = mapper.{0}({1});",methodName,ids);
        method.addBodyLine("if (i > 0) {");
        //增加DELETED事件发布
        if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.DELETED.name())) {
            method.addBodyLine("publisher.publishEvent(result.getResult(),EntityEventEnum.{0});",EntityEventEnum.DELETED.name());
        }
        method.addBodyLine("return ServiceResult.success(i, i);");
        method.addBodyLine("} else {");
        method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.FAIL);");
        method.addBodyLine("}");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.RUNTIME_ERROR, e.getMessage(), e);");
        method.addBodyLine("}");
    }
}
