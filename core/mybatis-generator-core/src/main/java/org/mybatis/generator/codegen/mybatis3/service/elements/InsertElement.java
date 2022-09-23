package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.pojo.RelationGeneratorConfiguration;

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
public class InsertElement extends AbstractServiceElementGenerator {

    public InsertElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        Method insertMethod = this.getInsertMethod(parentElement, false, false);
        insertMethod.addAnnotation("@Override");
        insertMethod.addAnnotation("@Transactional(rollbackFor = Exception.class)");
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        insertMethod.addBodyLine("ServiceResult<{0}> result = super.insert(record);", entityType.getShortName());
        insertMethod.addBodyLine("if (result.getAffectedRows()>0) {");
        List<RelationGeneratorConfiguration> configs = introspectedTable.getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableInsert)
                .collect(Collectors.toList());
        if (configs.size() > 0) {
            outSubBatchMethodBody(insertMethod, "INSERT", "record", parentElement, configs, false);
        }
        insertMethod.addBodyLine("return result;");
        insertMethod.addBodyLine("}else{\n" +
                "            return  ServiceResult.failure(ServiceCodeEnum.WARN);\n" +
                "        }");
        parentElement.addMethod(insertMethod);
        parentElement.addImportedType(SERVICE_CODE_ENUM);
    }
}
