package org.mybatis.generator.codegen.mybatis3.service.elements;

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
public class InsertSelectiveElement extends AbstractServiceElementGenerator {

    public InsertSelectiveElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        Method insertSelectiveMethod = this.getInsertMethod(parentElement, false, true);
        insertSelectiveMethod.addAnnotation("@Override");
        insertSelectiveMethod.addAnnotation("@Transactional(rollbackFor = Exception.class)");
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        insertSelectiveMethod.addBodyLine("ServiceResult<{0}> result = super.insertSelective(record);", entityType.getShortName());
        insertSelectiveMethod.addBodyLine("if (result.getAffectedRows()>0) {");
        List<RelationGeneratorConfiguration> configs1 = introspectedTable.getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableInsert)
                .collect(Collectors.toList());
        if (configs1.size() > 0) {
            outSubBatchMethodBody(insertSelectiveMethod, "INSERT", "record", parentElement, configs1, false);
        }
        insertSelectiveMethod.addBodyLine("return result;");
        insertSelectiveMethod.addBodyLine("}else{\n" +
                "            return  ServiceResult.failure(ServiceCodeEnum.WARN);\n" +
                "        }");
        parentElement.addMethod(insertSelectiveMethod);
        parentElement.addImportedType(SERVICE_CODE_ENUM);
    }
}