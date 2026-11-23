package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_CODE_ENUM;

/**
 * saveBatch实现方法。
 * <p>
 * 在单个事务内按删除、更新、新增顺序委托既有批量方法持久化子表变更集；
 * 删除、更新、新增的生命周期事件由被委托方法触发，本方法不重复发布。
 * </p>
 *
 * @author vgosoft-agent
 */
public class SaveBatchElement extends AbstractServiceElementGenerator {

    public SaveBatchElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(new FullyQualifiedJavaType("java.util.ArrayList"));
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.adapter.ServiceResult"));
        parentElement.addImportedType(SERVICE_CODE_ENUM);
        parentElement.addImportedType(exampleType);
        parentElement.addImportedType("com.vgosoft.core.exception.VgoException");
        parentElement.addImportedType("org.springframework.transaction.interceptor.TransactionAspectSupport");

        Method method = serviceMethods.getSaveBatchMethod(parentElement, false);
        method.addAnnotation("@Override");
        Mb3GenUtil.addTransactionalAnnotation(parentElement, method, "READ_COMMITTED");
        addMutationCacheEviction(method, parentElement);

        method.addBodyLine("List<{0}> savedEntities = new ArrayList<>();", entityType.getShortName());
        method.addBodyLine("long affectedRows = 0;");
        method.addBodyLine("try {");
        method.addBodyLine("if (deleteIds != null && !deleteIds.isEmpty()) {");
        method.addBodyLine("{0} example = new {0}();", exampleType.getShortName());
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        if (!primaryKeyColumns.isEmpty()) {
            IntrospectedColumn introspectedColumn = primaryKeyColumns.get(0);
            String javaProperty = introspectedColumn.getJavaProperty();
            method.addBodyLine("example.createCriteria().and{0}In(deleteIds);", VStringUtil.getFirstCharacterUppercase(javaProperty));
        } else {
            method.addBodyLine("example.createCriteria().andIdIn(deleteIds);");
        }
        // 0 匹配按幂等成功处理；真实删除异常由底层抛出并回滚整个事务
        method.addBodyLine("ServiceResult<Integer> deleteResult = this.deleteByExample(example);");
        method.addBodyLine("affectedRows += deleteResult.getAffectedRows();");
        method.addBodyLine("}");
        method.addBodyLine("if (updates != null && !updates.isEmpty()) {");
        method.addBodyLine("ServiceResult<List<{0}>> updateResult = this.updateBatchByPrimaryKey(updates);", entityType.getShortName());
        method.addBodyLine("if (!updateResult.isSuccess()) {");
        method.addBodyLine("TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();");
        method.addBodyLine("return ServiceResult.failure(updateResult.getCode(), updateResult.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("if (updateResult.hasResult()) {");
        method.addBodyLine("savedEntities.addAll(updateResult.getResult());");
        method.addBodyLine("}");
        method.addBodyLine("affectedRows += updateResult.getAffectedRows();");
        method.addBodyLine("}");
        method.addBodyLine("if (inserts != null && !inserts.isEmpty()) {");
        method.addBodyLine("ServiceResult<List<{0}>> insertResult = this.insertBatch(inserts);", entityType.getShortName());
        method.addBodyLine("if (!insertResult.isSuccess()) {");
        method.addBodyLine("TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();");
        method.addBodyLine("return ServiceResult.failure(insertResult.getCode(), insertResult.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("if (insertResult.hasResult()) {");
        method.addBodyLine("savedEntities.addAll(insertResult.getResult());");
        method.addBodyLine("}");
        method.addBodyLine("affectedRows += insertResult.getAffectedRows();");
        method.addBodyLine("}");
        method.addBodyLine("return ServiceResult.success(savedEntities, affectedRows);");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("throw new VgoException(e.getMessage(), e).setCode(ServiceCodeEnum.FAIL.code());");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
