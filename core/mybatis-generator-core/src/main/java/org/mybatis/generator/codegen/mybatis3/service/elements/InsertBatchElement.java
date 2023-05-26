package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.RelationGeneratorConfiguration;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.*;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class InsertBatchElement extends AbstractServiceElementGenerator {

    public InsertBatchElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(SERVICE_CODE_ENUM);
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);

        Method method = serviceMethods.getInsertBatchMethod(parentElement, false,true);
        method.addAnnotation("@Override");
        method.addAnnotation("@Transactional(rollbackFor = Exception.class)");
        List<RelationGeneratorConfiguration> configs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableInsert)
                .collect(Collectors.toList());
        if (configs.size() > 0) {
            method.addBodyLine("for ({0} {1} : {1}s) '{'", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            outSubBatchMethodBody(method, "INSERT", entityType.getShortNameFirstLowCase(), parentElement, configs, false);
            method.addBodyLine("}");
        }
        method.addBodyLine("int i = mapper.{0}({1});", introspectedTable.getInsertBatchStatementId(), entityType.getShortNameFirstLowCase() + "s");
        method.addBodyLine("if (i > 0) return ServiceResult.success({0},i);",entityType.getShortNameFirstLowCase() + "s");
        method.addBodyLine("else{");
        if (configs.size() > 0) {
            method.addBodyLine("TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();");
        }
        method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
        method.addBodyLine("}");
        parentElement.addMethod(method);
    }
}
